---



title: RAG项目
date: 2026-03-27 00:00:00
tags: [项目, RAG, AI, 大模型]
categories: 项目笔记
---

# RAG项目（AI 面试助手）

<!-- more -->

## 概述

每个项目笔记包含：

1. **全局认知：3分钟讲清项目全貌**
   1. 业务架构，从用户痛点到解决方案
   2. 技术架构，分层结构到数据流向
   3. 电梯演讲，业务价值+技术亮点+我的认知）
2. **模块拆解：看着项目前端说清楚系统内部如何协作**
   1. 模块依赖，讲清楚核心模块以及它们之间的调用关系
   2. 接口清单，列举关键方法，清楚入参/出参/异常
   3. 时序图，讲清楚核心场景的完整调用链
3. **代码深入：解释关键设计的决策**
   1. 精读配置类（线程池/超时/连接池参数及选型理由）
   2. 精读核心算法（一致性哈希/ID生成/幂等实现）
   3. 精读异常处理（降级/重试/雪崩防护策略）
4. **改造验证：证明动手能力和抗压场景思路**
   1. 测试幂等、降级逻辑
   2. 换数据库或缓存模式，对比差异
   3. 压力测试找瓶颈+准备"如果QPS翻10倍"的扩容方案和应对方案
   4. 功能测试，如果让你添加某个功能，你如何改动
5. **知识体系：形成方法论**
   1. 横向对比同类系统，分析优劣
   2. 准备灵魂对比，最大难点/重新开发怎么改/相比竞品的优势

## 全局认知

### 业务架构

**用户痛点：**

- **建立质量难把控**， **ToC** 求职者不清楚自己简历的问题，**ToB** HR 筛选简历效率低
- **面试准备无针对性**，通用面试题与实际简历不匹配，缺乏个性化练习
- **技术文档检索难**，企业/项目内部知识分散，查询答案耗时（这里可以把简历分析报告联动到RAG里去）

**解决方案：AI 面试助手**

- 简历管理（多格式上传、AI分析评分、改进建议、PDF报告导出）
- 模拟面试（AI 智能出题（**可改进为基于面经出题**）、实时问答交互、答案智能评估、面试报告生成）
- 知识库问答（文档上传与向量化、RAG检索增强、多轮对话问答、SSE流式响应）

### 技术架构

{% asset_img image-20260329113356381.png %}

**核心数据流向**

| 场景         | 数据流向                                                     | 关键技术               |
| ------------ | ------------------------------------------------------------ | ---------------------- |
| 简历上传分析 | 上传 → 文件解析(Tika) → MinIO存储 → Stream入队 → AI分析 → 结果入库 | 异步解耦、文件Hash去重 |
| 模拟面试     | 创建会话(Redis缓存) → AI出题 → 答题 → 分批评估 → 生成报告    | 分批处理防Token溢出    |
| RAG问答      | Query向量化 → pgvector相似度检索 → 上下文构建 → LLM生成 → SSE流式返回 | RAG检索增强            |

**电梯演讲**

开场（10秒）

>  "我独立开发了一个智能 AI 面试助手平台，帮助求职者和 HR 解决简历评估与面试准备的痛点。"

  业务价值（50秒）

>  "平台提供三大核心能力：
>
> 1. 简历智能分析 — 上传简历后 AI 自动评分并给出改进建议，支持 PDF 报告导出
> 2. 个性化模拟面试 — 基于简历内容生成针对性面试题，支持多轮追问和答案评估
> 3. RAG 知识库问答 — 技术文档向量化存储，实现检索增强的智能问答
>
>  解决的核心痛点是：简历质量难把控、面试准备缺乏针对性、技术文档检索困难。"

  技术亮点（90秒）

> "技术选型上采用 Spring Boot 4.0 + Java 21 + Spring AI 2.0 构建后端，前端用 React + TypeScript。四个核心亮点：
>
> 1. 异步解耦设计 — 简历分析、知识库向量化等耗时操作采用 Redis Stream 异步处理，避免 AI 调用阻塞用户请求，支持任务失败自动重试
> 2. RAG 检索增强 — 基于 pgvector 实现向量检索，无需引入额外向量数据库；动态调整 TopK 和相似度阈值，自动扩展提高召回率
> 3. 流式交互体验 — 基于 SSE 实现打字机效果的流式响应，减少用户等待焦虑
> 4. 稳定性保障 — 面试评估采用分批处理策略（每批8题），规避大模型 Token 溢出风险；注解式限流组件防止 API 滥用"

  个人认知（30秒）

>  "通过这个项目，我对 AI 应用架构 有了体系化认知：从 Prompt 工程到 RAG 优化，从异步任务设计到流式响应实现。最深刻的体会是 'AI 能力需要工程化封装' —— 大模型只是基础，真正交付价值需要完善的错误处理、状态管理和用户体验设计。"


  💡 面试 Tips

  1. 如果面试官追问"项目来源" → 个人向的ToC，简历筛选向的ToB
  2. 准备几个数字：

    • 向量维度：1024 维（text-embedding-v3）
    • 面试评估分批：每批 8 题
    • 支持格式：PDF/DOCX/DOC/TXT/MD
  3. 可以提到的扩展点：

    • 支持对接 Ollama 本地部署（成本优化）
    • 面试会话支持断点续面（Redis 缓存 24h）
    • 文件 Hash 去重避免重复处理

## 模块拆解

### 总览

```java
  ┌─────────────────────────────────────────────────────────────────────────────┐
  │                              智能 AI 面试助手平台                              │
  ├─────────────────┬─────────────────┬─────────────────┬───────────────────────┤
  │   📄 简历管理    │   🎤 模拟面试    │   📚 知识库     │      ⚙️ 系统设置        │
  ├─────────────────┼─────────────────┼─────────────────┼───────────────────────┤
  │  Resume Module  │ Interview Module│Knowledge Module │   Settings Module     │
  ├─────────────────┴─────────────────┴─────────────────┴───────────────────────┤
  │                            🤖 RAG 问答模块 (跨模块)                           │
  │                              RAG Chat Module                                │
  └─────────────────────────────────────────────────────────────────────────────┘
```

- 简历管理模块 — 文件上传、解析、AI分析、异步处理
- 模拟面试模块 — 会话管理、智能出题、答案评估、分批处理
- 知识库+RAG模块 — 文档向量化、向量检索、多轮对话
- 系统设置模块 — 多AI平台配置、限流组件

### 简历管理模块

#### **定位**

- 上传解析：多格式支持、文件验证、重复检测、文本提取
- AI分析：智能评分、改进建议、异步处理、失败重试
- 历史管理：列表查询、详情查看、报告PDF导出、删除管理
- 支持格式：PDF / DOCX / DOC / TXT / MD
- 文件限制：最大 10MB

#### **流程（重点）**

{% asset_img image-20260329143053847.png %}

我来将这个文件内容整理为Markdown格式：

#### **关键技术实现**（面试重点 ★★★）

1. 文件解析 —— Apache Tika

```java
/**
 * Tika 解析优化点（解决了什么问题）
 */
private String parseContent(InputStream inputStream) throws ... {
    AutoDetectParser parser = new AutoDetectParser();
    
    // 1. 限制提取文本长度（防止超大文件内存溢出）
    BodyContentHandler handler = new BodyContentHandler(MAX_TEXT_LENGTH);
    
    // 2. 禁用嵌入文档解析（关键！避免提取PDF图片引用和临时文件路径）
    context.set(EmbeddedDocumentExtractor.class, new NoOpEmbeddedDocumentExtractor());
    
    // 3. PDF 专用优化：按位置排序文本，改善多栏布局解析顺序
    PDFParserConfig pdfConfig = new PDFParserConfig();
    pdfConfig.setSortByPosition(true);
    context.set(PDFParserConfig.class, pdfConfig);
    
    parser.parse(inputStream, handler, metadata, context);
    return handler.toString();
}
```

**面试话术：** "使用 Apache Tika 实现多格式文档解析时，我遇到了一个生产环境问题——PDF 中的图片被解析成了临时文件路径。解决方案是自定义 NoOpEmbeddedDocumentExtractor 禁用嵌入资源解析，只提取纯文本，同时设置 SortByPosition 优化多栏布局的文本顺序。"

2. 异步处理 —— Redis Stream

