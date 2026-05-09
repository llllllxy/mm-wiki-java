package org.tinycloud.mmwiki.vo;

import java.util.List;
import java.util.Map;

import org.tinycloud.mmwiki.domain.LogEntry;

/**
 * Monitor view object.
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public class Monitor {

    /**
     * serverInfo.
     */
    private Map<String, String> serverInfo;

    /**
     * errorLogCount.
     */
    private long errorLogCount;

    /**
     * errLogs.
     */
    private List<LogEntry> errLogs;

    public Monitor() {
    }

    public Monitor(
            Map<String, String> serverInfo,
            long errorLogCount,
            List<LogEntry> errLogs
    ) {
        this.serverInfo = serverInfo;
        this.errorLogCount = errorLogCount;
        this.errLogs = errLogs;
    }

    public Map<String, String> getServerInfo() {
        return serverInfo;
    }

    public void setServerInfo(Map<String, String> serverInfo) {
        this.serverInfo = serverInfo;
    }

    public long getErrorLogCount() {
        return errorLogCount;
    }

    public void setErrorLogCount(long errorLogCount) {
        this.errorLogCount = errorLogCount;
    }

    public List<LogEntry> getErrLogs() {
        return errLogs;
    }

    public void setErrLogs(List<LogEntry> errLogs) {
        this.errLogs = errLogs;
    }

}
