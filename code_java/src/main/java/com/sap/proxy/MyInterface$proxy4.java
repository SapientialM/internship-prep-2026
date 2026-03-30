package com.sap.proxy;

public class MyInterface$proxy4 implements MyInterface {
    // target 字段用于嵌套代理：持有被包装的内部代理对象
    MyInterface target;

    @Override
    public void func1() {
        System.out.println("before");
        target.func1();
        System.out.println("after");
    }

    @Override
    public void func2() {
        System.out.println("before");
        target.func2();
        System.out.println("after");
    }

    @Override
    public void func3() {
        System.out.println("before");
        target.func3();
        System.out.println("after");
    }
}
