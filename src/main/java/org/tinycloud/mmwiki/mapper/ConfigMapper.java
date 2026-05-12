package org.tinycloud.mmwiki.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
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

    @Select("""
        select config_id, name, `key`, value, create_time, update_time
        from mw_config
        order by config_id asc
        """)
    List<ConfigEntry> findAll();

    @Select("""
        select value
        from mw_config
        where `key` = #{key}
        limit 1
        """)
    String findValueByKey(@Param("key") String key);

    @Update("""
        update mw_config
        set value = #{value},
                update_time = now()
        where `key` = #{key}
        """)
    int updateValueByKey(@Param("key") String key, @Param("value") String value);
}

