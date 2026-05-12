package org.tinycloud.mmwiki.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import org.tinycloud.mmwiki.domain.Space;

/**
 * 空间信息 Mapper 接口
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Repository
public interface SpaceMapper {

    /**
     * 根据空间ID查询有效空间（未删除）
     *
     * @param spaceId 空间ID
     * @return 空间对象
     */
    Space findActiveById(@Param("spaceId") Integer spaceId);

    /**
     * 统计所有未删除的空间总数
     *
     * @return 总数
     */
    long countAll();

    /**
     * 根据关键词和用户权限分页查询空间列表
     * 可查看：公开空间 + 私有但我是成员的空间
     *
     * @param userId  当前用户ID
     * @param keyword 查询关键词（名称/描述模糊匹配）
     * @return 空间列表
     */
    List<Space> pageByKeyword(@Param("userId") Integer userId, @Param("keyword") String keyword);

    /**
     * 查询所有未删除的空间
     *
     * @return 空间列表
     */
    List<Space> findAllActive();

    /**
     * 根据标签模糊查询空间
     *
     * @param tag 标签
     * @return 空间列表
     */
    List<Space> findByTag(@Param("tag") String tag);

    /**
     * 根据空间ID集合批量查询未删除空间
     *
     * @param spaceIds 空间ID集合
     * @return 空间列表
     */
    List<Space> findActiveByIds(@Param("spaceIds") List<Integer> spaceIds);

    /**
     * 根据空间名称统计数量（用于重名校验）
     *
     * @param name 空间名称
     * @return 数量
     */
    long countByName(@Param("name") String name);

    /**
     * 根据空间名称统计数量（排除当前ID，用于编辑时重名校验）
     *
     * @param spaceId 当前空间ID
     * @param name    空间名称
     * @return 数量
     */
    long countByNameAndNotId(@Param("spaceId") Integer spaceId, @Param("name") String name);

    /**
     * 新增空间
     *
     * @param space 空间对象
     * @return 受影响行数
     */
    int insert(Space space);

    /**
     * 更新空间信息
     *
     * @param space 空间对象
     * @return 受影响行数
     */
    int update(Space space);

    /**
     * 逻辑删除空间（将is_delete置为1）
     *
     * @param spaceId 空间ID
     * @return 受影响行数
     */
    int markDeleted(@Param("spaceId") Integer spaceId);
}