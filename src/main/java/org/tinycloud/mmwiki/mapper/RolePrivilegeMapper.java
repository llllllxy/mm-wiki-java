package org.tinycloud.mmwiki.mapper;

import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * MM-Wiki MyBatis 数据访问接口。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Mapper
public interface RolePrivilegeMapper {

    @Select("""
        select privilege_id
        from mw_role_privilege
        where role_id = #{roleId}
        order by privilege_id asc
        """)
    List<Integer> findPrivilegeIdsByRoleId(@Param("roleId") Integer roleId);

    @Delete("""
        delete from mw_role_privilege
        where role_id = #{roleId}
        """)
    int deleteByRoleId(@Param("roleId") Integer roleId);

    @Delete("""
        delete from mw_role_privilege
        where privilege_id = #{privilegeId}
        """)
    int deleteByPrivilegeId(@Param("privilegeId") Integer privilegeId);

    @Insert({
        "<script>",
        "insert into mw_role_privilege(role_id, privilege_id, create_time) values",
        "<foreach collection='privilegeIds' item='privilegeId' separator=','>",
        "(#{roleId}, #{privilegeId}, #{createTime})",
        "</foreach>",
        "</script>"
    })
    int insertBatch(
        @Param("roleId") Integer roleId,
        @Param("privilegeIds") List<Integer> privilegeIds,
        @Param("createTime") LocalDateTime createTime
    );
}
