package org.tinycloud.mmwiki.vo;

/**
 * Dashboard view object.
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public class Dashboard {

    /**
     * normalUserCount.
     */
    private long normalUserCount;

    /**
     * forbiddenUserCount.
     */
    private long forbiddenUserCount;

    /**
     * spaceCount.
     */
    private long spaceCount;

    /**
     * documentCount.
     */
    private long documentCount;

    /**
     * todayLoginUserCount.
     */
    private long todayLoginUserCount;

    /**
     * createMaxUser.
     */
    private TopUser createMaxUser;

    /**
     * editMaxUser.
     */
    private TopUser editMaxUser;

    /**
     * collectMaxUser.
     */
    private TopUser collectMaxUser;

    /**
     * fansMaxUser.
     */
    private TopUser fansMaxUser;

    public Dashboard() {
    }

    public Dashboard(
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
        this.normalUserCount = normalUserCount;
        this.forbiddenUserCount = forbiddenUserCount;
        this.spaceCount = spaceCount;
        this.documentCount = documentCount;
        this.todayLoginUserCount = todayLoginUserCount;
        this.createMaxUser = createMaxUser;
        this.editMaxUser = editMaxUser;
        this.collectMaxUser = collectMaxUser;
        this.fansMaxUser = fansMaxUser;
    }

    public long getNormalUserCount() {
        return normalUserCount;
    }

    public void setNormalUserCount(long normalUserCount) {
        this.normalUserCount = normalUserCount;
    }

    public long getForbiddenUserCount() {
        return forbiddenUserCount;
    }

    public void setForbiddenUserCount(long forbiddenUserCount) {
        this.forbiddenUserCount = forbiddenUserCount;
    }

    public long getSpaceCount() {
        return spaceCount;
    }

    public void setSpaceCount(long spaceCount) {
        this.spaceCount = spaceCount;
    }

    public long getDocumentCount() {
        return documentCount;
    }

    public void setDocumentCount(long documentCount) {
        this.documentCount = documentCount;
    }

    public long getTodayLoginUserCount() {
        return todayLoginUserCount;
    }

    public void setTodayLoginUserCount(long todayLoginUserCount) {
        this.todayLoginUserCount = todayLoginUserCount;
    }

    public TopUser getCreateMaxUser() {
        return createMaxUser;
    }

    public void setCreateMaxUser(TopUser createMaxUser) {
        this.createMaxUser = createMaxUser;
    }

    public TopUser getEditMaxUser() {
        return editMaxUser;
    }

    public void setEditMaxUser(TopUser editMaxUser) {
        this.editMaxUser = editMaxUser;
    }

    public TopUser getCollectMaxUser() {
        return collectMaxUser;
    }

    public void setCollectMaxUser(TopUser collectMaxUser) {
        this.collectMaxUser = collectMaxUser;
    }

    public TopUser getFansMaxUser() {
        return fansMaxUser;
    }

    public void setFansMaxUser(TopUser fansMaxUser) {
        this.fansMaxUser = fansMaxUser;
    }

}
