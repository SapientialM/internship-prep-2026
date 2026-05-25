# 10 - 分布式系统

> 分布式是后端进阶的核心。CAP、一致性协议、分布式 ID、负载均衡是面试的重点方向。

---

## 1. CAP 定理

分布式系统不可能同时满足三个特性：

| 特性 | 含义 |
|:---|:---|
| **C**onsistency（一致性） | 所有节点同时看到同一份数据 |
| **A**vailability（可用性） | 每个请求都能得到非错误响应（即使部分节点故障） |
| **P**artition Tolerance（分区容错） | 网络分区发生时系统仍能正常运行 |

**P 必须满足**（网络分区无法避免），实际是在 CP 和 AP 之间取舍：
- **CP 系统：** 分区时牺牲可用性保证一致性（ZooKeeper、Etcd）
- **AP 系统：** 分区时牺牲一致性保证可用性（Eureka、Cassandra）

**注意：** CAP 说的是网络分区发生时（P 触发）的取舍，正常运行时不适用。

---

## 2. BASE 理论

对 CAP 中 AP 方案的补充，牺牲强一致性换取可用性：

| 缩写 | 含义 |
|:---|:---|
| **B**asically Available | 基本可用（允许部分故障下的降级） |
| **S**oft State | 软状态（允许中间状态） |
| **E**ventually Consistent | 最终一致性（不保证实时一致，但最终一致） |

---

## 3. 一致性协议

### Paxos

- **角色：** Proposer（提案者）、Acceptor（接受者）、Learner（学习者）
- **两阶段：** Prepare → Accept
- 理解困难，工程中常用 Raft 替代

### Raft

核心：**Leader 选举 + 日志复制**。

**Leader 选举：** 节点状态从 Follower → Candidate → Leader，通过 Term 和选举超时驱动。

**日志复制：** Leader 接收写请求 → 日志复制到 Followers → 超过半数确认 → 提交 → 应用到状态机。

### 分布式事务

| 方案 | 说明 | 适用场景 |
|:---|:---|:---|
| **2PC** | 协调者 → 参与者预提交 → 确认提交 | 强一致性需求 |
| **TCC** | Try（预留资源）→ Confirm（确认）→ Cancel（取消） | 高并发金融场景 |
| **Saga** | 长事务拆分为多个本地事务，每个有对应的补偿操作 | 微服务长事务 |
| **本地消息表** | 业务 + 消息在同一 DB 事务中，定时任务异步投递 | 最终一致性 |
| **RocketMQ 事务消息** | 半消息 + 本地事务检查 | 异步解耦场景 |

---

## 4. 负载均衡

### 算法

| 算法 | 说明 | 适用场景 |
|:---|:---|:---|
| 轮询（Round Robin） | 依次分配 | 服务器性能相当 |
| 加权轮询（Weighted RR） | 按权重分配 | 服务器性能差异大 |
| 最少连接数 | 分配给连接数最少的服务器 | 长连接场景 |
| 一致性哈希 | 按请求特征映射到固定节点 | 缓存层负载均衡 |
| 随机 | 随机分配 | 简单场景 |

### 一致性哈希

**原理：** 将 node 和 key 都映射到一个哈希环上（0 ~ 2^32-1），key 顺时针找到最近的 node。

**虚拟节点：** 每个物理节点对应多个虚拟节点，均匀分布到环上，避免数据倾斜。

**应用：** Redis Cluster (16384 hash slots)、Dubbo 服务路由、Nginx 负载均衡。

---

## 5. 全局分布式 ID

| 方案 | 优点 | 缺点 |
|:---|:---|:---|
| UUID | 简单，本地生成 | 无序（B+树索引不友好）、字符串存储大 |
| 数据库自增 ID | 简单，递增 | 单点瓶颈，ID 号不够 |
| 数据库号段模式 | 批量获取号段，性能好 | 依赖 DB |
| Redis 自增 | 性能好 | 需持久化，可能丢失 |
| 雪花算法（Snowflake） | 高性能，趋势递增 | 依赖机器时钟，时钟回拨有问题 |

**雪花算法结构（64bit）：**
```
| 1bit 不用 | 41bit 时间戳 | 10bit 机器ID | 12bit 序列号 |
```

**时钟回拨处理：**
1. 等待时钟追上来
2. 换用备用 workerId
3. 使用扩展位来区分同一毫秒内的回拨

---

## 6. 微服务调用链追踪

**链路追踪：** 通过 Trace ID + Span ID 串联整个请求生命周期。

**实现方式：**
1. 入口生成全局 Trace ID
2. 每次 RPC 调用透传 Trace ID + Parent Span ID
3. 每个服务生成自己的 Span ID
4. 上报到追踪系统（Jaeger、Zipkin、SkyWalking）

**Java 实现：** SLF4J MDC（Mapped Diagnostic Context）+ ThreadLocal。

```java
// 入口拦截器
String traceId = UUID.randomUUID().toString();
MDC.put("traceId", traceId);

// RPC 调用时透传
request.setHeader("X-Trace-Id", MDC.get("traceId"));
```
