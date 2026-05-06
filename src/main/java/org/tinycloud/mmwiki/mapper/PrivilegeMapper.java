package org.tinycloud.mmwiki.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.tinycloud.mmwiki.domain.Privilege;

@Mapper
public interface PrivilegeMapper {

    @Select("""
        select privilege_id, name, parent_id, type, controller, action, icon, target, is_display, sequence, create_time, update_time
        from mw_privilege
        where is_display = 1
        order by sequence asc, privilege_id asc
        """)
    List<Privilege> findDisplayed();

    @Select("""
        select privilege_id, name, parent_id, type, controller, action, icon, target, is_display, sequence, create_time, update_time
        from mw_privilege
        order by sequence asc, privilege_id asc
        """)
    List<Privilege> findAllOrderBySequence();

    @Select("""
        select privilege_id, name, parent_id, type, controller, action, icon, target, is_display, sequence, create_time, update_time
        from mw_privilege
        where privilege_id = #{privilegeId}
        limit 1
        """)
    Privilege findById(@Param("privilegeId") Integer privilegeId);

    @Select("""
        select privilege_id, name, parent_id, type, controller, action, icon, target, is_display, sequence, create_time, update_time
        from mw_privilege
        where type = 'controller'
          and lower(controller) = lower(#{controller})
          and lower(action) = lower(#{action})
        limit 1
        """)
    Privilege findControllerPrivilege(@Param("controller") String controller, @Param("action") String action);

    @Select("""
        select count(*)
        from mw_privilege
        where parent_id = #{privilegeId}
        """)
    long countChildren(@Param("privilegeId") Integer privilegeId);

    @Insert("""
        insert into mw_privilege(name, parent_id, type, controller, action, icon, target, is_display, sequence, create_time, update_time)
        values(#{name}, #{parentId}, #{type}, #{controller}, #{action}, #{icon}, #{target}, #{isDisplay}, #{sequence}, #{createTime}, #{updateTime})
        """)
    @Options(useGeneratedKeys = true, keyProperty = "privilegeId")
    int insert(Privilege privilege);

    @Update("""
        update mw_privilege
        set name = #{name},
            parent_id = #{parentId},
            type = #{type},
            controller = #{controller},
            action = #{action},
            icon = #{icon},
            target = #{target},
            is_display = #{isDisplay},
            sequence = #{sequence},
            update_time = #{updateTime}
        where privilege_id = #{privilegeId}
        """)
    int update(Privilege privilege);

    @Delete("""
        delete from mw_privilege
        where privilege_id = #{privilegeId}
        """)
    int deleteById(@Param("privilegeId") Integer privilegeId);
}
