package org.tinycloud.mmwiki.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.tinycloud.mmwiki.domain.User;

/**
 * MM-Wiki MyBatis 数据访问接口。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Mapper
public interface UserMapper {

    @Select("""
            select *
            from mw_user
            where username = #{username}
            and is_delete = 0
            limit 1
            """)
    User findActiveByUsername(@Param("username") String username);

    @Select("""
            select *
            from mw_user
            where user_id = #{userId}
            and is_delete = 0
            limit 1
            """)
    User findActiveById(@Param("userId") Integer userId);

    @Update("""
            update mw_user
            set last_time = #{lastTime},
                last_ip = #{lastIp},
                update_time = now()
            where user_id = #{userId}
            and is_delete = 0
            """)
    int updateLoginSuccess(@Param("userId") Integer userId, @Param("lastIp") String lastIp, @Param("lastTime") Integer lastTime);

    @Select({
            "<script>",
            "select *",
            "from mw_user",
            "where is_delete = 0",
            "<choose>",
            "<when test='userIds != null and userIds.size() > 0'>",
            "and user_id in",
            "<foreach collection='userIds' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</when>",
            "<otherwise>",
            "and 1 = 0",
            "</otherwise>",
            "</choose>",
            "</script>"
    })
    List<User> findActiveByIds(@Param("userIds") List<Integer> userIds);

    @Select("""
            select *
            from mw_user
            where is_delete = 0
            order by user_id asc
            """)
    List<User> findAllActive();

    @Select("""
            select *
            from mw_user
            where is_delete = 0
            order by user_id desc
            """)
    List<User> pageAllActive();

    @Select("""
            select *
            from mw_user
            where is_delete = 0
              and username like concat('%', #{username}, '%')
            order by user_id desc
            """)
    List<User> pageByUsernameLike(@Param("username") String username);

    @Select({
            "<script>",
            "select *",
            "from mw_user",
            "where is_delete = 0",
            "<if test='username != null and username != \"\"'>",
            "and username like concat('%', #{username}, '%')",
            "</if>",
            "<if test='roleId != null'>",
            "and role_id = #{roleId}",
            "</if>",
            "order by user_id desc",
            "</script>"
    })
    List<User> pageByFilters(@Param("username") String username, @Param("roleId") Integer roleId);

    @Select({
            "<script>",
            "select *",
            "from mw_user",
            "where is_delete = 0",
            "<if test='excludedIds != null and excludedIds.size() > 0'>",
            "and user_id not in",
            "<foreach collection='excludedIds' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</if>",
            "order by user_id asc",
            "</script>"
    })
    List<User> findActiveExcludingIds(@Param("excludedIds") List<Integer> excludedIds);

    @Select("""
            select count(*)
            from mw_user
            where username = #{username}
              and is_delete = 0
            """)
    long countByUsername(@Param("username") String username);

    @Insert("""
            insert into mw_user(username, password, given_name, mobile, phone, email, department, position, location, im,
                                last_ip, last_time, role_id, is_forbidden, is_delete, create_time, update_time)
            values(#{username}, #{password}, #{givenName}, #{mobile}, #{phone}, #{email}, #{department}, #{position}, #{location}, #{im},
                   '', 0, #{roleId}, 0, 0, #{createTime}, #{updateTime})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "userId")
    int insert(User user);

    @Insert("""
            insert into mw_user(username, password, given_name, mobile, phone, email, department, position, location, im,
                                last_ip, last_time, role_id, is_forbidden, is_delete, create_time, update_time)
            values(#{username}, #{password}, #{givenName}, #{mobile}, #{phone}, #{email}, #{department}, #{position}, #{location}, #{im},
                   #{lastIp}, #{lastTime}, #{roleId}, 0, 0, #{createTime}, #{updateTime})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "userId")
    int insertAuthUser(User user);

    @Update("""
            update mw_user
            set given_name = #{givenName},
                password = #{password},
                email = #{email},
                mobile = #{mobile},
                phone = #{phone},
                department = #{department},
                position = #{position},
                location = #{location},
                im = #{im},
                last_time = #{lastTime},
                last_ip = #{lastIp},
                update_time = #{updateTime}
            where username = #{username}
              and is_delete = 0
            """)
    int updateAuthUserByUsername(User user);

    @Update("""
            update mw_user
            set given_name = #{givenName},
                email = #{email},
                mobile = #{mobile},
                phone = #{phone},
                department = #{department},
                position = #{position},
                location = #{location},
                im = #{im},
                role_id = #{roleId},
                update_time = #{updateTime}
            where user_id = #{userId}
              and is_delete = 0
            """)
    int updateSystemUser(User user);

    @Update("""
            update mw_user
            set given_name = #{givenName},
                email = #{email},
                mobile = #{mobile},
                phone = #{phone},
                department = #{department},
                position = #{position},
                location = #{location},
                im = #{im},
                role_id = #{roleId},
                password = #{password},
                update_time = #{updateTime}
            where user_id = #{userId}
              and is_delete = 0
            """)
    int updateSystemUserWithPassword(User user);

    @Update("""
            update mw_user
            set given_name = #{givenName},
                email = #{email},
                mobile = #{mobile},
                phone = #{phone},
                department = #{department},
                position = #{position},
                location = #{location},
                im = #{im},
                update_time = now()
            where user_id = #{userId}
              and is_delete = 0
            """)
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

    @Update("""
            update mw_user
            set password = #{password},
                update_time = now()
            where user_id = #{userId}
              and is_delete = 0
            """)
    int updatePassword(@Param("userId") Integer userId, @Param("password") String password);

    @Update("""
            update mw_user
            set is_forbidden = #{isForbidden},
                update_time = now()
            where user_id = #{userId}
              and is_delete = 0
            """)
    int updateForbidden(@Param("userId") Integer userId, @Param("isForbidden") Integer isForbidden);

    @Select("""
            select count(*)
            from mw_user
            where is_delete = 0
              and role_id = #{roleId}
            """)
    long countByRoleId(@Param("roleId") Integer roleId);

    @Update("""
            update mw_user
            set role_id = #{roleId},
                update_time = now()
            where user_id = #{userId}
              and is_delete = 0
            """)
    int updateRoleId(@Param("userId") Integer userId, @Param("roleId") Integer roleId);

    @Select("""
            select count(*)
            from mw_user
            where is_delete = 0
              and is_forbidden = 0
            """)
    long countNormalUsers();

    @Select("""
            select count(*)
            from mw_user
            where is_delete = 0
              and is_forbidden = 1
            """)
    long countForbiddenUsers();

    @Select("""
            select count(*)
            from mw_user
            where is_delete = 0
              and last_time >= #{startTime}
            """)
    long countByLastTimeAfter(@Param("startTime") Integer startTime);
}

