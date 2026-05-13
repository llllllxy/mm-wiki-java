package org.tinycloud.mmwiki.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.tinycloud.mmwiki.domain.Contact;

/**
 * MM-Wiki MyBatis 数据访问接口。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Mapper
public interface ContactMapper {

    List<Contact> findAll();

    Contact findById(@Param("contactId") Integer contactId);

    int insert(Contact contact);

    int update(Contact contact);

    int deleteById(@Param("contactId") Integer contactId);
}
