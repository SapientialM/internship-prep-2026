# 01 - JavaSE

> Java 基础是面试的起点，集合、反射、动态代理、泛型是高频考点。

---

## 1. 集合框架

### ArrayList vs LinkedList

| 特性 | ArrayList | LinkedList |
|:---|:---|:---|
| 底层结构 | Object[] 数组 | 双向链表 |
| 随机访问 | O(1) | O(n) |
| 头部插入 | O(n)（需要整体移动） | O(1) |
| 尾部插入 | O(1) 均摊 | O(1) |
| 内存占用 | 连续内存，空间浪费较少 | 每个节点额外存储前后指针 |
| 适用场景 | 读多写少 | 写多读少（尤其是头部插入） |

**扩容机制：** ArrayList 默认容量10，扩容为原来的 1.5 倍（`newCapacity = oldCapacity + (oldCapacity >> 1)`），通过 `Arrays.copyOf` 复制到新数组。

**为什么 ArrayList 的 elementData 用 transient 修饰？**

因为 elementData 数组的实际容量通常大于已使用的元素数量，直接序列化会浪费空间。ArrayList 通过自定义 `writeObject` 和 `readObject` 方法，只序列化实际元素。

### HashMap

**JDK 1.7 vs 1.8：**

| 特性 | JDK 1.7 | JDK 1.8 |
|:---|:---|:---|
| 底层结构 | 数组 + 链表 | 数组 + 链表 + 红黑树 |
| 链表插入 | 头插法 | 尾插法 |
| 哈希算法 | 4次扰动 | 1次扰动（高16位异或低16位） |
| 扩容条件 | size ≥ threshold && table[i] != null | size ≥ threshold |
| 树化阈值 | 无 | 链表长度 ≥ 8 且数组长度 ≥ 64 |

**为什么链表长度 ≥ 8 转红黑树？**

根据泊松分布，在负载因子 0.75 下，链表长度达到 8 的概率只有千万分之六。8 是在空间开销和性能之间的平衡点。

**为什么扩容是 2 的幂？**

1. 取模运算优化：`hash % length` 等价于 `hash & (length - 1)`，位运算效率更高
2. 扩容时元素迁移更简单：元素要么在原位置，要么在原位置 + 旧容量

**为什么线程不安全？（JDK 1.7 头插法死循环）**

JDK 1.7 使用头插法，扩容时链表反转，多线程并发扩容会形成环形链表，导致 `get()` 时 CPU 100%。JDK 1.8 改用尾插法避免了这个死循环，但仍存在数据覆盖问题，建议用 `ConcurrentHashMap`。

### ConcurrentHashMap

**JDK 1.7 Segment 分段锁：**
- 默认 16 个 Segment，每个 Segment 继承 ReentrantLock
- put 时先定位 Segment，再锁住该 Segment
- 并发度 = Segment 数量（默认 16）

**JDK 1.8 CAS + synchronized：**
- 抛弃 Segment，使用 Node 数组 + CAS + synchronized
- put 时如果槽为空，用 CAS 设置；如果槽非空，用 synchronized 锁头节点
- 锁粒度更细（槽级别 vs 段级别）
- 扩容支持多线程协同（transferIndex 分段迁移）

### CopyOnWriteArrayList

**核心思想：** 读操作不加锁，写操作加 ReentrantLock + 复制新数组。

**适用场景：** 读多写少的并发场景（如黑名单、监听器列表）。

**缺点：** 1) 内存占用高（写时复制完整数组）；2) 只能保证最终一致性，不能保证实时一致性。

---

## 2. 反射

### 什么是反射？

反射允许程序在运行时动态获取类的完整信息（构造器、字段、方法），并操作对象。

### 获取 Class 对象的三种方式

```java
// 1. 类名.class
Class<Student> clazz1 = Student.class;

// 2. 对象.getClass()
Class<?> clazz2 = student.getClass();

// 3. Class.forName(全限定类名)
Class<?> clazz3 = Class.forName("com.example.Student");
```

### 反射的应用场景

