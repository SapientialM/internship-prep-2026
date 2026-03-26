# GitHub 设置指南

首次推送到 GitHub 后，需要做一些设置才能启用自动部署。

---

## 1. 启用 GitHub Actions

### 步骤：

1. 打开仓库页面：https://github.com/SapientialM/internship-prep-2026

2. 点击顶部菜单的 **Actions** 标签

3. 你会看到提示："Workflows aren't being run on this forked repository"
   或者 "I understand my workflows, go ahead and enable them"

4. 点击 **"I understand my workflows, go ahead and enable them"**
   
   ![示意图](https://docs.github.com/assets/images/help/repository/enable-actions.png)

---

## 2. 配置 GitHub Pages

### 步骤：

1. 打开仓库 → **Settings** 标签（最右边）

2. 左侧菜单选择 **Pages**

3. 在 "Build and deployment" 部分：

   **Source** 选择：
   - ✅ **Deploy from a branch**（推荐）
   
   **Branch** 选择：
   - Branch: `gh-pages` 
   - Folder: `/(root)`
   
   然后点击 **Save**

   ![示意图](https://docs.github.com/assets/images/help/pages/choose-publish-source.png)

4. 如果 `gh-pages` 分支不存在，选择 **GitHub Actions** 方式：
   - Source: **GitHub Actions**
   
   这样 Actions 会自动创建分支并部署

---

## 3. 检查 Actions 权限

如果 Actions 运行失败，检查权限设置：

1. Settings → **Actions** → **General**

2. 在 "Workflow permissions" 部分，选择：
   - ✅ **Read and write permissions**
   - ✅ 勾选 "Allow GitHub Actions to create and approve pull requests"

3. 点击 **Save**

---

## 4. 手动触发部署

设置完成后，手动触发一次部署：

1. 打开仓库 → **Actions** 标签

2. 点击左侧的 **"Deploy Hexo to GitHub Pages"**

3. 点击右侧的 **"Run workflow"** 下拉按钮

4. 选择分支 `master`，然后点击 **"Run workflow"**

   ![示意图](https://docs.github.com/assets/images/help/actions/manual-run-workflow.png)

---

## 5. 等待部署完成

- 点击运行中的 workflow 可以查看详细日志
- 绿色 ✅ 表示部署成功
- 红色 ❌ 表示失败，点击查看错误信息

---

## 6. 访问网站

部署成功后，访问：

```
https://sapientialm.github.io/internship-prep-2026
```

---

## 常见问题

### Q: Actions 标签页没有显示 workflow？

**解决**：
1. 确认 `.github/workflows/pages.yml` 文件存在
2. 文件必须放在正确的路径，不能有错别字
3. 重新 push 一次代码触发检测：
   ```bash
   git commit --allow-empty -m "chore: trigger workflow"
   git push
   ```

### Q: 部署失败，提示 "Permission denied"？

**解决**：
1. Settings → Actions → General
2. Workflow permissions 选择 "Read and write permissions"
3. 保存后重新运行 workflow

### Q: gh-pages 分支没有自动创建？

**解决**：
使用 GitHub Actions 方式部署时，gh-pages 分支会自动创建。如果长时间没有创建：

1. 检查 Actions 日志是否有错误
2. 或者手动创建 gh-pages 分支：
   ```bash
   git checkout --orphan gh-pages
   git rm -rf .
   echo "Hello" > index.html
   git add index.html
   git commit -m "init"
   git push origin gh-pages
   git checkout master
   ```

### Q: 网站访问 404？

**解决**：
1. 确认仓库是 Public（Private 需要 GitHub Pro）
2. 确认 GitHub Pages 设置中的分支正确
3. 等待 5-10 分钟，DNS 生效需要时间

---

## 快速检查清单

- [ ] 启用 Actions（点击 "I understand..."）
- [ ] 配置 Pages（选择 gh-pages 分支或 GitHub Actions）
- [ ] 设置 Actions 权限（Read and write）
- [ ] 手动触发部署
- [ ] 等待部署完成（绿色✅）
- [ ] 访问网站验证

---

完成后就可以自动部署了！后续每次 push 到 master 都会自动触发部署。
