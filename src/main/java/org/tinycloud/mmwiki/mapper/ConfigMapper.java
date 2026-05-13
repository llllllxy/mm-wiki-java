package org.tinycloud.mmwiki.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import org.tinycloud.mmwiki.domain.ConfigEntry;

/**
 * MM-Wiki MyBatis 数据访问接口。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Mapper
public interface ConfigMapper {

    List<ConfigEntry> findAll();

    String findValueByKey(@Param("key") String key);

    int updateValueByKey(@Param("key") String key, @Param("value") String value);
}