```java
// 生产者：发送任务
@Component
public class AnalyzeStreamProducer {
    public void sendAnalyzeTask(Long resumeId, String content, ...) {
        Map<String, String> message = new HashMap<>();
        message.put("resumeId", resumeId.toString());
        message.put("content", content);
        // ...
        redisService.streamAdd(RESUME_ANALYZE_STREAM_KEY, message, STREAM_MAX_LEN);
    }
}

// 消费者：继承抽象类，专注业务逻辑
@Component
public class AnalyzeStreamConsumer extends AbstractStreamConsumer<AnalyzePayload> {
    
    @Override
    protected void processBusiness(AnalyzePayload payload) {
        // 1. 检查简历是否被删除
        if (!resumeRepository.existsById(payload.resumeId())) {
            return; // 优雅处理：简历已删除则跳过
        }
        
        // 2. 调用 AI 分析
        ResumeAnalysisResponse analysis = gradingService.analyzeResume(...);
        
        // 3. 保存结果
        persistenceService.saveAnalysis(resume, analysis);
    }
    
    @Override
    protected void retryMessage(AnalyzePayload payload, int retryCount) {
        // 失败重试：重新入队，最多3次
        if (retryCount < MAX_RETRY_COUNT) {
            // 重新发送任务到 Stream
        }
    }
}
```

#### **为什么选择** Redis Stream 而不是 MQ？

- **项目规模：** 不需要引入 Kafka/RabbitMQ 重量级组件
- **功能足够：** 支持持久化、消费者组、ACK 确认
- **架构精简：** 复用已有的 Redis 基础设施

3. 限流保护 —— Redis + Lua 滑动窗口

```java
@RestController
public class ResumeController {
    
    @PostMapping("/api/resumes/upload")
    @RateLimit(
        dimensions = {RateLimit.Dimension.GLOBAL, RateLimit.Dimension.IP},
        count = 5,           // 5次
        interval = 1,
        timeUnit = TimeUnit.MINUTES  // 每1分钟
    )
    public Result<?> uploadAndAnalyze(...) { ... }
}
```

#### **Lua** 脚本核心逻辑（滑动窗口算法）：

```lua
-- 清理窗口外的旧记录
redis.call('ZREMRANGEBYSCORE', key, 0, window_start)

-- 统计当前窗口内的请求数
local current_count = redis.call('ZCARD', key)

-- 判断是否超过限制
if current_count >= limit then
    return 0  -- 拒绝
end

-- 记录本次请求
redis.call('ZADD', key, timestamp, unique_id)
redis.call('EXPIRE', key, ttl)
return 1  -- 允许
```

#### **面试亮点**：

1. **滑动窗口 vs 固定窗口：** 避免窗口边界突发流量问题
2. **Redis + Lua：** 保证原子性，避免竞态条件
3. **多维度组合：** 同时支持全局 + IP + 用户级限流
4. **Hash Tag：** `{className:methodName}` 确保 Cluster 模式下同一 Slot

4. AI 分析 —— 结构化 Prompt + 重试机制

```java
@Service
public class ResumeGradingService {
    
    public ResumeAnalysisResponse analyzeResume(String resumeText, ...) {
        // 1. 加载 Prompt 模板
        String systemPrompt = systemPromptTemplate.render();
        
        // 2. 组装变量
        Map<String, Object> variables = new HashMap<>();
        variables.put("resumeText", resumeText);
        variables.put("extraInfo", buildExtraInfo(resumeType, targetPosition));
        
        String userPrompt = userPromptTemplate.render(variables);
        
        // 3. 添加结构化输出格式（关键！）
        String systemPromptWithFormat = systemPrompt + "\n\n" + outputConverter.getFormat();
        
        // 4. 调用 AI（带重试）
        ResumeAnalysisResponseDTO dto = structuredOutputInvoker.invoke(
            chatClient,
            systemPromptWithFormat,
            userPrompt,
            outputConverter,  // BeanOutputConverter 自动映射到 DTO
            ErrorCode.RESUME_ANALYSIS_FAILED,
            "简历分析",
            log
        );
        
        return convertToResponse(dto);
    }
}
```

#### **Prompt** 设计要点：

- **System Prompt：** 定义角色（资深 HR）、评分标准（5维度）、输出格式（JSON Schema）
- **User Prompt：** 简历内容 + 补充信息（校招/社招、目标岗位）
- **结构化输出：** 使用 BeanOutputConverter 强制 AI 返回 JSON，自动映射到 Java 对象

5. 重复检测 —— 文件 Hash

```java
@Service
public class ResumePersistenceService {
    
    public Optional<ResumeEntity> findExistingResume(MultipartFile file) {
        // 计算文件 MD5 Hash
        String fileHash = calculateFileHash(file);
        
        // 查询数据库
        return resumeRepository.findByFileHash(fileHash);
    }
    
    private String calculateFileHash(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            return DigestUtils.md5Hex(is);
        }
    }
}
```

**业务价值：** 避免用户重复上传相同简历，浪费 AI 调用成本（每次调用都计费）。

#### **面试重点与话术**

1. 高频问题

| 问题                       | 回答要点                                                     |
| -------------------------- | ------------------------------------------------------------ |
| "为什么 AI 分析要异步？"   | AI 调用耗时 3-10 秒，同步会阻塞用户请求；异步提升用户体验，支持失败重试；削峰填谷，避免瞬时高并发压垮 AI 服务 |
| "如何保证分析不丢失？"     | Redis Stream 持久化；消费者 ACK 确认；失败自动重试（最多3次）；支持手动重试 API |
| "限流是怎么实现的？"       | Redis + Lua 滑动窗口；原子操作避免竞态；多维度组合（全局+IP）；注解式使用，零侵入 |
| "文件解析遇到过什么问题？" | PDF 图片解析为临时路径问题 → 禁用嵌入资源解析；多栏布局顺序混乱 → SortByPosition；超大文件内存问题 → 限制 5MB |
| "重复上传怎么处理？"       | MD5 Hash 去重；返回历史分析结果；避免重复调用 AI 节约成本    |

2. 亮点数据（面试加分）

**技术细节：**

- **文件解析：** Apache Tika 支持 5+ 种格式
- **限流算法：** 滑动时间窗口，精度毫秒级
- **重试机制：** 最多 3 次，指数退避
- **文本限制：** 最大提取 5MB 文本（防止内存溢出）
- **文件限制：** 最大 10MB

3. 可能的追问

**Q: 如果 AI 服务挂了怎么办？**

> "Consumer 会捕获异常，标记任务为 FAILED，并保存错误信息。用户可以在前端看到失败状态，点击'重新分析'按钮手动触发重试。同时我们预留了降级策略的扩展点，可以对接本地 Ollama 模型作为备用。"

**Q: 上传大文件卡住怎么办？**

> "前端有上传进度条；后端文件验证优先检查大小（10MB限制），超大会立即返回错误，不会进入后续流程；Tika 解析设置 5MB 文本上限，防止超大文件导致 OOM。"

**Q: 如何防止用户刷接口？**

> "上传接口有 @RateLimit 注解，限制每个 IP 每分钟最多 5 次。使用 Redis + Lua 实现滑动窗口，相比固定窗口更平滑，避免窗口边界突发流量。"

#### **总结图**

```
┌─────────────────────────────────────────────────────────────────┐
│                      简历管理模块技术栈                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│   文件处理        Apache Tika + 自定义解析优化                     │
│   存储层          MinIO (S3兼容) + PostgreSQL                     │
│   缓存/队列       Redis (Hash缓存 + Stream消息队列)                │
│   AI 调用         Spring AI + 结构化 Prompt                      │
│   限流保护        Redis + Lua 滑动窗口                            │
│   任务调度        Redis Stream Consumer 异步消费                 │
│                                                                 │
│   可靠性设计:                                                    │
│   ✓ 文件 Hash 去重    ✓ 异步任务状态机    ✓ 失败自动重试          │
│   ✓ 手动重试 API     ✓ 注解式限流        ✓ 优雅异常处理          │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```



### 模拟面试

#### 总览

