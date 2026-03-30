---
title: 八股-ThreadLocal
date: 2026-03-30 00:00:00
tags: [八股, JUC]
categories: 八股, JUC







---

# ThreadLocal

ThreadLocal 提供了一种线程级别的数据存储机制。每个线程都拥有自己独立的 ThreadLocal ，意味着每个线程都可以独立地、安全地操作这些变量，而不会影响其他线程。

而且Spring其实属于 ThreadLoca l的重度用户，比如Spring 的 声明式编程魔法（@Transactional、@Secured、@RateLimit）底层全靠 ThreadLocal 隐式传参 —— 让框架能在线程任意深度拿到上下文，而你的业务代码保持干净。



<!-- more -->



****

# 使用场景/作用
**典型使用场景**

+ **用户身份信息存储**：在请求的拦截器/过滤器中鉴权校验用户身份，把用户信息(如用户ID、权限)存入 ThreadLocal 中，在请求的后续链路中如果需要获取用户信息的，直接在 ThreadLocal 中获取即可
+ **线程安全**：ThreadLocal 可以用来存储一些需要并发安全处理的成员变量，比如SimpleDateFormat，由于 SimpleDateFormat 不是线程安全的，可以使用 ThreadLocal 为每个线程创建一个独立的 SimpleDateFormat实例，从而避免线程安全问题
+ **日志上下文存储**：在常见的日志框架中，经常使用 ThreadLocal 来存储与当前线程相关的日志上下文。这允许开发者在打印日志消息时包含特定于线程的信息，如用户ID，这对于调试和监控是非常有用的（相当于为了打印每条日志时能看到用户ID或其他信息）
+ **traceld 存储**：和上面存储日志上下文类似，在分布式链路追踪中，需要存储本次请求的 traceld，通常也都是基于 ThreadLocal 存储的
+ **数据库 Session**：很多ORM框架，如Hibernate、Mybatis，都是使用 ThreadLocal 来存储和管理数据库会话的这样可以确保每个线程都有自己的会话实例，避免了在多线程环境中出现的线程安全问题

**主要的两个作用：**

1. 在线程中传递数据，在同一个线程执行过程中，ThreadLocal 的数据一直在，所以可以在前面把数据放到 ThreadLocal 中，在后面需要时再取出来用，就可以避免数据通过参数在多层方法传递
2. 解决并发问题



# 实现原理
1. Thread 类对象中 维护了 ThreadLocalMap 成员变量
2. ThreadLocalMap 类对象中 维护了 Entry 数组，Entry数组中的每一个元素都是一个Entry对象
    1. 每个Entry对象中存储着 一个ThreadLocal对象与其要存入的数据value值
    2. `每个Entry对象在Entry数组中的位置是通过ThreadLocal对象的threadLocalHashCode计算出来的，以此来快速定位Entry对象在Entry数组中的位置`
3. 所以，在Thread中，可以存储多个ThreadLocal对象

<img src="./%E5%85%AB%E8%82%A1-ThreadLocal/1743224545090-d3a9acba-22bf-4562-85b8-5c02a3d71c97.webp" width="1349" title="" crop="0,0,1,1" id="UuAap" class="ne-image">



ThreadLocal 的 set 方法实现：获取当前线程，获取当前线程的ThreadLocalMap，调用其set方法，存入的 key 是this(即ThreadLocal对象本身)，value是要存入的数据

<img src="./%E5%85%AB%E8%82%A1-ThreadLocal/1743220507947-eec1b726-0883-4a0a-aa18-94c496d8cd37.png" width="536.8749919999392" title="" crop="0,0,1,1" id="u71c10e64" class="ne-image">

ThreadLocal 的 get 方法实现：获取当前线程，获取当前线程的ThreadLocalMap，调用其get方法，通过this(即ThreadLocal对象本身) 获取到之前存入的数据

<img src="./%E5%85%AB%E8%82%A1-ThreadLocal/1743221420985-e712ce43-1520-4e93-a9d4-bdeff5b94a81.png" width="696.8749896157534" title="" crop="0,0,1,1" id="u77ffeca3" class="ne-image">

# 内存泄漏
`内存泄漏指：存在无法回收的对象`

****

**假设我们单独开启一个线程**，并且将数据存储到ThreadLocal中，当 Thread 线程执行任务结束退出时，线程对象被销毁，那么Thread 线程与 ThreadLocalMap 实例对象之间的引用关系就不存在，在 GC 时 ThreadLocalMap 对象、ThreadLocal 对象 、之前存储的数据 都会被回收掉，所以其实不存在内存泄漏

