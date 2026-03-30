package com.sap.proxy;

/**
 * 目标接口 - 动态代理演示用的示例接口
 * 
 * 本接口定义了三个方法，作为动态代理的目标。
 * 动态代理的目标是：在运行时动态生成实现此接口的类，
 * 而不是在编译时手动编写实现类。
 * 
 * 动态代理的价值：
 * 1. 可以在不修改原有代码的情况下，添加通用逻辑（如日志、事务、权限检查等）
 * 2. 可以实现 AOP（面向切面编程）的核心机制
 * 3. 框架开发中广泛使用（如 Spring AOP、MyBatis 等）
 * 
 * @author SapientialM
 * @date 2026/3/29 22:57
 * @since 1.0
 */
public interface MyInterface {

    /**
     * 示例方法 1
     */
    void func1();

    /**
     * 示例方法 2
     */
    void func2();

    /**
     * 示例方法 3
     */
    void func3();
}
