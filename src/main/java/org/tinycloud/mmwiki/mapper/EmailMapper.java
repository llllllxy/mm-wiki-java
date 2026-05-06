package org.tinycloud.mmwiki.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.tinycloud.mmwiki.domain.EmailServer;

@Mapper
public interface EmailMapper {

    @Select("""
        select email_id, name, sender_address, sender_name, sender_title_prefix, host, port, username, password, is_ssl, is_used, create_time, update_time
        from mw_email
        order by email_id desc
        """)
    List<EmailServer> findAll();

    @Select("""
        select email_id, name, sender_address, sender_name, sender_title_prefix, host, port, username, password, is_ssl, is_used, create_time, update_time
        from mw_email
        where name like concat('%', #{keyword}, '%')
        order by email_id desc
        """)
    List<EmailServer> findByNameLike(@Param("keyword") String keyword);

    @Select("""
        select email_id, name, sender_address, sender_name, sender_title_prefix, host, port, username, password, is_ssl, is_used, create_time, update_time
        from mw_email
        where email_id = #{emailId}
        limit 1
        """)
    EmailServer findById(@Param("emailId") Integer emailId);

    @Select("""
        select count(*)
        from mw_email
        where name = #{name}
        """)
    long countByName(@Param("name") String name);

    @Select("""
        select count(*)
        from mw_email
        where name = #{name}
          and email_id <> #{emailId}
        """)
    long countByNameAndNotId(@Param("emailId") Integer emailId, @Param("name") String name);

    @Select("""
        select email_id, name, sender_address, sender_name, sender_title_prefix, host, port, username, password, is_ssl, is_used, create_time, update_time
        from mw_email
        where is_used = 1
        limit 1
        """)
    EmailServer findUsed();

    @Insert("""
        insert into mw_email(name, sender_address, sender_name, sender_title_prefix, host, port, username, password, is_ssl, is_used, create_time, update_time)
        values(#{name}, #{senderAddress}, #{senderName}, #{senderTitlePrefix}, #{host}, #{port}, #{username}, #{password}, #{isSsl}, #{isUsed}, #{createTime}, #{updateTime})
        """)
    @Options(useGeneratedKeys = true, keyProperty = "emailId")
    int insert(EmailServer emailServer);

    @Update("""
        update mw_email
        set name = #{name},
            sender_address = #{senderAddress},
            sender_name = #{senderName},
            sender_title_prefix = #{senderTitlePrefix},
            host = #{host},
            port = #{port},
            username = #{username},
            password = #{password},
            is_ssl = #{isSsl},
            update_time = #{updateTime}
        where email_id = #{emailId}
        """)
    int update(EmailServer emailServer);

    @Update("""
        update mw_email
        set is_used = 0
        """)
    int clearUsed();

    @Update("""
        update mw_email
        set is_used = 1,
            update_time = unix_timestamp(now())
        where email_id = #{emailId}
        """)
    int markUsed(@Param("emailId") Integer emailId);

    @Update("""
        delete from mw_email
        where email_id = #{emailId}
        """)
    int deleteById(@Param("emailId") Integer emailId);
}
