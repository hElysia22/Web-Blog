package com.blog.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Set;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    // 根据用户id查询该用户所有权限标识
    @Select("SELECT DISTINCT p.perm_code FROM sys_user_role ur " +
            "LEFT JOIN sys_role_permission rp ON ur.role_id = rp.role_id " +
            "LEFT JOIN sys_permission p ON rp.perm_id = p.id " +
            "WHERE ur.user_id = #{userId}")
    Set<String> selectUserPerms(@Param("userId") Long userId);

    // 根据用户id查询角色编码
    @Select("SELECT DISTINCT r.role_code FROM sys_user_role ur " +
            "LEFT JOIN sys_role r ON ur.role_id = r.id " +
            "WHERE ur.user_id = #{userId}")
    Set<String> selectUserRoles(@Param("userId") Long userId);
}