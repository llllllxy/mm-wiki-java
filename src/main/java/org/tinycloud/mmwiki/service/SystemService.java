package org.tinycloud.mmwiki.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tinycloud.mmwiki.domain.Privilege;
import org.tinycloud.mmwiki.mapper.PrivilegeMapper;
import org.tinycloud.mmwiki.mapper.RolePrivilegeMapper;
import org.tinycloud.mmwiki.web.CurrentUser;

import java.util.*;
import java.util.stream.Collectors;

import static org.tinycloud.mmwiki.constant.GlobalConstant.ROOT_ROLE_ID;

/**
 * MM-Wiki 业务服务实现。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Service
public class SystemService {

    private static final Set<String> IMPLEMENTED_DISPLAY_ROUTES = Set.of(
        "profile/info",
        "profile/activity",
        "profile/password",
        "user/add",
        "user/list",
        "role/add",
        "role/list",
        "privilege/add",
        "privilege/list",
        "space/add",
        "space/list",
        "log/system",
        "log/document",
        "config/global",
        "email/list",
        "auth/list",
        "link/list",
        "contact/list",
        "static/default",
        "static/monitor",
        "plugin/list"
    );

    @Autowired
    private PrivilegeMapper privilegeMapper;
    @Autowired
    private RolePrivilegeMapper rolePrivilegeMapper;

    public List<MenuGroup> loadMenuGroups(CurrentUser currentUser) {
        List<Privilege> displayed = privilegeMapper.findDisplayed();
        boolean unrestricted = currentUser != null && currentUser.getRoleId() != null && currentUser.getRoleId() == ROOT_ROLE_ID;
        Set<Integer> allowedIds = unrestricted ? Set.of() : allowedPrivilegeIds(currentUser);
        List<Privilege> available = displayed.stream()
            .filter(item -> unrestricted || allowedIds.contains(item.getPrivilegeId()))
            .filter(this::isMenuOrImplementedController)
            .toList();

        Map<Integer, List<Privilege>> children = available.stream()
            .filter(item -> "controller".equalsIgnoreCase(item.getType()))
            .collect(Collectors.groupingBy(Privilege::getParentId, LinkedHashMap::new, Collectors.toList()));

        List<MenuGroup> groups = new ArrayList<>();
        for (Privilege menu : available) {
            if (!"menu".equalsIgnoreCase(menu.getType())) {
                continue;
            }
            List<Privilege> items = children.getOrDefault(menu.getPrivilegeId(), List.of());
            if (!items.isEmpty()) {
                groups.add(new MenuGroup(menu, items));
            }
        }
        return groups;
    }

    private Set<Integer> allowedPrivilegeIds(CurrentUser currentUser) {
        if (currentUser == null || currentUser.getRoleId() == null) {
            return Set.of();
        }
        return new LinkedHashSet<>(rolePrivilegeMapper.findPrivilegeIdsByRoleId(currentUser.getRoleId()));
    }

    private boolean isMenuOrImplementedController(Privilege privilege) {
        if ("menu".equalsIgnoreCase(privilege.getType())) {
            return true;
        }
        return IMPLEMENTED_DISPLAY_ROUTES.contains(routeKey(privilege));
    }

    private String routeKey(Privilege privilege) {
        return (privilege.getController() == null ? "" : privilege.getController())
            + "/"
            + (privilege.getAction() == null ? "" : privilege.getAction());
    }

    public static class MenuGroup {
        private final Privilege menu;
        private final List<Privilege> items;

        public MenuGroup(Privilege menu, List<Privilege> items) {
            this.menu = menu;
            this.items = items;
        }

        public Privilege getMenu() {
            return menu;
        }

        public List<Privilege> getItems() {
            return items;
        }
    }
}
