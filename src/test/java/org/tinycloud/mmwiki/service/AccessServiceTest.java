package org.tinycloud.mmwiki.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tinycloud.mmwiki.constant.GlobalConstant;
import org.tinycloud.mmwiki.constant.SpaceMemberPrivilegeEnum;
import org.tinycloud.mmwiki.constant.SpaceVisitLevelEnum;
import org.tinycloud.mmwiki.domain.Space;
import org.tinycloud.mmwiki.domain.SpaceUser;
import org.tinycloud.mmwiki.vo.Access;
import org.tinycloud.mmwiki.web.CurrentUser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccessServiceTest {

    @Mock
    private SpaceUserService spaceUserService;

    @InjectMocks
    private AccessService accessService;

    @Test
    void rootUserCanVisitEditAndManageAnySpace() {
        Access access = accessService.access(user(1, GlobalConstant.ROOT_ROLE_ID), privateSpace());

        assertThat(access.isVisit()).isTrue();
        assertThat(access.isEditor()).isTrue();
        assertThat(access.isManager()).isTrue();
    }

    @Test
    void publicSpaceAllowsNonMemberToVisitOnly() {
        Space space = space(10, SpaceVisitLevelEnum.PUBLIC.getCode());
        CurrentUser currentUser = user(8, GlobalConstant.DEFAULT_ROLE_ID);
        when(spaceUserService.findBySpaceIdAndUserId(10, 8)).thenReturn(null);

        Access access = accessService.access(currentUser, space);

        assertThat(access.isVisit()).isTrue();
        assertThat(access.isEditor()).isFalse();
        assertThat(access.isManager()).isFalse();
    }

    @Test
    void privateSpaceRejectsNonMember() {
        Space space = privateSpace();
        CurrentUser currentUser = user(8, GlobalConstant.DEFAULT_ROLE_ID);
        when(spaceUserService.findBySpaceIdAndUserId(10, 8)).thenReturn(null);

        Access access = accessService.access(currentUser, space);

        assertThat(access.isVisit()).isFalse();
        assertThat(access.isEditor()).isFalse();
        assertThat(access.isManager()).isFalse();
    }

    @Test
    void editorMemberCanVisitAndEditButCannotManage() {
        Space space = privateSpace();
        CurrentUser currentUser = user(8, GlobalConstant.DEFAULT_ROLE_ID);
        when(spaceUserService.findBySpaceIdAndUserId(10, 8)).thenReturn(member(SpaceMemberPrivilegeEnum.EDITOR.getCode()));

        Access access = accessService.access(currentUser, space);

        assertThat(access.isVisit()).isTrue();
        assertThat(access.isEditor()).isTrue();
        assertThat(access.isManager()).isFalse();
    }

    @Test
    void managerMemberCanVisitEditAndManage() {
        Space space = privateSpace();
        CurrentUser currentUser = user(8, GlobalConstant.DEFAULT_ROLE_ID);
        when(spaceUserService.findBySpaceIdAndUserId(10, 8)).thenReturn(member(SpaceMemberPrivilegeEnum.MANAGER.getCode()));

        Access access = accessService.access(currentUser, space);

        assertThat(access.isVisit()).isTrue();
        assertThat(access.isEditor()).isTrue();
        assertThat(access.isManager()).isTrue();
    }

    private static Space privateSpace() {
        return space(10, SpaceVisitLevelEnum.PRIVATE.getCode());
    }

    private static Space space(Integer spaceId, String visitLevel) {
        Space space = new Space();
        space.setSpaceId(spaceId);
        space.setVisitLevel(visitLevel);
        return space;
    }

    private static SpaceUser member(Integer privilege) {
        SpaceUser spaceUser = new SpaceUser();
        spaceUser.setPrivilege(privilege);
        return spaceUser;
    }

    private static CurrentUser user(Integer userId, Integer roleId) {
        CurrentUser currentUser = new CurrentUser();
        currentUser.setUserId(userId);
        currentUser.setRoleId(roleId);
        return currentUser;
    }
}
