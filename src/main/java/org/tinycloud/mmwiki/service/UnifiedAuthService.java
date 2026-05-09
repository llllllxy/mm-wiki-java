package org.tinycloud.mmwiki.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.tinycloud.mmwiki.domain.LoginAuth;

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

    @Autowired
    private ObjectMapper objectMapper;

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
            throw new IllegalStateException("登录认证失败, httpCode=" + response.statusCode());
        }

        HttpAuthResponse authResponse = objectMapper.readValue(response.body(), HttpAuthResponse.class);
        if (StringUtils.hasText(authResponse.getMessage())) {
            throw new IllegalStateException("登录认证失败, message=" + authResponse.getMessage());
        }
        if (authResponse.getData() == null) {
            throw new IllegalStateException("登录认证失败");
        }
        return authResponse.getData();
    }

    private AuthLoginProfile authenticateByLdap(LoginAuth loginAuth, String username, String password) throws Exception {
        if (!StringUtils.hasText(loginAuth.getExtData())) {
            throw new IllegalArgumentException("LDAP 配置数据错误");
        }
        LdapAuthConfig config = objectMapper.readValue(loginAuth.getExtData(), LdapAuthConfig.class);
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
                    String.format(config.getAccountPattern(), username),
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

    private void verifyLdapUserPassword(String url, String userDn, String password) throws Exception {
        InitialDirContext userContext = new InitialDirContext(ldapEnv(url, userDn, password));
        userContext.close();
    }

    private Hashtable<String, String> ldapEnv(String url, String principal, String credentials) {
        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, url);
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, value(principal));
        env.put(Context.SECURITY_CREDENTIALS, value(credentials));
        return env;
    }

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

    private void addIfHasText(List<String> attributes, String attribute) {
        if (StringUtils.hasText(attribute) && !attributes.contains(attribute)) {
            attributes.add(attribute);
        }
    }

    private String attributeValue(Attributes attributes, String name) throws Exception {
        if (!StringUtils.hasText(name)) {
            return "";
        }
        Attribute attribute = attributes.get(name);
        Object value = attribute == null ? null : attribute.get();
        return value == null ? "" : value.toString();
    }

    private String formEncode(Map<String, String> form) {
        return form.entrySet().stream()
                .map(entry -> urlEncode(entry.getKey()) + "=" + urlEncode(entry.getValue()))
                .collect(Collectors.joining("&"));
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value(value), StandardCharsets.UTF_8);
    }

    private String value(String value) {
        return value == null ? "" : value;
    }

    /**
     * HTTP 统一登录响应结构。
     */
    public static class HttpAuthResponse {
        private String message;
        private AuthLoginProfile data;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public AuthLoginProfile getData() {
            return data;
        }

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

        public String getGivenName() {
            return givenName;
        }

        public void setGivenName(String givenName) {
            this.givenName = givenName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getMobile() {
            return mobile;
        }

        public void setMobile(String mobile) {
            this.mobile = mobile;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getDepartment() {
            return department;
        }

        public void setDepartment(String department) {
            this.department = department;
        }

        public String getPosition() {
            return position;
        }

        public void setPosition(String position) {
            this.position = position;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public String getIm() {
            return im;
        }

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

        public void applyDefaults() {
            if (!StringUtils.hasText(accountPattern)) {
                accountPattern = LDAP_DEFAULT_ACCOUNT_PATTERN;
            }
            if (!StringUtils.hasText(givenNameKey)) {
                givenNameKey = LDAP_DEFAULT_GIVEN_NAME_KEY;
            }
        }

        public String getBasedn() {
            return basedn;
        }

        public void setBasedn(String basedn) {
            this.basedn = basedn;
        }

        public String getBindUsername() {
            return bindUsername;
        }

        public void setBindUsername(String bindUsername) {
            this.bindUsername = bindUsername;
        }

        public String getBindPassword() {
            return bindPassword;
        }

        public void setBindPassword(String bindPassword) {
            this.bindPassword = bindPassword;
        }

        public String getAccountPattern() {
            return accountPattern;
        }

        public void setAccountPattern(String accountPattern) {
            this.accountPattern = accountPattern;
        }

        public String getGivenNameKey() {
            return givenNameKey;
        }

        public void setGivenNameKey(String givenNameKey) {
            this.givenNameKey = givenNameKey;
        }

        public String getEmailKey() {
            return emailKey;
        }

        public void setEmailKey(String emailKey) {
            this.emailKey = emailKey;
        }

        public String getMobileKey() {
            return mobileKey;
        }

        public void setMobileKey(String mobileKey) {
            this.mobileKey = mobileKey;
        }

        public String getPhoneKey() {
            return phoneKey;
        }

        public void setPhoneKey(String phoneKey) {
            this.phoneKey = phoneKey;
        }

        public String getDepartmentKey() {
            return departmentKey;
        }

        public void setDepartmentKey(String departmentKey) {
            this.departmentKey = departmentKey;
        }

        public String getPositionKey() {
            return positionKey;
        }

        public void setPositionKey(String positionKey) {
            this.positionKey = positionKey;
        }

        public String getLocationKey() {
            return locationKey;
        }

        public void setLocationKey(String locationKey) {
            this.locationKey = locationKey;
        }

        public String getImKey() {
            return imKey;
        }

        public void setImKey(String imKey) {
            this.imKey = imKey;
        }
    }
}
