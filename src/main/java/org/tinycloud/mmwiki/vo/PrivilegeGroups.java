package org.tinycloud.mmwiki.vo;

import java.util.List;

import org.tinycloud.mmwiki.domain.Privilege;

/**
 * PrivilegeGroups view object.
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
public class PrivilegeGroups {

    /**
     * menus.
     */
    private List<Privilege> menus;

    /**
     * controllers.
     */
    private List<Privilege> controllers;

    public PrivilegeGroups() {
    }

    public PrivilegeGroups(
            List<Privilege> menus,
            List<Privilege> controllers
    ) {
        this.menus = menus;
        this.controllers = controllers;
    }

    public List<Privilege> getMenus() {
        return menus;
    }

    public void setMenus(List<Privilege> menus) {
        this.menus = menus;
    }

    public List<Privilege> getControllers() {
        return controllers;
    }

    public void setControllers(List<Privilege> controllers) {
        this.controllers = controllers;
    }

}
