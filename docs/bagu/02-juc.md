# 02 - JUC 并发编程

> 并发编程是后端面试的核心区分点。线程池、锁、AQS、ThreadLocal 几乎每轮必问。

---

## 1. 线程基础

### 线程的六种状态

Java 线程在 `Thread.State` 中定义了六种状态：

| 状态 | 说明 | 进入方式 |
|:---|:---|:---|
| NEW | 线程创建但未启动 | `new Thread()` |
| RUNNABLE | 就绪或运行中 | `thread.start()` |
| BLOCKED | 等待获取锁 | `synchronized` 竞争失败 |
| WAITING | 无限等待 | `wait()`, `join()`, `LockSupport.park()` |
| TIMED_WAITING | 计时等待 | `sleep(ms)`, `wait(ms)`, `join(ms)` |
| TERMINATED | 线程结束 | `run()` 执行完毕 |

**状态流转：**
```
NEW → RUNNABLE → TERMINATED
       ↑  ↓
    BLOCKED  ← →  WAITING / TIMED_WAITING
```

---

## 2. 创建线程的方式

1. **继承 Thread 类** — 重写 `run()` 方法，无法继承其他类
2. **实现 Runnable 接口** — 实现 `run()` 方法，无返回值
3. **实现 Callable 接口** — 实现 `call()` 方法，有返回值可抛异常，配合 `FutureTask`
4. **线程池** — 通过 `ExecutorService`，实现线程复用

**Callable 和 Runnable 的区别：**
- Runnable 无返回值，不抛受检异常
- Callable 有返回值（泛型），可抛受检异常

---

## 3. 线程池

### 核心参数（ThreadPoolExecutor）

```java
public ThreadPoolExecutor(int corePoolSize,      // 核心线程数
                          int maximumPoolSize,   // 最大线程数
                          long keepAliveTime,    // 空闲线程存活时间
                          TimeUnit unit,
                          BlockingQueue<Runnable> workQueue,  // 任务队列
                          ThreadFactory threadFactory,
                          RejectedExecutionHandler handler)   // 拒绝策略
```

### 线程池执行流程

```
提交任务
   │
   ▼
核心线程数未满？ ──是──→ 创建核心线程执行
   │否
   ▼
任务队列未满？ ──是──→ 放入队列等待
   │否
   ▼
最大线程数未满？ ──是──→ 创建非核心线程执行
   │否
   ▼
执行拒绝策略
```

### 四种内置线程池（不推荐直接使用）

| 类型 | 特点 | 问题 |
|:---|:---|:---|
| `FixedThreadPool` | 固定核心线程数，无界队列 | 队列可能 OOM |
| `CachedThreadPool` | 最大线程数为 Integer.MAX_VALUE | 线程数可能 OOM |
| `SingleThreadPool` | 单线程，无界队列 | 队列可能 OOM |
| `ScheduledThreadPool` | 支持定时和周期任务 | 最大线程数为 Integer.MAX_VALUE |

**阿里的规范：** 强制使用 `ThreadPoolExecutor` 手动创建，不允许使用 Executors。

### 拒绝策略

| 策略 | 行为 |
|:---|:---|
| `AbortPolicy`（默认） | 抛 RejectedExecutionException |
| `CallerRunsPolicy` | 由调用者线程执行任务 |
| `DiscardPolicy` | 静默丢弃任务 |
| `DiscardOldestPolicy` | 丢弃最旧的任务，重新提交 |

---

## 4. 锁

### synchronized

**锁升级过程（JDK 1.6 之后）：**
```
无锁 → 偏向锁 → 轻量级锁（自旋锁）→ 重量级锁
```

- **偏向锁：** 锁总是被同一线程获取，在对象头标记线程 ID
- **轻量级锁：** 多线程竞争不激烈，通过 CAS + 自旋尝试获取
- **重量级锁：** 竞争激烈，自旋超过一定次数（默认10）或自旋线程超过 CPU 核数一半时膨胀，进入内核态阻塞

**synchronized 和 Lock 的区别：**

| 特性 | synchronized | Lock |
|:---|:---|:---|
| 类型 | 关键字（JVM级别） | 接口（JDK类） |
| 锁释放 | 代码块执行完或异常时自动释放 | 必须在 finally 中手动 unlock |
| 中断 | 不可中断等待 | `lockInterruptibly()` 可中断 |
| 公平 | 非公平 | 可选公平/非公平 |
| 条件 | 一个条件队列 | 多个 Condition |
| 尝试获取 | 不支持 | `tryLock(timeout)` |

