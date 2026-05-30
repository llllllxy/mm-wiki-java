package org.tinycloud.mmwiki.service;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.tinycloud.mmwiki.domain.LoginAuth;
import org.tinycloud.mmwiki.exception.SystemException;
import org.tinycloud.mmwiki.util.JsonUtils;

/**
 * 统一登录认证服务。
 *
 * <p>兼容旧版 MM-Wiki 的 HTTP、HTTPS、LDAP、LDAPS 认证方式。</p>
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Service
public class UnifiedAuthService {

    private static final String LDAP_DEFAULT_ACCOUNT_PATTERN = "(&(objectClass=User)(userPrincipalName=%s))";
    private static final String LDAP_DEFAULT_GIVEN_NAME_KEY = "displayName";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    /**
     * 根据启用的认证配置校验统一登录账号密码。
     */
    public AuthLoginProfile authenticate(LoginAuth loginAuth, String username, String password) throws Exception {
        URI uri = URI.create(loginAuth.getUrl());
        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase();
        return switch (scheme) {
            case "http", "https" -> authenticateByHttp(loginAuth, username, password);
            case "ldap", "ldaps" -> authenticateByLdap(loginAuth, username, password);
            default -> throw new IllegalArgumentException("登录认证 URL 协议不支持");
        };
    }

    /**
     * 调用 HTTP/HTTPS 统一认证接口校验账号密码，并解析返回的用户资料。
     *
     * @param loginAuth 登录认证配置
     * @param username  用户名
     * @param password  明文密码
     * @return 统一认证返回的用户资料
     * @throws IOException          HTTP 请求或响应解析失败时抛出
     * @throws InterruptedException HTTP 请求被中断时抛出
     */
    private AuthLoginProfile authenticateByHttp(LoginAuth loginAuth, String username, String password)
            throws IOException, InterruptedException {
        Map<String, String> form = new LinkedHashMap<>();
        form.put("username", username);
        form.put("password", password);
        form.put("ext_data", value(loginAuth.getExtData()));

        HttpRequest request = HttpRequest.newBuilder(URI.create(loginAuth.getUrl()))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(formEncode(form)))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (!StringUtils.hasText(response.body())) {
            throw new SystemException("登录认证失败, httpCode=" + response.statusCode());
        }

        HttpAuthResponse authResponse = JsonUtils.readValue(response.body(), HttpAuthResponse.class);
        if (authResponse == null) {
            throw new SystemException("登录认证失败");
        }
        if (StringUtils.hasText(authResponse.getMessage())) {
            throw new SystemException("登录认证失败, message=" + authResponse.getMessage());
        }
        if (authResponse.getData() == null) {
            throw new SystemException("登录认证失败");
        }
        return authResponse.getData();
    }

    /**
     * 通过 LDAP/LDAPS 查询用户并校验用户密码，成功后组装统一用户资料。
     *
     * @param loginAuth 登录认证配置
     * @param username  用户名
     * @param password  明文密码
     * @return LDAP 用户资料
     * @throws Exception LDAP 查询或认证失败时抛出
     */
    private AuthLoginProfile authenticateByLdap(LoginAuth loginAuth, String username, String password) throws Exception {
        if (!StringUtils.hasText(loginAuth.getExtData())) {
            throw new IllegalArgumentException("LDAP 配置数据错误");
        }
        LdapAuthConfig config = JsonUtils.readValue(loginAuth.getExtData(), LdapAuthConfig.class);
        if (config == null) {
            throw new IllegalArgumentException("LDAP 配置数据错误");
        }
        config.applyDefaults();
        if (!StringUtils.hasText(config.getGivenNameKey())) {
            throw new IllegalArgumentException("LDAP 配置 given_name_key 错误");
        }

        InitialDirContext searchContext = new InitialDirContext(ldapEnv(
                loginAuth.getUrl(),
                config.getBindUsername(),
                config.getBindPassword()
        ));
        try {
            SearchControls controls = new SearchControls();
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            controls.setReturningAttributes(ldapReturningAttributes(config));

            NamingEnumeration<SearchResult> results = searchContext.search(
                    config.getBasedn(),
                    String.format(config.getAccountPattern(), escapeLdapFilterValue(username)),
                    controls
            );
            List<SearchResult> entries = new ArrayList<>();
            while (results.hasMore()) {
                entries.add(results.next());
            }
            if (entries.size() != 1) {
                throw new IllegalArgumentException("用户不存在或密码错误");
            }

            SearchResult entry = entries.get(0);
            verifyLdapUserPassword(loginAuth.getUrl(), entry.getNameInNamespace(), password);
            return ldapProfile(entry.getAttributes(), config);
        } finally {
            searchContext.close();
        }
    }

    /**
     * 使用用户 DN 和密码重新绑定 LDAP，用于验证用户密码是否正确。
     *
     * @param url      LDAP 服务地址
     * @param userDn   用户 DN
     * @param password 明文密码
     * @throws Exception LDAP 绑定失败时抛出
     */
    private void verifyLdapUserPassword(String url, String userDn, String password) throws Exception {
        InitialDirContext userContext = new InitialDirContext(ldapEnv(url, userDn, password));
        userContext.close();
    }

    /**
     * 构造 LDAP 连接环境参数。
     *
     * @param url         LDAP 服务地址
     * @param principal   绑定账号或用户 DN
     * @param credentials 绑定密码
     * @return LDAP 环境参数
     */
    private Hashtable<String, String> ldapEnv(String url, String principal, String credentials) {
        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, url);
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, value(principal));
        env.put(Context.SECURITY_CREDENTIALS, value(credentials));
        return env;
    }

    /**
     * 将 LDAP 属性转换为统一登录用户资料。
     *
     * @param attributes LDAP 用户属性
     * @param config     LDAP 字段映射配置
     * @return 统一登录用户资料
     * @throws Exception 读取 LDAP 属性失败时抛出
     */
    private AuthLoginProfile ldapProfile(Attributes attributes, LdapAuthConfig config) throws Exception {
        AuthLoginProfile profile = new AuthLoginProfile();
        profile.setGivenName(attributeValue(attributes, config.getGivenNameKey()));
        profile.setEmail(attributeValue(attributes, config.getEmailKey()));
        profile.setMobile(attributeValue(attributes, config.getMobileKey()));
        profile.setPhone(attributeValue(attributes, config.getPhoneKey()));
        profile.setDepartment(attributeValue(attributes, config.getDepartmentKey()));
        profile.setPosition(attributeValue(attributes, config.getPositionKey()));
        profile.setLocation(attributeValue(attributes, config.getLocationKey()));
        profile.setIm(attributeValue(attributes, config.getImKey()));
        return profile;
    }

    /**
     * 根据 LDAP 字段映射配置生成查询时需要返回的属性名列表。
     *
     * @param config LDAP 字段映射配置
     * @return LDAP 返回属性名数组
     */
    private String[] ldapReturningAttributes(LdapAuthConfig config) {
        List<String> attributes = new ArrayList<>();
        addIfHasText(attributes, "dn");
        addIfHasText(attributes, config.getGivenNameKey());
        addIfHasText(attributes, config.getEmailKey());
        addIfHasText(attributes, config.getMobileKey());
        addIfHasText(attributes, config.getPhoneKey());
        addIfHasText(attributes, config.getDepartmentKey());
        addIfHasText(attributes, config.getPositionKey());
        addIfHasText(attributes, config.getLocationKey());
        addIfHasText(attributes, config.getImKey());
        return attributes.toArray(String[]::new);
    }

    /**
     * 当属性名非空且未重复时加入返回属性列表。
     *
     * @param attributes 属性名列表
     * @param attribute  待加入的属性名
     */
    private void addIfHasText(List<String> attributes, String attribute) {
        if (StringUtils.hasText(attribute) && !attributes.contains(attribute)) {
            attributes.add(attribute);
        }
    }

    /**
     * 读取 LDAP 属性值，字段名为空或属性不存在时返回空字符串。
     *
     * @param attributes LDAP 属性集合
     * @param name       属性名
     * @return 属性字符串值
     * @throws Exception 读取属性失败时抛出
     */
    private String attributeValue(Attributes attributes, String name) throws Exception {
        if (!StringUtils.hasText(name)) {
            return "";
        }
        Attribute attribute = attributes.get(name);
        Object value = attribute == null ? null : attribute.get();
        return value == null ? "" : value.toString();
    }

    /**
     * 将表单参数编码为 application/x-www-form-urlencoded 请求体。
     *
     * @param form 表单参数
     * @return 编码后的表单字符串
     */
    private String formEncode(Map<String, String> form) {
        return form.entrySet().stream()
                .map(entry -> urlEncode(entry.getKey()) + "=" + urlEncode(entry.getValue()))
                .collect(Collectors.joining("&"));
    }

    /**
     * 使用 UTF-8 对 URL 表单参数值进行编码。
     *
     * @param value 原始参数值
     * @return 编码后的参数值
     */
    private String urlEncode(String value) {
        return URLEncoder.encode(value(value), StandardCharsets.UTF_8);
    }

    /**
     * 将可空字符串转换为空字符串，避免认证请求和 LDAP 环境中出现 null。
     *
     * @param value 原始字符串
     * @return 非 null 字符串
     */
    private String value(String value) {
        return value == null ? "" : value;
    }

    /**
     * 转义 LDAP filter 值，避免用户名注入过滤表达式。
     */
    static String escapeLdapFilterValue(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '\\' -> builder.append("\\5c");
                case '*' -> builder.append("\\2a");
                case '(' -> builder.append("\\28");
                case ')' -> builder.append("\\29");
                case '\u0000' -> builder.append("\\00");
                default -> builder.append(ch);
            }
        }
        return builder.toString();
    }

    /**
     * HTTP 统一登录响应结构。
     */
    public static class HttpAuthResponse {
        private String message;
        private AuthLoginProfile data;

        /**
         * 获取认证接口返回的错误消息。
         *
         * @return 错误消息
         */
        public String getMessage() {
            return message;
        }

        /**
         * 设置认证接口返回的错误消息。
         *
         * @param message 错误消息
         */
        public void setMessage(String message) {
            this.message = message;
        }

        /**
         * 获取认证接口返回的用户资料。
         *
         * @return 用户资料
         */
        public AuthLoginProfile getData() {
            return data;
        }

        /**
         * 设置认证接口返回的用户资料。
         *
         * @param data 用户资料
         */
        public void setData(AuthLoginProfile data) {
            this.data = data;
        }
    }

    /**
     * 统一登录返回的用户资料。
     */
    public static class AuthLoginProfile {
        @JsonProperty("given_name")
        private String givenName;
        private String email;
        private String mobile;
        private String phone;
        private String department;
        private String position;
        private String location;
        private String im;

        /**
         * 获取用户姓名。
         *
         * @return 用户姓名
         */
        public String getGivenName() {
            return givenName;
        }

        /**
         * 设置用户姓名。
         *
         * @param givenName 用户姓名
         */
        public void setGivenName(String givenName) {
            this.givenName = givenName;
        }

        /**
         * 获取用户邮箱。
         *
         * @return 用户邮箱
         */
        public String getEmail() {
            return email;
        }

        /**
         * 设置用户邮箱。
         *
         * @param email 用户邮箱
         */
        public void setEmail(String email) {
            this.email = email;
        }

        /**
         * 获取用户手机号。
         *
         * @return 用户手机号
         */
        public String getMobile() {
            return mobile;
        }

        /**
         * 设置用户手机号。
         *
         * @param mobile 用户手机号
         */
        public void setMobile(String mobile) {
            this.mobile = mobile;
        }

        /**
         * 获取用户电话。
         *
         * @return 用户电话
         */
        public String getPhone() {
            return phone;
        }

        /**
         * 设置用户电话。
         *
         * @param phone 用户电话
         */
        public void setPhone(String phone) {
            this.phone = phone;
        }

        /**
         * 获取用户部门。
         *
         * @return 用户部门
         */
        public String getDepartment() {
            return department;
        }

        /**
         * 设置用户部门。
         *
         * @param department 用户部门
         */
        public void setDepartment(String department) {
            this.department = department;
        }

        /**
         * 获取用户职位。
         *
         * @return 用户职位
         */
        public String getPosition() {
            return position;
        }

        /**
         * 设置用户职位。
         *
         * @param position 用户职位
         */
        public void setPosition(String position) {
            this.position = position;
        }

        /**
         * 获取用户所在地。
         *
         * @return 用户所在地
         */
        public String getLocation() {
            return location;
        }

        /**
         * 设置用户所在地。
         *
         * @param location 用户所在地
         */
        public void setLocation(String location) {
            this.location = location;
        }

        /**
         * 获取用户即时通讯账号。
         *
         * @return 即时通讯账号
         */
        public String getIm() {
            return im;
        }

        /**
         * 设置用户即时通讯账号。
         *
         * @param im 即时通讯账号
         */
        public void setIm(String im) {
            this.im = im;
        }
    }

    /**
     * LDAP 统一登录扩展配置。
     */
    public static class LdapAuthConfig {
        private String basedn;
        @JsonProperty("bind_username")
        private String bindUsername;
        @JsonProperty("bind_password")
        private String bindPassword;
        @JsonProperty("account_pattern")
        private String accountPattern;
        @JsonProperty("given_name_key")
        private String givenNameKey;
        @JsonProperty("email_key")
        private String emailKey;
        @JsonProperty("mobile_key")
        private String mobileKey;
        @JsonProperty("phone_key")
        private String phoneKey;
        @JsonProperty("department_key")
        private String departmentKey;
        @JsonProperty("position_key")
        private String positionKey;
        @JsonProperty("location_key")
        private String locationKey;
        @JsonProperty("im_key")
        private String imKey;

        /**
         * 填充 LDAP 默认账号过滤表达式和姓名字段名。
         */
        public void applyDefaults() {
            if (!StringUtils.hasText(accountPattern)) {
                accountPattern = LDAP_DEFAULT_ACCOUNT_PATTERN;
            }
            if (!StringUtils.hasText(givenNameKey)) {
                givenNameKey = LDAP_DEFAULT_GIVEN_NAME_KEY;
            }
        }

        /**
         * 获取 LDAP 查询基础 DN。
         *
         * @return LDAP 基础 DN
         */
        public String getBasedn() {
            return basedn;
        }

        /**
         * 设置 LDAP 查询基础 DN。
         *
         * @param basedn LDAP 基础 DN
         */
        public void setBasedn(String basedn) {
            this.basedn = basedn;
        }

        /**
         * 获取 LDAP 绑定账号。
         *
         * @return 绑定账号
         */
        public String getBindUsername() {
            return bindUsername;
        }

        /**
         * 设置 LDAP 绑定账号。
         *
         * @param bindUsername 绑定账号
         */
        public void setBindUsername(String bindUsername) {
            this.bindUsername = bindUsername;
        }

        /**
         * 获取 LDAP 绑定密码。
         *
         * @return 绑定密码
         */
        public String getBindPassword() {
            return bindPassword;
        }

        /**
         * 设置 LDAP 绑定密码。
         *
         * @param bindPassword 绑定密码
         */
        public void setBindPassword(String bindPassword) {
            this.bindPassword = bindPassword;
        }

        /**
         * 获取 LDAP 账号查询过滤表达式。
         *
         * @return 账号过滤表达式
         */
        public String getAccountPattern() {
            return accountPattern;
        }

        /**
         * 设置 LDAP 账号查询过滤表达式。
         *
         * @param accountPattern 账号过滤表达式
         */
        public void setAccountPattern(String accountPattern) {
            this.accountPattern = accountPattern;
        }

        /**
         * 获取 LDAP 姓名字段名。
         *
         * @return 姓名字段名
         */
        public String getGivenNameKey() {
            return givenNameKey;
        }

        /**
         * 设置 LDAP 姓名字段名。
         *
         * @param givenNameKey 姓名字段名
         */
        public void setGivenNameKey(String givenNameKey) {
            this.givenNameKey = givenNameKey;
        }

        /**
         * 获取 LDAP 邮箱字段名。
         *
         * @return 邮箱字段名
         */
        public String getEmailKey() {
            return emailKey;
        }

        /**
         * 设置 LDAP 邮箱字段名。
         *
         * @param emailKey 邮箱字段名
         */
        public void setEmailKey(String emailKey) {
            this.emailKey = emailKey;
        }

        /**
         * 获取 LDAP 手机号字段名。
         *
         * @return 手机号字段名
         */
        public String getMobileKey() {
            return mobileKey;
        }

        /**
         * 设置 LDAP 手机号字段名。
         *
         * @param mobileKey 手机号字段名
         */
        public void setMobileKey(String mobileKey) {
            this.mobileKey = mobileKey;
        }

        /**
         * 获取 LDAP 电话字段名。
         *
         * @return 电话字段名
         */
        public String getPhoneKey() {
            return phoneKey;
        }

        /**
         * 设置 LDAP 电话字段名。
         *
         * @param phoneKey 电话字段名
         */
        public void setPhoneKey(String phoneKey) {
            this.phoneKey = phoneKey;
        }

        /**
         * 获取 LDAP 部门字段名。
         *
         * @return 部门字段名
         */
        public String getDepartmentKey() {
            return departmentKey;
        }

        /**
         * 设置 LDAP 部门字段名。
         *
         * @param departmentKey 部门字段名
         */
        public void setDepartmentKey(String departmentKey) {
            this.departmentKey = departmentKey;
        }

        /**
         * 获取 LDAP 职位字段名。
         *
         * @return 职位字段名
         */
        public String getPositionKey() {
            return positionKey;
        }

        /**
         * 设置 LDAP 职位字段名。
         *
         * @param positionKey 职位字段名
         */
        public void setPositionKey(String positionKey) {
            this.positionKey = positionKey;
        }

        /**
         * 获取 LDAP 所在地字段名。
         *
         * @return 所在地字段名
         */
        public String getLocationKey() {
            return locationKey;
        }

        /**
         * 设置 LDAP 所在地字段名。
         *
         * @param locationKey 所在地字段名
         */
        public void setLocationKey(String locationKey) {
            this.locationKey = locationKey;
        }

        /**
         * 获取 LDAP 即时通讯字段名。
         *
         * @return 即时通讯字段名
         */
        public String getImKey() {
            return imKey;
        }

        /**
         * 设置 LDAP 即时通讯字段名。
         *
         * @param imKey 即时通讯字段名
         */
        public void setImKey(String imKey) {
            this.imKey = imKey;
        }
    }
}
