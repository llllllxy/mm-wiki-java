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

    /**
     * 根据文档ID、存储路径和来源查找附件，用于校验图片资源确实属于指定文档。
     *
     * @param documentId 文档ID
     * @param path       附件存储相对路径
     * @param source     附件来源类型
     * @return 匹配的附件记录，不存在时返回 null
     */
    Attachment findByDocumentIdPathAndSource(@Param("documentId") String documentId,
                                             @Param("path") String path,
                                             @Param("source") Integer source);

    int insert(Attachment attachment);

    int deleteById(@Param("attachmentId") Integer attachmentId);

    int deleteByDocumentId(@Param("documentId") String documentId);
}
