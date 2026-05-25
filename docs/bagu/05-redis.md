# 05 - Redis

> Redis 是后端系统的性能核心。数据结构、缓存策略、分布式锁、高可用是面试重点。

---

## 1. 数据类型

### 基础数据类型

| 类型 | 底层实现 | 应用场景 |
|:---|:---|:---|
| String | SDS（简单动态字符串） | 缓存、计数器、分布式锁、Session |
| List | QuickList（3.2+） | 消息队列、最新列表 |
| Hash | ListPack / HashTable | 对象缓存、购物车 |
| Set | IntSet / HashTable | 去重、共同好友、标签 |
| Sorted Set | ListPack / SkipList + Dict | 排行榜、延时队列 |

### 高级数据类型

| 类型 | 说明 | 应用场景 |
|:---|:---|:---|
| BitMap | 位图 | 签到统计、用户在线状态 |
| HyperLogLog | 基数统计（误判率 0.81%） | UV 统计 |
| GEO | 地理位置 | 附近的人 |
| Stream | 消息队列 | 消息队列、异步任务（Redis 5.0+） |

### 底层数据结构

| 数据结构 | 特点 |
|:---|:---|
| SDS | C 字符串的替代，O(1) 获取长度，二进制安全，杜绝缓冲区溢出 |
| QuickList | 链表 + 压缩列表的混合体，每个节点是一个 ListPack |
| SkipList | 多级索引实现 O(log n) 查找，支持范围查询 |
| ListPack | 紧凑的内存布局，替代 ZipList |

---

## 2. 分布式锁

### 基于 Redis 的实现

```java
// 加锁
String lockKey = "lock:order:" + orderId;
String lockValue = UUID.randomUUID().toString();
Boolean locked = redisTemplate.opsForValue()
    .setIfAbsent(lockKey, lockValue, 30, TimeUnit.SECONDS);

// 解锁（Lua 脚本保证原子性）
String lua = "if redis.call('get', KEYS[1]) == ARGV[1] then " +
             "return redis.call('del', KEYS[1]) else return 0 end";
redisTemplate.execute(new DefaultRedisScript<>(lua, Long.class),
    Collections.singletonList(lockKey), lockValue);
```

**关键设计：**
1. **set NX + EX 原子操作** — 加锁和设过期时间必须原子
2. **唯一 value 标识** — 每个客户端持有自己的 UUID，防止误删别人的锁
3. **Lua 原子解锁** — 检查 value + 删除必须原子
4. **看门狗续期** — Redisson 的 WatchDog 自动续期（默认锁 30s，每 10s 续期）

### RedLock 算法

N 个独立的 Redis 节点（通常 5 个），在超过半数（N/2+1）节点上加锁成功才算获取锁。不推荐使用，建议用 ZooKeeper 或 Etcd。

---

## 3. 持久化

### RDB（快照）

- 将某个时间点的内存数据保存到磁盘（.rdb 文件）
- `save`（阻塞主线程）/ `bgsave`（fork 子进程，COW）
- 优点：文件紧凑，恢复速度快
- 缺点：两次快照之间的数据可能丢失

### AOF（追加日志）

- 记录每个写命令到日志文件
- 刷盘策略：`always` / `everysec` / `no`
- AOF 重写：压缩冗余命令，减小文件体积
- 优点：数据安全性更高（最多丢 1 秒数据）
- 缺点：文件体积大，恢复速度慢于 RDB

### 混合持久化（Redis 4.0+）

RDB 全量 + AOF 增量结合：重写时先写 RDB 快照，后续增量命令以 AOF 格式追加。

---

## 4. 内存淘汰策略

### 过期删除策略

- **惰性删除：** 访问 key 时检查是否过期
- **定期删除：** 每 100ms 随机抽取一批 key 检查和删除

### 内存淘汰（内存满时）

