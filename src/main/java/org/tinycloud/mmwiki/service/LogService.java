package org.tinycloud.mmwiki.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.tinycloud.mmwiki.domain.LogDocumentView;
import org.tinycloud.mmwiki.domain.LogEntry;
import org.tinycloud.mmwiki.mapper.LogDocumentMapper;
import org.tinycloud.mmwiki.mapper.LogMapper;
import org.tinycloud.mmwiki.util.IpUtils;
import org.tinycloud.mmwiki.util.TimeUtils;
import org.tinycloud.mmwiki.web.CurrentUser;
import org.tinycloud.mmwiki.web.PageModel;

/**
 * MM-Wiki 业务服务实现。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Service
public class LogService {

    public static final int LEVEL_ERROR = 3;

    @Autowired
    private LogMapper logMapper;
    @Autowired
    private LogDocumentMapper logDocumentMapper;
    @Autowired
    @Qualifier("asyncServiceExecutor")
    private ThreadPoolTaskExecutor asyncServiceExecutor;

    /**
     * 分页查询系统操作日志。
     *
     * @param level    日志级别，可为空
     * @param message  日志内容关键字
     * @param username 操作人用户名关键字
     * @param pageNum  当前页码
     * @param pageSize 每页条数
     * @return 系统操作日志分页数据
     */
    public PageModel<LogEntry> systemLogPage(Integer level, String message, String username, int pageNum, int pageSize) {
        String searchMessage = message == null ? "" : message.trim();
        String searchUsername = username == null ? "" : username.trim();
        PageInfo<LogEntry> pageInfo = PageHelper.startPage(pageNum, pageSize)
                .doSelectPageInfo(() -> logMapper.pageByFilters(level, searchMessage, searchUsername));
        List<LogEntry> logs = pageInfo.getList();
        logs.forEach(log -> log.setCreateTimeText(TimeUtils.format(log.getCreateTime())));
        return PageModel.build((long) pageInfo.getPageNum(), (long) pageInfo.getPageSize(), logs, pageInfo.getTotal(), (long) pageInfo.getPages());
    }

    /**
     * 根据日志 ID 查询系统日志详情。
     *
     * @param logId 日志 ID
     * @return 系统日志详情，不存在时返回 null
     */
    public LogEntry findLog(Long logId) {
        LogEntry log = logId == null ? null : logMapper.findById(logId);
        if (log != null) {
            log.setCreateTimeText(TimeUtils.format(log.getCreateTime()));
        }
        return log;
    }

    /**
     * 分页查询文档操作日志。
     *
     * @param userId   操作人 ID，可为空
     * @param keyword  文档或操作关键字
     * @param pageNum  当前页码
     * @param pageSize 每页条数
     * @return 文档操作日志分页数据
     */
    public PageModel<LogDocumentView> documentLogPage(Integer userId, String keyword, int pageNum, int pageSize) {
        String search = keyword == null ? "" : keyword.trim();
        PageInfo<LogDocumentView> pageInfo = PageHelper.startPage(pageNum, pageSize)
                .doSelectPageInfo(() -> logDocumentMapper.pageForSystem(userId, search));
        pageInfo.getList().forEach(log -> log.setCreateTimeText(TimeUtils.format(log.getCreateTime())));
        return PageModel.from(pageInfo);
    }

    /**
     * 异步记录系统后台操作日志。
     * <p>
     * 这里会先同步读取 request 并构造 LogEntry，异步线程只负责写库，避免请求结束后继续访问 HttpServletRequest。
     *
     * @param request     当前请求
     * @param currentUser 当前登录用户
     * @param ex          Controller 执行异常，正常完成时为空
     */
    public void recordSystemOperationAsync(HttpServletRequest request, CurrentUser currentUser, Exception ex) {
        if (request == null || currentUser == null) {
            return;
        }
        try {
            LogEntry logEntry = buildSystemOperationLog(request, currentUser, ex);
            asyncServiceExecutor.execute(() -> saveSystemOperation(logEntry));
        } catch (RuntimeException ignored) {
            // 操作日志记录失败不能影响用户请求主流程。
        }
    }

    /**
     * 构造系统后台操作日志实体。
     *
     * @param request     当前请求
     * @param currentUser 当前登录用户
     * @param ex          Controller 执行异常，正常完成时为空
     * @return 待保存的日志实体
     */
    private LogEntry buildSystemOperationLog(HttpServletRequest request, CurrentUser currentUser, Exception ex) {
        boolean success = ex == null;
        String path = request.getRequestURI();
        LogEntry logEntry = new LogEntry();
        logEntry.setLevel(success ? 6 : LEVEL_ERROR);
        logEntry.setPath(left(path, 100));
        logEntry.setGet(left(request.getQueryString() == null ? "" : request.getQueryString(), 4096));
        logEntry.setPost(left(postParameters(request), 4096));
        logEntry.setMessage(left((success ? "系统操作" : "系统操作异常") + ": " + request.getMethod() + " " + path, 255));
        logEntry.setIp(left(IpUtils.getClientIp(request), 100));
        logEntry.setUserAgent(left(header(request, "User-Agent"), 255));
        logEntry.setReferer(left(header(request, "Referer"), 100));
        logEntry.setUserId(currentUser.getUserId());
        logEntry.setUsername(currentUser.getUsername());
        logEntry.setCreateTime(LocalDateTime.now());
        return logEntry;
    }

    /**
     * 保存系统后台操作日志。
     *
     * @param logEntry 待保存的日志实体
     */
    private void saveSystemOperation(LogEntry logEntry) {
        try {
            logMapper.insert(logEntry);
        } catch (RuntimeException ignored) {
            // Logging must never break the user-facing request path.
        }
    }

    /**
     * 拼接 POST 参数，并对密码类字段脱敏。
     *
     * @param request 当前请求
     * @return 日志可保存的参数文本
     */
    private String postParameters(HttpServletRequest request) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            if (builder.length() > 0) {
                builder.append('&');
            }
            builder.append(entry.getKey()).append('=');
            String key = entry.getKey().toLowerCase(Locale.ROOT);
            if (key.contains("password") || key.contains("pwd")) {
                builder.append("***");
            } else {
                builder.append(String.join(",", entry.getValue()));
            }
        }
        return builder.toString();
    }

    /**
     * 读取请求头。
     *
     * @param request 当前请求
     * @param name    请求头名称
     * @return 请求头值，为空时返回空字符串
     */
    private String header(HttpServletRequest request, String name) {
        String value = request.getHeader(name);
        return value == null ? "" : value;
    }

    /**
     * 截取指定长度的文本，避免日志字段超长。
     *
     * @param value     原始文本
     * @param maxLength 最大长度
     * @return 截断后的文本
     */
    private String left(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
