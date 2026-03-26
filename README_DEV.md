# 🔧 开发者操作手册

> 本仓库的详细使用说明，面向仓库维护者（你自己）

---

## 📁 仓库结构说明

```
internship-prep-2026/
├── .github/
│   └── workflows/
│       └── pages.yml              # GitHub Actions 自动部署配置
├── docs/                           # 文档目录
│   └── how-to-use.md              # 详细使用指南
├── templates/                      # 模板文件目录
│   ├── knowledge-checklist-template.md    # 知识清单模板
│   ├── tech-note-template.md              # 技术笔记模板
│   ├── project-note-template.md           # 项目笔记模板
│   ├── interview-exp-template.md          # 面经记录模板
│   └── daily-record-template.md           # 日常记录模板
├── source/                         # Hexo 源文件目录
│   └── _posts/                    # 博客文章目录
│       ├── 知识清单/              # 知识清单分类
│       ├── 算法与数据结构/        # 算法相关
│       ├── 计算机网络/            # 网络相关
│       ├── 操作系统/              # OS相关
│       ├── 数据库/                # 数据库相关
│       ├── Java基础/              # Java基础
│       ├── Java并发/              # Java并发
│       ├── JVM/                   # JVM
│       ├── 框架-Spring/           # Spring框架
│       ├── 框架-中间件/           # 中间件
│       ├── 项目笔记/              # 项目经验
│       ├── 面经记录/              # 面经
│       └── 学习记录/              # 学习日志
├── themes/                         # Hexo 主题目录
├── _config.yml                     # Hexo 主配置
├── _config.next.yml                # NexT主题配置
├── package.json                    # Node.js 依赖
├── README.md                       # 给访问者看的 README
└── README_DEV.md                   # 本文件（开发者手册）
```

---

## 🚀 快速开始

### 1. 克隆仓库

```bash
git clone git@github.com:your-username/internship-prep-2026.git
cd internship-prep-2026
```

### 2. 安装依赖

```bash
# 使用 npm
npm install

# 或使用 yarn
yarn install

# 或使用 pnpm
pnpm install
```

### 3. 本地预览

```bash
# 启动本地服务器
npm run server

# 或带草稿预览
npm run dev
```

访问 http://localhost:4000 查看效果

### 4. 部署到 GitHub Pages

```bash
# 一键部署
npm run deploy
```

---

## 📝 如何使用模板创建新内容

### 模板说明

所有模板都在 `templates/` 目录下：

| 模板文件 | 用途 | 适用场景 |
|:---|:---|:---|
| `knowledge-checklist-template.md` | 知识清单 | 系统学习某个领域，追踪学习进度 |
| `tech-note-template.md` | 技术笔记 | 深入理解某个技术点，整理详细笔记 |
| `project-note-template.md` | 项目笔记 | 整理项目经验，准备项目介绍 |
| `interview-exp-template.md` | 面经记录 | 记录面试过程，复盘总结 |
| `daily-record-template.md` | 日常记录 | 每日学习打卡，时间管理 |

### 使用方式

#### 方式一：复制模板手动创建（推荐，配合Typora）

1. 打开 `templates/` 目录
2. 复制需要的模板文件到 `source/_posts/对应分类/`
3. 用 Typora 打开文件，填充内容
4. 修改文件名（去掉 `template`，使用有意义的标题）

例如：
```bash
# 创建一个HashMap的技术笔记
cp templates/tech-note-template.md "source/_posts/Java基础/HashMap源码解析.md"

# 用Typora打开编辑
typora "source/_posts/Java基础/HashMap源码解析.md"
```

#### 方式二：使用脚本快速创建

```bash
# 创建技术笔记
npm run new:tech "文章标题"

# 创建项目笔记  
npm run new:project "项目名称"

# 创建面经记录
npm run new:interview "公司-岗位"

# 创建知识清单
npm run new:checklist "知识点名称"
```

> 💡 提示：这些命令需要在后续添加 npm scripts 支持

---

## 📂 内容分类指南

### 1. 知识清单 (source/_posts/知识清单/)

**用途**: 系统学习某个领域前，先建立知识清单

**命名规范**: `XXX清单.md`

**示例**:
- `Java基础清单.md`
- `Java并发清单.md`
- `计算机网络清单.md`

**清单状态**:
- ⬜ **未开始**: 还没有开始学习的知识点
- 🟡 **了解中**: 正在学习，有了初步理解
- 🟠 **练习中**: 已理解概念，正在进行练习巩固
- 🟢 **已掌握**: 已经掌握，可以应对面试问题
- 🔵 **已复习**: 定期复习后确认记忆牢固

---

### 2. 技术笔记 (source/_posts/技术分类/)

**用途**: 深入学习某个技术点后的详细笔记

**命名规范**: 使用有意义的标题，如 `HashMap源码解析.md`

