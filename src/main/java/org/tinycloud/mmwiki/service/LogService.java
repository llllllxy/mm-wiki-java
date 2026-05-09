package org.tinycloud.mmwiki.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.tinycloud.mmwiki.vo.DocumentLogPage;
import org.tinycloud.mmwiki.vo.SystemLogPage;

import org.springframework.beans.factory.annotation.Autowired;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.tinycloud.mmwiki.domain.LogDocumentView;
import org.tinycloud.mmwiki.domain.LogEntry;
import org.tinycloud.mmwiki.mapper.LogDocumentMapper;
import org.tinycloud.mmwiki.mapper.LogMapper;
import org.tinycloud.mmwiki.mapper.UserMapper;
import org.tinycloud.mmwiki.util.TimeUtils;
import org.tinycloud.mmwiki.web.Paginator;

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
    private UserMapper userMapper;

    public SystemLogPage systemLogs(Integer level, String message, String username, int page, int number) {
        String searchMessage = message == null ? "" : message.trim();
        String searchUsername = username == null ? "" : username.trim();
        PageInfo<LogEntry> pageInfo = PageHelper.startPage(page, number)
                .doSelectPageInfo(() -> logMapper.pageByFilters(level, searchMessage, searchUsername));
        List<LogEntry> logs = pageInfo.getList();
        logs.forEach(log -> log.setCreateTimeText(TimeUtils.formatUnix(log.getCreateTime())));
        String basePath = buildSystemLogBasePath(level, searchMessage, searchUsername);
        return new SystemLogPage(logs, level, searchMessage, searchUsername, Paginator.of(page, number, pageInfo.getTotal(), basePath));
    }

    public LogEntry findLog(Long logId) {
        LogEntry log = logId == null ? null : logMapper.findById(logId);
        if (log != null) {
            log.setCreateTimeText(TimeUtils.formatUnix(log.getCreateTime()));
        }
        return log;
    }

    public DocumentLogPage documentLogs(Integer userId, String keyword, int page, int number) {
        String search = keyword == null ? "" : keyword.trim();
        PageInfo<LogDocumentView> pageInfo = PageHelper.startPage(page, number)
                .doSelectPageInfo(() -> logDocumentMapper.pageForSystem(userId, search));
        List<LogDocumentView> logs = pageInfo.getList();
        logs.forEach(log -> log.setCreateTimeText(TimeUtils.formatUnix(log.getCreateTime())));
        String basePath = buildDocumentLogBasePath(userId, search);
        return new DocumentLogPage(logs, userMapper.findAllActive(), userId, search, Paginator.of(page, number, pageInfo.getTotal(), basePath));
    }

    private String buildSystemLogBasePath(Integer level, String message, String username) {
        List<String> params = new ArrayList<>();
        if (level != null) {
            params.add("level=" + level);
        }
        addQueryParam(params, "message", message);
        addQueryParam(params, "username", username);
        return buildBasePath("/system/log/system", params);
    }

    private String buildDocumentLogBasePath(Integer userId, String keyword) {
        List<String> params = new ArrayList<>();
        if (userId != null) {
            params.add("user_id=" + userId);
        }
        addQueryParam(params, "keyword", keyword);
        return buildBasePath("/system/log/document", params);
    }

    private void addQueryParam(List<String> params, String name, String value) {
        if (value != null && !value.isBlank()) {
            params.add(name + "=" + URLEncoder.encode(value, StandardCharsets.UTF_8));
        }
    }

    private String buildBasePath(String path, List<String> params) {
        return params.isEmpty() ? path : path + "?" + String.join("&", params);
    }
}
