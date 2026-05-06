package org.tinycloud.mmwiki.service;

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

    private final LogMapper logMapper;
    private final LogDocumentMapper logDocumentMapper;
    private final UserMapper userMapper;

    public LogService(LogMapper logMapper, LogDocumentMapper logDocumentMapper, UserMapper userMapper) {
        this.logMapper = logMapper;
        this.logDocumentMapper = logDocumentMapper;
        this.userMapper = userMapper;
    }

    public SystemLogPage systemLogs(Integer level, String message, String username, int page, int number) {
        int safePage = Math.max(1, page);
        int safeNumber = Math.max(10, Math.min(number, 100));
        int offset = (safePage - 1) * safeNumber;
        String searchMessage = message == null ? "" : message.trim();
        String searchUsername = username == null ? "" : username.trim();
        long count = logMapper.countByFilters(level, searchMessage, searchUsername);
        List<LogEntry> logs = logMapper.findByFilters(level, searchMessage, searchUsername, offset, safeNumber);
        logs.forEach(log -> log.setCreateTimeText(TimeUtils.formatUnix(log.getCreateTime())));
        String basePath = "/system/log/system?level=" + (level == null ? "" : level)
            + "&message=" + searchMessage
            + "&username=" + searchUsername;
        return new SystemLogPage(logs, level, searchMessage, searchUsername, Paginator.of(safePage, safeNumber, count, basePath));
    }

    public LogEntry findLog(Long logId) {
        LogEntry log = logId == null ? null : logMapper.findById(logId);
        if (log != null) {
            log.setCreateTimeText(TimeUtils.formatUnix(log.getCreateTime()));
        }
        return log;
    }

    public DocumentLogPage documentLogs(Integer userId, String keyword, int page, int number) {
        int safePage = Math.max(1, page);
        int safeNumber = Math.max(10, Math.min(number, 100));
        int offset = (safePage - 1) * safeNumber;
        String search = keyword == null ? "" : keyword.trim();
        long count = logDocumentMapper.countForSystem(userId, search);
        List<LogDocumentView> logs = logDocumentMapper.findForSystem(userId, search, offset, safeNumber);
        logs.forEach(log -> log.setCreateTimeText(TimeUtils.formatUnix(log.getCreateTime())));
        String basePath = "/system/log/document?keyword=" + search + (userId == null ? "" : "&user_id=" + userId);
        return new DocumentLogPage(logs, userMapper.findAllActive(), userId, search, Paginator.of(safePage, safeNumber, count, basePath));
    }

    public record SystemLogPage(List<LogEntry> logs, Integer level, String message, String username, Paginator paginator) {
    }

    public record DocumentLogPage(
        List<LogDocumentView> logDocuments,
        java.util.List<org.tinycloud.mmwiki.domain.User> users,
        Integer userId,
        String keyword,
        Paginator paginator
    ) {
    }
}