```markd
 ┌─────────────────────────────────────────────────────────────────┐
  │                      模拟面试模块职责边界                         │
  ├─────────────────────────────────────────────────────────────────┤
  │                                                                 │
  │   ┌─────────────┐   ┌─────────────┐   ┌─────────────┐          │
  │   │   会话管理   │   │   题目生成   │   │   答案评估   │          │
  │   ├─────────────┤   ├─────────────┤   ├─────────────┤          │
  │   │ • 创建会话   │   │ • AI 智能出题│   │ • 分批评估   │          │
  │   │ • 断点续面   │   │ • 追问机制   │   │ • 汇总报告   │          │
  │   │ • 状态流转   │   │ • 去重策略   │   │ • 参考答案   │          │
  │   │ • 提前交卷   │   │ • 默认兜底   │   │ • PDF导出   │          │
  │   └─────────────┘   └─────────────┘   └─────────────┘          │
  │                                                                 │
  │   核心状态：CREATED → IN_PROGRESS → COMPLETED → EVALUATED        │
  │   缓存策略：Redis 24h TTL，支持断点续面                           │
  └─────────────────────────────────────────────────────────────────┘
```

#### 流程（重点）

{% asset_img image-20260329192156865.png %}

#### 关键技术实现（面试重点 ★★★）

#### **会话管理** —— Redis 缓存优先 + 数据库持久化

```java
@Service
public class InterviewSessionService {

    public InterviewSessionDTO createSession(CreateInterviewRequest request) {
        // 1. 检查是否有未完成的会话（断点续面）
        Optional<InterviewSessionDTO> unfinishedOpt = findUnfinishedSession(request.resumeId());
        if (unfinishedOpt.isPresent()) {
            return unfinishedOpt.get(); // 直接返回已有会话
        }

        // 2. AI 生成面试问题
        List<InterviewQuestionDTO> questions = questionService.generateQuestions(
            request.resumeText(),
            request.questionCount(),
            historicalQuestions  // 去重：避免重复提问
        );

        // 3. 双写策略：Redis + PostgreSQL
        sessionCache.saveSession(sessionId, resumeText, resumeId, questions, 0, CREATED);
        persistenceService.saveSession(sessionId, resumeId, questions.size(), questions);

        return new InterviewSessionDTO(...);
    }

    /**
     * 获取会话：缓存优先，支持从数据库恢复
     */
    public InterviewSessionDTO getSession(String sessionId) {
        // 1. 先查 Redis
        Optional<CachedSession> cachedOpt = sessionCache.getSession(sessionId);
        if (cachedOpt.isPresent()) {
            return toDTO(cachedOpt.get());
        }

        // 2. 缓存未命中，从数据库恢复（支持断点续面）
        CachedSession restoredSession = restoreSessionFromDatabase(sessionId);
        return toDTO(restoredSession);
    }
}
```

**面试话术：**

> "面试会话采用 '缓存优先 + 双写策略'。所有状态变更先写 Redis（24h TTL），异步同步到数据库。这样做有两个好处：
> 1. 性能：答题交互毫秒级响应，不依赖数据库
> 2. 断点续面：即使 Redis 过期，也能从数据库恢复会话，用户体验不中断"

#### **智能出题** —— 分类配比 + 历史去重 + 追问机制

```java
@Service
public class InterviewQuestionService {

    // 问题类型权重分配（面试重点！）
    private static final double PROJECT_RATIO = 0.20;      // 20% 项目经历
    private static final double MYSQL_RATIO = 0.20;        // 20% MySQL
    private static final double REDIS_RATIO = 0.20;        // 20% Redis
    private static final double JAVA_BASIC_RATIO = 0.10;   // 10% Java基础
    private static final double JAVA_COLLECTION_RATIO = 0.10; // 10% 集合
    private static final double JAVA_CONCURRENT_RATIO = 0.10; // 10% 并发

    public List<InterviewQuestionDTO> generateQuestions(String resumeText, int questionCount,
                                                       List<String> historicalQuestions) {
        // 1. 计算各类问题数量
        QuestionDistribution distribution = calculateDistribution(questionCount);

        // 2. 组装 Prompt，传入历史问题避免重复
        Map<String, Object> variables = new HashMap<>();
        variables.put("historicalQuestions", String.join("\n", historicalQuestions));
        variables.put("projectCount", distribution.project);
        variables.put("mysqlCount", distribution.mysql);
        // ...

        // 3. AI 生成问题
        QuestionListDTO dto = structuredOutputInvoker.invoke(...);

        // 4. 转换并添加追问（线性追问流）
        return convertToQuestions(dto);
    }

    private List<InterviewQuestionDTO> convertToQuestions(QuestionListDTO dto) {
        List<InterviewQuestionDTO> questions = new ArrayList<>();
        for (QuestionDTO q : dto.questions()) {
            // 主问题
            questions.add(InterviewQuestionDTO.create(index++, q.question(), type, category, false, null));

            // 追问（默认1条，最多2条）
            List<String> followUps = sanitizeFollowUps(q.followUps());
            for (int i = 0; i < followUps.size(); i++) {
                questions.add(InterviewQuestionDTO.create(
                    index++,
                    followUps.get(i),
                    type,
                    buildFollowUpCategory(category, i + 1),
                    true,           // isFollowUp = true
                    mainQuestionIndex  // 关联主问题
                ));
            }
        }
        return questions;
    }

    /**
     * 兜底策略：AI 失败时返回默认问题
     */
    private List<InterviewQuestionDTO> generateDefaultQuestions(int count) {
        String[][] defaultQuestions = {
            {"请介绍一下你在简历中提到的最重要的项目...", "PROJECT", "项目经历"},
            {"MySQL的索引有哪些类型？B+树索引的原理是什么？", "MYSQL", "MySQL"},
            // ...
        };
        return convertDefaultToDTOs(defaultQuestions, count);
    }
}
```

**面试亮点：**

1. **分类配比**：根据后端面试重点，按权重分配各类问题数量
2. **历史去重**：传入历史问题列表，Prompt 中明确要求 "不要重复以下历史问题"
3. **追问机制**：每个主问题配置 followUpCount 条追问，构建线性追问流
4. **优雅降级**：AI 调用失败时返回预设默认问题，保证服务可用性

#### **答案评估** —— 分批处理规避 Token 溢出（核心亮点 ★★★）

```java
@Service
public class AnswerEvaluationService {

    @Value("${app.interview.evaluation.batch-size:8}")
    private int evaluationBatchSize;  // 默认每批8题

    /**
     * 分批评估核心逻辑
     */
    public InterviewReportDTO evaluateInterview(String sessionId, String resumeText,
                                               List<InterviewQuestionDTO> questions) {
        // 1. 分批评估（关键！避免单次 Token 超限）
        List<BatchEvaluationResult> batchResults = evaluateInBatches(sessionId, resumeSummary, questions);

        // 2. 合并各批次评估结果
        List<QuestionEvaluationDTO> mergedEvaluations = mergeQuestionEvaluations(batchResults);
        String fallbackOverallFeedback = mergeOverallFeedback(batchResults);

        // 3. 二次汇总（让 AI 基于分批结果生成整体评价）
        FinalSummaryDTO finalSummary = summarizeBatchResults(
            sessionId, resumeSummary, questions,
            mergedEvaluations, fallbackOverallFeedback, ...
        );

        return convertToReport(sessionId, mergedEvaluations, questions, finalSummary);
    }

    private List<BatchEvaluationResult> evaluateInBatches(...) {
        List<BatchEvaluationResult> results = new ArrayList<>();

        // 每批 evaluationBatchSize 题（默认8题）
        for (int start = 0; start < questions.size(); start += evaluationBatchSize) {
            int end = Math.min(start + evaluationBatchSize, questions.size());
            List<InterviewQuestionDTO> batchQuestions = questions.subList(start, end);

            // 调用 AI 评估这一批
            EvaluationReportDTO report = evaluateBatch(sessionId, resumeSummary, batchQuestions, start, end);
            results.add(new BatchEvaluationResult(start, end, report));
        }
        return results;
    }

    private EvaluationReportDTO evaluateBatch(...) {
        // 构建 Prompt，只包含本批次的问答记录
        String qaRecords = buildQARecords(batchQuestions);

        // 调用 AI
        return structuredOutputInvoker.invoke(
            aiChatClientFactory.getChatClient(),
            systemPromptWithFormat,
            userPrompt,
            outputConverter,
            ...
        );
    }
}
```

