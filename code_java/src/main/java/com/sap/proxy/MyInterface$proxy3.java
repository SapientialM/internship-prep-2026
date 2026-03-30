package com.sap.proxy;

public class MyInterface$proxy3 implements MyInterface {
    // target 字段用于嵌套代理：持有被包装的内部代理对象
    MyInterface target;

    @Override
    public void func1() {
        System.out.println("test1"); System.out.println("func1");
    }

    @Override
    public void func2() {
        System.out.println("test1"); System.out.println("func2");
    }

    @Override
    public void func3() {
        System.out.println("test1"); System.out.println("func3");
    }
}