**分类目录**:
- `算法与数据结构/`: 算法题解、数据结构原理
- `计算机网络/`: HTTP/TCP/DNS等
- `操作系统/`: 进程线程、内存管理、文件系统等
- `数据库/`: MySQL、Redis等
- `Java基础/`: Java语言基础、集合框架等
- `Java并发/`: 多线程、并发包、锁等
- `JVM/`: JVM内存模型、GC、调优等
- `框架-Spring/`: Spring、Spring Boot、Spring Cloud
- `框架-中间件/`: MQ、ES、Dubbo等

---

### 3. 项目笔记 (source/_posts/项目笔记/)

**用途**: 整理项目经验，包含技术选型、遇到的问题、面试题等

**命名规范**: `项目名称.md`

**必须包含的内容**:
1. 项目概述（背景、功能、技术栈）
2. 技术亮点（2-3个，详细描述问题和解决方案）
3. 技术难点与解决方案
4. 项目中涉及的知识点清单（关联到技术笔记）
5. 项目相关面试题及答案

---

### 4. 面经记录 (source/_posts/面经记录/)

**用途**: 记录面试过程，方便复盘和复习

**命名规范**: `公司-岗位-第几面.md` 或 `公司-岗位-日期.md`

**示例**:
- `字节跳动-后端开发-一面.md`
- `阿里巴巴-Java开发-20240120.md`

**必须记录的内容**:
1. 面试基本信息（时间、岗位、形式）
2. 所有面试题目（包括追问）
3. 自己的回答（不管对不对都记录）
4. 标准答案参考
5. 表现自评和反思
6. 知识点查缺补漏清单

---

### 5. 学习记录 (source/_posts/学习记录/)

**用途**: 每日/每周学习打卡

**命名规范**: 
- 每日: `2024-01-20-学习记录.md`
- 每周: `2024-W03-周总结.md`

---

## 🎨 Typora 使用建议

### 推荐的 Typora 设置

**偏好设置 → 图像**:
- ✅ 复制图片到 `./assets/${filename}.assets`
- ✅ 优先使用相对路径

**偏好设置 → Markdown**:
- ✅ 代码块显示行号
- ✅ 启用斜体下划线

**偏好设置 → 编辑器**:
- 字体大小: 16px
- 行高: 1.6

### Typora 快捷键（常用）

| 功能 | Windows/Linux | Mac |
|:---|:---|:---|
| 加粗 | Ctrl+B | Cmd+B |
| 斜体 | Ctrl+I | Cmd+I |
| 代码块 | Ctrl+Shift+K | Cmd+Opt+C |
| 插入链接 | Ctrl+K | Cmd+K |
| 插入图片 | Ctrl+Shift+I | Cmd+Shift+I |
| 插入表格 | Ctrl+T | Cmd+Opt+T |
| 一级标题 | Ctrl+1 | Cmd+1 |
| 二级标题 | Ctrl+2 | Cmd+2 |
| 源代码模式 | Ctrl+/ | Cmd+/ |

---

## 🔧 Hexo 配置详解

### 部署分支配置

> 📖 详细配置说明请参考：[部署配置指南](./docs/deployment-guide.md)

我们支持多种部署方式：
- **方式1（默认）**: 部署到 `gh-pages` 分支 ⭐ 推荐
- **方式2**: 部署到 `main` 分支的 `docs/` 文件夹
- **方式3**: 部署到任意自定义分支

### 修改基本信息

编辑 `_config.yml`:

```yaml
# 站点信息
title: 你的笔记标题
subtitle: '副标题'
description: '站点描述'
author: '你的名字'

# URL（部署前必须修改）
url: https://your-username.github.io/internship-prep-2026

# 部署配置
deploy:
  type: git
  repo: git@github.com:your-username/internship-prep-2026.git
  branch: gh-pages
```

### 添加新文章

```bash
# 创建新文章
npx hexo new "文章标题"

# 创建草稿
npx hexo new draft "草稿标题"

# 发布草稿
npx hexo publish "草稿标题"
```

### 文章 Front-matter 格式

```yaml
---
title: 文章标题
date: 2024-01-20 14:00:00
tags: [标签1, 标签2]
categories: 分类名称
description: 文章简介（显示在首页）
---
```

---

## 🚀 部署流程

### 部署分支选择

GitHub Pages 支持多种部署方式，你可以根据需要选择：

#### 方式1: gh-pages 分支（默认推荐）

这是最常用的方式，源代码在 `main` 分支，生成的网站在 `gh-pages` 分支。

**优点**: 源代码和生成的网站完全分离
**配置**: 已默认启用，无需修改

```bash
# 自动部署
# 只需 push 到 main 分支，GitHub Actions 会自动部署到 gh-pages
git push origin main
```

#### 方式2: main 分支 + docs 文件夹

如果你想把网站部署在 `main` 分支的 `docs/` 文件夹中：

**步骤1**: 修改 `_config.yml`:
```yaml
public_dir: docs    # 默认是 public，改为 docs
deploy:
  type: git
  repo: git@github.com:your-username/internship-prep-2026.git
  branch: main      # 部署到 main 分支
```

