package org.tinycloud.mmwiki.service;

import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.tinycloud.mmwiki.domain.SpaceUser;
import org.tinycloud.mmwiki.mapper.SpaceUserMapper;
import org.tinycloud.mmwiki.util.TimeUtils;

/**
 * 空间成员服务，负责空间用户关系查询、成员添加、权限调整和移除。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Service
public class SpaceUserService {

    @Autowired
    private SpaceUserMapper spaceUserMapper;

    /**
     * 查询指定用户加入的空间成员关系。
     *
     * @param userId 用户ID
     * @return 用户关联的空间成员列表
     */
    public List<SpaceUser> findByUserId(Integer userId) {
        return spaceUserMapper.findByUserId(userId);
    }

    /**
     * 查询用户在指定空间中的成员关系。
     *
     * @param spaceId 空间ID
     * @param userId  用户ID
     * @return 空间成员记录，不存在时返回 null
     */
    public SpaceUser findBySpaceIdAndUserId(Integer spaceId, Integer userId) {
        return spaceUserMapper.findBySpaceIdAndUserId(spaceId, userId);
    }

    /**
     * 分页查询指定空间的成员记录，分页上下文由调用方创建。
     *
     * @param spaceId 空间ID
     * @return 当前分页范围内的空间成员列表
     */
    public List<SpaceUser> pageBySpaceId(Integer spaceId) {
        return spaceUserMapper.pageBySpaceId(spaceId);
    }

    /**
     * 查询指定空间的全部成员记录。
     *
     * @param spaceId 空间ID
     * @return 空间成员列表
     */
    public List<SpaceUser> findBySpaceId(Integer spaceId) {
        return spaceUserMapper.findBySpaceId(spaceId);
    }

    /**
     * 新增空间成员并设置其空间权限等级。
     *
     * @param spaceId   空间ID
     * @param userId    用户ID
     * @param privilege 空间权限等级
     */
    public void add(Integer spaceId, Integer userId, Integer privilege) {
        LocalDateTime now = LocalDateTime.now();
        SpaceUser spaceUser = new SpaceUser();
        spaceUser.setSpaceId(spaceId);
        spaceUser.setUserId(userId);
        spaceUser.setPrivilege(privilege);
        spaceUser.setCreateTime(now);
        spaceUser.setUpdateTime(now);
        spaceUserMapper.insert(spaceUser);
    }

    /**
     * 更新空间成员的权限等级。
     *
     * @param spaceUserId 空间成员关系ID
     * @param privilege   新权限等级
     */
    public void updatePrivilege(Integer spaceUserId, Integer privilege) {
        SpaceUser spaceUser = new SpaceUser();
        spaceUser.setSpaceUserId(spaceUserId);
        spaceUser.setPrivilege(privilege);
        spaceUser.setUpdateTime(TimeUtils.now());
        spaceUserMapper.updatePrivilege(spaceUser);
    }

    /**
     * 根据空间成员关系ID删除成员。
     *
     * @param spaceUserId 空间成员关系ID
     */
    public void deleteById(Integer spaceUserId) {
        spaceUserMapper.deleteById(spaceUserId);
    }

    /**
     * 删除指定空间下的全部成员关系，通常在空间删除时调用。
     *
     * @param spaceId 空间ID
     */
    public void deleteBySpaceId(Integer spaceId) {
        spaceUserMapper.deleteBySpaceId(spaceId);
    }
}