**面试话术：**

> "答案评估最大的挑战是 Token 溢出。假设一份简历有 20 道题，每道题的回答平均 500 字，加上简历本身和 Prompt，总 Token 可能超过 8k，超出模型限制。
> 我的解决方案是 '分批评估 + 二次汇总'：
> 1. 将问题按每批 8 题分组，每批独立调用 AI 评估
> 2. 合并各批次的评估结果
> 3. 再调用一次 AI 对整体表现进行汇总（strengths、improvements、overallFeedback）
> 这样即使 50 道题也能稳定评估，同时保证报告的连贯性。"

#### **会话缓存设计** —— Redis 数据结构

**Redis Key 设计：**

```
┌─────────────────────────────────────────────────────────────────┐
│  interview:session:{sessionId}                                  │
│  ├─ Hash 存储 CachedSession（序列化为 JSON）                      │
│  │   ├─ sessionId: "abc123"                                     │
│  │   ├─ resumeText: "简历文本..."                                │
│  │   ├─ questionsJson: "[{question: '...', answer: '...'}, ...]"│
│  │   ├─ currentIndex: 5                                         │
│  │   └─ status: "IN_PROGRESS"                                   │
│  └─ TTL: 24小时                                                  │
├─────────────────────────────────────────────────────────────────┤
│  interview:resume:{resumeId}                                    │
│  ├─ String 存储 sessionId（用于快速查找未完成会话）                 │
│  └─ TTL: 24小时                                                  │
└─────────────────────────────────────────────────────────────────┘
```

**断点续面实现：**

```java
@Service
public class InterviewSessionCache {

    /**
     * 查找未完成的会话（支持从数据库恢复）
     */
    public Optional<String> findUnfinishedSessionId(Long resumeId) {
        // 1. 先从 Redis 查找
        String sessionId = redisService.get(RESUME_SESSION_KEY_PREFIX + resumeId);
        if (sessionId != null) {
            return Optional.of(sessionId);
        }

        // 2. Redis 未命中，从数据库查找
        Optional<InterviewSessionEntity> entityOpt =
            persistenceService.findUnfinishedSession(resumeId);

        if (entityOpt.isPresent()) {
            // 3. 从数据库恢复到 Redis
            restoreSessionFromEntity(entityOpt.get());
            return Optional.of(entityOpt.get().getSessionId());
        }

        return Optional.empty();
    }
}
```

#### 面试重点与话术

**高频问题**

| 问题                        | 回答要点                                                     |
| --------------------------- | ------------------------------------------------------------ |
| "为什么用 Redis 缓存会话？" | 性能：答题交互毫秒级；断点续面：24h 内可恢复；削峰：减少数据库压力 |
| "分批评估的具体策略？"      | 默认每批8题；多轮调用 AI；合并后二次汇总生成整体评价         |
| "如何防止重复提问？"        | 查询历史问题列表传入 Prompt；AI 生成时明确要求避免重复       |
| "追问机制怎么实现的？"      | AI 生成主问题时同时生成追问；转换时展开为线性列表；isFollowUp 标记关联主问题 |
| "提前交卷怎么处理？"        | 更新状态为 COMPLETED；发送评估任务到 Stream；异步生成报告    |

**亮点数据**

**技术细节:**
- 缓存TTL: 24小时
- 分批大小: 每批8题（可配置）
- 追问数量: 默认1条，最多2条
- 问题类型: 7大类（项目、MySQL、Redis、Java基础、集合、并发、Spring）
- 权重分配: 项目20%、MySQL20%、Redis20%、Java基础10%、集合10%、并发10%、Spring10%

**可能的追问**

**Q: 如果 Redis 挂了怎么办？**

> "有降级策略。每次状态变更都会异步同步到数据库，如果 Redis 不可用，会直接从数据库恢复会话。虽然性能有所下降，但核心功能可用。"

**Q: AI 评估失败怎么重试？**

> "Consumer 层有统一重试机制，最多3次。如果仍失败，标记为 FAILED，用户可以点击'重新生成报告'手动触发。"

**Q: 如何控制问题难度？**

> "Prompt 中传入候选人类型（校招/社招/实习）和目标岗位，AI 会根据这些信息调整问题难度和侧重点。比如校招侧重基础，社招侧重项目和性能优化。"

#### 总结图

```
┌─────────────────────────────────────────────────────────────────┐
│                      模拟面试模块技术栈                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│   会话管理        Redis 缓存（24h TTL）+ 数据库持久化              │
│   智能出题        分类权重配比 + 历史去重 + 追问机制               │
│   答案评估        分批处理（8题/批）+ 二次汇总                     │
│   状态流转        CREATED → IN_PROGRESS → COMPLETED → EVALUATED │
│   异步处理        Redis Stream Consumer                           │
│                                                                 │
│   核心亮点:                                                      │
│   ✓ 断点续面      ✓ Token溢出防护    ✓ 线性追问流                │
│   ✓ 优雅降级      ✓ 分类权重配比     ✓ 双写一致性                 │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### RAG流程

#### 知识库上传与向量化流程

{% asset_img image-20260329194030117.png %}

#### RAG问答流程

{% asset_img image-20260329195909969.png %}

#### 关键技术实现（面试重点 ★★★）

#### **文本分块与向量化**

```java
@Service
public class KnowledgeBaseVectorService {

    // 阿里云 DashScope Embedding API 批量限制
    private static final int MAX_BATCH_SIZE = 10;

    private final VectorStore vectorStore;  // Spring AI 的 pgvector 实现
    private final TextSplitter textSplitter; // TokenTextSplitter

    public KnowledgeBaseVectorService(VectorStore vectorStore, ...) {
        this.vectorStore = vectorStore;
        // 每个 chunk 约 500 tokens，重叠 50 tokens（保持语义连贯）
        this.textSplitter = new TokenTextSplitter();
    }

    @Transactional
    public void vectorizeAndStore(Long knowledgeBaseId, String content) {
        // 1. 删除旧向量（幂等性：支持重新向量化）
        deleteByKnowledgeBaseId(knowledgeBaseId);

        // 2. 文本分块
        List<Document> chunks = textSplitter.apply(List.of(new Document(content)));

        // 3. 添加 metadata（用于后续过滤）
        chunks.forEach(chunk ->
            chunk.getMetadata().put("kb_id", knowledgeBaseId.toString())
        );

        // 4. 分批向量化（API 限制 batch size <= 10）
        int batchCount = (totalChunks + MAX_BATCH_SIZE - 1) / MAX_BATCH_SIZE;
        for (int i = 0; i < batchCount; i++) {
            List<Document> batch = chunks.subList(start, end);
            vectorStore.add(batch);  // Spring AI 自动调用 Embedding API 并存储
        }
    }
}
```

**面试话术：**

> "文本分块使用 TokenTextSplitter，设置每块约 500 tokens、重叠 50 tokens。重叠设计是为了避免关键信息被截断在边界。
> 向量化使用阿里云 text-embedding-v3 模型，生成 1024 维向量。考虑到 API 的 batch size 限制（≤10），我实现了分批处理，每批最多 10 个 chunk。
> 向量存储使用 pgvector 扩展，通过 Spring AI 的 VectorStore 抽象，无需关心底层 SQL。"

#### **RAG** 检索优化 —— 动态参数 + Query Rewrite

```java
@Service
public class KnowledgeBaseQueryService {

    // 动态检索参数配置
    @Value("${app.ai.rag.search.short-query-length:4}")
    private int shortQueryLength;      // 短查询阈值：4个字符

    @Value("${app.ai.rag.search.topk-short:20}")
    private int topkShort;             // 短查询 TopK：20

    @Value("${app.ai.rag.search.topk-medium:12}")
    private int topkMedium;            // 中查询 TopK：12

    @Value("${app.ai.rag.search.topk-long:8}")
    private int topkLong;              // 长查询 TopK：8

    @Value("${app.ai.rag.search.min-score-short:0.18}")
    private double minScoreShort;      // 短查询相似度阈值：0.18（放宽）

    @Value("${app.ai.rag.search.min-score-default:0.28}")
    private double minScoreDefault;    // 默认相似度阈值：0.28（严格）

