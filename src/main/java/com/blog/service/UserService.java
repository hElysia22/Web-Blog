package com.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.blog.entity.LoginUser;
import com.blog.entity.User;
import com.blog.mapper.UserMapper;
import com.blog.util.UserContextUtil;
import jdk.nashorn.internal.runtime.Debug;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Set;

@Service
public class UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private PasswordEncoder passwordEncoder;

    // 注册
    public boolean register(User user) {
        if (user == null || user.getUsername() == null || user.getUsername().trim().isEmpty()
                || user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            return false;
        }
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", user.getUsername());

        User exist = userMapper.selectOne(wrapper);
        if (exist != null) {
            return false; // 已存在
        }
        //密码加密
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreateTime(LocalDateTime.now());

        //  插入数据库
        return userMapper.insert(user) > 0;
    }

    // 登录
    public User login(String username, String password) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username);

        User dbUser = userMapper.selectOne(wrapper);
        if(dbUser != null && passwordEncoder.matches(password, dbUser.getPassword()))
        {
            return dbUser;
        }

        return null;
    }
}