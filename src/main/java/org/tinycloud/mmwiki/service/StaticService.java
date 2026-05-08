package org.tinycloud.mmwiki.service;

import org.springframework.beans.factory.annotation.Autowired;
import com.sun.management.OperatingSystemMXBean;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.tinycloud.mmwiki.domain.LogEntry;
import org.tinycloud.mmwiki.domain.User;
import org.tinycloud.mmwiki.mapper.CollectionMapper;
import org.tinycloud.mmwiki.mapper.DocumentMapper;
import org.tinycloud.mmwiki.mapper.FollowMapper;
import org.tinycloud.mmwiki.mapper.LogMapper;
import org.tinycloud.mmwiki.mapper.SpaceMapper;
import org.tinycloud.mmwiki.mapper.UserMapper;

/**
 * MM-Wiki 业务服务实现。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Service
public class StaticService {

    private final long startTime = Instant.now().getEpochSecond();
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private SpaceMapper spaceMapper;
    @Autowired
    private DocumentMapper documentMapper;
    @Autowired
    private CollectionMapper collectionMapper;
    @Autowired
    private FollowMapper followMapper;
    @Autowired
    private LogMapper logMapper;

    public Dashboard dashboard() {
        int todayStart = Math.toIntExact(LocalDate.now()
            .atStartOfDay(ZoneId.systemDefault())
            .toEpochSecond());
        return new Dashboard(
            userMapper.countNormalUsers(),
            userMapper.countForbiddenUsers(),
            spaceMapper.countAll(),
            documentMapper.countActive(),
            userMapper.countByLastTimeAfter(todayStart),
            topUser(documentMapper.findTopCreateUserId()),
            topUser(documentMapper.findTopEditUserId()),
            topUser(collectionMapper.findTopUserIdByType(CollectionService.TYPE_DOC)),
            topUser(followMapper.findTopObjectIdByType(FollowService.TYPE_USER))
        );
    }

    public List<Map<String, Object>> spaceDocsRank(int number) {
        return documentMapper.findSpaceDocumentRank(Math.max(1, Math.min(number, 100)));
    }

    public List<Map<String, Object>> collectDocRank(int number) {
        return collectionMapper.findResourceRank(CollectionService.TYPE_DOC, Math.max(1, Math.min(number, 100)));
    }

    public List<Map<String, Object>> docCountByTime(int limitDay) {
        int days = Math.max(1, Math.min(limitDay, 365));
        int start = Math.toIntExact(Instant.now().getEpochSecond() - days * 86400L);
        return documentMapper.countGroupByCreateDate(start);
    }

    public Monitor monitor() {
        Map<String, String> serverInfo = new LinkedHashMap<>();
        try {
            InetAddress local = InetAddress.getLocalHost();
            serverInfo.put("localIp", local.getHostAddress());
            serverInfo.put("hostname", local.getHostName());
        } catch (Exception ignored) {
            serverInfo.put("localIp", "127.0.0.1");
            serverInfo.put("hostname", "");
        }
        serverInfo.put("os", System.getProperty("os.name", ""));
        serverInfo.put("platform", System.getProperty("os.version", ""));
        serverInfo.put("platformFamily", System.getProperty("os.arch", ""));
        List<LogEntry> errLogs = logMapper.findByLevel(LogService.LEVEL_ERROR, 0, 5);
        errLogs.forEach(log -> log.setCreateTimeText(org.tinycloud.mmwiki.util.TimeUtils.formatUnix(log.getCreateTime())));
        return new Monitor(serverInfo, logMapper.countByLevel(LogService.LEVEL_ERROR), errLogs);
    }

    public Map<String, Object> serverStatus() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("cpu_used_percent", cpuUsedPercent());
        data.put("memory_used_percent", memoryUsedPercent());
        data.put("disk_used_percent", diskUsedPercent());
        return data;
    }

    public Map<String, Object> serverTime() {
        long now = Instant.now().getEpochSecond();
        return Map.of("server_time", now, "run_time", now - startTime);
    }

    private TopUser topUser(Integer userId) {
        if (userId == null || userId <= 0) {
            return new TopUser(0, "");
        }
        User user = userMapper.findActiveById(userId);
        return new TopUser(userId, user == null ? "" : user.getUsername());
    }

    private int cpuUsedPercent() {
        try {
            OperatingSystemMXBean os = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
            double load = os.getCpuLoad();
            return load < 0 ? 0 : (int) Math.round(load * 100);
        } catch (Exception ignored) {
            return 0;
        }
    }

    private int memoryUsedPercent() {
        Runtime runtime = Runtime.getRuntime();
        long max = runtime.maxMemory();
        long used = runtime.totalMemory() - runtime.freeMemory();
        return max <= 0 ? 0 : (int) Math.round(used * 100.0 / max);
    }

    private int diskUsedPercent() {
        File root = new File(".").getAbsoluteFile().toPath().getRoot().toFile();
        long total = root.getTotalSpace();
        long free = root.getFreeSpace();
        return total <= 0 ? 0 : (int) Math.round((total - free) * 100.0 / total);
    }

    public record TopUser(Integer userId, String username) {
    }

    public record Dashboard(
        long normalUserCount,
        long forbiddenUserCount,
        long spaceCount,
        long documentCount,
        long todayLoginUserCount,
        TopUser createMaxUser,
        TopUser editMaxUser,
        TopUser collectMaxUser,
        TopUser fansMaxUser
    ) {
    }

    public record Monitor(Map<String, String> serverInfo, long errorLogCount, List<LogEntry> errLogs) {
    }
}