    /**
     * 构建查询上下文（动态参数 + Query Rewrite）
     */
    private QueryContext buildQueryContext(String originalQuestion) {
        String normalizedQuestion = normalizeQuestion(originalQuestion);

        // 1. Query Rewrite：短查询自动扩展
        String rewrittenQuestion = rewriteQuestion(normalizedQuestion);

        // 2. 候选查询列表（原查询 + 重写查询）
        Set<String> candidates = new LinkedHashSet<>();
        candidates.add(rewrittenQuestion);
        candidates.add(normalizedQuestion);

        // 3. 根据查询长度动态选择检索参数
        SearchParams searchParams = resolveSearchParams(normalizedQuestion);

        return new QueryContext(normalizedQuestion, new ArrayList<>(candidates), searchParams);
    }

    /**
     * 动态检索参数决策
     */
    private SearchParams resolveSearchParams(String question) {
        int compactLength = question.replaceAll("\\s+", "").length();

        if (compactLength <= shortQueryLength) {
            // 短查询：提高召回率（TopK=20，降低相似度阈值）
            return new SearchParams(topkShort, minScoreShort);
        }
        if (compactLength <= 12) {
            // 中查询：平衡精度和召回
            return new SearchParams(topkMedium, minScoreDefault);
        }
        // 长查询：提高精度（TopK=8，严格相似度阈值）
        return new SearchParams(topkLong, minScoreDefault);
    }

    /**
     * Query Rewrite：使用 AI 扩展短查询
     */
    private String rewriteQuestion(String question) {
        if (!rewriteEnabled || question.isBlank()) {
            return question;
        }
        // Prompt 模板："将用户的问题扩展为更详细的查询语句..."
        String rewritePrompt = rewritePromptTemplate.render(variables);

        String rewritten = aiChatClientFactory.getChatClient().prompt()
            .user(rewritePrompt)
            .call()
            .content();

        log.info("Query rewrite: origin='{}', rewritten='{}'", question, normalized);
        return normalized;
    }
}
```

**面试亮点：**

1. **动态参数**：短查询用更高的 TopK 和更低的相似度阈值，提高召回率；长查询则更严格，减少噪音
2. **Query Rewrite**：短查询（如"Redis"）语义模糊，通过 AI 扩展为"Redis 的数据结构和常用命令有哪些"，提高检索质量
3. **多候选查询**：原查询和重写查询都尝试检索，取第一个有效结果

#### **检索结果有效性校验** —— 避免 AI "幻觉"

```java
@Service
public class KnowledgeBaseQueryService {

    /**
     * 检索命中 ≠ 可回答
     * 对短 token 场景增加二次确认，避免把弱相关片段交给模型后生成"信息不足"的废话
     */
    private boolean hasEffectiveHit(String question, List<Document> docs) {
        if (docs == null || docs.isEmpty()) {
            return false;
        }

        // 非短查询直接通过
        if (!isShortTokenQuery(normalized)) {
            return true;
        }

        // 短查询：要求检索结果必须包含查询关键词
        String loweredToken = normalized.toLowerCase();
        for (Document doc : docs) {
            String text = doc.getText();
            if (text != null && text.toLowerCase().contains(loweredToken)) {
                return true;  // 至少有一个片段包含关键词
            }
        }

        log.info("短 query 命中确认失败，视为无有效结果");
        return false;  // 虽然向量相似度高，但内容不包含关键词，可能是误匹配
    }

    private boolean isShortTokenQuery(String question) {
        // 匹配 2-20 字符的短词（如"Redis"、"MySQL索引"）
        return SHORT_TOKEN_PATTERN.matcher(compact).matches();
    }
}
```

**面试话术：**

> "向量相似度高的结果不一定真的相关。比如查询'Redis'，可能返回一个提到'缓存策略'的段落，向量距离很近，但实际没有 Redis 的具体信息。
> 我增加了有效性校验：对于短查询（2-20 字符），要求检索结果必须包含查询关键词，否则视为无有效结果，直接返回'未检索到相关信息'，避免 AI 看到弱相关片段后生成大段废话。"

#### **流式响应优化** —— 探测窗口归一化

```java
@Service
public class KnowledgeBaseQueryService {

    private static final int STREAM_PROBE_CHARS = 120;  // 探测窗口大小

    /**
     * 流式输出归一化
     * 先观察前 120 字符，快速识别"无信息"模板
     * - 命中无信息：立即输出固定模板并结束，防止长篇拒答
     * - 非无信息：尽快释放缓冲并继续实时透传
     */
    private Flux<String> normalizeStreamOutput(Flux<String> rawFlux) {
        return Flux.create(sink -> {
            StringBuilder probeBuffer = new StringBuilder();
            AtomicBoolean passthrough = new AtomicBoolean(false);

            rawFlux.subscribe(
                chunk -> {
                    if (passthrough.get()) {
                        sink.next(chunk);  // 已确认有效，直接透传
                        return;
                    }

                    probeBuffer.append(chunk);
                    String probeText = probeBuffer.toString();

                    // 探测期内发现"无信息"特征
                    if (isNoResultLike(probeText)) {
                        sink.next(NO_RESULT_RESPONSE);  // 输出固定模板
                        sink.complete();
                        return;
                    }

                    // 探测窗口已满（120字符），确认有效内容
                    if (probeBuffer.length() >= STREAM_PROBE_CHARS) {
                        passthrough.set(true);
                        sink.next(probeText);  // 一次性释放缓冲
                    }
                },
                sink::error,
                () -> {
                    if (!passthrough.get()) {
                        sink.next(normalizeAnswer(probeBuffer.toString()));
                    }
                    sink.complete();
                }
            );
        });
    }

    private boolean isNoResultLike(String text) {
        return text.contains("没有找到相关信息")
            || text.contains("未检索到相关信息")
            || text.contains("信息不足")
            || text.contains("无法根据提供内容回答");
    }
}
```

**面试话术：**

> "流式响应有一个痛点：如果检索结果不足，AI 可能会输出大段'抱歉，我没有找到相关信息...'，用户要看完这段话才知道没结果，体验很差。
> 我的解决方案是探测窗口归一化：先缓冲前 120 个字符，如果发现'无信息'特征，立即中断并输出固定短句'抱歉，在选定的知识库中未检索到相关信息'；如果是有效内容，立即释放缓冲并继续实时透传。
> 这样既保留了流式的速度感，又避免了无信息场景下的长篇废话。"

#### **多知识库联合检索**

```java
@Service
public class KnowledgeBaseVectorService {

    /**
     * 基于多个知识库进行相似度搜索
     */
    public List<Document> similaritySearch(String query, List<Long> knowledgeBaseIds,
                                           int topK, double minScore) {
        SearchRequest.Builder builder = SearchRequest.builder()
            .query(query)
            .topK(Math.max(topK, 1));

        if (minScore > 0) {
            builder.similarityThreshold(minScore);
        }

        // 构建过滤表达式：kb_id IN ['1', '2', '3']
        if (knowledgeBaseIds != null && !knowledgeBaseIds.isEmpty()) {
            builder.filterExpression(buildKbFilterExpression(knowledgeBaseIds));
        }

        return vectorStore.similaritySearch(builder.build());
    }

    private String buildKbFilterExpression(List<Long> knowledgeBaseIds) {
        String values = knowledgeBaseIds.stream()
            .map(String::valueOf)
            .map(id -> "'" + id + "'")
            .collect(Collectors.joining(", "));
        return "kb_id in [" + values + "]";
    }
}
```

**面试话术：**

> "系统支持多知识库联合检索，用户可以同时选择多个知识库进行问答。实现方式是在向量检索时添加 filter 表达式 kb_id in ['1', '2']，只检索指定知识库的向量。
> 这个功能的价值是：比如用户同时上传了'Spring 官方文档'和'公司内部规范'，可以跨文档进行问答，AI 会综合多个来源给出答案。"

#### **RAG** 多轮对话 —— 会话管理

```java
@Service
public class RagChatSessionService {

