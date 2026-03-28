#!/bin/bash

# 初始化脚本 - 安装依赖和主题

echo "🚀 开始初始化项目..."

# 1. 安装 Node.js 依赖
echo "📦 安装 Node.js 依赖..."
npm install

# 2. 安装 Hexo CLI（如果还没安装）
echo "🔧 检查 Hexo CLI..."
if ! command -v hexo &> /dev/null; then
    echo "安装 Hexo CLI..."
    npm install -g hexo-cli
fi

# 3. 安装 NexT 主题
echo "🎨 安装 NexT 主题..."
if [ ! -d "themes/next" ]; then
    mkdir -p themes
    cd themes
    git clone https://github.com/next-theme/hexo-theme-next.git next
    cd ..
else
    echo "NexT 主题已存在，更新到最新版本..."
    cd themes/next
    git pull
    cd ../..
fi

# 4. 复制主题配置（如果不存在）
if [ ! -f "_config.next.yml" ]; then
    echo "📝 复制主题配置文件..."
    cp themes/next/_config.yml _config.next.yml
fi

# 5. 创建必要的目录
echo "📁 创建必要的目录..."
mkdir -p source/_posts
mkdir -p source/_drafts
mkdir -p source/about
mkdir -p source/categories
mkdir -p source/tags

# 6. 显示完成信息
echo ""
echo "✅ 初始化完成！"
echo ""
echo "📝 接下来请："
echo "   1. 修改 _config.yml 中的个人信息"
echo "   2. 修改 _config.next.yml 中的主题配置"
echo "   3. 运行 'npm run server' 启动本地预览"
echo ""
echo "🚀 开始你的学习之旅吧！"
