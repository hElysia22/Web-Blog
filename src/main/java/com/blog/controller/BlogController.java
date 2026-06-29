package com.blog.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.annotation.RequirePerm;
import com.blog.entity.Article;
import com.blog.entity.LoginUser;
import com.blog.entity.User;
import com.blog.service.ArticleService;
import com.blog.service.UserService;
import com.blog.util.JwtUtil;
import com.blog.util.UserContextUtil;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class BlogController {
    @Resource
    private UserService userService;
    @Resource
    private ArticleService articleService;
    @Resource
    private JwtUtil jwtUtil;
    // 注册
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody User user) {
        Map<String, Object> res = new HashMap<>();
        try {
            boolean ok = userService.register(user);
            res.put("success", ok);
            res.put("msg", ok ? "注册成功" : "用户名已存在");
        } catch (Exception e) {
            res.put("success", false);
            res.put("msg", "后端报错：" + e.getMessage());
            e.printStackTrace();
        }
        return res;
    }

    // 登录
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody User user) {
        Map<String, Object> res = new HashMap<>();
        User loginUser = userService.login(user.getUsername(), user.getPassword());
        if(loginUser!=null)
        {
            Map<String, Object> tokenMap = new HashMap<>();
            tokenMap.put("username", loginUser.getUsername());
            tokenMap.put("id", loginUser.getId());
            tokenMap.put("isAdmin", loginUser.getIsAdmin());
            String token = jwtUtil.generateToken(tokenMap);

            res.put("success", true);
            res.put("token", token);
        }else {
            res.put("success", false);
            res.put("msg", "账号或密码错误");
        }
        return res;
    }

    @PostMapping("/upload")
    public Map<String, Object> upload(@RequestParam("file") MultipartFile file,
                                      @RequestHeader("Authorization") String token)
    {
        Map<String, Object> res = new HashMap<>();
        if(token == null || !jwtUtil.validateToken(token))
        {
            res.put("success", false);
            res.put("msg", "请先登录");
            return res;
        }
        String uploadPath = "D:/upload/";
        File filePath = new File(uploadPath);
        if(!filePath.exists())
        {
            filePath.mkdirs();
        }

        String originalFileName = file.getOriginalFilename();
        if(file.isEmpty() || !originalFileName.contains("."))
        {
            res.put("success", false);
            res.put("msg", "未上传文件 || 文件名要带后缀");
            return res;
        }
        //后缀
        String suffix = originalFileName.substring(originalFileName.lastIndexOf('.'));
        String newFileName = UUID.randomUUID() + suffix;
        File targetPath = new File(uploadPath + newFileName);
        try{
            file.transferTo(targetPath);
            res.put("success", true);
            res.put("msg", "/upload/" + newFileName);
            return res;
        }catch (IOException e){
            res.put("success", false);
            res.put("msg", e.getMessage());
            return res;
        }
    }

    // 发布文章
    @PostMapping("/article/add")
    @RequirePerm("article:publish")
    public Map<String, Object> addArticle(
            @RequestBody Article article) {

        Map<String, Object> res = new HashMap<>();
        LoginUser loginUser = UserContextUtil.getLoginUser();
        article.setUserId(loginUser.getUserId());
        article.setCreateTime(LocalDateTime.now());
        res.put("success", articleService.add(article));
        return res;
    }

    // 文章列表
    @GetMapping("/article/list")
    public Map<String, Object> list(
            // 默认第1页，每页5条，不传参自动赋值
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int pageSize
    ) {
        Map<String, Object> res = new HashMap<>();
        // 调用分页查询，传入页码、页大小
        Page<Article> pageData = articleService.getArticlePage(page, pageSize);

        res.put("success", true);
        // 前端需要的分页结构 records列表 / pages总页数 / current当前页
        Map<String, Object> data = new HashMap<>();
        data.put("records", pageData.getRecords());  // 当前页文章数据
        data.put("pages", pageData.getPages());      // 总页数
        data.put("current", pageData.getCurrent()); // 当前页码
        data.put("total", pageData.getTotal());      // 总条数  暂未使用
        data.put("size", pageData.getSize());        // 每页条数  暂未使用
        res.put("data", data);
        return res;
    }

    @PutMapping("/article/edit/{artId}")
    @RequirePerm("article:edit")
    public  Map<String, Object> editArticle(
            @RequestBody Article article,
            @PathVariable Long artId){

        Map<String,Object> res = new HashMap<>();

        LoginUser loginUser = UserContextUtil.getLoginUser();
        Article dbArticle = articleService.getById(artId);
        if(dbArticle == null)
        {
            res.put("success", false);
            res.put("msg", "文章不存在");
            return res;
        }
        if(dbArticle.getUserId() != loginUser.getUserId())
        {
            res.put("success", false);
            res.put("msg", "非作者无法修改");
            return res;
        }
        article.setId(artId);
        res.put("success", articleService.edit(article));
        return res;
    }

    @DeleteMapping("/article/delete/{id}")
    @RequirePerm("article:remove")
    public Map<String, Object> deleteArticle(
            @PathVariable Long id) {

        Map<String, Object> res = new HashMap<>();

        LoginUser loginUser = UserContextUtil.getLoginUser();
        Long userId = loginUser.getUserId();
        Article article = articleService.getById(id);
        if (article == null) {
            res.put("success", false);
            res.put("msg", "文章不存在");
            return res;
        }

        if(loginUser.getIsAdmin())
        {
            boolean flag = articleService.deleteById(id);
            res.put("success", flag);
            res.put("msg", flag ? "删除成功" : "删除失败");
            return res;
        }

        // 直接对比数字类型
        if (!article.getUserId().equals(userId)) {
            res.put("success", false);
            res.put("msg", "无权限：只能删除自己的文章");
            return res;
        }

        boolean flag = articleService.deleteById(id);
        res.put("success", flag);
        res.put("msg", flag ? "删除成功" : "删除失败");
        return res;
    }
}