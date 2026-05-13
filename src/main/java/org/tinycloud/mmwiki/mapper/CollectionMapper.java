package org.tinycloud.mmwiki.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.tinycloud.mmwiki.domain.CollectionEntry;

/**
 * MM-Wiki MyBatis 数据访问接口。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Mapper
public interface CollectionMapper {

    List<CollectionEntry> findByUserIdAndType(@Param("userId") Integer userId, @Param("type") Integer type);

    CollectionEntry findByUserTypeAndResourceId(@Param("userId") Integer userId, @Param("type") Integer type, @Param("resourceId") String resourceId);

    CollectionEntry findById(@Param("collectionId") Integer collectionId);

    int insert(CollectionEntry collectionEntry);

    int deleteById(@Param("collectionId") Integer collectionId);

    List<Map<String, Object>> findResourceRank(@Param("type") Integer type, @Param("size") int size);

    Integer findTopUserIdByType(@Param("type") Integer type);
}