<img src="./%E5%85%AB%E8%82%A1-ThreadLocal/1743226227831-0998ce3b-1bff-466e-baff-4d2571b9a5ed.png" width="1015.0000268883182" title="" crop="0,0,1,1" id="ue0813c1d" class="ne-image">



**但是如果使用线程池的方式，核心线程是会反复使用的**，<font style="color:rgb(51, 51, 51);">线程中对应的 ThreadLocalMap 会被线程 强引用</font>

**<font style="color:rgb(51, 51, 51);">所以 ThreadLocalMap 不能被 GC 自动回收，</font>**<font style="color:rgb(51, 51, 51);">而 ThreadLocalMap 中包含一个 Entry  数组</font>

`<font style="color:rgb(51, 51, 51);">Entry 数组中含有多个< Key 为 ThreadLocal，value为存储的数据 > 的Entry对象</font>`

**<font style="color:rgb(51, 51, 51);">虽然 Entry 对象中的 Key </font>****<font style="color:rgb(51, 51, 51);">是弱引用</font>****<font style="color:rgb(51, 51, 51);">，能够被 GC 自动回收</font>**

**<font style="color:rgb(51, 51, 51);">但 value 是强引用，不能被 GC 自动回收</font>**<font style="color:rgb(51, 51, 51);">，所以，在线程池中使用ThreadLocal会存在内存泄露的风险</font>

<img src="./%E5%85%AB%E8%82%A1-ThreadLocal/1743224545090-d3a9acba-22bf-4562-85b8-5c02a3d71c97-1774788936378-1.png" width="1142.4999829754236" title="" crop="0,0,1,1" id="u55a46f93" class="ne-image">

```java
public class ThreadLocal<T>{
    static class ThreadLocalMap {
        static class Entry extends WeakReference<ThreadLocal<?>> {
            /** The value associated with this ThreadLocal. */
            Object value;
            Entry(ThreadLocal<?> k, Object v) {
                super(k);
                value = v;
            }
        }
    }
}
```

> **<font style="color:rgb(51, 51, 51);">强引用:  </font>**<font style="color:rgb(51, 51, 51);">Java中默认的引用类型，一个对象如果具有强引用那么只要这种引用还存在就不会被回收。比如String str = new String("Hello ThreadLocal");  其中 str 就是一个强引用，当然，一旦强引用出了其作用域，那么强引用随着方法弹出线程栈，那么它所指向的对象将在合适的时机被JVM垃圾收集器回收</font>
>
> <font style="color:rgb(51, 51, 51);"></font>
>
> **<font style="color:rgb(51, 51, 51);">软引用</font>**<font style="color:rgb(51, 51, 51);">：如果一个对象具有软引用，在JVM发生内存溢出之前 (即内存充足够使用) ，是不会GC这个对象的；只有到IVM内存不足的时候才会调用垃圾回收期回收掉这个对象。软引用和一个引用队列联合使用，如果软引用所引用的对象被回收之后，该引用就会加入到与之关联的引用队列中</font>
>
> **<font style="color:rgb(51, 51, 51);"></font>**
>
> **<font style="color:rgb(51, 51, 51);">弱引用</font>**<font style="color:rgb(51, 51, 51);">：这里讨论ThreadLocalMap中的Entry类的重点，如果一个对象只具有弱引用，那么这个对象就会被垃圾回收器回收掉 (被弱引用所引用的对象只能生存到下一次GC之前，当发生GC时候，无论当前内存是否足够弱引用所引用的对象都会被回收掉)。弱引用也是和一个引用队列联合使用，如果弱引用的对象被垃圾回收期回收掉，JVM会将这个引用加入到与之关联的引用队列中。弱引用的对象可以通过弱引用的get方法得到，当引用的对象被回收掉之后，再调用qet方法就会返回nuI</font>
>
> **<font style="color:rgb(51, 51, 51);"></font>**
>
> **<font style="color:rgb(51, 51, 51);">虚引用</font>**<font style="color:rgb(51, 51, 51);">：虚引用是所有引用中最弱的一种引用，其存在就是为了将关联虚引用的对象在被GC掉之后收到一个通知</font>
>



**如何避免内存泄漏（解决方案）**

<font style="color:rgb(51, 51, 51);">在使用完ThreadLocal对象中保存的数据后，一般在拦截器过滤器的后置处理中，在 finally{} 代码块中调用 ThreadLocal 的 remove() 方法</font>

<img src="./%E5%85%AB%E8%82%A1-ThreadLocal/1743224900664-bcf40bdc-5673-453f-a6cb-5f041d6aa780.png" width="687.4999897554518" title="" crop="0,0,1,1" id="u7da2924b" class="ne-image">



