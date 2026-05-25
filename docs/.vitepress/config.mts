import { defineConfig } from 'vitepress'

export default defineConfig({
  base: '/internship-prep-2026/',
  lang: 'zh-CN',
  title: '2026暑期实习备战笔记',
  description: '知识清单 | 技术笔记 | 项目笔记 | 面经记录',
  head: [
    ['link', { rel: 'icon', href: '/internship-prep-2026/favicon.ico' }],
  ],

  themeConfig: {
    logo: false,
    search: {
      provider: 'local',
      options: {
        translations: {
          button: { buttonText: '搜索文档', buttonAriaLabel: '搜索文档' },
          modal: {
            noResultsText: '无法找到相关结果',
            resetButtonTitle: '清除查询条件',
            footer: {
              selectText: '选择',
              navigateText: '切换',
              closeText: '关闭',
            },
          },
        },
      },
    },

    nav: [
      { text: '首页', link: '/' },
      { text: '八股文', link: '/bagu/' },
      { text: '项目笔记', link: '/projects/' },
      { text: '手写笔记', link: '/handwritten/' },
      { text: '杂谈', link: '/misc/' },
    ],

    sidebar: {
      '/bagu/': [
        {
          text: '八股文',
          items: [
            { text: '总览', link: '/bagu/' },
            { text: '01 - JavaSE', link: '/bagu/01-javase' },
            { text: '02 - JUC并发', link: '/bagu/02-juc' },
            { text: '03 - JVM', link: '/bagu/03-jvm' },
            { text: '04 - MySQL', link: '/bagu/04-mysql' },
            { text: '05 - Redis', link: '/bagu/05-redis' },
            { text: '06 - Kafka', link: '/bagu/06-kafka' },
            { text: '07 - Spring', link: '/bagu/07-spring' },
            { text: '08 - 计算机网络', link: '/bagu/08-network' },
            { text: '09 - 操作系统', link: '/bagu/09-os' },
            { text: '10 - 分布式', link: '/bagu/10-distributed' },
            { text: '11 - 问题排查', link: '/bagu/11-troubleshooting' },
            { text: '12 - 场景题', link: '/bagu/12-scenarios' },
            { text: '13 - 数据结构与算法', link: '/bagu/13-datastructures' },
            { text: '14 - 分库分表', link: '/bagu/14-sharding' },
            { text: '15 - 面试手撕算法', link: '/bagu/15-coding' },
            { text: '16 - 智力题', link: '/bagu/16-brain-teasers' },
            { text: '17 - AI大模型', link: '/bagu/17-ai-llm' },
          ],
        },
      ],
      '/projects/': [
        {
          text: '项目笔记',
          items: [
            { text: '总览', link: '/projects/' },
            { text: 'IM即时通讯项目', link: '/projects/im-project' },
            { text: 'RAG AI面试助手', link: '/projects/rag-project' },
          ],
        },
      ],
      '/handwritten/': [
        {
          text: '手写笔记',
          items: [
            { text: '总览', link: '/handwritten/' },
            { text: '手写动态代理', link: '/handwritten/dynamic-proxy' },
            { text: '手写ArrayList和LinkedList', link: '/handwritten/arraylist-linkedlist' },
          ],
        },
      ],
      '/misc/': [
        {
          text: '杂谈',
          items: [
            { text: 'ArrayList vs LinkedList', link: '/misc/' },
          ],
        },
      ],
    },

    outline: {
      level: [2, 3],
      label: '本页目录',
    },

    docFooter: {
      prev: '上一章',
      next: '下一章',
    },

    lastUpdated: {
      text: '最后更新于',
      formatOptions: { dateStyle: 'medium', timeStyle: 'short' },
    },

    editLink: {
      pattern: 'https://github.com/SapientialM/internship-prep-2026/edit/master/docs/:path',
      text: '在 GitHub 上编辑此页',
    },

    socialLinks: [
      { icon: 'github', link: 'https://github.com/SapientialM/internship-prep-2026' },
    ],
  },

  markdown: {
    lineNumbers: true,
    container: {
      tipLabel: '提示',
      warningLabel: '注意',
      dangerLabel: '警告',
      infoLabel: '信息',
      detailsLabel: '详细说明',
    },
  },
})
