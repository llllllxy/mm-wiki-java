package org.tinycloud.mmwiki.service;

import org.tinycloud.paginate.Page;
import org.tinycloud.paginate.request.PaginateRequest;
import org.tinycloud.mmwiki.util.TimeUtils;
import org.tinycloud.mmwiki.vo.Dashboard;
import org.tinycloud.mmwiki.vo.Monitor;
import org.tinycloud.mmwiki.vo.TopUser;

import org.springframework.beans.factory.annotation.Autowired;
import com.sun.management.OperatingSystemMXBean;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.tinycloud.mmwiki.constant.CollectionTypeEnum;
import org.tinycloud.mmwiki.constant.FollowTypeEnum;
import org.tinycloud.mmwiki.domain.LogEntry;
import org.tinycloud.mmwiki.domain.User;
import org.tinycloud.mmwiki.mapper.CollectionMapper;
import org.tinycloud.mmwiki.mapper.DocumentMapper;
import org.tinycloud.mmwiki.mapper.FollowMapper;
import org.tinycloud.mmwiki.mapper.LogMapper;
import org.tinycloud.mmwiki.mapper.SpaceMapper;
import org.tinycloud.mmwiki.mapper.UserMapper;

/**
 * 后台统计服务，负责仪表盘概览、排行榜、趋势图和服务器监控数据查询。
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

    /**
     * 汇总后台仪表盘统计数据，包括用户、空间、文档和活跃用户指标。
     *
     * @return 仪表盘统计视图
     */
    public Dashboard dashboard() {
        int todayStart = Math.toIntExact(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toEpochSecond());
        return new Dashboard(
                userMapper.countNormalUsers(),
                userMapper.countForbiddenUsers(),
                spaceMapper.countAll(),
                documentMapper.countActive(),
                userMapper.countByLastTimeAfter(todayStart),
                topUser(documentMapper.findTopCreateUserId()),
                topUser(documentMapper.findTopEditUserId()),
                topUser(collectionMapper.findTopUserIdByType(CollectionTypeEnum.DOCUMENT.getCode())),
                topUser(followMapper.findTopObjectIdByType(FollowTypeEnum.USER.getCode()))
        );
    }

    /**
     * 查询空间文档数量排行，数量会限制在 1 到 100 之间。
     *
     * @param number 期望返回的排行数量
     * @return 空间文档排行数据
     */
    public List<Map<String, Object>> spaceDocsRank(int number) {
        return documentMapper.findSpaceDocumentRank(Math.max(1, Math.min(number, 100)));
    }

    /**
     * 查询文档收藏数量排行，数量会限制在 1 到 100 之间。
     *
     * @param number 期望返回的排行数量
     * @return 文档收藏排行数据
     */
    public List<Map<String, Object>> collectDocRank(int number) {
        return collectionMapper.findResourceRank(CollectionTypeEnum.DOCUMENT.getCode(), Math.max(1, Math.min(number, 100)));
    }

    /**
     * 查询最近指定天数内的文档创建数量趋势，天数会限制在 1 到 365 之间。
     *
     * @param limitDay 统计天数
     * @return 按日期聚合的文档数量数据
     */
    public List<Map<String, Object>> docCountByTime(int limitDay) {
        int days = Math.max(1, Math.min(limitDay, 365));
        LocalDateTime start = LocalDateTime.now().minusDays(days);
        return documentMapper.countGroupByCreateDate(start);
    }

    /**
     * 加载系统监控页数据，包括服务器信息和最近错误日志。
     *
     * @return 系统监控视图
     */
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
        Page<LogEntry> pageInfo = PaginateRequest.of(1, 5).request(() -> logMapper.pageByLevel(LogService.LEVEL_ERROR));
        List<LogEntry> errLogs = pageInfo.getRecords();
        errLogs.forEach(log -> log.setCreateTimeText(TimeUtils.format(log.getCreateTime())));
        return new Monitor(serverInfo, pageInfo.getTotal(), errLogs);
    }

    /**
     * 获取服务器 CPU、内存和磁盘使用率。
     *
     * @return 使用率数据映射
     */
    public Map<String, Object> serverStatus() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("cpu_used_percent", cpuUsedPercent());
        data.put("memory_used_percent", memoryUsedPercent());
        data.put("disk_used_percent", diskUsedPercent());
        return data;
    }

    /**
     * 获取当前服务器时间和应用运行时长。
     *
     * @return 服务时间数据映射
     */
    public Map<String, Object> serverTime() {
        long now = Instant.now().getEpochSecond();
        return Map.of("server_time", now, "run_time", now - startTime);
    }

    /**
     * 根据用户ID构造排行用户信息，用户不存在时返回空用户名。
     *
     * @param userId 用户ID
     * @return 排行用户信息
     */
    private TopUser topUser(Integer userId) {
        if (userId == null || userId <= 0) {
            return new TopUser(0, "");
        }
        User user = userMapper.findActiveById(userId);
        return new TopUser(userId, user == null ? "" : user.getUsername());
    }

    /**
     * 读取 JVM 暴露的系统 CPU 使用率，读取失败时返回 0。
     *
     * @return CPU 使用率百分比
     */
    private int cpuUsedPercent() {
        try {
            OperatingSystemMXBean os = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
            double load = os.getCpuLoad();
            return load < 0 ? 0 : (int) Math.round(load * 100);
        } catch (Exception ignored) {
            return 0;
        }
    }

    /**
     * 计算当前 JVM 内存使用率。
     *
     * @return 内存使用率百分比
     */
    private int memoryUsedPercent() {
        Runtime runtime = Runtime.getRuntime();
        long max = runtime.maxMemory();
        long used = runtime.totalMemory() - runtime.freeMemory();
        return max <= 0 ? 0 : (int) Math.round(used * 100.0 / max);
    }

    /**
     * 计算当前磁盘根分区使用率。
     *
     * @return 磁盘使用率百分比
     */
    private int diskUsedPercent() {
        File root = new File(".").getAbsoluteFile().toPath().getRoot().toFile();
        long total = root.getTotalSpace();
        long free = root.getFreeSpace();
        return total <= 0 ? 0 : (int) Math.round((total - free) * 100.0 / total);
    }
}
