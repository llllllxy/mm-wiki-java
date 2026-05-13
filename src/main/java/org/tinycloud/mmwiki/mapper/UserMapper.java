package org.tinycloud.mmwiki.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.tinycloud.mmwiki.domain.User;

/**
 * MM-Wiki MyBatis 数据访问接口。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Mapper
public interface UserMapper {

    User findActiveByUsername(@Param("username") String username);

    User findActiveById(@Param("userId") Integer userId);

    int updateLoginSuccess(@Param("userId") Integer userId, @Param("lastIp") String lastIp, @Param("lastTime") Integer lastTime);

    List<User> findActiveByIds(@Param("userIds") List<Integer> userIds);

    List<User> findAllActive();

    List<User> pageAllActive();

    List<User> pageByUsernameLike(@Param("username") String username);

    List<User> pageByFilters(@Param("username") String username, @Param("roleId") Integer roleId);

    List<User> findActiveExcludingIds(@Param("excludedIds") List<Integer> excludedIds);

    long countByUsername(@Param("username") String username);

    int insert(User user);

    int insertAuthUser(User user);

    int updateAuthUserByUsername(User user);

    int updateSystemUser(User user);

    int updateSystemUserWithPassword(User user);

    int updateProfile(
            @Param("userId") Integer userId,
            @Param("givenName") String givenName,
            @Param("email") String email,
            @Param("mobile") String mobile,
            @Param("phone") String phone,
            @Param("department") String department,
            @Param("position") String position,
            @Param("location") String location,
            @Param("im") String im
    );

    int updatePassword(@Param("userId") Integer userId, @Param("password") String password);

    int updateForbidden(@Param("userId") Integer userId, @Param("isForbidden") Integer isForbidden);

    long countByRoleId(@Param("roleId") Integer roleId);

    int updateRoleId(@Param("userId") Integer userId, @Param("roleId") Integer roleId);

    long countNormalUsers();

    long countForbiddenUsers();

    long countByLastTimeAfter(@Param("startTime") Integer startTime);
}
