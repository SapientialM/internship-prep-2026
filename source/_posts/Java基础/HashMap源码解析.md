---
title: HashMap源码深度解析
date: 2024-01-15 10:00:00
tags: [Java, HashMap, 源码, 集合]
categories: Java基础
description: 深入剖析HashMap源码，包括JDK 1.7和1.8的区别、put流程、扩容机制等
---

# HashMap 源码深度解析

> 📅 创建日期：2024-01-15
> 🔄 最后更新：2024-01-15
> 📂 分类：Java基础 / 集合框架
> 🏷️ 标签：Java, HashMap, 源码, 集合
> ⭐ 重要程度：⭐⭐⭐⭐⭐

---

## 1. 问题/知识点概述

### 1.1 背景

HashMap是Java中最常用的集合类之一，几乎是面试必考点。理解其源码对于理解Java集合框架、散列表原理、并发编程等都有帮助。

### 1.2 核心概念

- **散列表(哈希表)**：基于数组实现，通过哈希函数将键映射到数组索引
- **哈希冲突**：不同键计算出相同索引，解决方法：链表法、开放寻址法
- **负载因子**：默认0.75，表示桶的使用率，超过则扩容

### 1.3 适用场景

- ✅ 适用：快速查找、插入、删除；键值对存储
- ❌ 不适用：需要有序遍历；多线程环境(应使用ConcurrentHashMap)

---

## 2. 详细内容

### 2.1 数据结构演进

#### JDK 1.7：数组 + 链表

```
数组
+---+---+---+---+---+---+---+---+
| 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 |
+---+---+---+---+---+---+---+---+
      |           |
      v           v
    +---+       +---+
    | A |       | B |
    +---+       +---+
      |           |
      v           v
    +---+       +---+
    | C |       | D |
    +---+       +---+
```

**问题**：链表过长时查询效率低(O(n))；并发扩容可能出现死循环

#### JDK 1.8：数组 + 链表/红黑树

```
数组
+---+---+---+---+---+---+---+---+
| 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 |
+---+---+---+---+---+---+---+---+
      |           |
      v           v
    红黑树      链表(<8)
      B
     / \
    A   D
       /
      C
```

**改进**：链表长度≥8时转为红黑树，查询效率O(log n)

---

### 2.2 核心源码分析

#### 关键常量

```java
// 默认初始容量16，必须是2的幂
static final int DEFAULT_INITIAL_CAPACITY = 1 << 4;

// 最大容量 2^30
static final int MAXIMUM_CAPACITY = 1 << 30;

// 默认负载因子0.75
static final float DEFAULT_LOAD_FACTOR = 0.75f;

// 链表转红黑树阈值
static final int TREEIFY_THRESHOLD = 8;

// 红黑树转链表阈值
static final int UNTREEIFY_THRESHOLD = 6;

// 最小树化容量(桶数组长度<64时优先扩容)
static final int MIN_TREEIFY_CAPACITY = 64;
```

#### 核心节点类

```java
// 基础节点
static class Node<K,V> implements Map.Entry<K,V> {
    final int hash;    // 哈希值
    final K key;       // 键
    V value;           // 值
    Node<K,V> next;    // 下一个节点
    
    // ...
}

// 红黑树节点
static final class TreeNode<K,V> extends LinkedHashMap.Entry<K,V> {
    TreeNode<K,V> parent;
    TreeNode<K,V> left;
    TreeNode<K,V> right;
    TreeNode<K,V> prev;
    boolean red;
    
    // ...
}
```

#### hash()方法 - 扰动函数

```java
static final int hash(Object key) {
    int h;
    // 高16位与低16位异或，减少哈希冲突
    return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
}
```

**为什么这样设计？**
- `hashCode()`是int类型，32位
- 如果直接用`hashCode % length`，只有低位参与运算
- 通过`hashCode ^ (hashCode >>> 16)`，让高16位也参与运算
- 降低冲突概率，分布更均匀

#### put()方法流程

