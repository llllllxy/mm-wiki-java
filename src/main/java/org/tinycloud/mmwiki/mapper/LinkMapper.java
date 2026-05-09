package org.tinycloud.mmwiki.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.tinycloud.mmwiki.domain.Link;

/**
 * MM-Wiki MyBatis 数据访问接口。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Mapper
public interface LinkMapper {

    @Select("""
        select *
        from mw_link
        order by sequence asc
        """)
    List<Link> findAllOrderBySequence();

    @Select("""
        select *
        from mw_link
        where link_id = #{linkId}
        limit 1
        """)
    Link findById(@Param("linkId") Integer linkId);

    @Select("""
        select *
        from mw_link
        order by sequence asc, link_id desc
        """)
    List<Link> pageAll();

    @Select("""
        select *
        from mw_link
        where name like concat('%', #{keyword}, '%')
        order by sequence asc, link_id desc
        """)
    List<Link> pageByKeyword(@Param("keyword") String keyword);

    @Select("""
        select count(*)
        from mw_link
        where name = #{name}
        """)
    long countByName(@Param("name") String name);

    @Select("""
        select count(*)
        from mw_link
        where name = #{name}
          and link_id <> #{linkId}
        """)
    long countByNameAndNotId(@Param("linkId") Integer linkId, @Param("name") String name);

    @Insert("""
        insert into mw_link(name, url, sequence, create_time, update_time)
        values(#{name}, #{url}, #{sequence}, #{createTime}, #{updateTime})
        """)
    @Options(useGeneratedKeys = true, keyProperty = "linkId")
    int insert(Link link);

    @Update("""
        update mw_link
        set name = #{name},
            url = #{url},
            sequence = #{sequence},
            update_time = #{updateTime}
        where link_id = #{linkId}
        """)
    int update(Link link);

    @Update("""
        delete from mw_link
        where link_id = #{linkId}
        """)
    int deleteById(@Param("linkId") Integer linkId);
}
