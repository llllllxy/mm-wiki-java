package org.tinycloud.mmwiki.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.tinycloud.mmwiki.domain.Attachment;

@Mapper
public interface AttachmentMapper {

    @Select("""
        select attachment_id, user_id, document_id, name, path, source, create_time, update_time
        from mw_attachment
        where document_id = #{documentId}
        order by attachment_id desc
        """)
    List<Attachment> findByDocumentId(@Param("documentId") String documentId);

    @Select("""
        select a.attachment_id, a.user_id, a.document_id, a.name, a.path, a.source, a.create_time, a.update_time
        from mw_attachment a
        inner join mw_document d on d.document_id = a.document_id
        where d.space_id = #{spaceId}
          and d.is_delete = 0
        order by a.attachment_id desc
        """)
    List<Attachment> findBySpaceId(@Param("spaceId") Integer spaceId);

    @Select("""
        select a.attachment_id, a.user_id, a.document_id, a.name, a.path, a.source, a.create_time, a.update_time, u.username
        from mw_attachment a
        left join mw_user u on u.user_id = a.user_id
        where a.document_id = #{documentId}
          and a.source = #{source}
        order by a.attachment_id desc
        """)
    List<Attachment> findByDocumentIdAndSource(@Param("documentId") String documentId, @Param("source") Integer source);

    @Select("""
        select attachment_id, user_id, document_id, name, path, source, create_time, update_time
        from mw_attachment
        where attachment_id = #{attachmentId}
        limit 1
        """)
    Attachment findById(@Param("attachmentId") Integer attachmentId);

    @Insert("""
        insert into mw_attachment(user_id, document_id, name, path, source, create_time, update_time)
        values(#{userId}, #{documentId}, #{name}, #{path}, #{source}, #{createTime}, #{updateTime})
        """)
    int insert(Attachment attachment);

    @Delete("""
        delete from mw_attachment
        where attachment_id = #{attachmentId}
        """)
    int deleteById(@Param("attachmentId") Integer attachmentId);

    @Delete("""
        delete from mw_attachment
        where document_id = #{documentId}
        """)
    int deleteByDocumentId(@Param("documentId") String documentId);
}
