package org.tinycloud.mmwiki.service;

import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.tinycloud.mmwiki.domain.SpaceUser;
import org.tinycloud.mmwiki.mapper.SpaceUserMapper;
import org.tinycloud.mmwiki.util.TimeUtils;

/**
 * MM-Wiki 业务服务实现。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Service
public class SpaceUserService {

    @Autowired
    private SpaceUserMapper spaceUserMapper;

    public List<SpaceUser> findByUserId(Integer userId) {
        return spaceUserMapper.findByUserId(userId);
    }

    public SpaceUser findBySpaceIdAndUserId(Integer spaceId, Integer userId) {
        return spaceUserMapper.findBySpaceIdAndUserId(spaceId, userId);
    }

    public List<SpaceUser> pageBySpaceId(Integer spaceId) {
        return spaceUserMapper.pageBySpaceId(spaceId);
    }

    public List<SpaceUser> findBySpaceId(Integer spaceId) {
        return spaceUserMapper.findBySpaceId(spaceId);
    }

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

    public void updatePrivilege(Integer spaceUserId, Integer privilege) {
        SpaceUser spaceUser = new SpaceUser();
        spaceUser.setSpaceUserId(spaceUserId);
        spaceUser.setPrivilege(privilege);
        spaceUser.setUpdateTime(TimeUtils.now());
        spaceUserMapper.updatePrivilege(spaceUser);
    }

    public void deleteById(Integer spaceUserId) {
        spaceUserMapper.deleteById(spaceUserId);
    }

    public void deleteBySpaceId(Integer spaceId) {
        spaceUserMapper.deleteBySpaceId(spaceId);
    }
}

