# 11 - 问题排查

> 线上问题排查是后端工程师的必备技能。CPU、内存、GC、死锁是最常见的排查方向。

---

## 1. CPU 100% 排查

### 排查步骤

```bash
# 1. 找到 CPU 最高的进程
top -c

# 2. 找到该进程中 CPU 最高的线程
top -Hp <pid>
# 或
ps -mp <pid> -o THREAD,tid,time

# 3. 将线程 ID 转为 16 进制（Java 线程栈中用 16 进制表示 nid）
printf "%x\n" <tid>

# 4. 导出线程栈，搜索该线程
jstack <pid> | grep -A 20 <十六进制tid>
```

### 常见原因

| 原因 | 特征 | 解决方案 |
|:---|:---|:---|
| 死循环 | 某个线程始终在同一个方法内 | 检查循环条件和退出机制 |
| GC 频繁 | GC 线程 CPU 高 | 分析堆内存，调整 GC 策略 |
| 正则表达式回溯 | 线程卡在 regex 相关方法 | 优化正则（避免 `.*` 等贪婪模式） |
| JSON 序列化过大 | 大量数据序列化 | 限制数据量、使用流式序列化 |

---

## 2. 内存泄漏排查

### 排查步骤

```bash
# 1. 导出堆 Dump
jmap -dump:format=b,file=heap.hprof <pid>
# 或 JVM 参数自动导出
-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/path/to/dump

# 2. 分析 Dump 文件（使用 MAT / JProfiler / VisualVM）
# 重点关注：内存占用最大的对象、GC Root 引用链
```

### 常见原因

| 原因 | 示例 |
|:---|:---|
| ThreadLocal 未清理 | 线程池中线程的 ThreadLocal 变量不释放 |
| 静态集合持续添加 | `static List<?>` 持续 add 不 remove |
| 连接未关闭 | 数据库连接、文件流、网络连接未在 finally 中关闭 |
| 监听器未注销 | 注册了 listener 但不取消注册 |
| finalize() 阻塞 | 对象重写 finalize 方法，导致 Finalizer 队列堆积 |

### 常用工具

| 工具 | 用途 |
|:---|:---|
| jstat | 查看 GC 统计、类加载信息 |
| jmap | 导出堆 Dump、查看堆信息 |
| jstack | 线程栈快照 |
| jinfo | 查看 JVM 运行参数 |
| jhat | Dump 分析（已废弃，建议用 MAT） |
| MAT / JProfiler | 堆 Dump 分析 |
| Arthas | 在线诊断（阿里开源，强烈推荐） |

---

## 3. 死锁排查

### 排查步骤

```bash
# 1. jstack 直接支持死锁检测
jstack <pid> | grep -A 50 "Found one Java-level deadlock"

# 2. 或者用 Arthas
thread -b  # 查找阻塞线程和死锁
```

### 解决方式

1. 按统一顺序加锁（最推荐）
2. 使用 `ReentrantLock.tryLock(timeout)` 代替 `synchronized`
3. 减小锁粒度，尽量减少持有锁的时间

---

## 4. OOM 排查

| 异常类型 | 原因 | 排查方向 |
|:---|:---|:---|
| `Java heap space` | 堆内存不足 | 查大对象、内存泄漏 |
| `Metaspace` | 方法区不足 | 查动态生成的类（CGLIB、反射） |
| `GC overhead limit exceeded` | GC 占用 98% 以上 CPU 但只回收不到 2% 内存 | 几乎内存耗尽 |
| `Direct buffer memory` | 直接内存不足 | 查 NIO 使用（`-XX:MaxDirectMemorySize`） |
| `unable to create new native thread` | 无法创建新线程（`ulimit -u`限制） | 减少线程数或修改系统限制 |

---

## 5. 接口响应慢排查

### 排查思路

```
1. 确认哪个环节慢
   ├── 客户端 → 网络（ping/网络延迟）
   ├── 应用服务器 → 代码（Arthas trace/火焰图）
   ├── 缓存（缓存命中率低/大 Key）
   ├── 数据库（慢查询/索引/连接池满）
   └── 下游服务（RPC 调用慢/超时）

2. 常用工具
   ├── Arthas trace: 追踪方法调用链及耗时
   ├── Arthas monitor: 监控方法调用统计
   ├── 火焰图: 分析 CPU 热点
   └── SkyWalking/Pinpoint: 分布式链路追踪
```

### 数据库慢查询分析

```sql
-- 开启慢查询日志
SET GLOBAL slow_query_log = ON;
SET GLOBAL long_query_time = 1;

-- 查看慢查询日志
SHOW VARIABLES LIKE 'slow_query_log_file';

-- EXPLAIN 分析
EXPLAIN SELECT * FROM orders WHERE user_id = 123;
-- 关注: type(避免ALL), rows, Extra(避免Using filesort, Using temporary)
```
