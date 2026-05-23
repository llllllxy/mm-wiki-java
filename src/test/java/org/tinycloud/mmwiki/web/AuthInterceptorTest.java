package org.tinycloud.mmwiki.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.tinycloud.mmwiki.constant.GlobalConstant;
import org.tinycloud.mmwiki.domain.User;
import org.tinycloud.mmwiki.mapper.PrivilegeMapper;
import org.tinycloud.mmwiki.mapper.RolePrivilegeMapper;
import org.tinycloud.mmwiki.service.LogService;
import org.tinycloud.mmwiki.service.UserService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthInterceptorTest {

    @Mock
    private UserService userService;
    @Mock
    private PrivilegeMapper privilegeMapper;
    @Mock
    private RolePrivilegeMapper rolePrivilegeMapper;
    @Mock
    private LogService logService;

    private AuthInterceptor authInterceptor;

    @BeforeEach
    void setUp() {
        authInterceptor = new AuthInterceptor();
        ReflectionTestUtils.setField(authInterceptor, "userService", userService);
        ReflectionTestUtils.setField(authInterceptor, "privilegeMapper", privilegeMapper);
        ReflectionTestUtils.setField(authInterceptor, "rolePrivilegeMapper", rolePrivilegeMapper);
        ReflectionTestUtils.setField(authInterceptor, "logService", logService);
    }

    @Test
    void freshSessionUserDoesNotQueryDatabaseForStatus() throws Exception {
        CurrentUser currentUser = currentUser(5, GlobalConstant.DEFAULT_ROLE_ID, System.currentTimeMillis());
        MockHttpServletRequest request = requestWithUser("/main/index", currentUser);
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = authInterceptor.preHandle(request, response, new Object());

        assertThat(allowed).isTrue();
        assertThat(request.getSession(false).getAttribute(AuthInterceptor.SESSION_AUTHOR)).isSameAs(currentUser);
        verify(userService, never()).findActiveById(5);
    }

    @Test
    void staleSessionUserRefreshesStatusAndRewritesSessionUser() throws Exception {
        CurrentUser currentUser = currentUser(5, GlobalConstant.DEFAULT_ROLE_ID, 0L);
        MockHttpServletRequest request = requestWithUser("/main/index", currentUser);
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(userService.findActiveById(5)).thenReturn(activeUser(5, "new-name", 2, 0));

        boolean allowed = authInterceptor.preHandle(request, response, new Object());

        assertThat(allowed).isTrue();
        CurrentUser refreshed = (CurrentUser) request.getSession(false).getAttribute(AuthInterceptor.SESSION_AUTHOR);
        assertThat(refreshed).isNotSameAs(currentUser);
        assertThat(refreshed.getUsername()).isEqualTo("new-name");
        assertThat(refreshed.getRoleId()).isEqualTo(2);
        assertThat(refreshed.getStatusRefreshTime()).isGreaterThan(0L);
    }

    @Test
    void forbiddenUserInvalidatesSessionAndRedirectsToLogin() throws Exception {
        CurrentUser currentUser = currentUser(5, GlobalConstant.DEFAULT_ROLE_ID, 0L);
        MockHttpServletRequest request = requestWithUser("/main/index", currentUser);
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(userService.findActiveById(5)).thenReturn(activeUser(5, "blocked", 2, 1));

        boolean allowed = authInterceptor.preHandle(request, response, new Object());

        assertThat(allowed).isFalse();
        assertThat(response.getRedirectedUrl()).isEqualTo("/author/index");
        assertThat(request.getSession(false)).isNull();
    }

    private static MockHttpServletRequest requestWithUser(String uri, CurrentUser currentUser) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
        request.getSession(true).setAttribute(AuthInterceptor.SESSION_AUTHOR, currentUser);
        return request;
    }

    private static CurrentUser currentUser(Integer userId, Integer roleId, long statusRefreshTime) {
        CurrentUser currentUser = new CurrentUser();
        currentUser.setUserId(userId);
        currentUser.setUsername("user-" + userId);
        currentUser.setRoleId(roleId);
        currentUser.setStatusRefreshTime(statusRefreshTime);
        return currentUser;
    }

    private static User activeUser(Integer userId, String username, Integer roleId, Integer isForbidden) {
        User user = new User();
        user.setUserId(userId);
        user.setUsername(username);
        user.setGivenName(username);
        user.setRoleId(roleId);
        user.setIsForbidden(isForbidden);
        return user;
    }
}
