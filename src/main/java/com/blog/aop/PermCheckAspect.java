package com.blog.aop;
import com.blog.annotation.RequirePerm;
import com.blog.entity.LoginUser;
import com.blog.mapper.UserMapper;
import com.blog.util.JwtUtil;
import com.blog.util.UserContextUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Set;

@Aspect
@Component
public class PermCheckAspect {
    @Resource
    private JwtUtil jwtUtil;
    @Resource
    private UserMapper userMapper;

    @Around("@annotation(requirePerm)")
    public Object checkPerm(ProceedingJoinPoint point, RequirePerm requirePerm) throws Throwable{

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String auth = request.getHeader("Authorization");
        LoginUser loginUser = null;

        if(auth != null && auth.startsWith("Bearer ")){
            String token = auth.substring(7);

            if(jwtUtil.validateToken(token)){
                Long userId = jwtUtil.extractId(token);
                Boolean isAdmin = jwtUtil.extractIsAdmin(token);
                Set<String> perms = userMapper.selectUserPerms(userId);
                Set<String> roles = userMapper.selectUserRoles(userId);
                loginUser = new LoginUser();
                loginUser.setUserId(userId);
                loginUser.setUsername(jwtUtil.extractUsername(token));
                loginUser.setPerms(perms);
                loginUser.setRoles(roles);
                loginUser.setIsAdmin(isAdmin);
                UserContextUtil.setLoginUser(loginUser);
            }
        }else {
            throw new RuntimeException("请先登录");
        }

        // 接口需要的权限
        String needPerm = requirePerm.value();
        // 判断用户权限集合是否包含所需权限
        if(!loginUser.getPerms().contains(needPerm)){
            throw new RuntimeException("权限不足，禁止访问");
        }
        // 权限通过，执行接口
        return point.proceed();
    }
}
