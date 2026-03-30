# Java 动态代理实现教程

本项目通过从零开始实现一个动态代理框架，帮助理解 Java 动态代理的核心原理。

## 什么是动态代理？

**动态代理**是指在运行时动态生成类的技术，而不是在编译时确定。

传统方式：
```java
// 编译时就确定了类的实现
public class MyImpl implements MyInterface {
    public void func1() { ... }
}
```

动态代理：
```java
// 运行时动态生成类
MyInterface proxy = MyInterfaceFactory.createProxyObject(handler);
// proxy 的类是在运行时才创建的！
```

## 为什么要用动态代理？

### 1. 代码复用与解耦
- 通用逻辑（日志、事务、权限）只需写一次，应用到多个方法
- 业务代码与横切关注点（cross-cutting concerns）分离

### 2. 灵活性
- 行为可以在运行时决定
- 同一个接口可以有不同的实现，无需修改源代码

### 3. 框架基础
- Spring AOP、MyBatis、Hibernate 等都基于动态代理
- RPC 框架（Dubbo、gRPC）使用动态代理隐藏网络调用细节

## 动态创建类的完整流程

```
┌─────────────────────────────────────────────────────────────┐
│  1. 生成源码    →  组装 Java 源代码字符串                      │
│     (createJavaFile)                                         │
├─────────────────────────────────────────────────────────────┤
│  2. 写入文件    →  将源码保存为 .java 文件                     │
│     (src/main/java/com/sap/proxy/MyInterface$proxyN.java)    │
├─────────────────────────────────────────────────────────────┤
│  3. 编译        →  使用 JavaCompiler 编译为 .class             │
│     (Compiler.compile)                                       │
├─────────────────────────────────────────────────────────────┤
│  4. 加载类      →  使用 ClassLoader 加载字节码                 │
│     (URLClassLoader)                                         │
├─────────────────────────────────────────────────────────────┤
│  5. 创建实例    →  反射调用构造器创建对象                       │
│     (Constructor.newInstance)                                │
└─────────────────────────────────────────────────────────────┘
```

## 项目结构

```
com.sap.proxy
├── MyInterface.java          # 目标接口（要代理的接口）
├── MyHandler.java            # 方法体生成器接口
├── MyInterfaceFactory.java   # 动态代理工厂（核心实现）
├── Compiler.java             # Java 动态编译器
├── NameAndLengthImpl.java    # 静态实现示例（对比用）
└── Main.java                 # 演示入口
```

## 核心组件详解

### 1. MyHandler - 方法体生成器

```java
public interface MyHandler {
    // 根据方法名返回该方法的方法体代码
    String functionBody(String methodName);
}
```

**关键理解**：返回的是 Java 代码字符串！

例如：
```java
public String functionBody(String methodName) {
    return "System.out.println(\"" + methodName + "\");";
}
```

生成的类会变成：
```java
public void func1() {
    System.out.println("func1");
}
```

### 2. MyInterfaceFactory - 代理工厂

核心方法 `createProxyObject`：

```java
public static MyInterface createProxyObject(MyHandler myHandler) {
    String className = getClassName();                    // 1. 生成唯一类名
    File javaFile = createJavaFile(className, myHandler); // 2. 生成源码文件
    Compiler.compile(javaFile);                           // 3. 编译
    return newInstance(className, myHandler);             // 4. 加载并创建实例
}
```

### 3. Compiler - 动态编译器

使用 `javax.tools.JavaCompiler` API 在运行时编译 Java 文件：

```java
JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
CompilationTask task = compiler.getTask(...);
boolean success = task.call();  // 执行编译
```

## 运行演示

### 方式一：使用 Maven

```bash
mvn compile exec:java -Dexec.mainClass="com.sap.proxy.Main"
```

### 方式二：使用 IDEA

1. 打开项目
2. 运行 `Main` 类的 `main` 方法

### 预期输出

