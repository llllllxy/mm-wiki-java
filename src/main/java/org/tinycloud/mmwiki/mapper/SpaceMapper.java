package org.tinycloud.mmwiki.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.tinycloud.mmwiki.domain.Space;

/**
 * MM-Wiki MyBatis 数据访问接口。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Mapper
public interface SpaceMapper {

    @Select("""
        select *
        from mw_space
        where space_id = #{spaceId}
          and is_delete = 0
        limit 1
        """)
    Space findActiveById(@Param("spaceId") Integer spaceId);

    @Select("""
        select count(*)
        from mw_space
        where is_delete = 0
        """)
    long countAll();

    @Select("""
        select *
        from mw_space
        where is_delete = 0
        order by space_id desc
        limit #{offset}, #{size}
        """)
    List<Space> findAllPaged(@Param("offset") int offset, @Param("size") int size);

    @Select("""
        select count(*)
        from mw_space
        where is_delete = 0
          and (name like concat('%', #{keyword}, '%') or description like concat('%', #{keyword}, '%'))
        """)
    long countByKeyword(@Param("keyword") String keyword);

    @Select("""
        select *
        from mw_space
        where is_delete = 0
          and (name like concat('%', #{keyword}, '%') or description like concat('%', #{keyword}, '%'))
        order by space_id desc
        limit #{offset}, #{size}
        """)
    List<Space> findByKeywordPaged(@Param("keyword") String keyword, @Param("offset") int offset, @Param("size") int size);

    @Select("""
        select *
        from mw_space
        where is_delete = 0
        order by space_id desc
        """)
    List<Space> findAllActive();

    @Select("""
        select *
        from mw_space
        where tags like concat('%', #{tag}, '%')
          and is_delete = 0
        order by space_id desc
        """)
    List<Space> findByTag(@Param("tag") String tag);

    @Select({
        "<script>",
        "select *",
        "from mw_space",
        "where is_delete = 0",
        "<choose>",
        "<when test='spaceIds != null and spaceIds.size() > 0'>",
        "and space_id in",
        "<foreach collection='spaceIds' item='id' open='(' separator=',' close=')'>",
        "#{id}",
        "</foreach>",
        "</when>",
        "<otherwise>",
        "and 1 = 0",
        "</otherwise>",
        "</choose>",
        "</script>"
    })
    List<Space> findActiveByIds(@Param("spaceIds") List<Integer> spaceIds);

    @Select("""
        select count(*)
        from mw_space
        where name = #{name}
          and is_delete = 0
        """)
    long countByName(@Param("name") String name);

    @Select("""
        select count(*)
        from mw_space
        where name = #{name}
          and space_id <> #{spaceId}
          and is_delete = 0
        """)
    long countByNameAndNotId(@Param("spaceId") Integer spaceId, @Param("name") String name);

    @Insert("""
        insert into mw_space(name, description, tags, visit_level, is_share, is_export, is_delete, create_time, update_time)
        values(#{name}, #{description}, #{tags}, #{visitLevel}, #{isShare}, #{isExport}, 0, #{createTime}, #{updateTime})
        """)
    @Options(useGeneratedKeys = true, keyProperty = "spaceId")
    int insert(Space space);

    @Update("""
        update mw_space
        set name = #{name},
            description = #{description},
            tags = #{tags},
            visit_level = #{visitLevel},
            is_share = #{isShare},
            is_export = #{isExport},
            update_time = #{updateTime}
        where space_id = #{spaceId}
          and is_delete = 0
        """)
    int update(Space space);

    @Update("""
        update mw_space
        set is_delete = 1,
            update_time = unix_timestamp(now())
        where space_id = #{spaceId}
          and is_delete = 0
        """)
    int markDeleted(@Param("spaceId") Integer spaceId);
}
