# GitHub Pages 部署配置指南

本仓库支持多种部署方式，你可以根据自己的需求选择。

---

## 📋 部署方式对比

| 方式 | 部署分支 | 适用场景 | 复杂度 |
|:---|:---|:---|:---:|
| [方式1](#方式1-gh-pages分支-推荐) | `gh-pages` | 个人博客/笔记，最常用 | ⭐ |
| [方式2](#方式2-main分支--docs文件夹) | `main` + `/docs` | 希望源代码和网站在一起 | ⭐⭐ |
| [方式3](#方式3-其他自定义分支) | 自定义 | 特殊需求 | ⭐⭐ |

---

## 方式1: gh-pages 分支（推荐）

这是最经典的方式，也是本仓库默认配置：

- **源代码**存放在 `main` 分支
- **生成的网站**存放在 `gh-pages` 分支

### 配置步骤

1. **确认 GitHub 设置**
   - 进入仓库 → Settings → Pages
   - Source: Deploy from a branch
   - Branch: `gh-pages` → `/(root)`

2. **确认 workflow 文件**
   文件 `.github/workflows/pages.yml` 中默认配置就是部署到 `gh-pages`：
   ```yaml
   - name: Deploy to gh-pages branch
     uses: peaceiris/actions-gh-pages@v3
     with:
       github_token: ${{ secrets.GITHUB_TOKEN }}
       publish_dir: ./public
       publish_branch: gh-pages
   ```

3. **推送代码**
   ```bash
   git push origin main
   ```

4. **查看部署状态**
   - 仓库 → Actions 标签
   - 等待部署完成
   - 访问 `https://your-username.github.io/internship-prep-2026`

---

## 方式2: main 分支 + docs 文件夹

这种方式把生成的网站放在 `main` 分支的 `docs/` 文件夹中。

### 适用场景

- 希望源代码和生成的网站在同一个分支
- 不想维护多个分支
- 想直接在 GitHub 上查看生成的文件

### 配置步骤

#### 1. 修改 Hexo 配置

编辑 `_config.yml`：

```yaml
# 修改输出目录
public_dir: docs    # 原来是 public

# 修改部署分支
deploy:
  type: git
  repo: git@github.com:your-username/internship-prep-2026.git
  branch: main      # 部署到 main 分支
```

#### 2. 修改 GitHub Actions

编辑 `.github/workflows/pages.yml`，注释掉方式1，启用方式2：

```yaml
# 注释掉或删除这个
# - name: Deploy to gh-pages branch
#   uses: peaceiris/actions-gh-pages@v3
#   ...

# 启用这个
- name: Deploy to main branch (docs folder)
  uses: peaceiris/actions-gh-pages@v3
  if: github.ref == 'refs/heads/main'
  with:
    github_token: ${{ secrets.GITHUB_TOKEN }}
    publish_dir: ./docs      # 对应上面的 public_dir: docs
    publish_branch: main     # 部署到 main 分支
    destination_dir: docs    # 部署到 docs 子目录
    keep_files: true         # 保留其他文件
```

#### 3. 修改 .gitignore

因为 `docs/` 现在是部署目录，不能忽略：

```bash
# 从 .gitignore 中删除或注释掉这一行
# docs/
# 或者
# public/
```

改为：
```
# Hexo 输出目录（根据你的配置决定是否忽略）
# 如果用方式1（gh-pages分支），保持忽略 public/
# 如果用方式2（main分支），注释掉 public/ 或改为 docs/
public/
# docs/    # 如果用方式2，注释掉这行
```

#### 4. 配置 GitHub Pages

- Settings → Pages → Build and deployment
- Source: Deploy from a branch
- Branch: `main` → `/docs`

#### 5. 推送代码

```bash
git add .
git commit -m "chore: 配置部署到main分支的docs文件夹"
git push origin main
```

---

## 方式3: 其他自定义分支

如果你想部署到 `master`、`site`、`blog` 等其他分支。

### 配置步骤

#### 1. 修改 GitHub Actions

编辑 `.github/workflows/pages.yml`：

```yaml
- name: Deploy to custom branch
  uses: peaceiris/actions-gh-pages@v3
  with:
    github_token: ${{ secrets.GITHUB_TOKEN }}
    publish_dir: ./public
    publish_branch: master    # 改为你想要的分支名，如 master/site/blog
```

#### 2. 配置 GitHub Pages（如果走分支部署）

- Settings → Pages → Build and deployment
- Source: Deploy from a branch
- Branch: 选择你的分支 → `/(root)`

或者如果你用 GitHub Actions，选择：
- Source: GitHub Actions

#### 3. 推送代码

```bash
git push origin main
```

---

## 🔧 手动部署（不使用 GitHub Actions）

如果你更喜欢手动控制部署过程：

### 1. 配置 Hexo

编辑 `_config.yml`：

```yaml
deploy:
  type: git
  repo: git@github.com:your-username/internship-prep-2026.git
  branch: gh-pages    # 改为你想要的分支
```

### 2. 部署命令

```bash
# 生成并部署
npm run deploy

# 或者分开执行
npm run build      # 生成静态文件
hexo deploy        # 部署
```

### 3. 首次部署需要配置

如果是第一次部署，可能需要配置 git：

```bash
# 配置 git 用户信息（如果还没配置）
git config --global user.name "Your Name"
git config --global user.email "your-email@example.com"

# 确保有 SSH key 或者使用 HTTPS 并输入密码
```

---

## 🌐 自定义域名（可选）

如果你想用自己的域名（如 `notes.yourdomain.com`）：

### 1. 配置 DNS

在你的域名服务商处添加 CNAME 记录：
```
主机记录: notes
记录类型: CNAME
记录值: your-username.github.io
```

### 2. 添加 CNAME 文件

创建 `source/CNAME` 文件：
```
notes.yourdomain.com
```

### 3. 配置 GitHub Pages

- Settings → Pages → Custom domain
- 填入 `notes.yourdomain.com`
- 勾选 Enforce HTTPS

### 4. 重新部署

```bash
git add .
git commit -m "chore: 添加自定义域名"
git push
```

---

## ❓ 常见问题

### Q: 部署后网站 404？

检查以下几点：
1. 仓库是否设置为 Public（Private 需要 Pro 才能用 Pages）
2. Settings → Pages 中的分支配置是否正确
3. URL 是否正确：`https://username.github.io/repo-name`

### Q: 部署成功但样式错乱？

检查 `_config.yml` 中的 `url` 和 `root` 配置：

```yaml
# 如果是项目页面（username.github.io/repo-name）
url: https://username.github.io/internship-prep-2026
root: /internship-prep-2026/

# 如果是用户页面（username.github.io）
url: https://username.github.io
root: /
```

### Q: 如何部署到 Gitee/Coding 等其他平台？

修改 `_config.yml` 中的 deploy 配置：

```yaml
deploy:
  type: git
  repo: 
    github: git@github.com:username/repo.git,gh-pages
    gitee: git@gitee.com:username/repo.git,master
```

### Q: Actions 部署失败？

检查 Actions 日志，常见问题：
1. 权限不足 → 检查 Settings → Actions → General → Workflow permissions
2. 主题安装失败 → 检查网络或手动安装主题
3. 构建错误 → 本地先运行 `npm run build` 看是否有错

---

## 📝 总结

| 需求 | 推荐方式 |
|:---|:---|
| 简单快速开始 | 方式1: gh-pages（默认）|
| 单分支管理 | 方式2: main + docs |
| 公司/团队要求 | 方式3: 自定义分支 |

不确定选哪个？**直接用默认的 gh-pages 分支方式就好！**