```
===================== Demo 1: 简单代理（打印方法名）
Generated source file: .../MyInterface$proxy1.java
Compilation successful: MyInterface$proxy1.java
func1
func2
func3
---------------------
===================== Demo 2: 简单代理变体
Generated source file: .../MyInterface$proxy2.java
Compilation successful: MyInterface$proxy2.java
test1
func1
test1
func2
test1
func3
---------------------
===================== Demo 3: 嵌套代理（添加日志增强）
Generated source file: .../MyInterface$proxy3.java
Compilation successful: MyInterface$proxy3.java
before
test1
func1
after
before
test1
func2
after
before
test1
func3
after
```

## 三种代理模式对比

### 模式 1：简单代理（PrintFunctionName）

```
┌─────────────────┐
│  Proxy Object   │  ← 动态生成，执行打印方法名的代码
│  (MyInterface)  │
└─────────────────┘
```

### 模式 2：嵌套代理（LogHandler）

```
┌─────────────────┐
│  Proxy Object   │  ← 动态生成，执行 before/after 逻辑
│  (LogHandler)   │     内部调用 target.xxx()
├─────────────────┤
│  target field   │ ──→ 指向内部代理
└─────────────────┘
         │
         ▼
┌─────────────────┐
│  Inner Proxy    │  ← 动态生成，执行业务逻辑
│ (PrintFunction  │
│    Name1)       │
└─────────────────┘
```

**嵌套代理的价值**：
- 实现了 AOP 的核心思想：在不修改原代码的情况下增强功能
- 可以层层包装，添加多种横切关注点

## 关键知识点

### 1. 类加载器（ClassLoader）

动态编译的类不在默认的 classpath 中，需要使用 `URLClassLoader`：

```java
URLClassLoader classLoader = new URLClassLoader(
    new URL[]{new File("./target/classes").toURI().toURL()},
    parentClassLoader
);
```

### 2. 反射注入

嵌套代理需要通过反射将内部代理注入到外部代理的字段：

```java
Field targetField = proxyClass.getDeclaredField("target");
targetField.setAccessible(true);
targetField.set(proxy, innerProxy);
```

### 3. 代码生成技巧

生成代码时要注意：
- 返回的是合法的 Java 代码字符串
- 注意转义字符（如 `\"`）
- 代码缩进要正确（生成的代码可读性好）

## 与标准 JDK 动态代理的对比

| 特性 | 本项目 | JDK Proxy (`java.lang.reflect.Proxy`) |
|------|--------|---------------------------------------|
| 实现方式 | 生成 .java → 编译 → 加载 | 直接生成字节码 |
| 接口要求 | 可以代理类或接口 | 只能代理接口 |
| 性能 | 较慢（涉及文件 IO 和编译） | 快（直接操作字节码） |
| 学习价值 | 高（流程清晰可见） | 低（封装复杂） |
| 实用价值 | 低（主要用于学习） | 高（生产环境使用） |

## 扩展思考

1. **如何支持有返回值的方法？**
   - 修改 `functionBody` 返回类型为 `String`，包含 return 语句
   - 或者增加参数传递返回类型信息

2. **如何支持带参数的方法？**
   - 修改接口，让 `functionBody` 接收参数类型和值
   - 生成的代码需要使用这些参数

3. **如何像 CGLIB 一样代理类（而不仅是接口）？**
   - 继承目标类而不是实现接口
   - 处理 final 方法的限制

4. **如何缓存生成的类？**
   - 使用 Map 存储类名到 Class 对象的映射
   - 避免重复生成相同的代理类

## 相关技术

- **JDK 动态代理**：`java.lang.reflect.Proxy`，基于接口
- **CGLIB**：基于继承，可以代理类
- **ByteBuddy**：现代字节码操作库，更灵活高效
- **Javassist**：另一个字节码操作库，语法接近 Java

## 总结

通过本项目，你应该理解：

1. ✅ 动态代理的核心概念和用途
2. ✅ 动态生成类的完整流程（源码 → 编译 → 加载 → 实例化）
3. ✅ 方法拦截和代码生成的原理
4. ✅ 嵌套代理实现 AOP 的思想
5. ✅ 类加载器和反射在动态代理中的作用

动态代理是 Java 高级编程的基础，理解其原理对于学习 Spring AOP、RPC 框架等都有很大帮助。
