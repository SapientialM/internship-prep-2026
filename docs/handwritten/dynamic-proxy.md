# 手写动态代理

> 从零实现 JDK 动态代理，深入理解 `Proxy.newProxyInstance()` 背后的原理。

---

## 一、为什么需要动态代理？

**传统静态实现的痛点：**

假设需求是"让每个方法都打印自己的名字和名字长度"，传统做法是手动编写每个方法：

```java
public class NameAndLengthImpl implements MyInterface {
    @Override
    public void func1() {
        String methodName = "func1";
        System.out.println(methodName);
        System.out.println(methodName.length());
    }

    @Override
    public void func2() {
        String methodName = "func2";  // 重复逻辑！
        System.out.println(methodName);
        System.out.println(methodName.length());
    }
    // ... func3 同样重复
}
```

**问题：**
1. 代码重复 — 每个方法都重复了获取方法名的逻辑
2. 修改困难 — 如果要修改打印格式，需要修改所有方法
3. 扩展困难 — 新增方法时需要重复同样的模板代码

**动态代理的解决方案：** 通过动态生成代码，只写一次逻辑，应用到所有方法上！

---

## 二、动态代理核心流程

```
┌─────────────────┐
│ 1. 生成类名     │ 使用原子计数器确保类名唯一
└────────┬────────┘
         ▼
┌─────────────────┐
│ 2. 生成源码     │ 根据 Handler 提供的方法体，组装成完整 Java 类
└────────┬────────┘
         ▼
┌─────────────────┐
│ 3. 写入文件     │ 将源码写入 .java 文件
└────────┬────────┘
         ▼
┌─────────────────┐
│ 4. 编译         │ 调用 JavaCompiler 编译为 .class
└────────┬────────┘
         ▼
┌─────────────────┐
│ 5. 加载类       │ 使用 URLClassLoader 加载
└────────┬────────┘
         ▼
┌─────────────────┐
│ 6. 创建实例     │ 反射调用构造器，返回代理对象
└─────────────────┘
```

---

## 三、核心实现

### 3.1 目标接口

```java
package com.sap.proxy;

public interface MyInterface {
    void func1();
    void func2();
    void func3();
}
```

### 3.2 方法体生成器接口（核心抽象）

```java
package com.sap.proxy;

public interface MyHandler {
    /**
     * 根据方法名生成方法体代码
     * 返回的字符串会被直接嵌入到生成类的方法中
     */
    String functionBody(String methodName);

    /**
     * 设置代理对象引用（用于嵌套代理场景）
     */
    default void setProxy(MyInterface proxy) throws Exception {
        // 默认空实现
    }
}
```

### 3.3 动态代理工厂（核心实现）

```java
package com.sap.proxy;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

public class MyInterfaceFactory {

    private static final AtomicInteger count = new AtomicInteger();

    public static MyInterface createProxyObject(MyHandler myHandler) throws Exception {
        String className = getClassName();
        File javaFile = createJavaFile(className, myHandler);
        Compiler.compile(javaFile);
        return newInstance(className, myHandler);
    }

    private static String getClassName() {
        return "MyInterface$proxy" + count.incrementAndGet();
    }

    private static File createJavaFile(String className, MyHandler myHandler) throws Exception {
        String func1Body = myHandler.functionBody("func1");
        String func2Body = myHandler.functionBody("func2");
        String func3Body = myHandler.functionBody("func3");

        String sourceCode = "package com.sap.proxy;\n" +
                "\n" +
                "public class " + className + " implements MyInterface {\n" +
                "    MyInterface target;\n" +
                "\n" +
                "    @Override\n" +
                "    public void func1() {\n" +
                "        " + func1Body + "\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public void func2() {\n" +
                "        " + func2Body + "\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public void func3() {\n" +
                "        " + func3Body + "\n" +
                "    }\n" +
                "}\n";

        Path sourceDir = Path.of("src/main/java/com/sap/proxy");
        if (!Files.exists(sourceDir)) {
            Files.createDirectories(sourceDir);
        }
        File javaFile = sourceDir.resolve(className + ".java").toFile();
        Files.writeString(javaFile.toPath(), sourceCode);
        return javaFile;
    }

    private static MyInterface newInstance(String className, MyHandler handler) throws Exception {
        File classesDir = new File("./target/classes");
        URLClassLoader classLoader = new URLClassLoader(
                new URL[]{classesDir.toURI().toURL()},
                MyInterfaceFactory.class.getClassLoader()
        );

        Class<?> clazz = classLoader.loadClass("com.sap.proxy." + className);
        Constructor<?> constructor = clazz.getConstructor();
        MyInterface proxy = (MyInterface) constructor.newInstance();
        handler.setProxy(proxy);
        return proxy;
    }
}
```

### 3.4 使用示例 — 嵌套代理 (AOP 思想)

```java
// 简单代理：打印方法名
MyInterface proxy1 = MyInterfaceFactory.createProxyObject(
    new MyHandler() {
        @Override
        public String functionBody(String methodName) {
            return "System.out.println(\"" + methodName + "\");";
        }
    }
);
proxy1.func1();  // 输出: func1

// 嵌套代理：AOP 前置后置日志增强
MyInterface innerProxy = MyInterfaceFactory.createProxyObject(new PrintHandler());
MyInterface proxy2 = MyInterfaceFactory.createProxyObject(new LogHandler(innerProxy));
proxy2.func1();  // 输出: before → func1 → after
```

---

## 四、JDK 动态代理 vs CGLIB

| 特性 | JDK 动态代理 | CGLIB 动态代理 |
|:---|:---|:---|
| **实现方式** | 基于接口 | 基于继承（生成子类） |
| **要求** | 目标类必须实现接口 | 目标类不能是 final |
| **性能** | 稍慢（反射调用） | 较快（直接调用） |
| **依赖** | JDK 内置 | 需要引入 CGLIB 库 |
| **使用场景** | Spring 默认（有接口时） | 目标无接口时 |

---

## 五、易错点记录

1. **类加载器问题** — 必须使用 URLClassLoader 从 target/classes 加载，默认 ClassLoader 找不到动态编译的类
2. **包名问题** — 生成的源码必须放在正确的包目录下，否则编译器找不到依赖
3. **JDK vs JRE** — 需要 JDK 环境才能使用 `ToolProvider.getSystemJavaCompiler()`
4. **setProxy 时机** — 嵌套代理时，必须在创建实例后注入 target 字段
5. **字段名匹配** — setProxy 中反射获取的字段名必须与生成源码中的字段名一致
