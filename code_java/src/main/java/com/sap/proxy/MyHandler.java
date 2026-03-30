package com.sap.proxy;

/**
 * 方法体生成器接口 - 动态代理的核心抽象
 * 
 * 本接口定义了如何为接口方法生成具体的方法体代码。
 * 这是实现动态代理的关键：通过实现此接口，可以控制生成的方法内容。
 * 
 * 工作流程：
 * 1. MyInterfaceFactory 创建代理类时，会调用 handler 的 functionBody 方法
 * 2. functionBody 返回该方法的 Java 代码字符串
 * 3. 工厂将代码字符串嵌入到生成的类中
 * 4. 编译、加载后，调用方法时执行的就是生成的代码
 * 
 * 两种使用模式：
 * 1. 简单模式：直接返回方法体代码（如打印日志）
 * 2. 包装模式：通过 setProxy 注入目标对象，实现嵌套代理（如添加 before/after 逻辑）
 * 
 * @author SapientialM
 * @date 2026/3/29 23:57
 * @since 1.0
 */
public interface MyHandler {
    
    /**
     * 根据方法名生成方法体代码
     * 
     * 返回的字符串必须是合法的 Java 代码，会被直接插入到生成类的方法中。
     * 例如：返回 "System.out.println(\"hello\");" 
     * 则生成的方法为：
     *   public void func1() {
     *       System.out.println("hello");
     *   }
     *
     * @param methodName 方法名（如 "func1", "func2" 等）
     * @return 该方法的方法体 Java 代码字符串
     */
    String functionBody(String methodName);

    /**
     * 设置代理对象引用（用于嵌套代理场景）
     * 
     * 当需要实现包装代理（即在已有代理基础上添加功能）时，
     * 通过此方法将内部代理对象注入到生成的代理类中。
     * 
     * 默认实现为空，表示不需要注入。
     * 
     * @param proxy 生成的代理对象实例
     * @throws Exception 反射操作可能抛出的异常
     */
    default void setProxy(MyInterface proxy) throws Exception {
        // 默认空实现：简单代理不需要注入目标对象
    }
}