    /**
     * 准备流式消息
     * 设计：先同步保存用户消息和 AI 消息占位，再返回流式响应
     */
    @Transactional
    public Long prepareStreamMessage(Long sessionId, String question) {
        // 1. 保存用户消息
        RagChatMessageEntity userMessage = new RagChatMessageEntity();
        userMessage.setType(MessageType.USER);
        userMessage.setContent(question);
        userMessage.setCompleted(true);
        messageRepository.save(userMessage);

        // 2. 创建 AI 消息占位（状态：未完成）
        RagChatMessageEntity assistantMessage = new RagChatMessageEntity();
        assistantMessage.setType(MessageType.ASSISTANT);
        assistantMessage.setContent("");  // 先空着
        assistantMessage.setCompleted(false);
        assistantMessage = messageRepository.save(assistantMessage);

        return assistantMessage.getId();  // 返回消息ID用于后续更新
    }

    /**
     * 流式响应完成后更新消息
     */
    @Transactional
    public void completeStreamMessage(Long messageId, String content) {
        RagChatMessageEntity message = messageRepository.findById(messageId)
            .orElseThrow(...);

        message.setContent(content);
        message.setCompleted(true);
        messageRepository.save(message);
    }

    /**
     * 获取流式回答
     */
    public Flux<String> getStreamAnswer(Long sessionId, String question) {
        RagChatSessionEntity session = sessionRepository
            .findByIdWithKnowledgeBases(sessionId)
            .orElseThrow(...);

        // 获取关联的知识库ID列表
        List<Long> kbIds = session.getKnowledgeBaseIds();

        // 复用 KnowledgeBaseQueryService 的流式查询能力
        return queryService.answerQuestionStream(kbIds, question);
    }
}
```

**面试话术：**

> "RAG 多轮对话的实现关键是消息状态管理。用户发送问题时，我同步保存两条消息：用户消息（completed=true）和 AI 消息占位（completed=false）。
> 然后返回 SSE 流式响应，前端实时展示打字机效果。流式完成后，通过 completeStreamMessage 更新 AI 消息的内容和状态。
> 这种设计的优点是：即使流式过程中断，已接收的内容也会保存，用户可以刷新页面后继续查看。"

#### 面试重点与话术

**高频问题**

| 问题                                       | 回答要点                                                     |
| ------------------------------------------ | ------------------------------------------------------------ |
| "为什么用 pgvector 而不是专用向量数据库？" | 架构精简，不引入新组件；PG 的向量功能已满足需求；事务一致性（向量和业务数据在一个数据库） |
| "文本分块的策略是什么？"                   | TokenTextSplitter，每块 500 tokens，重叠 50 tokens；重叠保持语义连贯 |
| "Query Rewrite 的作用？"                   | 短查询语义模糊，通过 AI 扩展为更具体的查询，提高召回率       |
| "如何处理检索不到结果的情况？"             | 探测窗口识别无信息模板；有效性校验（短查询二次确认）；优雅降级返回固定提示 |
| "多知识库检索怎么实现？"                   | filter 表达式 kb_id in [...]；Spring AI SearchRequest 支持过滤条件 |

**亮点数据**

**技术细节:**
- 向量维度: 1024 (text-embedding-v3)
- 分块大小: 500 tokens / 块
- 重叠大小: 50 tokens
- 批量限制: 10 chunks / 批 (Embedding API 限制)
- 动态TopK: 短查询 20, 中查询 12, 长查询 8
- 相似度阈值: 短查询 0.18, 其他 0.28
- 探测窗口: 120 字符 (流式优化)
- 响应方式: 非流式 + SSE 流式双支持

**可能的追问**

**Q: 向量检索失败怎么办？**

> "有 fallback 机制。如果带过滤条件的检索失败（可能是表达式语法问题），会降级到全量检索，然后在内存中过滤 kb_id，保证功能可用性。"

**Q: 如何更新已上传的知识库？**

> "支持重新向量化 API。删除旧向量，重新解析文件并生成新向量。设计上保证幂等性，可以安全地多次执行。"

**Q: 流式响应中断怎么处理？**

> "通过 doOnError 捕获异常，保存已接收的内容到数据库，并标记消息状态。用户刷新页面后可以看到已生成的部分内容。"

#### 总结图

```
┌─────────────────────────────────────────────────────────────────┐
│                    知识库 + RAG 模块技术栈                        │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│   文档处理        Apache Tika (PDF/DOCX/MD/TXT)                   │
│   文本分块        TokenTextSplitter (500 tokens/chunk)            │
│   向量化          text-embedding-v3 (1024维)                      │
│   向量存储        PostgreSQL + pgvector (HNSW索引)                │
│   RAG检索         Spring AI VectorStore + 相似度搜索              │
│                                                                 │
│   优化策略:                                                      │
│   ✓ Query Rewrite      ✓ 动态检索参数      ✓ 短Query二次确认      │
│   ✓ 多知识库联合检索    ✓ 探测窗口归一化     ✓ 流式/SSE双模式       │
│                                                                 │
│   状态流转: PENDING → PROCESSING → COMPLETED/FAILED              │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```



## 其他

### AI 配置管理

**一句话概括**

> "考虑到不同 AI 平台的成本和效果差异，我设计了一套配置驱动的多平台适配方案，支持在阿里云 DashScope、Moonshot、DeepSeek 之间动态切换，无需重启服务。"

**技术实现要点（4个维度）**

| 维度         | 实现方式                                | 亮点                                 |
| ------------ | --------------------------------------- | ------------------------------------ |
| **配置存储** | PostgreSQL 持久化 + AES 加密 API Key    | 安全、可动态更新                     |
| **工厂模式** | AiChatClientFactory 统一创建 ChatClient | 封装不同平台的 BaseUrl、默认模型差异 |
| **动态加载** | 运行时从 DB 读取配置创建客户端          | 无需重启，即时生效                   |
| **兼容封装** | Spring AI 的 OpenAI 兼容模式            | 一套代码适配多平台                   |

**代码亮点说明**

```java
// 1. 平台差异化处理（特殊参数适配）
if ("moonshot".equalsIgnoreCase(provider) && modelName.startsWith("kimi-k2.5")) {
    // kimi-k2.5 对 temperature/topP 敏感，特殊处理避免报错
} else {
    optionsBuilder.temperature(temperature);
}

// 2. 配置优先级（灵活性设计）
public ChatClient getChatClient() {
    // 优先使用用户配置的 AI 平台
    if (dbConfig.isPresent()) {
        return buildChatClient(dbConfig.get());
    }
    // 降级到默认配置（环境变量）
    return defaultChatClientBuilder.build();
}
```

📝 **更新后的电梯演讲（增加这一段）**

在技术亮点部分插入：

> "多AI平台动态适配。考虑到不同平台的成本和效果差异，我设计了配置驱动的工厂模式，支持阿里云 DashScope、Moonshot、DeepSeek 等平台动态切换，API Key 使用 AES 加密存储，配置变更无需重启服务即可生效。这样可以根据业务场景灵活选择模型，比如在需要深度推理时切换到 DeepSeek，日常对话使用轻量级模型降低成本。"

🎯 **可能的追问 & 应对**

| 追问                                          | 回答要点                                                     |
| --------------------------------------------- | ------------------------------------------------------------ |
| "为什么不用配置文件而用数据库？"              | 配置变更无需重启；支持多租户/多用户场景；便于前端管理界面配置 |
| "如何防止 API Key 泄露？"                     | AES 加密存储；接口返回时脱敏；前端展示时隐藏真实 Key         |
| "不同平台的模型特性差异如何处理？"            | 工厂类内置默认模型映射；特殊参数（如 kimi-k2.5 的 temperature）做兼容处理 |
| "如果要加新平台（比如 OpenAI）要改多少代码？" | 只需在工厂类添加 BaseUrl 和默认模型，符合开闭原则            |

我来读取完整文件内容，确保转换完整：

文件内容已经是 **Markdown 格式**，以下是完整的文档内容：

---

```markdown
# 2.2 核心实现

