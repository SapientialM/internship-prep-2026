# 详细使用指南

本指南详细介绍如何使用本仓库记录和整理你的技术知识。

---

## 目录

1. [仓库概述](#仓库概述)
2. [首次使用](#首次使用)
3. [日常使用](#日常使用)
4. [内容创建指南](#内容创建指南)
5. [部署与分享](#部署与分享)

---

## 仓库概述

这是一个基于 Hexo + GitHub Pages 的技术知识管理仓库，帮助你：

- 📋 系统化学习技术知识，追踪学习进度
- 📝 记录详细的技术笔记，方便复习
- 💼 整理项目经验，准备面试
- 🎤 记录面经，复盘总结
- 🌐 通过 GitHub Pages 在线分享

---

## 首次使用

### 1. 初始化仓库

```bash
# 1. 克隆仓库到本地
git clone git@github.com:your-username/internship-prep-2026.git
cd internship-prep-2026

# 2. 安装 Node.js 依赖
npm install

# 3. 安装 Hexo CLI（如果还没安装）
npm install -g hexo-cli
```

### 2. 配置个人信息

编辑以下文件：

**`_config.yml`**:
```yaml
title: 你的笔记标题
subtitle: '副标题'
description: '站点描述'
author: '你的名字'
url: https://your-username.github.io/internship-prep-2026

# 部署配置
deploy:
  type: git
  repo: git@github.com:your-username/internship-prep-2026.git
  branch: gh-pages
```

**`_config.next.yml`**:
```yaml
social:
  GitHub: https://github.com/your-username || fab fa-github
  # 添加其他社交链接
```

### 3. 启动本地预览

```bash
npm run server
```

访问 http://localhost:4000 查看效果。

---

## 日常使用

### 启动本地服务器

```bash
# 基础命令
npm run server

# 或带草稿预览
npm run dev

# 或简写
hexo s
hexo s --drafts
```

### 创建新内容

#### 创建知识清单

1. 复制模板：
```bash
cp templates/knowledge-checklist-template.md "source/_posts/知识清单/XXX清单.md"
```

2. 用 Typora 打开编辑，填充知识点

3. 更新表格中的进度

#### 创建技术笔记

1. 复制模板：
```bash
cp templates/tech-note-template.md "source/_posts/分类/文章标题.md"
```

2. 填充内容：
   - 问题/知识点概述
   - 详细内容（原理、代码、图示）
   - 常见问题
   - 相关资料

#### 创建项目笔记

1. 复制模板：
```bash
cp templates/project-note-template.md "source/_posts/项目笔记/项目名称.md"
```

2. 重点填写：
   - 技术亮点（必须量化！）
   - 技术难点和解决方案
   - 项目相关面试题

#### 创建面经记录

1. 面试结束后立即创建：
```bash
cp templates/interview-exp-template.md "source/_posts/面经记录/公司-岗位-日期.md"
```

2. 记录所有问题和自己的回答

3. 当天复盘，补充标准答案

### 提交更新

```bash
# 添加所有修改
git add .

# 提交（使用有意义的提交信息）
git commit -m "add: HashMap源码解析笔记"
git commit -m "update: Java基础清单进度"
git commit -m "add: 字节跳动一面面经"

# 推送到 GitHub
git push origin main
```

推送后会自动触发 GitHub Actions 部署。

---

## 内容创建指南

### Markdown 语法速查

#### 标题
```markdown
# 一级标题
## 二级标题
### 三级标题
```

#### 文本样式
```markdown
**加粗**
*斜体*
~~删除线~~
`行内代码`
```

#### 代码块
```markdown
```java
public class Hello {
    public static void main(String[] args) {
        System.out.println("Hello World");
    }
}
```
```

#### 列表
```markdown
- 无序列表项
- 无序列表项
  - 子项

1. 有序列表项
2. 有序列表项
```

#### 表格
```markdown
| 表头1 | 表头2 |
|:---:|:---:|
| 内容1 | 内容2 |
| 内容3 | 内容4 |
```

#### 链接和图片
```markdown
[链接文字](https://example.com)
![图片描述](./path/to/image.png)
```

#### 引用
```markdown
> 引用文字
> 多行引用
```

### 表情符号速查

常用表情：
- ⭐ 重要程度
- ✅ 完成 / ❌ 未完成
- 📅 日期 / 🔄 更新
- 📁 文件 / 🔗 链接
- 💡 提示 / ⚠️ 警告
- 🎯 目标 / 📊 统计
- 📝 笔记 / 📖 文档

### Typora 图片处理

推荐设置：

**文件 → 偏好设置 → 图像**

1. 插入图片时... → 选择「复制图片到指定路径」
2. 勾选「优先使用相对路径」
3. 路径设置为 `./assets/${filename}.assets`

这样每篇文章的图片会保存在 `文章名.assets/` 目录下，便于管理。

---

## 部署与分享

### 选择部署方式

本仓库支持多种部署方式：

1. **gh-pages 分支（默认）**: 源代码和网站分开，最常用 ⭐推荐
2. **main 分支 + docs 文件夹**: 源代码和网站在同一个分支  
3. **自定义分支**: 灵活配置

详细配置请参考：[部署配置指南](./deployment-guide.md)

### 自动部署

默认已配置 GitHub Actions，每次 push 到 main 分支会自动部署到 `gh-pages` 分支。

**如果你想部署到其他分支**，需要修改 `.github/workflows/pages.yml` 文件。

部署状态查看：
- 仓库页面 → Actions 标签
- 绿色 ✅ 表示部署成功
- 红色 ❌ 表示部署失败

### 部署后的 URL

```
https://your-username.github.io/internship-prep-2026
```

### 分享给他人

直接分享上述 URL 即可，他人可以看到你整理的所有笔记。

### 手动部署（备用）

如果自动部署失败，可以手动部署：

```bash
# 生成静态文件
npm run build

# 部署
npm run deploy
```

> ⚠️ 注意：手动部署需要有 gh-pages 分支的写入权限。

---

## 进阶配置

### 自定义主题样式

创建 `source/_data/styles.styl`：

```stylus
// 修改主色调
$brand-primary = #1abc9c

// 修改字体大小
$font-size-base = 16px

// 自定义样式
.post-body {
  line-height: 1.8;
}
```

### 添加评论功能

1. 注册 [Gitalk](https://github.com/settings/applications/new) 或 [Valine](https://valine.js.org/)

2. 编辑 `_config.next.yml`：

```yaml
comments:
  active: gitalk  # 或 valine

gitalk:
  enable: true
  github_id: your-github-id
  repo: internship-prep-2026
  client_id: your-client-id
  client_secret: your-client-secret
```

### 添加网站统计

编辑 `_config.next.yml`：

```yaml
# 百度统计
baidu_analytics: your-baidu-analytics-key

# Google Analytics
google_analytics: your-ga-tracking-id
```

---

## 常见问题 FAQ

### Q: 如何修改网站图标？

准备 `favicon.ico` 文件，放到 `source/` 目录下。

### Q: 如何添加友链？

编辑 `_config.next.yml`：

```yaml
links:
  朋友博客: https://friend-blog.com
```

### Q: 如何禁用某个页面的评论？

在文章 Front-matter 中添加：
```yaml
comments: false
```

### Q: 如何设置文章置顶？

在文章 Front-matter 中添加：
```yaml
sticky: 100  # 数字越大优先级越高
```

### Q: 本地图片在网站上显示不了？

检查：
1. 图片路径是否为相对路径
2. 图片是否在 `source/` 目录下
3. 文件名是否包含特殊字符

---

## 学习建议

### 每日流程

1. **早上**：查看今日学习计划（知识清单）
2. **学习时**：用 Typora 记录笔记
3. **学习后**：更新知识清单状态
4. **晚上**：提交今日更新到 GitHub

### 每周复盘

1. 检查知识清单完成进度
2. 复习之前学过的内容
3. 规划下周学习计划

### 面试准备

1. 整理项目笔记，准备项目介绍
2. 复习知识清单中的重点内容
3. 查看之前的面经，总结常问问题

---

## 相关链接

- [Hexo 文档](https://hexo.io/zh-cn/docs/)
- [NexT 主题文档](https://theme-next.js.org/docs/)
- [GitHub Pages 文档](https://docs.github.com/zh/pages)
- [Markdown 语法](https://www.markdownguide.org/)

---

祝你学习顺利！🎉
