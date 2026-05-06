package org.tinycloud.mmwiki.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.tinycloud.mmwiki.domain.Follow;

@Mapper
public interface FollowMapper {

    @Select("""
        select follow_id, user_id, type, object_id, create_time
        from mw_follow
        where user_id = #{userId}
          and type = #{type}
          and object_id = #{objectId}
        limit 1
        """)
    Follow findByUserTypeAndObjectId(
        @Param("userId") Integer userId,
        @Param("type") Integer type,
        @Param("objectId") String objectId
    );

    @Select("""
        select follow_id, user_id, type, object_id, create_time
        from mw_follow
        where follow_id = #{followId}
        limit 1
        """)
    Follow findById(@Param("followId") Integer followId);

    @Select("""
        select follow_id, user_id, type, object_id, create_time
        from mw_follow
        where user_id = #{userId}
          and type = #{type}
        order by follow_id desc
        """)
    List<Follow> findByUserIdAndType(@Param("userId") Integer userId, @Param("type") Integer type);

    @Select("""
        select follow_id, user_id, type, object_id, create_time
        from mw_follow
        where object_id = #{objectId}
          and type = #{type}
        order by follow_id desc
        """)
    List<Follow> findByObjectIdAndType(@Param("objectId") String objectId, @Param("type") Integer type);

    @Insert("""
        insert into mw_follow(user_id, type, object_id, create_time)
        values(#{userId}, #{type}, #{objectId}, #{createTime})
        """)
    int insert(Follow follow);

    @Delete("""
        delete from mw_follow
        where follow_id = #{followId}
        """)
    int deleteById(@Param("followId") Integer followId);

    @Delete("""
        delete from mw_follow
        where object_id = #{objectId}
          and type = #{type}
        """)
    int deleteByObjectIdAndType(@Param("objectId") String objectId, @Param("type") Integer type);

    @Select("""
        select cast(object_id as signed)
        from mw_follow
        where type = #{type}
        group by object_id
        order by count(*) desc
        limit 1
        """)
    Integer findTopObjectIdByType(@Param("type") Integer type);
}
