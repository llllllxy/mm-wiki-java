package org.tinycloud.mmwiki.mapper;

import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * MM-Wiki MyBatis 数据访问接口。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Mapper
public interface RolePrivilegeMapper {

    List<Integer> findPrivilegeIdsByRoleId(@Param("roleId") Integer roleId);

    int deleteByRoleId(@Param("roleId") Integer roleId);

    int deleteByPrivilegeId(@Param("privilegeId") Integer privilegeId);

    int insertBatch(
        @Param("roleId") Integer roleId,
        @Param("privilegeIds") List<Integer> privilegeIds,
        @Param("createTime") LocalDateTime createTime
    );
}
