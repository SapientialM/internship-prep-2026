# 手写 ArrayList 和 LinkedList

> 手动实现简化版集合类，深入理解数据结构的底层实现。

---

## ArrayList 简易实现

```java
public class MyArrayList<E> {
    private static final int DEFAULT_CAPACITY = 10;
    private Object[] elements;
    private int size;

    public MyArrayList() {
        elements = new Object[DEFAULT_CAPACITY];
    }

    public MyArrayList(int initialCapacity) {
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal Capacity: " + initialCapacity);
        elements = new Object[initialCapacity];
    }

    public boolean add(E e) {
        ensureCapacity(size + 1);
        elements[size++] = e;
        return true;
    }

    public void add(int index, E e) {
        rangeCheckForAdd(index);
        ensureCapacity(size + 1);
        System.arraycopy(elements, index, elements, index + 1, size - index);
        elements[index] = e;
        size++;
    }

    @SuppressWarnings("unchecked")
    public E get(int index) {
        rangeCheck(index);
        return (E) elements[index];
    }

    public E remove(int index) {
        rangeCheck(index);
        E oldValue = (E) elements[index];
        int numMoved = size - index - 1;
        if (numMoved > 0)
            System.arraycopy(elements, index + 1, elements, index, numMoved);
        elements[--size] = null; // help GC
        return oldValue;
    }

    private void ensureCapacity(int minCapacity) {
        if (minCapacity > elements.length) {
            int newCapacity = elements.length + (elements.length >> 1); // 1.5倍扩容
            if (newCapacity < minCapacity)
                newCapacity = minCapacity;
            elements = Arrays.copyOf(elements, newCapacity);
        }
    }

    private void rangeCheck(int index) {
        if (index >= size)
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
    }

    private void rangeCheckForAdd(int index) {
        if (index > size || index < 0)
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
    }

    public int size() {
        return size;
    }
}
```

**关键设计：**
1. 底层用 `Object[]` 存储元素
2. 默认容量 10，扩容为 1.5 倍
3. `System.arraycopy` 实现元素的批量移动（插入/删除）
4. 删除时置 null 帮助 GC

---

## LinkedList 简易实现

```java
public class MyLinkedList<E> {
    private Node<E> first;
    private Node<E> last;
    private int size;

    private static class Node<E> {
        E item;
        Node<E> prev;
        Node<E> next;

        Node(Node<E> prev, E item, Node<E> next) {
            this.item = item;
            this.prev = prev;
            this.next = next;
        }
    }

    public void add(E e) {
        // 尾插法 — 避免 JDK 1.7 HashMap 头插法死循环问题
        Node<E> l = last;
        Node<E> newNode = new Node<>(l, e, null);
        last = newNode;
        if (l == null)
            first = newNode;
        else
            l.next = newNode;
        size++;
    }

    public void add(int index, E e) {
        if (index == size)
            add(e);  // 加到尾部
        else
            linkBefore(e, node(index));  // 在指定节点前插入
    }

    public E get(int index) {
        return node(index).item;
    }

    public E remove(int index) {
        return unlink(node(index));
    }

    /**
     * 查找第 index 个节点
     * 优化：如果 index 在前半部分，从头部遍历；在后半部分，从尾部遍历
     */
    private Node<E> node(int index) {
        if (index < (size >> 1)) {
            // 从头遍历
            Node<E> x = first;
            for (int i = 0; i < index; i++)
                x = x.next;
            return x;
        } else {
            // 从尾遍历
            Node<E> x = last;
            for (int i = size - 1; i > index; i--)
                x = x.prev;
            return x;
        }
    }

    private E unlink(Node<E> x) {
        E item = x.item;
        Node<E> prev = x.prev;
        Node<E> next = x.next;

        if (prev == null) {
            first = next;
        } else {
            prev.next = next;
            x.prev = null;
        }

        if (next == null) {
            last = prev;
        } else {
            next.prev = prev;
            x.next = null;
        }

        x.item = null;  // help GC
        size--;
        return item;
    }

    private void linkBefore(E e, Node<E> succ) {
        Node<E> pred = succ.prev;
        Node<E> newNode = new Node<>(pred, e, succ);
        succ.prev = newNode;
        if (pred == null)
            first = newNode;
        else
            pred.next = newNode;
        size++;
    }

    public int size() {
        return size;
    }
}
```

**关键设计：**
1. 双向链表，头尾指针
2. `node(index)` 查找时根据位置选择从头(前半)或从尾(后半)遍历，O(n/2)
3. 尾插入 O(1)，指定位置插入 O(n)（需要先定位到该位置）
4. 删除时断开前后引用并置 null 帮助 GC

---

## ArrayList vs LinkedList 实际场景测试

```java
// 尾部追加：ArrayList 更快（连续内存，无需创建节点）
// ArrayList: ~10ms (100万次)
// LinkedList: ~30ms (需要创建 Node 对象)

// 头部插入：LinkedList 更快（无需移动元素）
// ArrayList: ~500ms (每次插入都需要整体移动)
// LinkedList: ~10ms (只需修改指针)

// 随机访问：ArrayList 完胜
// ArrayList: O(1) 直接索引
// LinkedList: O(n) 需要遍历

// 遍历：ArrayList 更快（连续内存，CPU 缓存友好）
```

**结论：大多数情况下 ArrayList 是更好的选择，除非频繁在头部/中间插入删除。**
