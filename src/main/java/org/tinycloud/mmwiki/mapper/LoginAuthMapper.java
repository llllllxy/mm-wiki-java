package org.tinycloud.mmwiki.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.tinycloud.mmwiki.domain.LoginAuth;

@Mapper
public interface LoginAuthMapper {

    @Select("""
        select login_auth_id, name, username_prefix, url, ext_data, is_used, is_delete, create_time, update_time
        from mw_login_auth
        where is_delete = 0
        order by login_auth_id desc
        """)
    List<LoginAuth> findAllActive();

    @Select("""
        select count(*)
        from mw_login_auth
        where is_delete = 0
          and name like concat('%', #{keyword}, '%')
        """)
    long countByKeyword(@Param("keyword") String keyword);

    @Select("""
        select count(*)
        from mw_login_auth
        where is_delete = 0
        """)
    long countAllActive();

    @Select("""
        select login_auth_id, name, username_prefix, url, ext_data, is_used, is_delete, create_time, update_time
        from mw_login_auth
        where is_delete = 0
          and name like concat('%', #{keyword}, '%')
        order by login_auth_id desc
        limit #{offset}, #{size}
        """)
    List<LoginAuth> findByKeywordPaged(@Param("keyword") String keyword, @Param("offset") int offset, @Param("size") int size);

    @Select("""
        select login_auth_id, name, username_prefix, url, ext_data, is_used, is_delete, create_time, update_time
        from mw_login_auth
        where is_delete = 0
        order by login_auth_id desc
        limit #{offset}, #{size}
        """)
    List<LoginAuth> findAllActivePaged(@Param("offset") int offset, @Param("size") int size);

    @Select("""
        select login_auth_id, name, username_prefix, url, ext_data, is_used, is_delete, create_time, update_time
        from mw_login_auth
        where login_auth_id = #{loginAuthId}
          and is_delete = 0
        limit 1
        """)
    LoginAuth findActiveById(@Param("loginAuthId") Integer loginAuthId);

    @Select("""
        select count(*)
        from mw_login_auth
        where is_delete = 0
          and name = #{name}
        """)
    long countByName(@Param("name") String name);

    @Select("""
        select count(*)
        from mw_login_auth
        where is_delete = 0
          and name = #{name}
          and login_auth_id <> #{loginAuthId}
        """)
    long countByNameAndNotId(@Param("loginAuthId") Integer loginAuthId, @Param("name") String name);

    @Select("""
        select count(*)
        from mw_login_auth
        where is_delete = 0
          and username_prefix = #{usernamePrefix}
        """)
    long countByUsernamePrefix(@Param("usernamePrefix") String usernamePrefix);

    @Select("""
        select count(*)
        from mw_login_auth
        where is_delete = 0
          and username_prefix = #{usernamePrefix}
          and login_auth_id <> #{loginAuthId}
        """)
    long countByUsernamePrefixAndNotId(@Param("loginAuthId") Integer loginAuthId, @Param("usernamePrefix") String usernamePrefix);

    @Select("""
        select login_auth_id, name, username_prefix, url, ext_data, is_used, is_delete, create_time, update_time
        from mw_login_auth
        where is_delete = 0
          and is_used = 1
        limit 1
        """)
    LoginAuth findUsed();

    @Insert("""
        insert into mw_login_auth(name, username_prefix, url, ext_data, is_used, is_delete, create_time, update_time)
        values(#{name}, #{usernamePrefix}, #{url}, #{extData}, #{isUsed}, #{isDelete}, #{createTime}, #{updateTime})
        """)
    @Options(useGeneratedKeys = true, keyProperty = "loginAuthId")
    int insert(LoginAuth loginAuth);

    @Update("""
        update mw_login_auth
        set name = #{name},
            username_prefix = #{usernamePrefix},
            url = #{url},
            ext_data = #{extData},
            update_time = #{updateTime}
        where login_auth_id = #{loginAuthId}
          and is_delete = 0
        """)
    int update(LoginAuth loginAuth);

    @Update("""
        update mw_login_auth
        set is_used = 0
        where is_delete = 0
        """)
    int clearUsed();

    @Update("""
        update mw_login_auth
        set is_used = 1,
            update_time = unix_timestamp(now())
        where login_auth_id = #{loginAuthId}
          and is_delete = 0
        """)
    int markUsed(@Param("loginAuthId") Integer loginAuthId);

    @Update("""
        update mw_login_auth
        set is_delete = 1,
            update_time = unix_timestamp(now())
        where login_auth_id = #{loginAuthId}
          and is_delete = 0
        """)
    int markDeleted(@Param("loginAuthId") Integer loginAuthId);
}
