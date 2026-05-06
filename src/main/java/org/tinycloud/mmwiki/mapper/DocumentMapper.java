package org.tinycloud.mmwiki.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.tinycloud.mmwiki.domain.Document;

@Mapper
public interface DocumentMapper {

    @Select({
        "<script>",
        "select document_id, parent_id, space_id, name, type, path, sequence, create_user_id, edit_user_id, create_time, update_time",
        "from mw_document",
        "where is_delete = 0",
        "<choose>",
        "<when test='documentIds != null and documentIds.size() > 0'>",
        "and document_id in",
        "<foreach collection='documentIds' item='id' open='(' separator=',' close=')'>",
        "#{id}",
        "</foreach>",
        "</when>",
        "<otherwise>",
        "and 1 = 0",
        "</otherwise>",
        "</choose>",
        "order by sequence asc, create_time asc",
        "</script>"
    })
    List<Document> findActiveByIds(@Param("documentIds") List<String> documentIds);

    @Select("""
        select document_id, parent_id, space_id, name, type, path, sequence, create_user_id, edit_user_id, create_time, update_time
        from mw_document
        where document_id = #{documentId}
          and is_delete = 0
        limit 1
        """)
    Document findActiveById(@Param("documentId") String documentId);

    @Select("""
        select document_id, parent_id, space_id, name, type, path, sequence, create_user_id, edit_user_id, create_time, update_time
        from mw_document
        where space_id = #{spaceId}
          and parent_id = '0'
          and is_delete = 0
        limit 1
        """)
    Document findSpaceDefaultDocument(@Param("spaceId") Integer spaceId);

    @Select("""
        select document_id, parent_id, space_id, name, type, path, sequence, create_user_id, edit_user_id, create_time, update_time
        from mw_document
        where space_id = #{spaceId}
          and parent_id > '0'
          and is_delete = 0
        order by sequence asc
        """)
    List<Document> findAllSpaceDocuments(@Param("spaceId") Integer spaceId);

    @Select("""
        select document_id, parent_id, space_id, name, type, path, sequence, create_user_id, edit_user_id, create_time, update_time
        from mw_document
        where space_id = #{spaceId}
          and is_delete = 0
        order by sequence asc, create_time asc
        """)
    List<Document> findActiveBySpaceId(@Param("spaceId") Integer spaceId);

    @Select("""
        select count(*)
        from mw_document
        where space_id = #{spaceId}
          and is_delete = 0
        """)
    long countActiveBySpaceId(@Param("spaceId") Integer spaceId);

    @Select("""
        select document_id, parent_id, space_id, name, type, path, sequence, create_user_id, edit_user_id, create_time, update_time
        from mw_document
        where parent_id = #{parentId}
          and is_delete = 0
        """)
    List<Document> findByParentId(@Param("parentId") String parentId);

    @Select("""
        select document_id, parent_id, space_id, name, type, path, sequence, create_user_id, edit_user_id, create_time, update_time
        from mw_document
        where name = #{name}
          and parent_id = #{parentId}
          and space_id = #{spaceId}
          and type = #{type}
          and is_delete = 0
        limit 1
        """)
    Document findByNameParentIdAndSpaceId(
        @Param("name") String name,
        @Param("parentId") String parentId,
        @Param("spaceId") Integer spaceId,
        @Param("type") Integer type
    );

    @Select("""
        select coalesce(max(sequence), 0)
        from mw_document
        where parent_id = #{parentId}
          and space_id = #{spaceId}
        """)
    Integer findMaxSequence(@Param("parentId") String parentId, @Param("spaceId") Integer spaceId);

    @Insert("""
        insert into mw_document(document_id, parent_id, space_id, name, type, path, sequence, create_user_id, edit_user_id, is_delete, create_time, update_time)
        values(#{documentId}, #{parentId}, #{spaceId}, #{name}, #{type}, #{path}, #{sequence}, #{createUserId}, #{editUserId}, 0, #{createTime}, #{updateTime})
        """)
    int insert(Document document);

    @Update("""
        update mw_document
        set name = #{name},
            edit_user_id = #{editUserId},
            update_time = #{updateTime}
        where document_id = #{documentId}
          and is_delete = 0
        """)
    int updateNameAndEditor(Document document);

    @Update("""
        update mw_document
        set name = #{name},
            edit_user_id = #{editUserId},
            update_time = #{updateTime}
        where document_id = #{documentId}
          and parent_id = '0'
          and is_delete = 0
        """)
    int updateDefaultDocumentName(Document document);

    @Update("""
        update mw_document
        set parent_id = #{parentId},
            path = #{path},
            edit_user_id = #{editUserId},
            update_time = #{updateTime}
        where document_id = #{documentId}
          and is_delete = 0
        """)
    int updateParentPathEditor(Document document);

    @Update("""
        update mw_document
        set sequence = sequence + #{delta},
            update_time = #{updateTime}
        where space_id = #{spaceId}
          and sequence >= #{startSequence}
          and is_delete = 0
        """)
    int bumpSequenceBySpaceIdFrom(
        @Param("spaceId") Integer spaceId,
        @Param("startSequence") Integer startSequence,
        @Param("delta") Integer delta,
        @Param("updateTime") Integer updateTime
    );

    @Update("""
        update mw_document
        set sequence = #{sequence},
            edit_user_id = #{editUserId},
            update_time = #{updateTime}
        where document_id = #{documentId}
          and is_delete = 0
        """)
    int updateSequence(Document document);

    @Update("""
        update mw_document
        set is_delete = 1,
            edit_user_id = #{editUserId},
            update_time = #{updateTime}
        where document_id = #{documentId}
          and is_delete = 0
        """)
    int markDeleted(Document document);

    @Select("""
        select distinct d.document_id, d.parent_id, d.space_id, d.name, d.type, d.path, d.sequence, d.create_user_id, d.edit_user_id, d.create_time, d.update_time
        from mw_document d
        left join mw_space s on s.space_id = d.space_id
        left join mw_space_user su on su.space_id = d.space_id and su.user_id = #{userId}
        where d.is_delete = 0
          and d.name like concat('%', #{keyword}, '%')
          and (s.visit_level = 'public' or su.space_user_id is not null)
        order by d.update_time desc
        """)
    List<Document> findVisibleByNameLike(@Param("userId") Integer userId, @Param("keyword") String keyword);

    @Select("""
        select count(*)
        from mw_document
        where is_delete = 0
        """)
    long countActive();

    @Select("""
        select create_user_id
        from mw_document
        where is_delete = 0
          and create_user_id > 0
        group by create_user_id
        order by count(*) desc
        limit 1
        """)
    Integer findTopCreateUserId();

    @Select("""
        select edit_user_id
        from mw_document
        where is_delete = 0
          and edit_user_id > 0
        group by edit_user_id
        order by count(*) desc
        limit 1
        """)
    Integer findTopEditUserId();

    @Select("""
        select coalesce(s.name, '') as space_name, count(d.document_id) as total
        from mw_document d
        left join mw_space s on s.space_id = d.space_id
        where d.is_delete = 0
        group by d.space_id, s.name
        order by total desc
        limit #{size}
        """)
    List<Map<String, Object>> findSpaceDocumentRank(@Param("size") int size);

    @Select("""
        select from_unixtime(create_time, '%Y-%m-%d') as date, count(*) as total
        from mw_document
        where is_delete = 0
          and create_time >= #{startTime}
        group by from_unixtime(create_time, '%Y-%m-%d')
        order by date asc
        """)
    List<Map<String, Object>> countGroupByCreateDate(@Param("startTime") Integer startTime);
}
