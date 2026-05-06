package org.tinycloud.mmwiki.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.tinycloud.mmwiki.domain.Contact;

/**
 * MM-Wiki MyBatis 数据访问接口。
 *
 * @author liuxingyu01
 * @since 2026-05-06
 */
@Mapper
public interface ContactMapper {

    @Select("""
        select *
        from mw_contact
        """)
    List<Contact> findAll();

    @Select("""
        select *
        from mw_contact
        where contact_id = #{contactId}
        limit 1
        """)
    Contact findById(@Param("contactId") Integer contactId);

    @Insert("""
        insert into mw_contact(name, mobile, email, position, create_time, update_time)
        values(#{name}, #{mobile}, #{email}, #{position}, #{createTime}, #{updateTime})
        """)
    @Options(useGeneratedKeys = true, keyProperty = "contactId")
    int insert(Contact contact);

    @Update("""
        update mw_contact
        set name = #{name},
            mobile = #{mobile},
            email = #{email},
            position = #{position},
            update_time = #{updateTime}
        where contact_id = #{contactId}
        """)
    int update(Contact contact);

    @Update("""
        delete from mw_contact
        where contact_id = #{contactId}
        """)
    int deleteById(@Param("contactId") Integer contactId);
}
