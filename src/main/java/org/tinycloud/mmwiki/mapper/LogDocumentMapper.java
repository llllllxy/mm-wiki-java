package org.tinycloud.mmwiki.mapper;

import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
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

    @Select("""
        select ld.log_document_id,
               ld.document_id,
               ld.space_id,
               ld.user_id,
               ld.action,
               ld.comment,
               ld.create_time,
               u.username,
               u.given_name,
               d.name as document_name,
               d.type as document_type
        from mw_log_document ld
        left join mw_user u on u.user_id = ld.user_id
        left join mw_document d on d.document_id = ld.document_id
            where #{root} = true
            or exists (
            select 1
            from mw_space s
            where s.space_id = ld.space_id
              and s.visit_level = 'public'
        )
        or exists (
            select 1
            from mw_space_user su
            where su.space_id = ld.space_id
              and su.user_id = #{userId}
        )
        order by ld.log_document_id desc
        """)
    List<LogDocumentView> pageVisibleByUserId(@Param("userId") Integer userId, @Param("root") boolean root);

    @Select({
            "<script>",
            "select ld.log_document_id,",
            "       ld.document_id,",
            "       ld.space_id,",
            "       ld.user_id,",
            "       ld.action,",
            "       ld.comment,",
            "       ld.create_time,",
            "       d.name as document_name,",
            "       d.type as document_type",
            "from mw_log_document ld",
            "left join mw_document d on d.document_id = ld.document_id",
            "left join mw_space s on s.space_id = ld.space_id",
            "left join mw_space_user su on su.space_id = ld.space_id and su.user_id = #{viewerUserId}",
            "where ld.user_id = #{profileUserId}",
            "and d.is_delete = 0",
            "and s.is_delete = 0",
            "and (#{root} = true or s.visit_level = 'public' or su.space_user_id is not null)",
            "<if test='keyword != null and keyword != \"\"'>",
            "and (ld.comment like concat('%', #{keyword}, '%') or d.name like concat('%', #{keyword}, '%'))",
            "</if>",
            "order by ld.log_document_id desc",
            "</script>"
    })
    List<LogDocumentView> pageByUserIdVisibleToViewer(
            @Param("profileUserId") Integer profileUserId,
            @Param("viewerUserId") Integer viewerUserId,
            @Param("root") boolean root,
            @Param("keyword") String keyword
    );

    @Select("""
        select ld.log_document_id,
               ld.document_id,
               ld.space_id,
               ld.user_id,
               ld.action,
               ld.comment,
               ld.create_time,
               d.name as document_name,
               d.type as document_type
        from mw_log_document ld
        left join mw_document d on d.document_id = ld.document_id
        where ld.user_id = #{userId}
        order by ld.log_document_id desc
        """)
    List<LogDocumentView> pageByUserId(@Param("userId") Integer userId);

    @Select("""
        select ld.log_document_id,
               ld.document_id,
               ld.space_id,
               ld.user_id,
               ld.action,
               ld.comment,
               ld.create_time,
               d.name as document_name,
               d.type as document_type
        from mw_log_document ld
        left join mw_document d on d.document_id = ld.document_id
        where ld.user_id = #{userId}
          and (
            ld.comment like concat('%', #{keyword}, '%')
            or d.name like concat('%', #{keyword}, '%')
          )
        order by ld.log_document_id desc
        """)
    List<LogDocumentView> pageByUserIdAndKeyword(@Param("userId") Integer userId, @Param("keyword") String keyword);

    @Select("""
        select ld.log_document_id,
               ld.document_id,
               ld.user_id,
               ld.action,
               ld.comment,
               ld.create_time,
               u.username
        from mw_log_document ld
        left join mw_user u on u.user_id = ld.user_id
        where ld.document_id = #{documentId}
        order by ld.log_document_id desc
        """)
    List<DocumentHistoryView> pageByDocumentId(@Param("documentId") String documentId);

    @Select({
        "<script>",
        "select ld.log_document_id,",
        "       ld.document_id,",
        "       ld.space_id,",
        "       ld.user_id,",
        "       ld.action,",
        "       ld.comment,",
        "       ld.create_time,",
        "       u.username,",
        "       u.given_name,",
        "       d.name as document_name,",
        "       d.type as document_type",
        "from mw_log_document ld",
        "left join mw_user u on u.user_id = ld.user_id",
        "left join mw_document d on d.document_id = ld.document_id",
        "where 1 = 1",
        "<if test='userId != null'>and ld.user_id = #{userId}</if>",
        "<if test='keyword != null and keyword != \"\"'>",
        "and (ld.comment like concat('%', #{keyword}, '%') or d.name like concat('%', #{keyword}, '%'))",
        "</if>",
        "order by ld.log_document_id desc",
        "</script>"
    })
    List<LogDocumentView> pageForSystem(@Param("userId") Integer userId, @Param("keyword") String keyword);

    @Insert("""
        insert into mw_log_document(document_id, space_id, user_id, action, comment, create_time)
        values(#{documentId}, #{spaceId}, #{userId}, #{action}, #{comment}, #{createTime})
        """)
    int insert(
        @Param("documentId") String documentId,
        @Param("spaceId") Integer spaceId,
        @Param("userId") Integer userId,
        @Param("action") Integer action,
        @Param("comment") String comment,
        @Param("createTime") LocalDateTime createTime
    );
}
