package org.tinycloud.mmwiki.mapper;

import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.tinycloud.mmwiki.domain.DocumentHistoryView;
import org.tinycloud.mmwiki.domain.LogDocumentView;

/**
 * MM-Wiki MyBatis 数据访问接口。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Mapper
public interface LogDocumentMapper {

    List<LogDocumentView> pageVisibleByUserId(@Param("userId") Integer userId, @Param("root") boolean root);

    List<LogDocumentView> pageByUserIdVisibleToViewer(
            @Param("profileUserId") Integer profileUserId,
            @Param("viewerUserId") Integer viewerUserId,
            @Param("root") boolean root,
            @Param("keyword") String keyword
    );

    List<LogDocumentView> pageByUserId(@Param("userId") Integer userId);

    List<LogDocumentView> pageByUserIdAndKeyword(@Param("userId") Integer userId, @Param("keyword") String keyword);

    List<DocumentHistoryView> pageByDocumentId(@Param("documentId") String documentId);

    List<LogDocumentView> pageForSystem(@Param("userId") Integer userId, @Param("keyword") String keyword);

    int insert(
        @Param("documentId") String documentId,
        @Param("spaceId") Integer spaceId,
        @Param("userId") Integer userId,
        @Param("action") Integer action,
        @Param("comment") String comment,
        @Param("createTime") LocalDateTime createTime
    );
}
