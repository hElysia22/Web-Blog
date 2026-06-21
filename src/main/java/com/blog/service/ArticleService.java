package com.blog.service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.entity.Article;
import com.blog.mapper.ArticleMapper;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;

@Service
public class ArticleService {
    @Resource
    private ArticleMapper articleMapper;

    // 发布文章
    public boolean add(Article article) {
        return articleMapper.insert(article) > 0;
    }

    // 查询所有文章（按时间倒序）
    public Page<Article> getArticlePage(int page, int pageSize) {
        // 构建分页对象 Page(当前页, 每页条数)
        Page<Article> pageObj = new Page<>(page, pageSize);
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<Article>()
                .orderByDesc(Article::getCreateTime);
        // mp自动分页，内部计算 offset=(page-1)*pageSize，limit分页
        return articleMapper.selectPage(pageObj, wrapper);
    }

    // ===================== 新增：根据ID查询文章（校验权限用） =====================
    public Article getById(Long id) {
        return articleMapper.selectById(id);
    }

    // ===================== 新增：删除文章 =====================
    public boolean deleteById(Long id) {
        return articleMapper.deleteById(id) > 0;
    }
}