```java
public V put(K key, V value) {
    return putVal(hash(key), key, value, false, true);
}

final V putVal(int hash, K key, V value, boolean onlyIfAbsent, boolean evict) {
    Node<K,V>[] tab; Node<K,V> p; int n, i;
    
    // 1. 数组为空或长度为0，初始化数组
    if ((tab = table) == null || (n = tab.length) == 0)
        n = (tab = resize()).length;
    
    // 2. 计算索引，如果该位置为空，直接插入
    if ((p = tab[i = (n - 1) & hash]) == null)
        tab[i] = newNode(hash, key, value, null);
    
    else {
        Node<K,V> e; K k;
        
        // 3. 首节点key相同，覆盖
        if (p.hash == hash &&
            ((k = p.key) == key || (key != null && key.equals(k))))
            e = p;
        
        // 4. 首节点是红黑树节点
        else if (p instanceof TreeNode)
            e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
        
        // 5. 链表遍历
        else {
            for (int binCount = 0; ; ++binCount) {
                // 链表尾部插入新节点
                if ((e = p.next) == null) {
                    p.next = newNode(hash, key, value, null);
                    // 链表长度≥8，转红黑树
                    if (binCount >= TREEIFY_THRESHOLD - 1)
                        treeifyBin(tab, hash);
                    break;
                }
                // 找到相同key
                if (e.hash == hash &&
                    ((k = e.key) == key || (key != null && key.equals(k))))
                    break;
                p = e;
            }
        }
        
        // 6. 覆盖旧值
        if (e != null) {
            V oldValue = e.value;
            if (!onlyIfAbsent || oldValue == null)
                e.value = value;
            afterNodeAccess(e);
            return oldValue;
        }
    }
    
    // 7. 修改计数，检查扩容
    ++modCount;
    if (++size > threshold)
        resize();
    afterNodeInsertion(evict);
    return null;
}
```

**put流程总结：**
1. 计算key的hash值
2. 数组为空则初始化
3. 计算索引位置 `(n-1) & hash`
4. 位置为空：直接插入
5. 位置不为空：
   - key相同：覆盖value
   - 是树节点：按红黑树方式插入
   - 是链表节点：尾插法，长度≥8转树
6. 检查是否需要扩容

#### resize()扩容方法

```java
final Node<K,V>[] resize() {
    Node<K,V>[] oldTab = table;
    int oldCap = (oldTab == null) ? 0 : oldTab.length;
    int oldThr = threshold;
    int newCap, newThr = 0;
    
    if (oldCap > 0) {
        // 超过最大容量，不再扩容
        if (oldCap >= MAXIMUM_CAPACITY) {
            threshold = Integer.MAX_VALUE;
            return oldTab;
        }
        // 容量翻倍
        else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                 oldCap >= DEFAULT_INITIAL_CAPACITY)
            newThr = oldThr << 1; // 阈值翻倍
    }
    // 初始化
    else if (oldThr > 0)
        newCap = oldThr;
    else {
        newCap = DEFAULT_INITIAL_CAPACITY;
        newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
    }
    
    threshold = newThr;
    
    @SuppressWarnings({"rawtypes","unchecked"})
    Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap];
    table = newTab;
    
    // 迁移旧数据到新数组
    if (oldTab != null) {
        for (int j = 0; j < oldCap; ++j) {
            Node<K,V> e;
            if ((e = oldTab[j]) != null) {
                oldTab[j] = null;
                // 单个节点
                if (e.next == null)
                    newTab[e.hash & (newCap - 1)] = e;
                // 红黑树
                else if (e instanceof TreeNode)
                    ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);
                // 链表
                else {
                    Node<K,V> loHead = null, loTail = null;
                    Node<K,V> hiHead = null, hiTail = null;
                    Node<K,V> next;
                    
                    // 拆分红黑树或链表
                    do {
                        next = e.next;
                        // 判断hash新增的高位是0还是1
                        if ((e.hash & oldCap) == 0) {
                            // 低位链表：位置不变
                            if (loTail == null)
                                loHead = e;
                            else
                                loTail.next = e;
                            loTail = e;
                        }
                        else {
                            // 高位链表：位置 = 原索引 + oldCap
                            if (hiTail == null)
                                hiHead = e;
                            else
                                hiTail.next = e;
                            hiTail = e;
                        }
                    } while ((e = next) != null);
                    
                    if (loTail != null) {
                        loTail.next = null;
                        newTab[j] = loHead;
                    }
                    if (hiTail != null) {
                        hiTail.next = null;
                        newTab[j + oldCap] = hiHead;
                    }
                }
            }
        }
    }
    return newTab;
}
```

**扩容关键点：**
1. 容量翻倍（2倍）
2. 链表拆分为两个链表：
   - `hash & oldCap == 0`：位置不变
   - `hash & oldCap == 1`：位置 = 原位置 + oldCap
3. 无需重新计算hash，位运算效率高

---

## 3. 常见问题与解决方案

### 3.1 为什么容量必须是2的幂？

**原因1：位运算替代取模**
```java
// 如果容量是2的幂，n = 16
// hash % n 等价于 hash & (n - 1)
// 位运算比取模运算快得多
```

**原因2：扩容时的rehash优化**
```java
// 扩容后，元素要么在原位置，要么在原位置+旧容量
// 只需要判断hash的高位即可，无需重新计算hash
```

---

### 3.2 为什么链表转红黑树的阈值是8？

