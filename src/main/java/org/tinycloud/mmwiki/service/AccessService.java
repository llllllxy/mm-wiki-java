package org.tinycloud.mmwiki.service;

import org.springframework.stereotype.Service;
import org.tinycloud.mmwiki.domain.Space;
import org.tinycloud.mmwiki.domain.SpaceUser;
import org.tinycloud.mmwiki.web.CurrentUser;

@Service
public class AccessService {

    public static final int ROLE_ROOT_ID = 1;
    public static final int SPACE_EDITOR = 1;
    public static final int SPACE_MANAGER = 2;

    private final SpaceUserService spaceUserService;

    public AccessService(SpaceUserService spaceUserService) {
        this.spaceUserService = spaceUserService;
    }

    public Access access(CurrentUser currentUser, Space space) {
        if (currentUser == null || space == null) {
            return new Access(false, false, false);
        }
        if (currentUser.getRoleId() != null && currentUser.getRoleId() == ROLE_ROOT_ID) {
            return new Access(true, true, true);
        }

        SpaceUser membership = spaceUserService.findBySpaceIdAndUserId(space.getSpaceId(), currentUser.getUserId());
        if (membership == null) {
            if ("private".equalsIgnoreCase(space.getVisitLevel())) {
                return new Access(false, false, false);
            }
            return new Access(true, false, false);
        }

        if (membership.getPrivilege() != null && membership.getPrivilege() == SPACE_MANAGER) {
            return new Access(true, true, true);
        }
        if (membership.getPrivilege() != null && membership.getPrivilege() == SPACE_EDITOR) {
            return new Access(true, true, false);
        }
        return new Access(true, false, false);
    }

    public record Access(boolean visit, boolean editor, boolean manager) {
    }
}
