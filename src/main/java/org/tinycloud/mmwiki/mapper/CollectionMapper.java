package org.tinycloud.mmwiki.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.tinycloud.mmwiki.domain.CollectionEntry;

/**
 * MM-Wiki MyBatis 数据访问接口。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Mapper
public interface CollectionMapper {

    @Select("""
        select *
        from mw_collection
        where user_id = #{userId}
          and type = #{type}
        """)
    List<CollectionEntry> findByUserIdAndType(@Param("userId") Integer userId, @Param("type") Integer type);

    @Select("""
        select *
        from mw_collection
        where user_id = #{userId}
          and type = #{type}
          and resource_id = #{resourceId}
        limit 1
        """)
    CollectionEntry findByUserTypeAndResourceId(@Param("userId") Integer userId, @Param("type") Integer type, @Param("resourceId") String resourceId);

    @Select("""
        select *
        from mw_collection
        where collection_id = #{collectionId}
        limit 1
        """)
    CollectionEntry findById(@Param("collectionId") Integer collectionId);

    @Insert("""
        insert into mw_collection(user_id, type, resource_id, create_time)
        values(#{userId}, #{type}, #{resourceId}, #{createTime})
        """)
    int insert(CollectionEntry collectionEntry);

    @Delete("""
        delete from mw_collection
        where collection_id = #{collectionId}
        """)
    int deleteById(@Param("collectionId") Integer collectionId);

    @Select("""
        select c.resource_id, coalesce(d.name, '') as document_name, count(*) as total
        from mw_collection c
        left join mw_document d on d.document_id = c.resource_id
        where c.type = #{type}
        group by c.resource_id, d.name
        order by total desc
        limit #{size}
        """)
    List<Map<String, Object>> findResourceRank(@Param("type") Integer type, @Param("size") int size);

    @Select("""
        select user_id
        from mw_collection
        where type = #{type}
        group by user_id
        order by count(*) desc
        limit 1
        """)
    Integer findTopUserIdByType(@Param("type") Integer type);
}
