# 目录结构说明

```
internship-prep-2026/                          # 项目根目录
│
├── .github/                                    # GitHub 配置
│   └── workflows/                             
│       └── pages.yml                          # GitHub Actions 自动部署配置
│
├── .gitignore                                  # Git 忽略文件配置
│
├── LICENSE                                     # 开源协议 (MIT)
│
├── README.md                                   # 给访问者看的说明文档
│
├── README_DEV.md                               # 开发者操作手册（详细使用指南）
│
├── DIRECTORY_STRUCTURE.md                      # 本文件，目录结构说明
│
├── _config.yml                                 # Hexo 主配置文件
│
├── _config.next.yml                            # NexT 主题配置文件
│
├── package.json                                # Node.js 依赖和脚本
│
├── docs/                                       # 文档目录
│   └── how-to-use.md                          # 详细使用指南
│
├── scripts/                                    # 辅助脚本
│   ├── init.sh                                # 初始化脚本
│   └── new-post.js                            # 快速创建文章脚本
│
├── templates/                                  # 模板文件目录
│   ├── knowledge-checklist-template.md        # 知识清单模板
│   ├── tech-note-template.md                  # 技术笔记模板
│   ├── project-note-template.md               # 项目笔记模板
│   ├── interview-exp-template.md              # 面经记录模板
│   └── daily-record-template.md               # 日常记录模板
│
├── themes/                                     # Hexo 主题目录
│   └── .gitkeep                               # 保持目录（主题通过 npm 安装）
│
└── source/                                     # Hexo 源文件目录
    │
    ├── _data/                                  # Hexo 数据文件
    │
    ├── _posts/                                 # 博客文章目录（核心内容）
    │   │
    │   ├── 知识清单/                           # 知识清单分类
    │   │   ├── README.md                      # 知识清单总览
    │   │   └── Java基础清单.md                # 示例清单
    │   │
    │   ├── 算法与数据结构/                     # 算法相关笔记
    │   │
    │   ├── 计算机网络/                         # 计算机网络笔记
    │   │
    │   ├── 操作系统/                           # 操作系统笔记
    │   │
    │   ├── 数据库/                             # 数据库笔记
    │   │
    │   ├── Java基础/                           # Java基础笔记
    │   │   └── HashMap源码解析.md             # 示例笔记
    │   │
    │   ├── Java并发/                           # Java并发笔记
    │   │
    │   ├── JVM/                                # JVM笔记
    │   │
    │   ├── 框架-Spring/                        # Spring框架笔记
    │   │
    │   ├── 框架-中间件/                         # 中间件笔记
    │   │
    │   ├── 项目笔记/                           # 项目经验
    │   │   └── 分布式电商系统.md              # 示例项目
    │   │
    │   ├── 面经记录/                           # 面经记录
    │   │   └── 示例-字节跳动-后端开发-一面.md # 示例面经
    │   │
    │   └── 学习记录/                           # 学习日志
    │
    ├── about/                                  # 关于页面
    │   └── index.md
    │
    ├── categories/                             # 分类页面
    │   └── index.md
    │
    └── tags/                                   # 标签页面
        └── index.md
```

---

## 核心目录说明

### `templates/` - 模板文件

存放各种内容的模板，新建内容时从这里复制：

| 模板文件 | 用途 | 输出位置 |
|:---|:---|:---|
| `knowledge-checklist-template.md` | 知识清单 | `source/_posts/知识清单/` |
| `tech-note-template.md` | 技术笔记 | `source/_posts/对应分类/` |
| `project-note-template.md` | 项目笔记 | `source/_posts/项目笔记/` |
| `interview-exp-template.md` | 面经记录 | `source/_posts/面经记录/` |
| `daily-record-template.md` | 日常记录 | `source/_posts/学习记录/` |

### `source/_posts/` - 核心内容

所有笔记、文章都在这里，按分类组织：

```
_posts/
├── 知识清单/          # 用来追踪学习进度
├── 算法与数据结构/    # 算法学习笔记
├── 计算机网络/        # 网络协议笔记
├── 操作系统/          # OS笔记
├── 数据库/            # MySQL、Redis等
├── Java基础/          # Java语言基础
├── Java并发/          # 并发编程
├── JVM/               # JVM原理
├── 框架-Spring/       # Spring全家桶
├── 框架-中间件/        # MQ、ES等
├── 项目笔记/          # 项目经验
├── 面经记录/          # 面试记录
└── 学习记录/          # 每日学习打卡
```

### `scripts/` - 辅助脚本

| 脚本 | 用途 |
|:---|:---|
| `init.sh` | 初始化项目，安装依赖和主题 |
| `new-post.js` | 快速创建新文章 |

---

## 文件命名规范

### 知识清单
```
XXX清单.md
例如：Java基础清单.md、计算机网络清单.md
```

### 技术笔记
```
有意义的标题.md
例如：HashMap源码解析.md、TCP三次握手详解.md
```

### 项目笔记
```
项目名称.md
例如：分布式电商系统.md、秒杀系统.md
```

### 面经记录
```
公司-岗位-第几面.md
或：公司-岗位-日期.md

例如：字节跳动-后端开发-一面.md
     阿里巴巴-Java开发-20240120.md
```

---

## 快速导航

- **开始编写**: 复制 `templates/` 中的模板到 `source/_posts/对应分类/`
- **本地预览**: `npm run server`
- **部署上线**: `git push`（自动部署）或 `npm run deploy`（手动部署）
- **详细文档**: 查看 `README_DEV.md` 和 `docs/how-to-use.md`
