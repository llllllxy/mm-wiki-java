package org.tinycloud.mmwiki.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.tinycloud.mmwiki.domain.SpaceUser;

/**
 * 空间成员关系表数据访问接口，负责成员查询、权限维护和关系删除。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Mapper
public interface SpaceUserMapper {

    List<SpaceUser> findByUserId(@Param("userId") Integer userId);

    SpaceUser findBySpaceIdAndUserId(@Param("spaceId") Integer spaceId, @Param("userId") Integer userId);

    List<SpaceUser> pageBySpaceId(@Param("spaceId") Integer spaceId);

    List<SpaceUser> findBySpaceId(@Param("spaceId") Integer spaceId);

    int insert(SpaceUser spaceUser);

    int updatePrivilege(SpaceUser spaceUser);

    int deleteById(@Param("spaceUserId") Integer spaceUserId);

    int deleteBySpaceId(@Param("spaceId") Integer spaceId);
}
