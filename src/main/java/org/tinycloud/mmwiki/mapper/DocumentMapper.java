package org.tinycloud.mmwiki.mapper;

import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.tinycloud.mmwiki.domain.Document;

/**
 * 文档表数据访问接口，负责文档树、文档详情、排序和统计查询。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Mapper
public interface DocumentMapper {

    List<Document> findActiveByIds(@Param("documentIds") List<String> documentIds);

    List<Document> findVisibleByIds(@Param("userId") Integer userId, @Param("root") boolean root, @Param("documentIds") List<String> documentIds);

    Document findActiveById(@Param("documentId") String documentId);

    Document findSpaceDefaultDocument(@Param("spaceId") Integer spaceId);

    List<Document> findAllSpaceDocuments(@Param("spaceId") Integer spaceId);

    List<Document> findActiveBySpaceId(@Param("spaceId") Integer spaceId);

    long countActiveBySpaceId(@Param("spaceId") Integer spaceId);

    List<Document> findByParentId(@Param("parentId") String parentId);

    Document findByNameParentIdAndSpaceId(@Param("name") String name, @Param("parentId") String parentId,
                                          @Param("spaceId") Integer spaceId, @Param("type") Integer type);

    Integer findMaxSequence(@Param("parentId") String parentId, @Param("spaceId") Integer spaceId);

    int insert(Document document);

    int updateNameAndEditor(Document document);

    int updateDefaultDocumentName(Document document);

    int updateParentPathEditor(Document document);

    /**
     * 调整同一父目录下指定起始序号之后的文档排序，排除正在移动的文档自身。
     *
     * @param parentId          父文档 ID，只影响同级文档
     * @param spaceId           空间 ID，限制排序范围
     * @param excludeDocumentId 需要排除的文档 ID，避免移动自身时重复 bump
     * @param startSequence     起始序号，序号大于等于该值的文档会整体偏移
     * @param delta             序号偏移量
     * @param updateTime        更新时间
     * @return 更新的行数
     */
    int bumpSequenceByParentIdFrom(@Param("parentId") String parentId, @Param("spaceId") Integer spaceId,
                                   @Param("excludeDocumentId") String excludeDocumentId,
                                   @Param("startSequence") Integer startSequence,
                                   @Param("delta") Integer delta, @Param("updateTime") LocalDateTime updateTime);

    int updateSequence(Document document);

    int markDeleted(Document document);

    List<Document> findVisibleByNameLike(@Param("userId") Integer userId, @Param("root") boolean root, @Param("keyword") String keyword);

    long countActive();

    Integer findTopCreateUserId();

    Integer findTopEditUserId();

    List<Map<String, Object>> findSpaceDocumentRank(@Param("size") int size);

    List<Map<String, Object>> countGroupByCreateDate(@Param("startTime") LocalDateTime startTime);
}