**步骤2**: 在 GitHub 仓库设置中：
- Settings → Pages → Build and deployment
- Source: Deploy from a branch
- Branch: main → /docs

**步骤3**: 修改 `.github/workflows/pages.yml`，取消方案2的注释

#### 方式3: 其他自定义分支

如果你想部署到 `master`、`site` 等其他分支：

**修改 `.github/workflows/pages.yml`**:
```yaml
- name: Deploy to custom branch
  uses: peaceiris/actions-gh-pages@v3
  with:
    github_token: ${{ secrets.GITHUB_TOKEN }}
    publish_dir: ./public
    publish_branch: master    # 改为你想要的分支名
```

### 手动部署

如果你不想用 GitHub Actions，可以手动部署：

```bash
# 1. 先修改 _config.yml 中的 branch 为你想要的分支
deploy:
  type: git
  repo: git@github.com:your-username/internship-prep-2026.git
  branch: gh-pages    # 改为你的部署分支

# 2. 生成静态文件
npm run build

# 3. 部署
npm run deploy
```

### 自动部署（GitHub Actions）

已配置 `.github/workflows/pages.yml`，默认部署到 `gh-pages` 分支。

**如果你想部署到其他分支**，请按上面的"方式3"修改。

**触发方式**:
```bash
git add .
git commit -m "update: xxx"
git push origin main
```

然后到 GitHub 仓库页面 → Actions 查看部署进度。

### 部署后访问地址

根据你的部署方式，访问地址可能不同：

| 部署方式 | 访问地址 |
|:---|:---|
| gh-pages 分支 | `https://your-username.github.io/internship-prep-2026` |
| main/docs 文件夹 | `https://your-username.github.io/internship-prep-2026` |
| 其他情况 | 看仓库 Settings → Pages 中的提示 |

---

## 📋 工作流建议

### 每日学习流程

```
1. 查看今日学习计划（知识清单）
   ↓
2. 使用 Typora 创建/编辑学习笔记
   - 如果是新知识点，复制 tech-note-template.md
   - 如果是复习，更新知识清单状态
   ↓
3. 本地预览确认
   npm run server
   ↓
4. 提交到GitHub
   git add . && git commit -m "add: xxx笔记" && git push
   ↓
5. GitHub Actions 自动部署
```

### 面试后流程

```
1. 面试结束后立即记录（使用 interview-exp-template.md）
   - 记录所有问题
   - 记录自己的回答（凭记忆）
   ↓
2. 当天复盘
   - 查找标准答案
   - 评估表现
   - 列出知识点查缺补漏清单
   ↓
3. 针对性学习
   - 创建新的技术笔记
   - 或更新知识清单状态
   ↓
4. 提交更新
```

### 项目整理流程

```
1. 项目开发过程中记录关键问题
   ↓
2. 项目结束后整理（使用 project-note-template.md）
   - 技术亮点（必须量化）
   - 技术难点
   - 涉及的知识点
   ↓
3. 准备项目介绍话术
   ↓
4. 准备可能的面试题和答案
   ↓
5. 关联到面经记录（这个项目被问过的问题）
```

---

## 🐛 常见问题

### Q1: 本地预览正常，部署后样式丢失？

检查 `_config.yml` 中的 `url` 和 `root` 配置是否正确。

### Q2: 如何修改主题颜色和样式？

编辑 `_config.next.yml` 文件，或者创建 `source/_data/styles.styl` 自定义样式。

### Q3: 如何添加评论功能？

编辑 `_config.next.yml`，配置 comments 部分（支持 Gitalk、Valine 等）。

### Q4: Typora 插入的图片在网页上显示不了？

确保图片使用相对路径，推荐设置 Typora 自动复制图片到 `文章名.assets` 文件夹。

### Q5: 如何添加数学公式支持？

编辑 `_config.next.yml`:
```yaml
math:
  every_page: true
  mathjax:
    enable: true
```

---

## 📚 推荐阅读

### 算法与数据结构
- 《算法导论》
- 《剑指Offer》
- LeetCode  Hot 100

### Java
- 《Java核心技术》
- 《深入理解Java虚拟机》
- 《Java并发编程实战》

### 系统设计
- 《设计数据密集型应用》
- 《大型网站技术架构》

### 面试准备
- 《程序员面试金典》
- 牛客网面经

---

## 📝 更新日志

| 日期 | 更新内容 |
|:---|:---|
| 2024-01-01 | 初始化仓库，创建基础结构 |
| 2024-01-10 | 添加项目笔记模板和示例 |
| 2024-01-20 | 添加面经记录示例 |

---

## 🤝 给自己打打气 💪

> 面试准备是一场马拉松，不是短跑。
> 
> 每天坚持一点点，积累起来就是巨大的进步。
> 
> 加油！你可以的！🎉

---

如有问题或建议，欢迎随时优化本手册！
