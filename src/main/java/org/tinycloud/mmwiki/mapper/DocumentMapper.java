package org.tinycloud.mmwiki.mapper;

import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.tinycloud.mmwiki.domain.Document;

/**
 * MM-Wiki MyBatis 数据访问接口。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Mapper
public interface DocumentMapper {

    List<Document> findActiveByIds(@Param("documentIds") List<String> documentIds);

    List<Document> findVisibleByIds(
            @Param("userId") Integer userId,
            @Param("root") boolean root,
            @Param("documentIds") List<String> documentIds
    );

    Document findActiveById(@Param("documentId") String documentId);

    Document findSpaceDefaultDocument(@Param("spaceId") Integer spaceId);

    List<Document> findAllSpaceDocuments(@Param("spaceId") Integer spaceId);

    List<Document> findActiveBySpaceId(@Param("spaceId") Integer spaceId);

    long countActiveBySpaceId(@Param("spaceId") Integer spaceId);

    List<Document> findByParentId(@Param("parentId") String parentId);

    Document findByNameParentIdAndSpaceId(
        @Param("name") String name,
        @Param("parentId") String parentId,
        @Param("spaceId") Integer spaceId,
        @Param("type") Integer type
    );

    Integer findMaxSequence(@Param("parentId") String parentId, @Param("spaceId") Integer spaceId);

    int insert(Document document);

    int updateNameAndEditor(Document document);

    int updateDefaultDocumentName(Document document);

    int updateParentPathEditor(Document document);

    int bumpSequenceBySpaceIdFrom(
        @Param("spaceId") Integer spaceId,
        @Param("startSequence") Integer startSequence,
        @Param("delta") Integer delta,
        @Param("updateTime") LocalDateTime updateTime
    );

    int updateSequence(Document document);

    int markDeleted(Document document);

    List<Document> findVisibleByNameLike(
            @Param("userId") Integer userId,
            @Param("root") boolean root,
            @Param("keyword") String keyword
    );

    long countActive();

    Integer findTopCreateUserId();

    Integer findTopEditUserId();

    List<Map<String, Object>> findSpaceDocumentRank(@Param("size") int size);

    List<Map<String, Object>> countGroupByCreateDate(@Param("startTime") LocalDateTime startTime);
}
