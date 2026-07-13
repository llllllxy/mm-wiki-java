package org.tinycloud.mmwiki.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.tinycloud.mmwiki.service.ConfigService;
import org.tinycloud.mmwiki.service.DocumentSearchIndexService;
import org.tinycloud.mmwiki.service.InstallService;

import java.time.Instant;

/**
 * 文档全文搜索索引定时任务，负责按系统配置周期触发本地索引补偿重建。
 *
 * @author liuxingyu01
 * @since 2026-06-22
 */
@Component
public class DocumentSearchIndexScheduler {
    private static final Logger log = LoggerFactory.getLogger(DocumentSearchIndexScheduler.class);

    private static final int DEFAULT_REBUILD_INTERVAL_SECONDS = 3600;

    private Instant lastRebuildTime = Instant.EPOCH;

    @Autowired
    private InstallService installService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private DocumentSearchIndexService documentSearchIndexService;

    /**
     * 每分钟检查一次是否需要重建全文索引，真正的重建间隔由 doc_search_timer 控制。
     */
    @Scheduled(fixedDelayString = "60000")
    public void rebuildDocumentSearchIndex() {
        log.info("DocumentSearchIndexScheduler.rebuildDocumentSearchIndex start");
        if (!this.installService.installed() || !this.documentSearchIndexService.isFullTextSearchOpen()) {
            return;
        }
        long intervalSeconds = docSearchTimerSeconds();
        if (Instant.now().minusSeconds(intervalSeconds).isBefore(this.lastRebuildTime)) {
            return;
        }
        this.documentSearchIndexService.rebuildAll();
        this.lastRebuildTime = Instant.now();
        log.info("DocumentSearchIndexScheduler.rebuildDocumentSearchIndex done");
    }

    /**
     * 读取索引重建间隔配置，配置缺失或无效时返回默认间隔。
     *
     * @return 重建间隔秒数
     */
    private long docSearchTimerSeconds() {
        try {
            String value = this.configService.getValue("doc_search_timer", String.valueOf(DEFAULT_REBUILD_INTERVAL_SECONDS));
            long seconds = Long.parseLong(value);
            return seconds > 0 ? seconds : DEFAULT_REBUILD_INTERVAL_SECONDS;
        } catch (Exception ex) {
            return DEFAULT_REBUILD_INTERVAL_SECONDS;
        }
    }
}
