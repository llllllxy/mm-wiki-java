package org.tinycloud.mmwiki.service;

import org.tinycloud.mmwiki.exception.SystemException;
import org.tinycloud.mmwiki.util.BCrypt;
import org.tinycloud.mmwiki.vo.EnvView;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.tinycloud.mmwiki.config.MmwikiProperties;
import org.tinycloud.mmwiki.domain.InstallData;
import org.tinycloud.mmwiki.util.PasswordUtils;
import org.tinycloud.mmwiki.util.JsonUtils;

/**
 * 安装向导流程、数据库初始化和外部配置生成服务。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Service
public class InstallService {

    private static final Pattern SAFE_DATABASE_NAME = Pattern.compile("^[A-Za-z0-9_]+$");
    private final InstallData data = new InstallData();
    private final Path configDir = Path.of("config").toAbsolutePath().normalize();
    private final Path lockFile = configDir.resolve("install.lock");

    @Autowired
    private ThreadPoolTaskExecutor asyncServiceExecutor;

    @Autowired
    private MmwikiProperties properties;

    @Autowired
    private Environment environment;

    /**
     * 返回当前安装向导运行期状态。
     */
    public InstallData data() {
        return data;
    }

    /**
     * 判断系统是否已经完成安装。
     */
    public boolean installed() {
        return environment.matchesProfiles("dev") || Files.exists(lockFile);
    }

    /**
     * 读取 MIT License 内容。
     */
    public String licenseText() {
        try {
            return readRequiredResource("LICENSE");
        } catch (IOException ignored) {
            return "MIT License\n\nCopyright (c) 2018 phachon\n\nPermission is hereby granted, free of charge, to any person obtaining a copy of this software.";
        }
    }

    /**
     * 执行环境检测并返回页面展示数据。
     */
    public EnvView envView() {
        data.setEnv(InstallData.ENV_ACCESS);
        Map<String, String> server = new LinkedHashMap<>();
        server.put("host", localIp());
        server.put("sys", System.getProperty("os.name", ""));
        server.put("install_dir", Path.of("").toAbsolutePath().normalize().toString());
        server.put("version", properties.getVersion());

        long memoryMb = Runtime.getRuntime().maxMemory() / 1024 / 1024;
        int cpuCount = Runtime.getRuntime().availableProcessors();
        Map<String, Object> mem = envRow("内存", "400M", memoryMb + "M", memoryMb >= 400);
        Map<String, Object> cpu = envRow("CPU", "1核", cpuCount + "核", cpuCount >= 1);
        if (!Boolean.TRUE.equals(mem.get("ok")) || !Boolean.TRUE.equals(cpu.get("ok"))) {
            data.setEnv(InstallData.ENV_NOT_ACCESS);
        }

        List<Map<String, Object>> dirData = List.of(
                dirRow("db/mmwiki-schema.sql", "存在且可读", resourceReadable("db/mmwiki-schema.sql")),
                dirRow("db/data.sql", "存在且可读", resourceReadable("db/data.sql")),
                dirRow("templates", "存在且不为空", resourceReadable("templates/install/index.html")),
                dirRow("static", "存在且不为空", resourceReadable("static/js/modules/install.js")),
                dirRow("config/application.yml", "安装后可写", canWriteConfigDirectory())
        );
        for (Map<String, Object> row : dirData) {
            if (!Boolean.TRUE.equals(row.get("ok"))) {
                data.setEnv(InstallData.ENV_NOT_ACCESS);
            }
        }
        return new EnvView(server, List.of(mem, cpu), dirData);
    }

    /**
     * 保存系统监听与文档目录配置。
     */
    public String saveSystemConfig(String addr, String port, String documentDir) {
        if (!StringUtils.hasText(addr)) {
            return "addr 不能为空，默认请填写 0.0.0.0";
        }
        int portValue = parsePositiveInt(port, 0);
        if (portValue <= 0 || portValue > 65535) {
            return "启动端口不能为空且必须在 1-65535 范围内";
        }
        if (!StringUtils.hasText(documentDir)) {
            return "文档保存目录不能为空";
        }
        Path documentPath = Path.of(documentDir).toAbsolutePath().normalize();
        if (!documentPath.isAbsolute()) {
            return "文档保存目录必须是绝对路径";
        }
        if (!Files.exists(documentPath) || !Files.isDirectory(documentPath)) {
            return "文档保存目录不存在";
        }
        Map<String, String> conf = new LinkedHashMap<>();
        conf.put("addr", addr.trim());
        conf.put("port", String.valueOf(portValue));
        conf.put("document_dir", documentPath.toString());
        data.setSystemConf(conf);
        data.setSystem(InstallData.SYS_ACCESS);
        return "";
    }

    /**
     * 保存数据库连接与管理员配置。
     */
    public String saveDatabaseConfig(Map<String, String> request) {
        Map<String, String> conf = new LinkedHashMap<>(InstallData.defaultDatabaseConf());
        conf.replaceAll((key, value) -> request.getOrDefault(key, value) == null ? "" : request.getOrDefault(key, value).trim());
        if (!StringUtils.hasText(conf.get("host"))) {
            return "数据库 host 不能为空";
        }
        if (parsePositiveInt(conf.get("port"), 0) <= 0) {
            return "数据库端口不能为空";
        }
        if (!StringUtils.hasText(conf.get("name")) || !SAFE_DATABASE_NAME.matcher(conf.get("name")).matches()) {
            return "数据库名不能为空，且只能包含字母、数字和下划线";
        }
        if (!StringUtils.hasText(conf.get("user"))) {
            return "数据库用户名不能为空";
        }
        if (!StringUtils.hasText(conf.get("pass"))) {
            return "数据库密码不能为空";
        }
        if (parsePositiveInt(conf.get("conn_max_idle"), 0) <= 0) {
            return "数据库空闲连接数不能为 0";
        }
        if (parsePositiveInt(conf.get("conn_max_connection"), 0) <= 0) {
            return "最大连接数不能为 0";
        }
        if (!StringUtils.hasText(conf.get("admin_name")) || !conf.get("admin_name").matches("^[A-Za-z0-9]+$")) {
            return "超级管理员用户名不能为空，且只能由数字和字母组成";
        }
        if (!StringUtils.hasText(conf.get("admin_pass"))) {
            return "超级管理员密码不能为空";
        }
        data.setDatabaseConf(conf);
        data.setDatabase(InstallData.DATABASE_ACCESS);
        return "";
    }

    /**
     * 启动异步安装任务。
     */
    public synchronized String startInstall() {
        if (!data.readyForInstall()) {
            return "请先完成安装准备";
        }
        if (data.getStatus() == InstallData.INSTALL_START) {
            return "";
        }
        data.setStatus(InstallData.INSTALL_START);
        data.setIsSuccess(InstallData.INSTALL_DEFAULT);
        data.setResult("");
        asyncServiceExecutor.execute(this::runInstall);
        return "";
    }

    private void runInstall() {
        try {
            checkDb();
            createDb();
            executeSql(connectToDatabase(), schemaSql());
            createAdmin();
            executeSql(connectToDatabase(), dataSql());
            insertSystemVersion();
            writeApplicationYaml();
            createLockFile();
            installSuccess();
        } catch (Exception ex) {
            installFailed(ex.getMessage());
        }
    }

    private void checkDb() throws Exception {
        try (Connection connection = connectWithoutDatabase()) {
            if (!connection.isValid(5)) {
                throw new SystemException("数据库连接不可用");
            }
        }
    }

    private void createDb() throws Exception {
        try (Connection connection = connectWithoutDatabase(); Statement statement = connection.createStatement()) {
            statement.execute("CREATE DATABASE IF NOT EXISTS `" + data.getDatabaseConf().get("name") + "` CHARACTER SET utf8mb4");
        }
    }

    private void createAdmin() throws Exception {
        String sql = "INSERT mw_user SET username=?, password=?, given_name=?, role_id=?, create_time=?, update_time=?";
        LocalDateTime now = LocalDateTime.now();
        try (Connection connection = connectToDatabase(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, data.getDatabaseConf().get("admin_name"));
            statement.setString(2, BCrypt.hashpw(PasswordUtils.sha256(data.getDatabaseConf().get("admin_pass")), BCrypt.gensalt()));
            statement.setString(3, data.getDatabaseConf().get("admin_name"));
            statement.setInt(4, 1);
            statement.setObject(5, now);
            statement.setObject(6, now);
            statement.executeUpdate();
        }
    }

    private void insertSystemVersion() throws Exception {
        String sql = "INSERT mw_config SET `name`=?, `key`=?, `value`=?, create_time=?, update_time=?";
        LocalDateTime now = LocalDateTime.now();
        try (Connection connection = connectToDatabase(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "系统版本号");
            statement.setString(2, "system_version");
            statement.setString(3, properties.getVersion());
            statement.setObject(4, now);
            statement.setObject(5, now);
            statement.executeUpdate();
        }
    }

    private void executeSql(Connection connection, String sql) throws Exception {
        try (connection; Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    private Connection connectWithoutDatabase() throws Exception {
        Map<String, String> conf = data.getDatabaseConf();
        String url = "jdbc:mysql://" + conf.get("host") + ":" + conf.get("port")
                + "/?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&sslMode=DISABLED&allowMultiQueries=true";
        return DriverManager.getConnection(url, conf.get("user"), conf.get("pass"));
    }

    private Connection connectToDatabase() throws Exception {
        Map<String, String> conf = data.getDatabaseConf();
        String url = "jdbc:mysql://" + conf.get("host") + ":" + conf.get("port") + "/" + conf.get("name")
                + "?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&sslMode=DISABLED&allowMultiQueries=true";
        return DriverManager.getConnection(url, conf.get("user"), conf.get("pass"));
    }

    private String schemaSql() throws IOException {
        return readRequiredResource("db/mmwiki-schema.sql");
    }

    private String dataSql() throws IOException {
        return readRequiredResource("db/data.sql");
    }

    private boolean resourceReadable(String path) {
        return new ClassPathResource(path).exists();
    }

    private String readRequiredResource(String resourcePath) throws IOException {
        ClassPathResource resource = new ClassPathResource(resourcePath);
        if (!resource.exists()) {
            throw new IOException("未找到 classpath:" + resourcePath);
        }
        try (InputStream inputStream = resource.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private void writeApplicationYaml() throws IOException {
        Map<String, String> db = data.getDatabaseConf();
        Map<String, String> sys = data.getSystemConf();
        Files.createDirectories(configDir);
        String jdbcUrl = "jdbc:mysql://" + db.get("host") + ":" + db.get("port") + "/" + db.get("name")
                + "?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&sslMode=DISABLED";
        String yaml = """
                server:
                  port: %s
                  address: %s
                  servlet:
                    session:
                      cookie:
                        http-only: true
                    encoding:
                      charset: UTF-8
                      force: true
                      enabled: true
                spring:
                  application:
                    name: mmwiki
                  profiles:
                    active: prod
                  datasource:
                    url: %s
                    driver-class-name: com.mysql.cj.jdbc.Driver
                    username: %s
                    password: %s
                    hikari:
                      minimum-idle: %s
                      maximum-pool-size: %s
                      idle-timeout: 30000
                      max-lifetime: 1800000
                      connection-timeout: 30000
                      connection-test-query: SELECT 1
                  thymeleaf:
                    cache: false
                    encoding: UTF-8
                    mode: HTML
                    prefix: classpath:/templates/
                    suffix: .html
                  session:
                    store-type: jdbc
                    timeout: 1800s
                    jdbc:
                      initialize-schema: never
                      cleanup-cron: 0 0/5 * * * ?
                  servlet:
                    multipart:
                      enabled: true
                      max-file-size: 100MB
                      max-request-size: 200MB
                  jackson:
                    time-zone: GMT+8
                    date-format: yyyy-MM-dd HH:mm:ss
                
                logging:
                  level:
                    root: info
                  file:
                    name: /opt/logs/mm-wiki.log
                  pattern:
                    console: "%%d{yyyy-MM-dd HH:mm:ss.SSS} %%-5level [%%thread] %%logger{36} - [%%method,%%line] - %%msg%%n"
                    file: "%%d{yyyy-MM-dd HH:mm:ss.SSS} %%-5level [%%thread] %%logger{36} - [%%method,%%line] - %%msg%%n"
                    level: "%%5p"
                    dateformat: "yyyy-MM-dd HH:mm:ss.SSS"
                    correlation: ""
                  logback:
                    rollingpolicy:
                      file-name-pattern: logs/mm-wiki.%%d{yyyy-MM-dd}.%%i.log.gz
                      max-file-size: 10MB
                      max-history: 7
                      total-size-cap: 1GB
                      clean-history-on-start: false
                
                mybatis:
                  type-aliases-package: org.tinycloud.mmwiki.domain
                  mapper-locations: classpath:/mapper/*.xml
                  configuration:
                    map-underscore-to-camel-case: true
                    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
                
                pagehelper:
                  helper-dialect: mysql
                  reasonable: false
                  support-methods-arguments: false
                
                async:
                  executor:
                    thread:
                      core-pool-size: 2
                      max-pool-size: 8
                      keep-alive-seconds: 60
                      queue-capacity: 100
                      name-prefix: mmwiki-async-
                
                mmwiki:
                  version: %s
                  copyright: %s
                  system-name-fallback: Markdown Mini Wiki
                  document-root-dir: %s
                  pdf:
                    font-path:
                  search:
                    interval-time: 30
                    batch-update-doc-num: 100
                """.formatted(
                sys.get("port"),
                yamlValue(sys.get("addr")),
                yamlValue(jdbcUrl),
                yamlValue(db.get("user")),
                yamlValue(db.get("pass")),
                db.get("conn_max_idle"),
                db.get("conn_max_connection"),
                yamlValue(properties.getVersion()),
                yamlValue(properties.getCopyright()),
                yamlValue(data.getSystemConf().get("document_dir").replace("\\", "/"))
        );
        Files.writeString(configDir.resolve("application.yml"), yaml, StandardCharsets.UTF_8);
    }

    private void createLockFile() throws IOException {
        Files.writeString(lockFile, "installed at " + Instant.now(), StandardCharsets.UTF_8);
    }

    private void installFailed(String message) {
        data.setResult(message == null ? "安装失败" : message);
        data.setStatus(InstallData.INSTALL_END);
        data.setIsSuccess(InstallData.INSTALL_FAILED);
    }

    private void installSuccess() {
        Map<String, String> result = new LinkedHashMap<>();
        result.put("cmd", "java -jar mmwiki-0.0.1-SNAPSHOT.jar");
        result.put("url", "http://127.0.0.1:" + data.getSystemConf().get("port"));
        result.put("config", configDir.resolve("application.yml").toString());
        data.setResult(JsonUtils.writeValueAsString(result));
        data.setStatus(InstallData.INSTALL_END);
        data.setIsSuccess(InstallData.INSTALL_SUCCESS);
    }

    private Map<String, Object> envRow(String name, String require, String value, boolean ok) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("name", name);
        row.put("require", require);
        row.put("value", value);
        row.put("result", ok ? "1" : "0");
        row.put("ok", ok);
        return row;
    }

    private Map<String, Object> dirRow(String path, String require, boolean ok) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("path", path);
        row.put("require", require);
        row.put("result", ok ? "1" : "0");
        row.put("ok", ok);
        return row;
    }

    private boolean canWriteConfigDirectory() {
        try {
            Files.createDirectories(configDir);
            Path probe = configDir.resolve(".install-write-test");
            Files.writeString(probe, "ok", StandardCharsets.UTF_8);
            Files.deleteIfExists(probe);
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    private String localIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception ex) {
            return "127.0.0.1";
        }
    }

    private int parsePositiveInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value == null ? "" : value.trim());
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private String yamlValue(String value) {
        String safeValue = value == null ? "" : value;
        if (safeValue.isBlank()) {
            return "\"\"";
        }
        if (safeValue.matches("^[A-Za-z0-9_./:@?&=+%#-]+$")) {
            return safeValue;
        }
        return "\"" + safeValue.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
