---
title: 手写 ArrayList 和 LinkedList - 深入理解 Java 集合框架
date: 2026-03-30 00:00:00
tags: [手写笔记, Java基础, 数据结构, 面试]
categories: 手写笔记
---

# 手写 ArrayList 和 LinkedList



<!-- more -->



---

## 一、List 接口定义

```java
package com.sap.collections;

public interface List<E> extends Iterable<E> {
    void add(E element);           // 尾部添加
    void add(E element, int index); // 指定位置插入
    E remove(int index);           // 按索引删除
    boolean remove(E element);     // 按元素删除
    E set(int index, E element);   // 修改指定位置元素
    E get(int index);              // 获取指定位置元素
    int size();                    // 获取元素个数
}
```

---

## 二、手写 ArrayList

### 2.1 核心原理

```
┌─────────────────────────────────────────────────────────┐
│  ArrayList 底层是 Object 数组，支持动态扩容               │
├─────────────────────────────────────────────────────────┤
│  初始容量：10（这里简化实现）                              │
│  扩容策略：原容量的 2 倍                                  │
│  扩容方式：创建新数组 + System.arraycopy                   │
└─────────────────────────────────────────────────────────┘

初始状态：[_, _, _, _, _, _, _, _, _, _]  size=0, capacity=10
         ↑
        待插入

添加 A 后：[A, _, _, _, _, _, _, _, _, _]  size=1
          
添加 B,C：[A, B, C, _, _, _, _, _, _, _]  size=3

在 index=1 插入 X：[A, X, B, C, _, _, _, _, _, _]  
                  // index 后的元素全部后移
```

### 2.2 手写代码

```java
package com.sap.collections;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

public class ArrayList<E> implements List<E> {
    private Object[] table = new Object[10];  // 初始容量 10
    private int size;  // 实际元素个数

    // ========== 添加元素 ==========
    @Override
    public void add(E element) {
        if (size == table.length) {
            resize();  // 扩容
        }
        table[size++] = element;
    }

    // 指定位置插入
    @Override
    public void add(E element, int index) {
        if (index > size || index < 0) {
            throw new IndexOutOfBoundsException();
        }
        if (size == table.length) {
            resize();
        }
        // index 及之后的元素后移一位
        System.arraycopy(table, index, table, index + 1, size - index);
        table[index] = element;
        size++;
    }

    // 扩容：创建 2 倍大小的新数组，拷贝旧数据
    private void resize() {
        Object[] newTable = new Object[table.length * 2];
        System.arraycopy(table, 0, newTable, 0, table.length);
        this.table = newTable;
    }

    // ========== 删除元素 ==========
    @Override
    public E remove(int index) {
        if (index >= size || index < 0) {
            throw new IndexOutOfBoundsException();
        }
        E removedElement = (E) table[index];
        // index 之后的元素前移一位
        System.arraycopy(table, index + 1, table, index, size - index - 1);
        size--;
        table[size] = null;  // 防止内存泄漏！
        return removedElement;
    }

    @Override
    public boolean remove(E element) {
        for (int i = 0; i < size; i++) {
            // 注意：用 Objects.equals 而不是 ==，支持 null 比较
            if (Objects.equals(element, table[i])) {
                remove(i);
                return true;
            }
        }
        return false;
    }

    // ========== 修改和查询 ==========
    @Override
    public E set(int index, E element) {
        if (index >= size || index < 0) {
            throw new IndexOutOfBoundsException();
        }
        E oldValue = (E) table[index];
        table[index] = element;
        return oldValue;
    }

    @Override
    public E get(int index) {
        if (index >= size || index < 0) {
            throw new IndexOutOfBoundsException();
        }
        return (E) table[index];
    }

    @Override
    public int size() {
        return size;
    }

    // ========== 迭代器 ==========
    @Override
    public Iterator<E> iterator() {
        return new ArrayListIterator();
    }

    class ArrayListIterator implements Iterator<E> {
        int cursor;  // 当前索引

        @Override
        public boolean hasNext() {
            return cursor != size;
        }

        @Override
        public E next() {
            if (cursor >= size) {
                throw new NoSuchElementException();
            }
            E element = (E) table[cursor];
            cursor++;
            return element;
        }
    }
}
```

---

## 三、手写 LinkedList

### 3.1 核心原理

