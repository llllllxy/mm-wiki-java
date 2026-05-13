package org.tinycloud.mmwiki.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.tinycloud.mmwiki.domain.EmailServer;

/**
 * MM-Wiki MyBatis 数据访问接口。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Mapper
public interface EmailMapper {

    List<EmailServer> findAll();

    List<EmailServer> findByNameLike(@Param("keyword") String keyword);

    EmailServer findById(@Param("emailId") Integer emailId);

    long countByName(@Param("name") String name);

    long countByNameAndNotId(@Param("emailId") Integer emailId, @Param("name") String name);

    EmailServer findUsed();

    int insert(EmailServer emailServer);

    int update(EmailServer emailServer);

    int clearUsed();

    int markUsed(@Param("emailId") Integer emailId);

    int deleteById(@Param("emailId") Integer emailId);
}
