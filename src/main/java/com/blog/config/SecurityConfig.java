package com.blog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf().disable();

        // 所有请求全部放行，不做任何登录拦截
        http.authorizeRequests()
                .anyRequest().permitAll();

        // 关掉自带登录弹窗、表单登录
        http.formLogin().disable();
        http.httpBasic().disable();

        return http.build();
    }
}