```
┌─────────────────────────────────────────────────────────┐
│  LinkedList 底层是双向链表，每个节点包含前后指针           │
├─────────────────────────────────────────────────────────┤
│  查找优化：index < size/2 时从 head 开始，否则从 tail    │
│  插入删除：修改前后节点的指针，无需移动元素               │
└─────────────────────────────────────────────────────────┘

空链表：  head = null, tail = null

添加 A：  head → [A] ← tail
              pre=null  next=null

添加 B：  head → [A] ⇄ [B] ← tail
                    next  pre

在 index=1 插入 X：
         head → [A] ⇄ [X] ⇄ [B] ← tail
                    ↑    ↑
               A.next  X.next
               X.pre   B.pre
```

### 3.2 手写代码

```java
package com.sap.collections;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

public class LinkedList<E> implements List<E> {
    private int size;
    private Node<E> head;  // 头节点
    private Node<E> tail;  // 尾节点

    // ========== 节点类 ==========
    class Node<E> {
        Node<E> pre;   // 前驱指针
        Node<E> next;  // 后继指针
        E value;       // 数据

        public Node(E value, Node<E> pre, Node<E> next) {
            this.value = value;
            this.pre = pre;
            this.next = next;
        }
    }

    // ========== 添加元素 ==========
    @Override
    public void add(E element) {
        Node<E> node = new Node<>(element, tail, null);
        if (tail != null) {
            tail.next = node;  // 旧尾节点的 next 指向新节点
        } else {
            head = node;  // 链表为空，新节点成为 head
        }
        tail = node;  // 新节点成为尾节点
        size++;
    }

    @Override
    public void add(E element, int index) {
        if (index > size || index < 0) {
            throw new IndexOutOfBoundsException();
        }
        if (index == size) {
            add(element);  // 尾部添加
            return;
        }
        
        Node<E> indexNode = findNode(index);  // 找到 index 位置的节点
        Node<E> newNode = new Node<>(element, indexNode.pre, indexNode);
        
        if (indexNode.pre == null) {
            head = newNode;  // 在头部插入
        } else {
            indexNode.pre.next = newNode;  // 前驱的 next 指向新节点
        }
        indexNode.pre = newNode;  // 当前节点的 pre 指向新节点
        size++;
    }

    // 查找节点：index < size/2 从 head 开始，否则从 tail 开始
    private Node<E> findNode(int index) {
        Node<E> cursor;
        if (index < size / 2) {
            cursor = head;
            for (int i = 0; i < index; i++) {
                cursor = cursor.next;
            }
        } else {
            cursor = tail;
            for (int i = size - 1; i > index; i--) {
                cursor = cursor.pre;
            }
        }
        return cursor;
    }

    // ========== 删除元素 ==========
    @Override
    public E remove(int index) {
        if (index >= size || index < 0) {
            throw new IndexOutOfBoundsException();
        }
        Node<E> node = findNode(index);
        removeNode(node);
        return node.value;
    }

    private void removeNode(Node<E> node) {
        Node<E> pre = node.pre;
        Node<E> next = node.next;
        
        if (pre == null) {
            head = next;  // 删除的是头节点
        } else {
            pre.next = next;
        }
        
        if (next == null) {
            tail = pre;  // 删除的是尾节点
        } else {
            next.pre = pre;
        }
        
        // 帮助 GC
        node.pre = null;
        node.next = null;
        size--;
    }

    @Override
    public boolean remove(E element) {
        Node<E> cursor = head;
        for (int i = 0; i < size; i++) {
            if (Objects.equals(cursor.value, element)) {
                removeNode(cursor);
                return true;
            }
            cursor = cursor.next;
        }
        return false;
    }

    // ========== 修改和查询 ==========
    @Override
    public E set(int index, E element) {
        if (index >= size || index < 0) {
            throw new IndexOutOfBoundsException();
        }
        Node<E> node = findNode(index);
        E oldValue = node.value;
        node.value = element;
        return oldValue;
    }

    @Override
    public E get(int index) {
        if (index >= size || index < 0) {
            throw new IndexOutOfBoundsException();
        }
        return findNode(index).value;
    }

    @Override
    public int size() {
        return size;
    }

    // ========== 迭代器 ==========
    @Override
    public Iterator<E> iterator() {
        return new LinkedListIterator();
    }

    class LinkedListIterator implements Iterator<E> {
        Node<E> cursor = head;

        @Override
        public boolean hasNext() {
            return cursor != null;
        }

        @Override
        public E next() {
            if (cursor == null) {
                throw new NoSuchElementException();
            }
            E result = cursor.value;
            cursor = cursor.next;
            return result;
        }
    }
}
```

---

## 四、ArrayList vs LinkedList 对比

### 4.1 时间复杂度对比