```java
@Component
public class AiChatClientFactory {

    private final AiConfigService aiConfigService;

    // 默认配置（从环境变量读取）
    @Value("${spring.ai.openai.api-key}")
    private String defaultApiKey;

    @Value("${spring.ai.openai.chat.options.model:qwen-plus}")
    private String defaultModel;

    /**
     * 获取ChatClient（优先使用DB配置，降级到默认配置）
     */
    public ChatClient getChatClient() {
        Optional<AiConfigEntity> configOpt = aiConfigService.getAiConfig();
        if (configOpt.isPresent()) {
            AiConfigEntity config = configOpt.get();
            return buildChatClient(
                config.getProvider(),
                AesEncryptionUtils.decrypt(config.getEncryptedApiKey()),
                config.getModelName(),
                config.getTemperature(),
                config.getTopP()
            );
        }
        // 降级到默认配置
        return defaultChatClientBuilder.build();
    }

    /**
     * 构建指定平台的ChatClient
     */
    public ChatClient buildChatClient(String provider, String apiKey,
                                     String modelName, Double temperature,
                                     Double topP) {
        // 1. 确定BaseUrl
        String baseUrl = getBaseUrlByProvider(provider);

        // 2. 参数兜底
        if (apiKey == null || apiKey.isEmpty()) {
            apiKey = defaultApiKey;
        }
        if (modelName == null || modelName.isEmpty()) {
            modelName = getDefaultModelByProvider(provider);
        }

        // 3. 构建OpenAiApi（Spring AI的兼容模式）
        OpenAiApi openAiApi = OpenAiApi.builder()
            .baseUrl(baseUrl)
            .apiKey(apiKey)
            .completionsPath("/chat/completions")
            .restClientBuilder(RestClient.builder()
                .requestFactory(new SimpleClientHttpRequestFactory() {{
                    setConnectTimeout((int) Duration.ofSeconds(60).toMillis());
                    setReadTimeout((int) Duration.ofMinutes(5).toMillis());
                }}))
            .build();

        // 4. 特殊参数处理（Moonshot kimi-k2.5对temperature敏感）
        OpenAiChatOptions.Builder optionsBuilder = OpenAiChatOptions.builder()
            .model(modelName);

        if ("moonshot".equalsIgnoreCase(provider)
            && modelName != null
            && modelName.startsWith("kimi-k2.5")) {
            // kimi-k2.5 特殊处理：不设置temperature避免报错
        } else {
            if (temperature != null) optionsBuilder.temperature(temperature);
            if (topP != null) optionsBuilder.topP(topP);
        }

        // 5. 构建ChatModel和ChatClient
        ChatModel chatModel = OpenAiChatModel.builder()
            .openAiApi(openAiApi)
            .defaultOptions(optionsBuilder.build())
            .build();

        return ChatClient.builder(chatModel).build();
    }

    private String getBaseUrlByProvider(String provider) {
        return switch (provider.toLowerCase()) {
            case "moonshot" -> "https://api.moonshot.cn/v1";
            case "deepseek" -> "https://api.deepseek.com/v1";
            default -> "https://dashscope.aliyuncs.com/compatible-mode/v1";
        };
    }
}
```

**面试话术：**

> "多AI平台适配采用工厂模式设计，核心思想是配置驱动 + 统一封装。
> 数据库表 sys_ai_config 存储用户选择的平台、加密后的API Key、模型名称和生成参数。系统优先使用DB配置，不存在时降级到环境变量的默认配置。
> 不同平台的差异在工厂类中屏蔽：BaseUrl映射、默认模型选择、特殊参数处理（如Moonshot的kimi-k2.5对temperature敏感，需要跳过设置）。
> 安全方面，API Key使用AES加密存储，前端查询时脱敏返回，只有保存时才更新。"

### 配置化、安全化

#### 限流组件 —— Redis + Lua 滑动窗口

**架构设计** @RateLimit

```
┌─────────────────────────────────────────────────────────────────┐
│                      限流组件架构图                               │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│   使用方式（注解式，零侵入）                                        │
│   ┌─────────────────────────────────────────────────────────┐  │
│   │   @PostMapping("/api/resumes/upload")                   │  │
│   │   @RateLimit(                                           │  │
│   │       dimensions = {GLOBAL, IP},  // 多维度组合          │  │
│   │       count = 5,                   // 5次                │  │
│   │       interval = 1,                // 1分钟              │  │
│   │       timeUnit = TimeUnit.MINUTES                        │  │
│   │   )                                                      │  │
│   │   public Result<?> upload(...) {...}                     │  │
│   └─────────────────────────────────────────────────────────┘  │
│                              │                                  │
│                              ▼                                  │
│   ┌─────────────────────────────────────────────────────────┐  │
│   │              RateLimitAspect (AOP切面)                   │  │
│   │  ├─ 解析注解参数                                         │  │
│   │  ├─ 生成限流Key（支持Hash Tag适配Cluster）                │  │
│   │  ├─ 调用Lua脚本（原子性检查+扣减）                        │  │
│   │  └─ 触发降级策略（抛出异常或调用fallback方法）             │  │
│   └─────────────────────────────────────────────────────────┘  │
│                              │                                  │
│                              ▼                                  │
│   ┌─────────────────────────────────────────────────────────┐  │
│   │              rate_limit.lua (Redis脚本)                  │  │
│   │                                                         │  │
│   │  Phase 1: 预检查（检查所有维度配额）                      │  │
│   │    FOR each key:                                        │  │
│   │      - 清理过期令牌（滑动窗口）                           │  │
│   │      - 检查 current_val >= permits                      │  │
│   │      - 任一维度不足 → RETURN 0                          │  │
│   │                                                         │  │
│   │  Phase 2: 扣减（只有全部通过才执行）                      │  │
│   │    FOR each key:                                        │  │
│   │      - 记录 permit (ZADD)                               │  │
│   │      - 扣减令牌 (DECR)                                  │  │
│   │    RETURN 1                                             │  │
│   │                                                         │  │
│   └─────────────────────────────────────────────────────────┘  │
│                                                                 │
│   Redis 数据结构：                                               │
│   ├─ {Class:Method}:global:value  - 全局限流计数器              │
│   ├─ {Class:Method}:global:permits - 全局限流记录（Sorted Set）  │
│   ├─ {Class:Method}:ip:{ip}:value - IP限流计数器                │
│   └─ {Class:Method}:ip:{ip}:permits - IP限流记录                │
│                                                                 │
│   Hash Tag {} 确保同一方法的所有Key落在同一Redis Slot             │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

**Lua 脚本详解（滑动窗口算法）**

```lua
-- rate_limit.lua
-- 原子化多维度限流脚本（滑动时间窗口）

-- 参数
local now_ms = tonumber(ARGV[1])        -- 当前时间戳（毫秒）
local permits = tonumber(ARGV[2])       -- 申请令牌数（默认1）
local interval = tonumber(ARGV[3])      -- 时间窗口（毫秒）
local max_tokens = tonumber(ARGV[4])    -- 窗口内最大令牌数
local request_id = ARGV[5]              -- 请求唯一标识

-- ========== Phase 1: 预检查 ==========
for i, key in ipairs(KEYS) do
    local value_key = key .. ":value"     -- 当前可用令牌数
    local permits_key = key .. ":permits" -- 历史请求记录（ZSet）

    -- 初始化
    if redis.call("exists", value_key) == 0 then
        redis.call("set", value_key, max_tokens)
    end

    -- 回收过期令牌（滑动窗口核心）
    -- 清理窗口外的记录，将配额返还到 value_key
    local expired_values = redis.call("zrangebyscore",
        permits_key, 0, now_ms - interval)

    if #expired_values > 0 then
        local expired_count = 0
        for _, v in ipairs(expired_values) do
            -- 解析格式 "request_id:permits"
            local p = tonumber(string.match(v, ":(%d+)$"))
            if p then expired_count = expired_count + p end
        end

        -- 删除过期记录
        redis.call("zremrangebyscore", permits_key, 0, now_ms - interval)

        -- 返还配额（上限为max_tokens）
        if expired_count > 0 then
            local curr_v = tonumber(redis.call("get", value_key) or max_tokens)
            local next_v = math.min(max_tokens, curr_v + expired_count)
            redis.call("set", value_key, next_v)
        end
    end

    -- 核心检查：当前可用令牌是否足够？
    local current_val = tonumber(redis.call("get", value_key) or max_tokens)
    if current_val < permits then
        return 0  -- 任一维度不足，直接拒绝
    end
end

-- ========== Phase 2: 扣减 ==========
for i, key in ipairs(KEYS) do
    local value_key = key .. ":value"
    local permits_key = key .. ":permits"

    -- 记录本次请求（用于后续过期回收）
    local permit_record = request_id .. ":" .. permits
    redis.call("zadd", permits_key, now_ms, permit_record)

    -- 扣减令牌
    local current_v = tonumber(redis.call("get", value_key) or max_tokens)
    redis.call("set", value_key, current_v - permits)

    -- 设置过期时间（窗口的2倍，确保过期回收）
    local expire_time = math.ceil(interval * 2 / 1000)
    redis.call("expire", value_key, expire_time)
    redis.call("expire", permits_key, expire_time)
