#!/usr/bin/env node

/**
 * 快速创建新文章的脚本
 * 用法: node scripts/new-post.js <type> <title>
 * type: tech | project | interview | checklist | daily
 */

const fs = require('fs');
const path = require('path');

const templates = {
  tech: {
    template: 'tech-note-template.md',
    dir: 'source/_posts/Java基础'
  },
  project: {
    template: 'project-note-template.md',
    dir: 'source/_posts/项目笔记'
  },
  interview: {
    template: 'interview-exp-template.md',
    dir: 'source/_posts/面经记录'
  },
  checklist: {
    template: 'knowledge-checklist-template.md',
    dir: 'source/_posts/知识清单'
  },
  daily: {
    template: 'daily-record-template.md',
    dir: 'source/_posts/学习记录'
  }
};

function main() {
  const args = process.argv.slice(2);
  
  if (args.length < 2) {
    console.log('用法: node scripts/new-post.js <type> <title>');
    console.log('');
    console.log('类型:');
    console.log('  tech      - 技术笔记');
    console.log('  project   - 项目笔记');
    console.log('  interview - 面经记录');
    console.log('  checklist - 知识清单');
    console.log('  daily     - 日常记录');
    console.log('');
    console.log('示例:');
    console.log('  node scripts/new-post.js tech "HashMap源码解析"');
    console.log('  node scripts/new-post.js interview "字节跳动-后端-一面"');
    process.exit(1);
  }
  
  const [type, ...titleParts] = args;
  const title = titleParts.join(' ');
  
  const config = templates[type];
  if (!config) {
    console.error(`错误: 未知的类型 "${type}"`);
    console.error('可用类型: tech, project, interview, checklist, daily');
    process.exit(1);
  }
  
  // 读取模板
  const templatePath = path.join(__dirname, '..', 'templates', config.template);
  if (!fs.existsSync(templatePath)) {
    console.error(`错误: 模板文件不存在 ${templatePath}`);
    process.exit(1);
  }
  
  let content = fs.readFileSync(templatePath, 'utf-8');
  
  // 替换模板变量
  const date = new Date().toISOString().slice(0, 10);
  content = content.replace(/\{\{date\}\}/g, date);
  content = content.replace(/\{\{[^}]+\}\}/g, (match) => {
    // 保留一些模板变量，只替换日期
    if (match === '{{date}}') return date;
    return match;
  });
  
  // 生成文件名
  const filename = title.replace(/[\/\\?%*:|"<>]/g, '-') + '.md';
  const outputPath = path.join(__dirname, '..', config.dir, filename);
  
  // 检查文件是否已存在
  if (fs.existsSync(outputPath)) {
    console.error(`错误: 文件已存在 ${outputPath}`);
    process.exit(1);
  }
  
  // 写入文件
  fs.writeFileSync(outputPath, content, 'utf-8');
  
  console.log('✅ 创建成功!');
  console.log(`📄 文件: ${outputPath}`);
  console.log(`📝 标题: ${title}`);
  console.log('');
  console.log('提示: 使用 Typora 打开编辑');
  console.log(`  typora "${outputPath}"`);
}

main();