| 操作 | ArrayList | LinkedList | 说明 |
|-----|-----------|------------|------|
| **get(index)** | O(1) ⭐ | O(n) | 数组随机访问 vs 链表遍历 |
| **add(element)** | 均摊 O(1) | O(1) | 数组尾部添加 / 链表尾部添加 |
| **add(index, element)** | O(n) | O(n) ⭐ | 数组需移动元素，链表查找耗时 |
| **remove(index)** | O(n) | O(n) ⭐ | 同上 |
| **remove(element)** | O(n) | O(n) | 都需要遍历查找 |

### 4.2 面试重点：什么时候用哪个？

```
选择 ArrayList 的场景：
├── 随机访问多（get(index) 频繁）
├── 遍历操作多（for 循环遍历）
├── 元素数量确定或增长缓慢
└── 内存敏感（LinkedList 每个节点有额外指针开销）

选择 LinkedList 的场景：
├── 频繁在头部/中间插入删除
├── 实现 Queue/Deque（双端队列）
├── 需要频繁的 addFirst/removeFirst
└── 不需要随机访问
```

### 4.3 内存占用对比

```
ArrayList（100个元素）：
├─ Object[] 引用数组：约 400 bytes（64位 JVM）
└─ 实际对象：取决于元素
总开销：较小，有预留空间可能浪费

LinkedList（100个元素）：
├─ 每个节点：pre(8) + next(8) + value(8) + 对象头(16) + 对齐(4) = 44 bytes
├─ 100个节点：约 4400 bytes
└─ 无预留空间
总开销：较大，但空间利用率高
```

---

## 五、易错点记录

1. **类型擦除** - 泛型数组 `new E[10]` 是非法的，必须用 `new Object[10]` 再强制转换
2. **扩容时机** - `size == table.length` 时才扩容，不是 `>=`
3. **边界检查** - `add(index)` 允许 `index == size`（尾部插入），但 `remove/get/set` 不允许
4. **内存泄漏** - `remove` 后必须将 `table[size] = null`，否则对象无法被 GC
5. **equals 比较** - 删除元素时用 `Objects.equals()` 而不是 `==`，支持 null 值比较
6. **双向链表指针** - 插入/删除时要同时修改 `pre` 和 `next`，容易漏掉
7. **空链表判断** - 添加第一个元素时，`tail == null` 要特殊处理设置 `head`

---

## 六、JDK 源码中的优化技巧

### 6.1 ArrayList 的 ensureCapacity

```java
// JDK 中的扩容逻辑更精细
private void grow(int minCapacity) {
    int oldCapacity = elementData.length;
    int newCapacity = oldCapacity + (oldCapacity >> 1);  // 1.5 倍，不是 2 倍
    if (newCapacity - minCapacity < 0)
        newCapacity = minCapacity;
    if (newCapacity - MAX_ARRAY_SIZE > 0)
        newCapacity = hugeCapacity(minCapacity);
    elementData = Arrays.copyOf(elementData, newCapacity);
}
```

### 6.2 LinkedList 的双向查找优化

```java
// JDK 源码中同样使用 index < size/2 判断查找方向
// 这是一个经典的时间优化技巧
```

### 6.3 快速失败机制（fail-fast）

```java
// JDK 的迭代器有 modCount 检查
// 遍历过程中修改集合会抛出 ConcurrentModificationException
// 我们的简化版没有实现这个
```

---

## 七、面试高频问题

### Q1: ArrayList 扩容为什么是 1.5 倍？
**答：** 1.5 倍是时间复杂度和空间复杂度的平衡：
- 扩容倍数太小 → 频繁扩容，拷贝开销大
- 扩容倍数太大 → 内存浪费严重
- 1.5 倍可以用位运算 `oldCapacity >> 1` 高效计算

### Q2: 为什么 LinkedList 没有索引还能 get(index)？
**答：** 可以，但效率低。LinkedList 的 `get(index)` 会遍历链表，时间复杂度 O(n)。它实现 `List` 接口是为了保持 API 一致性。

### Q3: 什么情况下 LinkedList 比 ArrayList 慢？
**答：** 
- **随机访问：** LinkedList O(n) vs ArrayList O(1)
- **遍历：** LinkedList 缓存不友好，CPU 预读取失效
- **现代 CPU：** 数组的连续内存访问性能远超链表

### Q4: ArrayList 是线程安全的吗？
**答：** 不是。多线程环境下需要：
- 使用 `Collections.synchronizedList(new ArrayList<>())`
- 或使用 `CopyOnWriteArrayList`（读多写少场景）
- 或手动加锁

_______
