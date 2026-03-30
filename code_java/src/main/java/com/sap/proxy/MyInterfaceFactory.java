package com.sap.proxy;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 动态代理工厂类 - 核心实现
 * 
 * 本类实现了完整的动态代理生成流程：
 *   Java 源码字符串 -> 写入文件 -> 编译 -> 加载 -> 创建实例
 * 
 * 动态创建类的完整流程：
 * ┌─────────────────┐
 * │ 1. 生成类名     │ 使用原子计数器确保类名唯一（MyInterface$proxy1, $proxy2...）
 * └────────┬────────┘
 *          ▼
 * ┌─────────────────┐
 * │ 2. 生成源码     │ 根据 MyHandler 提供的方法体，组装成完整 Java 类
 * └────────┬────────┘
 *          ▼
 * ┌─────────────────┐
 * │ 3. 写入文件     │ 将源码写入 src/main/java/com/sap/proxy/ 目录
 * └────────┬────────┘
 *          ▼
 * ┌─────────────────┐
 * │ 4. 编译         │ 调用 Compiler 将 .java 编译为 .class
 * └────────┬────────┘
 *          ▼
 * ┌─────────────────┐
 * │ 5. 加载类       │ 使用 URLClassLoader 从 target/classes 加载
 * └────────┬────────┘
 *          ▼
 * ┌─────────────────┐
 * │ 6. 创建实例     │ 反射调用构造器，并通过 setProxy 注入依赖
 * └─────────────────┘
 * 
 * @author SapientialM
 * @date 2026/3/29 23:05
 * @since 1.0
 */
public class MyInterfaceFactory {

    /** 
     * 原子计数器，用于生成唯一的类名
     * 例如：MyInterface$proxy1, MyInterface$proxy2, ...
     * 使用 AtomicInteger 确保线程安全
     */
    private static final AtomicInteger count = new AtomicInteger();

    /**
     * 创建动态代理对象
     * 
     * 这是对外提供的核心 API，通过传入不同的 MyHandler 实现，
     * 可以创建具有不同行为的代理对象。
     *
     * @param myHandler 方法体生成器，定义代理类的行为
     * @return 动态生成的代理对象
     * @throws Exception 创建过程中可能发生的各种异常
     */
    public static MyInterface createProxyObject(MyHandler myHandler) throws Exception {
        // 步骤 1：生成唯一类名
        String className = getClassName();
        
        // 步骤 2 & 3：生成源码并写入文件
        File javaFile = createJavaFile(className, myHandler);
        
        // 步骤 4：编译 Java 文件
        Compiler.compile(javaFile);
        
        // 步骤 5 & 6：加载类并创建实例
        return newInstance(className, myHandler);
    }

    /**
     * 生成唯一类名
     * 
     * 使用计数器确保每次生成的类名都不同，避免类名冲突。
     * 生成的类名格式：MyInterface$proxy{N}
     * 
     * @return 唯一的类名字符串
     */
    private static String getClassName() {
        return "MyInterface$proxy" + count.incrementAndGet();
    }

    /**
     * 生成 Java 源文件
     * 
     * 根据传入的 MyHandler，为每个接口方法生成对应的方法体，
     * 组装成完整的 Java 类，并写入文件系统。
     * 
     * 生成的类结构：
     * ```java
     * package com.sap.proxy;
     * public class MyInterface$proxyN implements MyInterface {
     *     MyInterface target;  // 用于嵌套代理
     *     
     *     public void func1() { [handler 生成的代码] }
     *     public void func2() { [handler 生成的代码] }
     *     public void func3() { [handler 生成的代码] }
     * }
     * ```
     *
     * @param className 类名（不含包名）
     * @param myHandler 方法体生成器
     * @return 生成的 Java 文件
     * @throws IOException 文件操作异常
     */
    private static File createJavaFile(String className, MyHandler myHandler) throws IOException {
        // 获取三个方法的方法体代码
        String func1Body = myHandler.functionBody("func1");
        String func2Body = myHandler.functionBody("func2");
        String func3Body = myHandler.functionBody("func3");

        // 组装完整的 Java 类源码
        // 注意：target 字段用于支持嵌套代理（如 LogHandler 场景）
        String sourceCode = "package com.sap.proxy;\n" +
                "\n" +
                "public class " + className + " implements MyInterface {\n" +
                "    // target 字段用于嵌套代理：持有被包装的内部代理对象\n" +
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

        // 将文件写入 src/main/java/com/sap/proxy/ 目录
        // 注意：必须放在正确的包目录下，否则编译器找不到依赖
        Path sourceDir = Path.of("src/main/java/com/sap/proxy");
        if (!Files.exists(sourceDir)) {
            Files.createDirectories(sourceDir);
        }
        
        File javaFile = sourceDir.resolve(className + ".java").toFile();
        Files.writeString(javaFile.toPath(), sourceCode);
        
        System.out.println("Generated source file: " + javaFile.getAbsolutePath());
        return javaFile;
    }

    /**
     * 加载类并创建实例
     * 
     * 使用 URLClassLoader 从 target/classes 目录加载编译后的类，
     * 并通过反射创建实例。创建后调用 handler.setProxy 完成依赖注入。
     *
     * @param className 类名（不含包名）
     * @param handler 方法体生成器（用于 setProxy 回调）
     * @return 代理对象实例
     * @throws Exception 类加载或实例化异常
     */
    private static MyInterface newInstance(String className, MyHandler handler) throws Exception {
        // 使用 URLClassLoader 从 target/classes 目录加载类
        // 这是关键：默认的 ClassLoader 可能找不到动态编译的类
        File classesDir = new File("./target/classes");
        URLClassLoader classLoader = new URLClassLoader(
                new URL[]{classesDir.toURI().toURL()},           // 类路径
                MyInterfaceFactory.class.getClassLoader()        // 父类加载器
        );

        // 加载类（注意：需要完整类名，包含包名）
        Class<?> clazz = classLoader.loadClass("com.sap.proxy." + className);
        
        // 获取无参构造器
        Constructor<?> constructor = clazz.getConstructor();
        
        // 创建实例
        MyInterface proxy = (MyInterface) constructor.newInstance();
        
        // 调用 handler 的 setProxy，允许 handler 向代理对象注入依赖
        // 这对于嵌套代理（如 LogHandler）非常重要
        handler.setProxy(proxy);
        
        return proxy;
    }
}