| 策略 | 行为 |
|:---|:---|
| `noeviction` | 不淘汰，返回错误（默认） |
| `allkeys-lru` | 所有 key 中淘汰最近最少使用的 |
| `volatile-lru` | 有过期时间的 key 中淘汰 LRU |
| `allkeys-lfu` | 所有 key 中淘汰访问频率最低的 |
| `volatile-lfu` | 有过期时间的 key 中淘汰 LFU |
| `allkeys-random` | 随机淘汰 |
| `volatile-random` | 有过期的 key 随机淘汰 |
| `volatile-ttl` | 淘汰 TTL 最短的 key |

推荐：**allkeys-lru**（大部分场景），**allkeys-lfu**（有明显冷热数据差异的场景）。

---

## 5. 缓存常见问题

### 缓存穿透

**问题：** 查询不存在的数据，请求穿透缓存直接打 DB。

**解决：**
1. **布隆过滤器** — 提前过滤不存在的 key
2. **缓存空值** — 查不到也缓存（设短过期时间，如 5 分钟）
3. **参数校验** — 拦截非法参数（如 id < 0）

### 缓存击穿

**问题：** 热点 key 过期瞬间，大量请求打到 DB。

**解决：**
1. **互斥锁** — 一个线程查 DB 并重建缓存，其他等待
2. **逻辑过期** — value 中包含过期时间字段，发现过期后用异步线程重建，旧值继续用
3. **永不过期** — 热点 key 不设过期时间，通过异步更新

### 缓存雪崩

**问题：** 大量 key 同时过期，或 Redis 宕机，请求全部打 DB。

**解决：**
1. **过期时间加随机值** — 避免集中过期（如 `TTL = 60 + random(0, 30)` s）
2. **多级缓存** — 本地缓存（Caffeine）+ 分布式缓存（Redis）
3. **高可用** — Redis 集群、哨兵模式
4. **熔断降级** — Hystrix/Sentinel 限流降级
5. **错峰更新** — 热点 key 不要设同一过期时间

### 缓存与数据库一致性

**Cache Aside 模式（最常用）：**
```
读：先查缓存 → 缓存命中返回 → 未命中查 DB → 写缓存
写：先更新 DB → 再删除缓存（不更新缓存，直接删除）
```

**为什么是删除缓存而非更新缓存？**
- 更新缓存容易产生并发问题（A 更新早于 B，DB 最终是 B 但缓存是 A）
- 删除缓存是幂等的，并发安全

**延迟双删（强一致性场景）：**
1. 先删除缓存
2. 更新数据库
3. 延迟再删一次缓存（延迟时间内其他请求可能读了旧 DB 值并写入缓存）

---

## 6. 高可用

### 主从复制

```
Master (写) ──replication──→ Slave (读)
```

- 全量复制（Sync）：slave 第一次连接 master 时
- 部分复制（PSync，Redis 2.8+）：基于 repl_backlog_buffer 增量同步

### 哨兵模式（Sentinel）

- 监控：Sentinel 定期 ping，判断节点是否存活
- 通知：通知管理员节点故障
- 自动故障转移：Master 宕机时，选举新 Master
- 配置提供者：客户端通过 Sentinel 获取 Master 地址

### Cluster 模式（Redis 3.0+）

- 16384 个 hash slot
- 每个节点负责一部分 slot
- 客户端计算 `CRC16(key) % 16384` 定位节点
- 使用 Hash Tag `{user:123}` 确保相关 key 在同一 slot

---

## 7. 热 Key 和大 Key

### 热 Key 问题

**场景：** 某个 key 被大量请求访问（如明星微博）。

**解决：**
1. **本地缓存** — 在应用层缓存热 key，减少 Redis 访问
2. **读写分离** — 加多个只读副本
3. **热 key 拆分** — `hotkey` → `hotkey:1`, `hotkey:2`, `hotkey:3`（客户端随机选一个）

### 大 Key 问题

**定义：** String 超过 10KB，或集合元素超过 5000 个。

**危害：** 1) 阻塞 Redis（单线程执行）；2) 网络带宽占用大；3) 主从同步慢

**解决：**
1. **拆分** — 大 Hash 按字段拆分，大 List 按时间维度拆
2. **异步删除** — Redis 4.0+ `UNLINK` 代替 `DEL`
3. **压缩** — 存入前压缩，取出后解压
4. **禁止 `KEYS *`** — 用 `SCAN` 替代
