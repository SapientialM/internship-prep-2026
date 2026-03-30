package com.sap.proxy;

/**
 * 静态实现示例 - 用于对比理解动态代理的价值
 * 
 * 本类展示了传统的手动实现方式：
 * 假设需求是"让每个方法都打印自己的名字和名字长度"，
 * 传统做法就是像下面这样手动编写每个方法的实现。
 * 
 * 问题：
 * 1. 代码重复：每个方法都重复了 "String methodName = ..." 的逻辑
 * 2. 修改困难：如果要修改打印格式，需要修改所有方法
 * 3. 扩展困难：新增方法时需要重复同样的模板代码
 * 
 * 动态代理的解决方案：
 * 通过动态生成代码，可以只写一次逻辑，应用到所有方法上。
 * 这就是 MyInterfaceFactory 的价值所在。
 * 
 * @author SapientialM
 * @date 2026/3/29 23:00
 * @since 1.0
 */
public class NameAndLengthImpl implements MyInterface {

    @Override
    public void func1() {
        // 重复的逻辑：获取方法名并打印
        String methodName = "func1";
        System.out.println(methodName);
        System.out.println(methodName.length());
        System.out.println("func1");
    }

    @Override
    public void func2() {
        // 同样的逻辑重复三遍...
        String methodName = "func2";
        System.out.println(methodName);
        System.out.println(methodName.length());
        System.out.println("func2");
    }

    @Override
    public void func3() {
        // 同样的逻辑重复三遍...
        String methodName = "func3";
        System.out.println(methodName);
        System.out.println(methodName.length());
        System.out.println("func3");
    }
}
