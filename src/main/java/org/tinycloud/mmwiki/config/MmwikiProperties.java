package org.tinycloud.mmwiki.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mmwiki")
public class MmwikiProperties {

    private String version = "v0.2.1-java-migration";
    private String copyright = "2018 phachon";
    private String systemNameFallback = "Markdown Mini Wiki";
    private String documentRootDir = "./data";
    private Author author = new Author();
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

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public Search getSearch() {
        return search;
    }

    public void setSearch(Search search) {
        this.search = search;
    }

    public static class Author {
        private String passport = "mmwikipassport";
        private int cookieExpired = 72000;

        public String getPassport() {
            return passport;
        }

        public void setPassport(String passport) {
            this.passport = passport;
        }

        public int getCookieExpired() {
            return cookieExpired;
        }

        public void setCookieExpired(int cookieExpired) {
            this.cookieExpired = cookieExpired;
        }
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
