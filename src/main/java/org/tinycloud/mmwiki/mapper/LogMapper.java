package org.tinycloud.mmwiki.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.tinycloud.mmwiki.domain.LogEntry;

@Mapper
public interface LogMapper {

    @Insert("""
        insert into mw_log(level, path, `get`, post, message, ip, user_agent, referer, user_id, username, create_time)
        values(#{level}, #{path}, #{get}, #{post}, #{message}, #{ip}, #{userAgent}, #{referer}, #{userId}, #{username}, #{createTime})
        """)
    int insert(LogEntry logEntry);

    @Select("""
        select log_id, level, path, `get`, post, message, ip, user_agent, referer, user_id, username, create_time
        from mw_log
        where log_id = #{logId}
        limit 1
        """)
    LogEntry findById(@Param("logId") Long logId);

    @Select({
        "<script>",
        "select count(*)",
        "from mw_log",
        "where 1 = 1",
        "<if test='level != null'>and level = #{level}</if>",
        "<if test='message != null and message != \"\"'>and message like concat('%', #{message}, '%')</if>",
        "<if test='username != null and username != \"\"'>and username like concat('%', #{username}, '%')</if>",
        "</script>"
    })
    long countByFilters(@Param("level") Integer level, @Param("message") String message, @Param("username") String username);

    @Select({
        "<script>",
        "select log_id, level, path, `get`, post, message, ip, user_agent, referer, user_id, username, create_time",
        "from mw_log",
        "where 1 = 1",
        "<if test='level != null'>and level = #{level}</if>",
        "<if test='message != null and message != \"\"'>and message like concat('%', #{message}, '%')</if>",
        "<if test='username != null and username != \"\"'>and username like concat('%', #{username}, '%')</if>",
        "order by log_id desc",
        "limit #{offset}, #{size}",
        "</script>"
    })
    List<LogEntry> findByFilters(
        @Param("level") Integer level,
        @Param("message") String message,
        @Param("username") String username,
        @Param("offset") int offset,
        @Param("size") int size
    );

    @Select("""
        select count(*)
        from mw_log
        where level = #{level}
        """)
    long countByLevel(@Param("level") Integer level);

    @Select("""
        select log_id, level, path, `get`, post, message, ip, user_agent, referer, user_id, username, create_time
        from mw_log
        where level = #{level}
        order by log_id desc
        limit #{offset}, #{size}
        """)
    List<LogEntry> findByLevel(@Param("level") Integer level, @Param("offset") int offset, @Param("size") int size);
}
