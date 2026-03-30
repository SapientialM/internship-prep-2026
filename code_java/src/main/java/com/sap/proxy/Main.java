package com.sap.proxy;

import java.lang.reflect.Field;

/**
 * 动态代理演示程序 - 入口类
 * 
 * 本类演示了三种使用动态代理的方式：
 * 
 * 1. 【简单代理】PrintFunctionName
 *    - 为每个方法生成打印方法名的代码
 *    - 展示了最基本的动态代码生成
 * 
 * 2. 【简单代理变体】PrintFunctionName1  
 *    - 在打印方法名前添加额外的 "test1" 输出
 *    - 展示了同样的接口可以有不同的实现
 * 
 * 3. 【嵌套代理/包装代理】LogHandler
 *    - 包装已有的代理对象，添加 before/after 逻辑
 *    - 展示了 AOP 的核心思想：在不修改原代码的情况下增强功能
 * 
 * 运行效果示例：
 * ```
 * ===================== Demo 1: 简单代理（打印方法名）
 * func1
 * func2
 * func3
 * ---------------------
 * ===================== Demo 2: 简单代理变体
 * test1
 * func1
 * test1
 * func2
 * test1
 * func3
 * ---------------------
 * ===================== Demo 3: 嵌套代理（添加日志增强）
 * before
 * test1
 * func1
 * after
 * before
 * test1
 * func2
 * after
 * before
 * test1
 * func3
 * after
 * ```
 * 
 * @author SapientialM
 * @date 2026/3/29 23:56
 * @since 1.0
 */
public class Main {

    public static void main(String[] args) throws Exception {
        // ============================================================
        // Demo 1: 简单代理 - 只打印方法名
        // ============================================================
        System.out.println("===================== Demo 1: 简单代理（打印方法名）");
        MyInterface proxyObject = MyInterfaceFactory.createProxyObject(new PrintFunctionName());
        proxyObject.func1();
        proxyObject.func2();
        proxyObject.func3();

        System.out.println("---------------------");

        // ============================================================
        // Demo 2: 简单代理变体 - 打印额外的 "test1" 再打印方法名
        // ============================================================
        System.out.println("===================== Demo 2: 简单代理变体");
        proxyObject = MyInterfaceFactory.createProxyObject(new PrintFunctionName1());
        proxyObject.func1();
        proxyObject.func2();
        proxyObject.func3();

        System.out.println("---------------------");

        // ============================================================
        // Demo 3: 嵌套代理 - 在 Demo 2 的基础上添加 before/after 日志
        // ============================================================
        // 这就是 AOP（面向切面编程）的核心思想：
        // 不修改原有代码，而是在其前后添加通用逻辑（日志、事务、权限等）
        System.out.println("===================== Demo 3: 嵌套代理（添加日志增强）");
        
        // 先创建内部代理（Demo 2 的版本）
        MyInterface innerProxy = MyInterfaceFactory.createProxyObject(new PrintFunctionName1());
        
        // 再用 LogHandler 包装它，添加 before/after 逻辑
        proxyObject = MyInterfaceFactory.createProxyObject(new LogHandler(innerProxy));
        proxyObject.func1();
        proxyObject.func2();
        proxyObject.func3();
    }

    /**
     * 简单处理器：打印方法名
     * 
     * 为每个方法生成代码：System.out.println("方法名");
     */
    static class PrintFunctionName implements MyHandler {
        @Override
        public String functionBody(String methodName) {
            // 生成打印方法名的代码
            // 注意：这是 Java 代码字符串，会被嵌入到生成类的方法中
            return "System.out.println(\"" + methodName + "\");";
        }
    }

    /**
     * 简单处理器变体：打印 "test1" 后再打印方法名
     */
    static class PrintFunctionName1 implements MyHandler {
        @Override
        public String functionBody(String methodName) {
            StringBuilder sb = new StringBuilder();
            sb.append("System.out.println(\"test1\"); ");
            sb.append("System.out.println(\"").append(methodName).append("\");");
            return sb.toString();
        }
    }

    /**
     * 包装处理器：为被代理对象添加 before/after 日志
     * 
     * 这是嵌套代理（装饰器模式）的实现：
     * - 持有一个内部的 MyInterface 对象（被包装的对象）
     * - 在方法调用前后添加额外逻辑
     * - 通过 setProxy 将内部对象注入到生成的代理类中
     * 
     * 工作流程：
     * 1. LogHandler 被传入 createProxyObject
     * 2. 工厂生成代理类，其中包含 target 字段
     * 3. setProxy 被调用，通过反射将内部对象注入到 target 字段
     * 4. 调用代理方法时，执行生成的代码：
     *    - 打印 "before"
     *    - 调用 target.funcX()（即内部代理的方法）
     *    - 打印 "after"
     */
    static class LogHandler implements MyHandler {
        
        /** 被包装的内部代理对象 */
        private final MyInterface target;
        
        public LogHandler(MyInterface target) {
            this.target = target;
        }

        /**
         * 生成方法体代码
         * 
         * 生成的代码结构：
         * ```java
         * System.out.println("before");
         * target.funcX();
         * System.out.println("after");
         * ```
         */
        @Override
        public String functionBody(String methodName) {
            // 注意：这里引用的是 target 字段
            // target 字段在 setProxy 方法中被注入
            return "System.out.println(\"before\");\n" +
                   "        target." + methodName + "();\n" +
                   "        System.out.println(\"after\");";
        }

        /**
         * 将内部代理对象注入到生成的代理类中
         * 
         * 生成的代理类包含 target 字段，本方法通过反射将 this.target
         * 设置到代理对象的 target 字段中。
         * 
         * 这是实现嵌套代理的关键步骤。
         */
        @Override
        public void setProxy(MyInterface proxy) {
            try {
                // 获取代理对象的类
                Class<? extends MyInterface> proxyClass = proxy.getClass();
                
                // 获取 target 字段（生成类中的字段名为 "target"）
                Field targetField = proxyClass.getDeclaredField("target");
                
                // 设置可访问（即使字段是 private 也能访问）
                targetField.setAccessible(true);
                
                // 将内部代理对象注入到生成的代理对象的 target 字段
                targetField.set(proxy, this.target);
                
            } catch (Exception e) {
                throw new RuntimeException("Failed to inject target into proxy: " + e.getMessage(), e);
            }
        }
    }
}
