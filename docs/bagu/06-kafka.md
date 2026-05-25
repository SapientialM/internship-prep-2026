# 06 - Kafka

> Kafka 是分布式消息队列的事实标准。高吞吐、持久化、水平扩展是核心特性，面试重点在可靠性保证和性能优化。

---

## 1. 基本架构

```
Producer → Kafka Cluster (Broker × N) → Consumer
              ↓
          ZooKeeper / KRaft
```

| 组件 | 说明 |
|:---|:---|
| Broker | Kafka 服务节点，负责存储和转发消息 |
| Topic | 消息的逻辑分类 |
| Partition | Topic 的物理分片，有序、不可变的消息序列 |
| Producer | 消息生产者 |
| Consumer | 消息消费者 |
| Consumer Group | 消费者组，组内每个消费者负责不同分区 |
| Replica | 分区副本（Leader + Follower） |

---

## 2. 高吞吐原理

1. **顺序写入** — 将消息追加到日志文件末尾，利用磁盘顺序写的高性能（比随机写快 6000+ 倍）
2. **Page Cache** — 利用 OS 页缓存，写入时先写页缓存，由 OS 异步刷盘
3. **零拷贝** — 发送数据时通过 `sendfile()` 系统调用，数据从 Page Cache 直接拷贝到网卡，不经过用户态
4. **批量压缩** — Producer 批量发送 + 消息压缩（gzip/snappy/lz4/zstd）
5. **分区并行** — 多个分区可以并行读写，水平扩展

---

## 3. 消息可靠性

### Producer 可靠性

**acks 参数：**

| 值 | 含义 | 可靠性 |
|:---|:---|:---|
| `acks=0` | 不等待确认 | 可能丢失 |
| `acks=1` | Leader 写入后确认 | 可能丢（Leader 宕机前 Follower 未同步） |
| `acks=all` / `-1` | 所有 ISR 确认后返回 | 最高可靠性 ✅ |

**幂等性：** `enable.idempotence=true`，Producer 自动去重，保证单分区内的消息不重复。

### Broker 可靠性

| 机制 | 说明 |
|:---|:---|
| 多副本（Replication） | 每个分区有多个副本，Leader 负责读写，Follower 同步备份 |
| ISR（In-Sync Replica） | 与 Leader 保持同步的副本集合 |
| 不完全首领选举(`unclean.leader.election`) | `false` 时，ISR 外副本不能选为 Leader，保证数据不丢 |
| `min.insync.replicas` | 最少同步副本数，搭配 `acks=all` 使用 |

### Consumer 可靠性

- **手动提交 offset** — 处理完消息再提交，实现至少一次/最多一次语义
- **Rebalance 防范** — 控制处理时间 < `max.poll.interval.ms`（默认 5min），避免被踢出消费者组

---

## 4. 消息不丢 / 不重的配置

### 不丢消息

```
Producer: acks=all, enable.idempotence=true, retries=MAX
Broker:   replication.factor=3, min.insync.replicas=2, unclean.leader.election=false
Consumer: enable.auto.commit=false, 手动提交offset（处理完再提交）
```

### 不重消息（精确一次语义）

1. **幂等 Producer + 事务** — `enable.idempotence=true` + `transactional.id`
2. **下游幂等处理** — 基于唯一键（如订单ID）去重插入
3. **消费端手动提交** — 业务处理+offset 提交在同一事务中

---

## 5. 消费者组与 Rebalance

### 消费者组

- 同一组内消费者互斥地消费不同分区
- 分区数 >= 消费者数（否则多余消费者空闲）
- 不同消费者组之间独立消费（广播模式）

### Rebalance 触发条件

1. 组内消费者数量变化
2. Topic 分区数变化
3. 消费者心跳超时（`session.timeout.ms`）

### 避免 Rebalance 的方法

- `max.poll.records` — 减小单次拉取数量（如 500）
- `max.poll.interval.ms` — 增大处理超时时间
- 避免频繁启停 Consumer

---

## 6. 消息顺序性

Kafka 只保证**分区内有序**，不保证跨分区有序。

**实现全局有序：** 将需要有序的消息发到**同一分区**（通过指定 Key 的哈希值）。

---

## 7. 消息积压处理

1. **临时扩容** — 增加分区数（注意与消费者数匹配）
2. **增加消费者** — 但不超过分区数
3. **批量转存** — 消费者快速读出来暂存到其他地方，后续慢慢处理
4. **检查消费逻辑** — 是否有慢查询、死循环等性能问题

---

## 8. Kafka vs 其他 MQ

| 特性 | Kafka | RocketMQ | RabbitMQ |
|:---|:---|:---|:---|
| 吞吐量 | 极高（百万 TPS） | 高（十万 TPS） | 中等（万 TPS） |
| 延迟 | ms 级 | ms 级 | μs 级 |
| 可靠性 | 高 | 高 | 高 |
| 事务消息 | 支持 | 支持 | 不支持 |
| 延时消息 | 不支持（需手动实现） | 支持 | 支持 |
| 适用场景 | 大数据、日志、流处理 | 电商、金融、业务消息 | 低延迟、复杂路由 |
