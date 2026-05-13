package org.tinycloud.mmwiki.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.tinycloud.mmwiki.domain.Follow;

/**
 * MM-Wiki MyBatis 数据访问接口。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Mapper
public interface FollowMapper {

    Follow findByUserTypeAndObjectId(
        @Param("userId") Integer userId,
        @Param("type") Integer type,
        @Param("objectId") String objectId
    );

    Follow findById(@Param("followId") Integer followId);

    List<Follow> findByUserIdAndType(@Param("userId") Integer userId, @Param("type") Integer type);

    List<Follow> findByObjectIdAndType(@Param("objectId") String objectId, @Param("type") Integer type);

    int insert(Follow follow);

    int deleteById(@Param("followId") Integer followId);

    int deleteByObjectIdAndType(@Param("objectId") String objectId, @Param("type") Integer type);

    Integer findTopObjectIdByType(@Param("type") Integer type);
}