1. **Spring IoC** — 通过反射实例化 Bean
2. **JDK 动态代理** — `Proxy.newProxyInstance()` 底层依赖反射
3. **JDBC 驱动加载** — `Class.forName("com.mysql.cj.jdbc.Driver")`
4. **序列化框架** — Jackson/Gson 通过反射读写字段

### 反射的性能开销

反射比直接调用慢的主要原因是：1) 需要检查方法可见性；2) 需要自动装箱拆箱；3) 无法被 JIT 内联优化。优化方式：`setAccessible(true)` 关闭访问检查；缓存 Method/Field 对象。

---

## 3. 动态代理

### JDK 动态代理 vs CGLIB

| 特性 | JDK 动态代理 | CGLIB |
|:---|:---|:---|
| 原理 | 基于接口，生成 `$Proxy` 类 | 基于继承，生成目标类的子类 |
| 要求 | 目标类必须实现接口 | 目标类不能是 final，方法不能是 final |
| 性能 | 反射调用，稍慢 | 直接调用，较快 |
| 依赖 | JDK 内置 | 需引入 CGLIB 库 |
| Spring 选择 | 默认（目标有接口时） | 无接口时使用 |

**Spring Boot 2.x/3.x 变化：** Spring Boot 2.0 之后，默认代理策略从 JDK 改为 CGLIB（`proxy-target-class=true` 作为默认值），因为开发体验更好，不需要强依赖接口。

### JDK 动态代理底层原理

1. `Proxy.newProxyInstance()` 调用 `ProxyGenerator.generateProxyClass()` 生成字节码
2. 生成的 `$Proxy` 类继承 `Proxy` 并实现目标接口
3. 每个方法调用都会被转发到 `InvocationHandler.invoke()`
4. 生成的字节码通过 native 方法 `defineClass0` 加载到 JVM

详细手写实现见：[手写动态代理](/handwritten/dynamic-proxy)

---

## 4. 泛型

### 类型擦除

Java 的泛型是编译时特性，编译后会被擦除：
- 无界类型 `T` → `Object`
- 有界类型 `T extends Number` → `Number`

```java
List<String> list1 = new ArrayList<>();
List<Integer> list2 = new ArrayList<>();
// 运行时两者都是 ArrayList.class，泛型信息被擦除
System.out.println(list1.getClass() == list2.getClass()); // true
```

### 桥接方法

当子类重写泛型方法导致签名不一致时，编译器自动生成桥接方法来保证多态。例如：

```java
class Parent<T> {
    public T getValue() { return null; }
}
class Child extends Parent<String> {
    @Override
    public String getValue() { return "hello"; }
}
// 编译器生成桥接方法: public Object getValue() { return this.getValue(); }
```

### PECS 原则

Producer Extends, Consumer Super：
- `? extends T` — 只能读取，不能写入（生产者）
- `? super T` — 只能写入，读取为 Object（消费者）

---

## 5. 深拷贝 vs 浅拷贝

- **浅拷贝：** 基本类型复制值，引用类型复制引用（指向同一对象）
- **深拷贝：** 基本类型复制值，引用类型递归复制（创建全新对象）

实现深拷贝的方式：1) 实现 `Cloneable` 并手动递归 clone；2) 通过序列化/反序列化；3) 通过 JSON 序列化（Jackson/Gson）；4) 使用 `BeanUtils.copyProperties`（仅浅拷贝）。

---

## 6. String、StringBuilder、StringBuffer

| 特性 | String | StringBuilder | StringBuffer |
|:---|:---|:---|:---|
| 可变性 | 不可变(final class) | 可变 | 可变 |
| 线程安全 | 安全（不可变） | 不安全 | 安全（synchronized） |
| 性能 | 拼接时创建新对象 | 快 | 较慢（有锁开销） |
| 适用场景 | 少量字符串操作 | 单线程大量拼接 | 多线程大量拼接 |

**String 为什么设计为不可变？**

1. 线程安全 — 不可变对象天然线程安全
2. 字符串常量池 — 可复用相同字符串，节省内存
3. 安全性 — 类加载器、网络连接参数等使用 String，不可变防止被篡改
4. HashMap 的 key — 不可变保证 hashCode 稳定性
