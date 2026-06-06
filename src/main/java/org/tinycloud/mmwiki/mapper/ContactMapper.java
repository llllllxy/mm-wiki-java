package org.tinycloud.mmwiki.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.tinycloud.mmwiki.domain.Contact;

/**
 * 联系人表数据访问接口，负责系统联系人查询、分页和维护。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Mapper
public interface ContactMapper {

    List<Contact> findAll();

    List<Contact> pageByKeyword(@Param("keyword") String keyword);

    Contact findById(@Param("contactId") Integer contactId);

    int insert(Contact contact);

    int update(Contact contact);

    int deleteById(@Param("contactId") Integer contactId);
}
