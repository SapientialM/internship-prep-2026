---
title: 栈队列的实现为什么要用 ArrayDeque 而不是 LinkedList？
date: 2026-03-28 22:04:00
comments: false
tags: [杂谈]
categories: 杂谈

---

# 栈队列的实现为什么要用 ArrayDeque 而不是 LinkedList？

> 最近看到了这篇讨论 [java - 为什么 ArrayDeque 比 LinkedList 更好 - Stack Overflow](https://stackoverflow.com/questions/6163166/why-is-arraydeque-better-than-linkedlist)
>
> 想来解释一下为什么？

<!-- more -->

## 一句话结论

**如果你需要栈或队列，用 `ArrayDeque`，别用 `LinkedList`。**

就这么简单。不管你的教科书怎么教，不管老代码怎么写，这是 Java 6 引入 `ArrayDeque` 以来最明确的结论。

---

## 为什么大家喜欢用 LinkedList？

因为教科书这么写的：

> "链表适合频繁插入删除，数组适合随机访问。"

这句话本身没错，但**完全用错了场景**。

当我们用 `LinkedList` 做队列时：
```java
Queue<String> queue = new LinkedList<>();
```

我们不是在"**随机位置插入**"，我们只是在**两端**加东西、拿东西。这时候链表的优势根本发挥不出来，反而暴露了它的致命弱点。

---

## 核心区别：内存里的布局方式

### LinkedList —— 非连续分布

```
堆内存分布：
[0x1000] Node1: "A" -> next=0x2000
[0x2000] Node2: "B" -> next=0x3000  
[0x3000] Node3: "C" -> next=null
```

每个元素都包装成一个 `Node` 对象，散落在内存各处。遍历时 CPU 到处找数据，**缓存命中率极低**。

### ArrayDeque —— 一个大数组存储

```
数组：[B, C, _, _, _, A]
      ↑           ↑
     head        tail
```

所有元素挤在一个数组里，连续存放。CPU 一次能加载一整块，**内存连续，缓存命中高**。

---

## 性能差距有多大？

来自于我的实测数据（百万级元素）：

{% asset_img image-20260328215455452.png %}

| 操作          | ArrayDeque | LinkedList | 差距                    |
| ------------- | ---------- | ---------- | ----------------------- |
| 两端添加/删除 | 1x         | 2-3x 慢    | 链表要不断创建/销毁对象 |
| 遍历所有元素  | 1x         | 5-10x 慢   | 缓存未命中              |
| 内存占用      | 1x         | 2-3x 多    | 每个节点额外 16-24 字节 |

**链表慢不是因为算法复杂，而是因为它在跟硬件作对。**

现代 CPU 有缓存机制，访问连续内存比随机内存快 10-100 倍。`LinkedList` 的节点分散在堆内存各处，每次跳转都是一次"长途跋涉"。

---

## 那 LinkedList 真的没用吗？

有，但场景很窄：

### 1. 你要存 null
```java
queue.add(null);  // ArrayDeque 会直接抛异常
```

### 2. 迭代时频繁删除当前元素
```java
Iterator<String> it = list.iterator();
while (it.hasNext()) {
    String s = it.next();
    if (s.startsWith("tmp")) {
        it.remove();  // LinkedList 这里是 O(1)
    }
}
```
`ArrayDeque` 的迭代器删除需要 O(n)，因为数组要搬东西。

### 3. 你明确知道元素很少（<100个），且懒得改代码

差距在数据量小的时候不明显。但如果这是你写新代码的理由，那属于偷懒。

---

## 正确的打开方式

```java
// ✅ 当栈用（后进先出）
Deque<String> stack = new ArrayDeque<>();
stack.push("first");
stack.push("second");
String top = stack.pop();  // "second"

// ✅ 当队列用（先进先出）
Deque<String> queue = new ArrayDeque<>();
queue.offer("job1");
queue.offer("job2");
String job = queue.poll();  // "job1"

// ✅ 双端队列
Deque<String> deque = new ArrayDeque<>();
deque.addFirst("front");
deque.addLast("back");
```

**注意**：`ArrayDeque` 不实现 `List` 接口，所以不能用 `get(0)`。如果你需要索引访问，那它不适合。

---

## 一个常见的误区

有人说：`LinkedList` 添加元素是严格 O(1)，`ArrayDeque` 扩容时要复制数组，不稳定。

**这只是理论上的。**

实际应用中：
1. 扩容是**摊销 O(1)**，发生频率随数据量增加而指数下降
2. 即使扩容，**复制连续内存**的速度也远快于链表创建节点的开销
3. 你可以预分配容量消除这个问题：
   ```java
   new ArrayDeque<>(10000);  // 一次性分配好
   ```

**链表创建节点要申请内存、初始化对象、最终还要 GC 回收**，这些隐性成本比数组扩容高得多。

---

## 总结

| 情况             | 用什么         |
| ---------------- | -------------- |
| 栈/队列/双端队列 | **ArrayDeque** |
| 需要存 null      | LinkedList     |
| 迭代中频繁删元素 | LinkedList     |
| 需要按索引访问   | ArrayList      |
