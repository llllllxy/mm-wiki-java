package org.tinycloud.mmwiki.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.tinycloud.mmwiki.domain.LogEntry;

/**
 * 系统日志数据访问接口，负责后台系统日志记录和分页查询。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Mapper
public interface LogMapper {

    int insert(LogEntry logEntry);

    LogEntry findById(@Param("logId") Long logId);

    List<LogEntry> pageByFilters(@Param("level") Integer level, @Param("message") String message, @Param("username") String username);

    List<LogEntry> pageByLevel(@Param("level") Integer level);
}