根据源码注释中的统计：
```java
/*
 * 理想情况下，在随机hashCode下，桶中节点频率遵循泊松分布
 * 0:    0.60653066
 * 1:    0.30326533
 * 2:    0.07581633
 * 3:    0.01263606
 * 4:    0.00157952
 * 5:    0.00015795
 * 6:    0.00001316
 * 7:    0.00000094
 * 8:    0.00000006
 * ...
 */
```

- 链表长度达到8的概率已经非常小(0.000006%)
- 如果频繁出现，说明hash函数有问题或遭受攻击
- 树节点占用空间是链表节点的2倍，不能轻易转换

---

### 3.3 为什么用红黑树不用AVL树？

- **AVL树**：更严格的平衡，查询更快，但旋转操作更多
- **红黑树**：近似平衡，插入删除旋转更少
- **HashMap场景**：插入删除频繁，红黑树综合性能更好

---

## 4. JDK 1.7 vs 1.8 对比

| 特性 | JDK 1.7 | JDK 1.8 |
|:---:|:---|:---|
| 数据结构 | 数组+链表 | 数组+链表/红黑树 |
| 插入方式 | 头插法 | 尾插法 |
| 哈希计算 | 4次位运算+5次异或 | 1次位运算+1次异或(扰动) |
| 扩容 | 重新计算hash | 判断高位，位置不变或+旧容量 |
| 并发安全 | 死循环问题 | 仍然存在，但不死循环 |

---

## 5. 最佳实践

### 5.1 Do's ✅

1. **预估容量初始化**
   ```java
   // 已知要存放1000个元素
   // 1000 / 0.75 = 1333，取2的幂 = 2048
   Map<String, String> map = new HashMap<>(2048);
   ```

2. **使用不可变对象作为key**
   - String、Integer等包装类都是好的选择
   - 自定义对象需要正确重写equals和hashCode

3. **多线程环境使用ConcurrentHashMap**

### 5.2 Don'ts ❌

1. **不要在遍历时修改结构**
   ```java
   // 错误！会抛出ConcurrentModificationException
   for (String key : map.keySet()) {
       map.remove(key);
   }
   
   // 正确做法1：使用Iterator
   Iterator<Map.Entry<String, String>> it = map.entrySet().iterator();
   while (it.hasNext()) {
       it.next();
       it.remove();
   }
   
   // 正确做法2：使用removeIf (Java 8+)
   map.entrySet().removeIf(entry -> entry.getValue() == null);
   ```

2. **不要依赖HashMap的顺序**

3. **不要用浮点数作为key**

---

## 6. 面试高频题

### Q1: HashMap的底层原理？

**回答要点：**
1. 基于散列表，数组+链表/红黑树
2. put流程：hash计算→寻址→处理冲突
3. 扩容机制：2倍扩容，元素重新分布
4. JDK 1.8的优化：红黑树、尾插法

### Q2: 为什么HashMap线程不安全？

**JDK 1.7：** 并发扩容时链表可能形成环，get时死循环
**JDK 1.8：** 不会死循环，但可能数据丢失

### Q3: HashMap和Hashtable的区别？

| 对比项 | HashMap | Hashtable |
|:---|:---|:---|
| 线程安全 | 否 | 是(synchronized) |
| 性能 | 更高 | 较低 |
| null键值 | 允许 | 不允许 |
| 出现版本 | JDK 1.2 | JDK 1.0 |

---

## 7. 相关资料

### 7.1 参考来源

- 📖 **源码**：OpenJDK 8
- 🔗 [HashMap源码分析](https://blog.csdn.net/v123411739/article/details/78996181)
- 📺 [极客时间 - Java核心技术面试精讲](https://time.geekbang.org/column/article/8053)

### 7.2 相关笔记

- [ConcurrentHashMap源码分析](../Java并发/ConcurrentHashMap源码分析.md)
- [红黑树原理](../算法与数据结构/红黑树.md)

### 7.3 面试题引用

- [字节跳动后端面经](../面经记录/字节跳动-后端开发-一面.md)

---

## 8. 个人总结

### 8.1 核心要点回顾

1. 扰动函数：`hash ^ (hash >>> 16)`，让高位参与运算
2. 容量必须是2的幂：方便位运算和rehash
3. 链表转红黑树阈值8：概率统计结果
4. 扩容机制：2倍扩容，元素位置 = 原位置 或 原位置+旧容量

### 8.2 记忆技巧

- "扰动函数记：高低异或散列均"
- "容量2的幂：取模变位运"
- "扩容不用重算：只看高位是0是1"

### 8.3 后续待深入

- [ ] HashMap的fast-fail机制源码
- [ ] LinkedHashMap的实现
- [ ] 自定义hashCode的最佳实践

---

> 💡 **更新日志**
> - 2024-01-15: 创建笔记，完成基础内容