end

return 1  -- 成功
```

**面试话术：**

> "限流组件主要用在 AI 调用密集型 和 资源消耗型 接口上：
>
> - 简历模块：上传和重新分析接口，限制每 IP 每分钟 2-5 次，防止恶意刷 AI 分析配额。
> - 面试模块：创建面试（AI 出题）限制 5 次/分钟，提交答案限制 10 次/分钟。
> - 知识库模块：上传（3 次/分钟）、重新向量化（2 次/分钟）、RAG 问答（10 次/分钟）、流式问答（5 次/分钟）。
> - 设计原则：AI 调用越贵、资源消耗越大、连接占用越长，限制越严格。使用 GLOBAL + IP 双维度，既防止单用户刷接口，又防止全局过载。"
>
> 限流组件基于 Redis + Lua 实现滑动时间窗口算法，保证原子性。
> 核心设计：
>
> 1. 双Key结构：value存储当前可用令牌数，permits（Sorted Set）存储历史请求时间戳
> 2. 滑动窗口：每次请求时清理窗口外的过期记录，并返还配额，相比固定窗口更平滑
> 3. 多维度组合：支持 GLOBAL、IP、USER 多维度同时限流，只有所有维度通过才放行
> 4. Hash Tag：Key使用 {class:method} 包含Hash Tag，确保Redis Cluster模式下同一Slot
> 为什么选择滑动窗口？固定窗口在边界处可能有突发流量（如最后1秒和最初1秒各5次），滑动窗口通过持续清理过期记录，避免这个问题。

#### 结构化输出调用器 —— 带重试机制

```java
@Component
public class StructuredOutputInvoker {

    private static final String STRICT_JSON_INSTRUCTION = """
请仅返回可被 JSON 解析器直接解析的 JSON 对象，并严格满足字段结构要求：
1) 不要输出 Markdown 代码块（如 ```json）。
2) 不要输出任何解释文字、前后缀、注释。
3) 所有字符串内引号必须正确转义。
""";

    private final int maxAttempts;  // 默认2次（1次原始 + 1次重试）

    /**
     * 调用AI并解析结构化输出（带重试）
     */
    public <T> T invoke(
        ChatClient chatClient,
        String systemPromptWithFormat,  // 包含JSON Schema的System Prompt
        String userPrompt,
        BeanOutputConverter<T> outputConverter,  // Spring AI的结构化转换器
        ErrorCode errorCode,            // 业务错误码
        String errorPrefix,             // 错误前缀
        String logContext,              // 日志上下文
        Logger log
    ) {
        Exception lastError = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            // 第一次用原Prompt，重试时加强调Strict JSON
            String attemptSystemPrompt = (attempt == 1)
                ? systemPromptWithFormat
                : buildRetrySystemPrompt(systemPromptWithFormat, lastError);

            try {
                return chatClient.prompt()
                    .system(attemptSystemPrompt)
                    .user(userPrompt)
                    .call()
                    .entity(outputConverter);  // 自动解析JSON并映射到DTO

            } catch (Exception e) {
                lastError = e;
                log.warn("{}结构化解析失败，准备重试: attempt={}, error={}",
                    logContext, attempt, e.getMessage());
            }
        }

        // 所有重试失败，抛出业务异常
        throw new BusinessException(errorCode, errorPrefix + lastError.getMessage());
    }

    private String buildRetrySystemPrompt(String originalPrompt, Exception lastError) {
        return originalPrompt
            + "\n\n"
            + STRICT_JSON_INSTRUCTION
            + "\n上次输出解析失败，请仅返回合法 JSON。"
            + "\n上次失败原因：" + sanitizeErrorMessage(lastError.getMessage());
    }
}
```

**面试话术：**

> "大模型返回结构化JSON时，可能出现格式问题（加了Markdown代码块、缺少引号、多了注释等）。
> 我封装了 StructuredOutputInvoker，实现带重试的结构化输出调用：
> 1. 第一次使用标准Prompt
> 2. 如果解析失败，第二次在Prompt中追加 Strict JSON Instruction，明确要求不输出Markdown、不输出解释文字、正确转义引号
> 3. 同时把上次的错误信息反馈给模型，帮助其修正
> 默认配置最多2次尝试，既保证成功率，又避免过多重试增加延迟和成本。"

#### AES 加密工具 —— API Key 安全存储

```java
public class AesEncryptionUtils {

    private static final String ALGORITHM = "AES";
    // 16字节静态密钥（实际生产环境应从环境变量读取）
    private static final String DEFAULT_KEY = "OfferFlowSecKey1";

    public static String encrypt(String value) {
        if (value == null || value.isEmpty()) return value;

        SecretKeySpec key = new SecretKeySpec(
            DEFAULT_KEY.getBytes(StandardCharsets.UTF_8),
            ALGORITHM
        );
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);

        byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public static String decrypt(String encryptedValue) {
        if (encryptedValue == null || encryptedValue.isEmpty()) return encryptedValue;

        SecretKeySpec key = new SecretKeySpec(
            DEFAULT_KEY.getBytes(StandardCharsets.UTF_8),
            ALGORITHM
        );
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);

        byte[] original = cipher.doFinal(
            Base64.getDecoder().decode(encryptedValue)
        );
        return new String(original, StandardCharsets.UTF_8);
    }
}
```

**面试话术：**

> "AI平台的API Key是敏感信息，需要加密存储。我实现了AES对称加密工具：
> 1. 使用AES算法，128位密钥（16字节）
> 2. 加密后Base64编码存储到数据库
> 3. 使用时解密后内存中使用，不落盘
> 注意：演示项目使用静态密钥，生产环境应从KMS或环境变量动态读取，并定期轮换。"

#### 面试重点与话术

**高频问题**

| 问题                           | 回答要点                                                     |
| ------------------------------ | ------------------------------------------------------------ |
| "限流为什么选择 Redis + Lua？" | 原子性：Lua脚本在Redis单线程执行，避免竞态；性能：内存操作，毫秒级；灵活性：支持多维度组合和动态参数 |
| "滑动窗口和固定窗口的区别？"   | 固定窗口在边界可能突发2倍流量；滑动窗口持续清理过期记录，更平滑；实现稍复杂，需要记录时间戳 |
| "多AI平台适配怎么保证兼容性？" | Spring AI的OpenAI兼容模式统一封装；工厂类处理平台差异（BaseUrl、默认模型、特殊参数）；配置驱动，动态切换 |
| "API Key加密怎么做的？"        | AES对称加密，Base64存储；读取时解密使用；前端展示脱敏；生产环境建议用KMS |
| "结构化输出失败怎么办？"       | 最多2次重试；第二次Prompt追加Strict JSON指令；反馈错误信息给模型；最终失败抛业务异常 |

**亮点数据**

**技术细节:**
- 限流算法: 滑动时间窗口
- 限流维度: GLOBAL, IP, USER（可组合）
- Lua脚本: 两阶段（预检查+扣减），保证原子性
- 重试机制: 最多2次，指数退避
- 加密算法: AES-128，Base64编码
- 支持平台: 阿里云DashScope, Moonshot, DeepSeek（可扩展）

**可能的追问**

**Q: 限流组件支持分布式吗？**

> "支持。Redis本身就是分布式存储，配合Hash Tag确保同一方法的所有Key在同一Slot，Cluster模式下也能正常工作。如果需要更高可用，可以用Redis Sentinel或Cluster模式。"

**Q: 如果要加新的AI平台（比如OpenAI）需要改多少代码？**

> "只需在 AiChatClientFactory 添加新的BaseUrl映射和默认模型，符合开闭原则。如果平台有特殊参数要求，在 buildChatClient 中添加对应的特殊处理逻辑即可。"
**Q: 限流降级怎么做**

> "@RateLimit 注解支持 fallback 参数指定降级方法。如果限流触发，会优先调用降级方法；如果没有配置或降级失败，抛出 RateLimitExceededException，由全局异常处理器返回友好提示。"