### AQS（AbstractQueuedSynchronizer）

AQS 是 JUC 锁和同步器的基础框架，核心是一个 **volatile int state** + **CLH 双向队列**。

**核心原理：**
- 用 state 表示同步状态（0=未锁定，1=已锁定）
- 获取锁失败时，用 CAS 将线程加入 CLH 队列尾部并 park
- 释放锁时，unpark 队列头部线程

**基于 AQS 的实现：**
- `ReentrantLock` — 可重入独占锁
- `Semaphore` — 信号量（共享锁，控制并发数）
- `CountDownLatch` — 倒计数器
- `ReentrantReadWriteLock` — 读写锁

### ReentrantLock 可重入原理

通过 state 记录重入次数：
- 加锁：state + 1
- 释放：state - 1
- state = 0 时真正释放锁

### synchronized 和 ReentrantLock 如何选择？

- **优先使用 synchronized**（JVM 自动优化，自动释放，无需担心死锁）
- 需要**手动控制锁释放**、**尝试获取锁**、**公平锁**、**多条件等待**时用 ReentrantLock

---

## 5. ThreadLocal

### 数据结构

每个 Thread 内部有一个 `ThreadLocalMap`，Key 是 ThreadLocal 的弱引用，Value 是存储的值。

```java
Thread.threadLocals  →  ThreadLocalMap
                            ├─ Entry(WeakRef<ThreadLocal1>, value1)
                            ├─ Entry(WeakRef<ThreadLocal2>, value2)
                            └─ ...
```

### 内存泄漏问题

**原因：** ThreadLocalMap 中 Entry 的 Key 是弱引用，GC 时 Key 被回收变成 null，但 Value 是强引用，只要线程还存活，Value 就不会被回收。

**解决：** 使用完 ThreadLocal 后**必须调用 `remove()`**。ThreadLocal 的 `get()`/`set()` 方法在操作时会顺便清理 Key 为 null 的 Entry，但 `remove()` 是最可靠的。

### 使用场景

1. **Spring 事务管理** — 同一个线程内共享数据库连接
2. **日志 Trace ID** — 全链路追踪
3. **用户上下文** — 登录用户信息传递（避免参数层层传递）
4. **SimpleDateFormat 线程安全** — 每个线程持有自己的实例

### InheritableThreadLocal

父线程 set 的值可以传递给子线程（在线程创建时复制）。但线程池场景下线程复用会导致值不更新，需要配合 `TransmittableThreadLocal`（阿里的开源组件）。

---

## 6. volatile

### 两大特性

1. **可见性** — 一个线程修改 volatile 变量后，其他线程立即可见（写后立即刷回主存，读时从主存取）
2. **禁止指令重排序** — 通过内存屏障实现（LoadLoad、StoreStore、LoadStore、StoreLoad）

### 双重检查锁定（DCL）为什么需要 volatile？

```java
public class Singleton {
    private volatile static Singleton instance;
    
    public static Singleton getInstance() {
        if (instance == null) {
            synchronized (Singleton.class) {
                if (instance == null) {
                    instance = new Singleton();  // 非原子操作
                }
            }
        }
        return instance;
    }
}
```

`new Singleton()` 分为三步：1) 分配内存 2) 初始化对象 3) 将引用指向内存。如果没有 volatile，步骤2和3可能发生指令重排序，导致其他线程拿到未初始化完成的对象。

### volatile 不能保证原子性

`count++` 这类复合操作即使使用 volatile 也不安全，因为读取和写入是两个操作。需要用 `synchronized`、`AtomicInteger` 或 `LongAdder`。

---

## 7. CAS

### 原理

CAS（Compare And Swap）通过三个操作数：内存地址 V、旧的预期值 A、要修改的新值 B。当且仅当 V == A 时，将 V 修改为 B，否则自旋重试。

### ABA 问题

线程1将 A → B → A，线程2的 CAS 操作看到值仍然是 A，认为没有变化。

**解决：** `AtomicStampedReference` 或 `AtomicMarkableReference`（加版本号/标记）。

### 自旋开销

高并发下 CAS 可能自旋很久，消耗 CPU。JDK 8 引入 `LongAdder`，将单变量的竞争分散到多个 Cell 上，减少自旋。
