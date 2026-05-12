package org.tinycloud.mmwiki.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.tinycloud.mmwiki.domain.Role;

/**
 * MM-Wiki MyBatis 数据访问接口。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Mapper
public interface RoleMapper {

    @Select("""
        select role_id, name, type, is_delete, create_time, update_time
        from mw_role
        where is_delete = 0
        order by role_id asc
        """)
    List<Role> findAllActive();

    @Select("""
        select role_id, name, type, is_delete, create_time, update_time
        from mw_role
        where role_id = #{roleId}
          and is_delete = 0
        limit 1
        """)
    Role findActiveById(@Param("roleId") Integer roleId);

    @Select("""
        select role_id, name, type, is_delete, create_time, update_time
        from mw_role
        where is_delete = 0
        order by role_id desc
        """)
    List<Role> pageAll();

    @Select("""
        select role_id, name, type, is_delete, create_time, update_time
        from mw_role
        where is_delete = 0
          and name like concat('%', #{keyword}, '%')
        order by role_id desc
        """)
    List<Role> pageByKeyword(@Param("keyword") String keyword);

    @Select("""
        select count(*)
        from mw_role
        where is_delete = 0
          and name = #{name}
        """)
    long countByName(@Param("name") String name);

    @Select("""
        select count(*)
        from mw_role
        where is_delete = 0
          and name = #{name}
          and role_id <> #{roleId}
        """)
    long countByNameAndNotId(@Param("roleId") Integer roleId, @Param("name") String name);

    @Insert("""
        insert into mw_role(name, type, is_delete, create_time, update_time)
        values(#{name}, #{type}, 0, #{createTime}, #{updateTime})
        """)
    @Options(useGeneratedKeys = true, keyProperty = "roleId")
    int insert(Role role);

    @Update("""
        update mw_role
        set name = #{name},
            update_time = #{updateTime}
        where role_id = #{roleId}
          and is_delete = 0
        """)
    int update(Role role);

    @Update("""
        update mw_role
        set is_delete = 1,
                update_time = now()
        where role_id = #{roleId}
          and is_delete = 0
        """)
    int markDeleted(@Param("roleId") Integer roleId);
}

