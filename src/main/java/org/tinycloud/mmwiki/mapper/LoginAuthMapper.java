package org.tinycloud.mmwiki.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.tinycloud.mmwiki.domain.LoginAuth;

/**
 * MM-Wiki MyBatis 数据访问接口。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Mapper
public interface LoginAuthMapper {

    List<LoginAuth> findAllActive();

    List<LoginAuth> pageByKeyword(@Param("keyword") String keyword);

    List<LoginAuth> pageAllActive();

    LoginAuth findActiveById(@Param("loginAuthId") Integer loginAuthId);

    long countByName(@Param("name") String name);

    long countByNameAndNotId(@Param("loginAuthId") Integer loginAuthId, @Param("name") String name);

    long countByUsernamePrefix(@Param("usernamePrefix") String usernamePrefix);

    long countByUsernamePrefixAndNotId(@Param("loginAuthId") Integer loginAuthId, @Param("usernamePrefix") String usernamePrefix);

    LoginAuth findUsed();

    int insert(LoginAuth loginAuth);

    int update(LoginAuth loginAuth);

    int clearUsed();

    int markUsed(@Param("loginAuthId") Integer loginAuthId);

    int markDeleted(@Param("loginAuthId") Integer loginAuthId);
}
