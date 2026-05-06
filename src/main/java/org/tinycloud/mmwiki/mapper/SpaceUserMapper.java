package org.tinycloud.mmwiki.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.tinycloud.mmwiki.domain.SpaceUser;

@Mapper
public interface SpaceUserMapper {

    @Select("""
        select *
        from mw_space_user
        where user_id = #{userId}
        """)
    List<SpaceUser> findByUserId(@Param("userId") Integer userId);

    @Select("""
        select *
        from mw_space_user
        where space_id = #{spaceId}
          and user_id = #{userId}
        limit 1
        """)
    SpaceUser findBySpaceIdAndUserId(@Param("spaceId") Integer spaceId, @Param("userId") Integer userId);

    @Select("""
        select count(*)
        from mw_space_user
        where space_id = #{spaceId}
        """)
    long countBySpaceId(@Param("spaceId") Integer spaceId);

    @Select("""
        select *
        from mw_space_user
        where space_id = #{spaceId}
        limit #{offset}, #{size}
        """)
    List<SpaceUser> findBySpaceIdPaged(@Param("spaceId") Integer spaceId, @Param("offset") int offset, @Param("size") int size);

    @Select("""
        select *
        from mw_space_user
        where space_id = #{spaceId}
        """)
    List<SpaceUser> findBySpaceId(@Param("spaceId") Integer spaceId);

    @Insert("""
        insert into mw_space_user(user_id, space_id, privilege, create_time, update_time)
        values(#{userId}, #{spaceId}, #{privilege}, #{createTime}, #{updateTime})
        """)
    int insert(SpaceUser spaceUser);

    @Update("""
        update mw_space_user
        set privilege = #{privilege},
            update_time = #{updateTime}
        where space_user_id = #{spaceUserId}
        """)
    int updatePrivilege(SpaceUser spaceUser);

    @Delete("""
        delete from mw_space_user
        where space_user_id = #{spaceUserId}
        """)
    int deleteById(@Param("spaceUserId") Integer spaceUserId);

    @Delete("""
        delete from mw_space_user
        where space_id = #{spaceId}
        """)
    int deleteBySpaceId(@Param("spaceId") Integer spaceId);
}
