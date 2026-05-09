package org.tinycloud.mmwiki.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * MM-Wiki 自定义配置类映射
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@ConfigurationProperties(prefix = "mmwiki")
@Component
public class MmwikiProperties {
    private String version = "v0.2.1-java-migration";
    private String copyright = "2026 MM-Wiki";
    private String systemNameFallback = "Markdown Mini Wiki";
    private String documentRootDir = "./data";
    private Search search = new Search();

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getCopyright() {
        return copyright;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }

    public String getSystemNameFallback() {
        return systemNameFallback;
    }

    public void setSystemNameFallback(String systemNameFallback) {
        this.systemNameFallback = systemNameFallback;
    }

    public String getDocumentRootDir() {
        return documentRootDir;
    }

    public void setDocumentRootDir(String documentRootDir) {
        this.documentRootDir = documentRootDir;
    }

    public Search getSearch() {
        return search;
    }

    public void setSearch(Search search) {
        this.search = search;
    }

    public static class Search {
        private int intervalTime = 30;
        private int batchUpdateDocNum = 100;

        public int getIntervalTime() {
            return intervalTime;
        }

        public void setIntervalTime(int intervalTime) {
            this.intervalTime = intervalTime;
        }

        public int getBatchUpdateDocNum() {
            return batchUpdateDocNum;
        }

        public void setBatchUpdateDocNum(int batchUpdateDocNum) {
            this.batchUpdateDocNum = batchUpdateDocNum;
        }
    }
}
