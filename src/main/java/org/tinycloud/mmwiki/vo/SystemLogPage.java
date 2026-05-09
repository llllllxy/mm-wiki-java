package org.tinycloud.mmwiki.vo;

import java.util.List;

import org.tinycloud.mmwiki.domain.LogEntry;
import org.tinycloud.mmwiki.web.Paginator;

/**
 * SystemLogPage view object.
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public class SystemLogPage {

    /**
     * logs.
     */
    private List<LogEntry> logs;

    /**
     * level.
     */
    private Integer level;

    /**
     * message.
     */
    private String message;

    /**
     * username.
     */
    private String username;

    /**
     * paginator.
     */
    private Paginator paginator;

    public SystemLogPage() {
    }

    public SystemLogPage(
            List<LogEntry> logs,
            Integer level,
            String message,
            String username,
            Paginator paginator
    ) {
        this.logs = logs;
        this.level = level;
        this.message = message;
        this.username = username;
        this.paginator = paginator;
    }

    public List<LogEntry> getLogs() {
        return logs;
    }

    public void setLogs(List<LogEntry> logs) {
        this.logs = logs;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Paginator getPaginator() {
        return paginator;
    }

    public void setPaginator(Paginator paginator) {
        this.paginator = paginator;
    }

}
