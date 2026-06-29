package com.blog.entity;

import lombok.Data;

import java.util.Set;

@Data
public class LoginUser {
    private Long userId;
    private String username;
    private Set<String> Perms;
    private Set<String> roles;
    private Boolean isAdmin;
}
