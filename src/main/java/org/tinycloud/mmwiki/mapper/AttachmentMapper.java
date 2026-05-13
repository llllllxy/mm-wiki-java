package org.tinycloud.mmwiki.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.tinycloud.mmwiki.domain.Attachment;

/**
 * MM-Wiki MyBatis 数据访问接口。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Mapper
public interface AttachmentMapper {

    List<Attachment> findByDocumentId(@Param("documentId") String documentId);

    List<Attachment> findBySpaceId(@Param("spaceId") Integer spaceId);

    List<Attachment> findByDocumentIdAndSource(@Param("documentId") String documentId, @Param("source") Integer source);

    Attachment findById(@Param("attachmentId") Integer attachmentId);

    int insert(Attachment attachment);

    int deleteById(@Param("attachmentId") Integer attachmentId);

    int deleteByDocumentId(@Param("documentId") String documentId);
}
