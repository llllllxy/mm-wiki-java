package org.tinycloud.mmwiki.domain;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 安装向导运行期状态模型。
 *
 * <p>安装流程内存状态，无直接对应数据库表。</p>
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public class InstallData {

    public static final int LICENSE_DISAGREE = 0;
    public static final int LICENSE_AGREE = 1;
    public static final int ENV_NOT_ACCESS = 0;
    public static final int ENV_ACCESS = 1;
    public static final int SYS_NOT_ACCESS = 0;
    public static final int SYS_ACCESS = 1;
    public static final int DATABASE_NOT_ACCESS = 0;
    public static final int DATABASE_ACCESS = 1;
    public static final int INSTALL_READY = 0;
    public static final int INSTALL_START = 1;
    public static final int INSTALL_END = 2;
    public static final int INSTALL_DEFAULT = 0;
    public static final int INSTALL_FAILED = 1;
    public static final int INSTALL_SUCCESS = 2;

    /**
     * 授权协议确认状态
     */
    private int license = LICENSE_DISAGREE;
    /**
     * 环境检查状态
     */
    private int env = ENV_NOT_ACCESS;
    /**
     * 系统配置检查状态
     */
    private int system = SYS_NOT_ACCESS;
    /**
     * 数据库配置检查状态
     */
    private int database = DATABASE_NOT_ACCESS;
    /**
     * 系统配置表单数据
     */
    private Map<String, String> systemConf = defaultSystemConf();
    /**
     * 数据库配置表单数据
     */
    private Map<String, String> databaseConf = defaultDatabaseConf();
    /**
     * 安装执行状态
     */
    private int status = INSTALL_READY;
    /**
     * 安装执行结果说明
     */
    private String result = "";
    /**
     * 安装是否成功
     */
    private int isSuccess = INSTALL_DEFAULT;

    public int getLicense() {
        return license;
    }

    public void setLicense(int license) {
        this.license = license;
    }

    public int getEnv() {
        return env;
    }

    public void setEnv(int env) {
        this.env = env;
    }

    public int getSystem() {
        return system;
    }

    public void setSystem(int system) {
        this.system = system;
    }

    public int getDatabase() {
        return database;
    }

    public void setDatabase(int database) {
        this.database = database;
    }

    public Map<String, String> getSystemConf() {
        return systemConf;
    }

    public void setSystemConf(Map<String, String> systemConf) {
        this.systemConf = systemConf;
    }

    public Map<String, String> getDatabaseConf() {
        return databaseConf;
    }

    public void setDatabaseConf(Map<String, String> databaseConf) {
        this.databaseConf = databaseConf;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public int getIsSuccess() {
        return isSuccess;
    }

    public void setIsSuccess(int isSuccess) {
        this.isSuccess = isSuccess;
    }

    public boolean readyForInstall() {
        return license == LICENSE_AGREE
                && env == ENV_ACCESS
                && system == SYS_ACCESS
                && database == DATABASE_ACCESS;
    }

    public static Map<String, String> defaultSystemConf() {
        Map<String, String> conf = new LinkedHashMap<>();
        conf.put("addr", "0.0.0.0");
        conf.put("port", "8081");
        conf.put("document_dir", "");
        return conf;
    }

    public static Map<String, String> defaultDatabaseConf() {
        Map<String, String> conf = new LinkedHashMap<>();
        conf.put("host", "127.0.0.1");
        conf.put("port", "3306");
        conf.put("name", "mm_wiki");
        conf.put("user", "");
        conf.put("pass", "");
        conf.put("conn_max_idle", "30");
        conf.put("conn_max_connection", "200");
        conf.put("admin_name", "");
        conf.put("admin_pass", "");
        return conf;
    }
}
