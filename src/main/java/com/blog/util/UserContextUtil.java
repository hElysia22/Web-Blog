package com.blog.util;

import com.blog.entity.LoginUser;

public class UserContextUtil {
    private static final ThreadLocal<LoginUser> LOCAL = new ThreadLocal<>();

    public static void setLoginUser(LoginUser loginUser)
    {
        LOCAL.set(loginUser);
    }

    public static LoginUser getLoginUser()
    {
        return LOCAL.get();
    }

    public static void clear()
    {
        LOCAL.remove();
    }
}
