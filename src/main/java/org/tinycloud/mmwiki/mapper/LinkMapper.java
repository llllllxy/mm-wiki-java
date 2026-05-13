package org.tinycloud.mmwiki.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.tinycloud.mmwiki.domain.Link;

/**
 * MM-Wiki MyBatis 数据访问接口。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Mapper
public interface LinkMapper {

    List<Link> findAllOrderBySequence();

    Link findById(@Param("linkId") Integer linkId);

    List<Link> pageAll();

    List<Link> pageByKeyword(@Param("keyword") String keyword);

    long countByName(@Param("name") String name);

    long countByNameAndNotId(@Param("linkId") Integer linkId, @Param("name") String name);

    int insert(Link link);

    int update(Link link);

    int deleteById(@Param("linkId") Integer linkId);
}
