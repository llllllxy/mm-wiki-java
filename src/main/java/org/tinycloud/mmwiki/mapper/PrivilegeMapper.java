package org.tinycloud.mmwiki.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.tinycloud.mmwiki.domain.Privilege;

/**
 * MM-Wiki MyBatis 数据访问接口。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Mapper
public interface PrivilegeMapper {

    List<Privilege> findDisplayed();

    List<Privilege> findAllOrderBySequence();

    Privilege findById(@Param("privilegeId") Integer privilegeId);

    Privilege findControllerPrivilege(@Param("controller") String controller, @Param("action") String action);

    long countChildren(@Param("privilegeId") Integer privilegeId);

    int insert(Privilege privilege);

    int update(Privilege privilege);

    int deleteById(@Param("privilegeId") Integer privilegeId);
}
