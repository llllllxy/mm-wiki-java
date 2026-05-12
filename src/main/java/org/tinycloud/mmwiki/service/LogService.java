package org.tinycloud.mmwiki.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import org.springframework.stereotype.Service;
import org.tinycloud.mmwiki.domain.LogDocumentView;
import org.tinycloud.mmwiki.domain.LogEntry;
import org.tinycloud.mmwiki.mapper.LogDocumentMapper;
import org.tinycloud.mmwiki.mapper.LogMapper;
import org.tinycloud.mmwiki.util.TimeUtils;
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

    public PageModel<LogEntry> systemLogPage(Integer level, String message, String username, int pageNum, int pageSize) {
        String searchMessage = message == null ? "" : message.trim();
        String searchUsername = username == null ? "" : username.trim();
        PageInfo<LogEntry> pageInfo = PageHelper.startPage(pageNum, pageSize)
                .doSelectPageInfo(() -> logMapper.pageByFilters(level, searchMessage, searchUsername));
        List<LogEntry> logs = pageInfo.getList();
        logs.forEach(log -> log.setCreateTimeText(TimeUtils.format(log.getCreateTime())));
        return PageModel.build((long) pageInfo.getPageNum(), (long) pageInfo.getPageSize(), logs, pageInfo.getTotal(), (long) pageInfo.getPages());
    }

    public LogEntry findLog(Long logId) {
        LogEntry log = logId == null ? null : logMapper.findById(logId);
        if (log != null) {
            log.setCreateTimeText(TimeUtils.format(log.getCreateTime()));
        }
        return log;
    }

    public PageModel<LogDocumentView> documentLogPage(Integer userId, String keyword, int pageNum, int pageSize) {
        String search = keyword == null ? "" : keyword.trim();
        PageInfo<LogDocumentView> pageInfo = PageHelper.startPage(pageNum, pageSize)
                .doSelectPageInfo(() -> logDocumentMapper.pageForSystem(userId, search));
        pageInfo.getList().forEach(log -> log.setCreateTimeText(TimeUtils.format(log.getCreateTime())));
        return PageModel.from(pageInfo);
    }
}
