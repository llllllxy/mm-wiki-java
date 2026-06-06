package org.tinycloud.mmwiki.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.tinycloud.mmwiki.domain.Role;

/**
 * 角色表数据访问接口，负责角色查询、分页、维护和删除标记。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Mapper
public interface RoleMapper {

    List<Role> findAllActive();

    Role findActiveById(@Param("roleId") Integer roleId);

    List<Role> pageAll();

    List<Role> pageByKeyword(@Param("keyword") String keyword);

    long countByName(@Param("name") String name);

    long countByNameAndNotId(@Param("roleId") Integer roleId, @Param("name") String name);

    int insert(Role role);

    int update(Role role);

    int markDeleted(@Param("roleId") Integer roleId);
}
