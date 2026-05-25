# 07 - Spring

> Spring 是 Java 后端的事实标准。IoC、AOP、事务、Bean 生命周期是面试的核心考点。

---

## 1. IoC（控制反转）

### 核心概念

IoC 将对象的创建和管理权交给 Spring 容器，开发者只需声明依赖关系。

**IoC 容器：**
- **BeanFactory** — 基础容器，延迟加载 Bean
- **ApplicationContext** — 高级容器，启动时预加载所有单例 Bean（常用）

### DI（依赖注入）方式

| 方式 | 说明 | 推荐 |
|:---|:---|:---:|
| 构造器注入 | `@Autowired` 在构造器上 | ✅ 推荐（不可变，易测试） |
| Setter 注入 | `@Autowired` 在 setter 上 | 可选依赖时使用 |
| 字段注入 | `@Autowired` 在字段上 | ❌ 不推荐（难测试，隐藏依赖） |

### Bean 的作用域

| 作用域 | 说明 |
|:---|:---|
| singleton | 单例（默认），整个容器共享一个实例 |
| prototype | 多例，每次注入/获取都创建新实例 |
| request | 每个 HTTP 请求一个实例（Web） |
| session | 每个 HTTP Session 一个实例（Web） |
| application | 每个 ServletContext 一个实例（Web） |

**singleton Bean 为什么线程安全？**

Bean 本身不保证线程安全。线程安全取决于 Bean 的状态：
- **无状态 Bean（如 Controller/Service/Repository）** → 天然线程安全
- **有状态 Bean（如持有可变成员变量）** → 需要自己保证线程安全（用 ThreadLocal 或加锁）

---

## 2. AOP（面向切面编程）

### 核心概念

| 概念 | 说明 |
|:---|:---|
| Aspect（切面） | 横切关注点的模块化（@Aspect） |
| Join Point（连接点） | 程序执行中的某个点（方法执行/异常抛出） |
| Advice（通知） | 在连接点执行的操作（@Before/@After/@Around） |
| Pointcut（切入点） | 匹配连接点的表达式 |
| Weaving（织入） | 将切面应用到目标对象的过程 |

### AOP 实现原理

基于**动态代理**：
1. **JDK 动态代理** — 目标类有接口时，生成 `$Proxy` 类
2. **CGLIB 代理** — 目标类无接口时，生成目标类的子类

Spring Boot 2.x+ 默认使用 CGLIB 代理。

### AOP 应用场景

- **事务管理** — `@Transactional`
- **日志记录** — 统一方法调用日志
- **权限验证** — `@PreAuthorize` 方法级权限检查
- **缓存** — `@Cacheable`
- **限流** — 自定义注解 `@RateLimit`
- **异常处理** — `@ControllerAdvice` 统一异常拦截

---

## 3. 事务管理

### @Transactional 原理

基于 AOP：方法执行前后通过 `TransactionInterceptor` 管理事务。

### 失效场景

```java
// 1. 方法非 public
@Transactional
private void doSomething() {}  // ❌ AOP 代理只能拦截 public 方法

// 2. 同类方法自调用
public void methodA() {
    this.methodB();  // ❌ 不经过代理，事务不生效
}
@Transactional
public void methodB() {}

// 3. 异常被 Catch
@Transactional
public void method() {
    try {
        // db operation
    } catch (Exception e) {
        // ❌ 事务不回滚（异常被吞了）
    }
}

// 4. rollbackFor 配置错误
@Transactional(rollbackFor = Exception.class)  // ✅ 正确：所有异常都回滚
```

### 传播行为

| 传播行为 | 说明 |
|:---|:---|
| REQUIRED（默认） | 有事务则加入，无则新建 |
| REQUIRES_NEW | 总是新建事务，挂起当前事务 |
| NESTED | 嵌套事务（基于 Savepoint） |
| SUPPORTS | 有事务则加入，无则非事务执行 |
| NOT_SUPPORTED | 非事务执行，挂起当前事务 |
| MANDATORY | 必须有事务，否则抛异常 |
| NEVER | 必须无事务，否则抛异常 |

---

## 4. Bean 生命周期

```
1. 实例化（通过反射调用构造器）
        ↓
2. 属性赋值（@Autowired, @Value 注入）
        ↓
3. Aware 接口回调（BeanNameAware → BeanFactoryAware → ApplicationContextAware）
        ↓
4. BeanPostProcessor 前置处理（postProcessBeforeInitialization）
        ↓
5. @PostConstruct 方法调用
        ↓
6. InitializingBean.afterPropertiesSet()
        ↓
7. init-method 调用
        ↓
8. BeanPostProcessor 后置处理（postProcessAfterInitialization → AOP 代理在此创建）
        ↓
9. Bean 就绪
        ↓
10. @PreDestroy / DisposableBean.destroy() / destroy-method（容器关闭时）
```

---

## 5. Spring Boot 自动配置（Auto-Configuration）

### @SpringBootApplication 做了什么

```java
@SpringBootApplication  // 组合注解
  = @SpringBootConfiguration + @EnableAutoConfiguration + @ComponentScan
```

### 自动配置原理

```
1. @EnableAutoConfiguration
     ↓
2. @Import(AutoConfigurationImportSelector.class)
     ↓
3. 读取 META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports (Spring Boot 3.x)
     ↓
4. 加载所有自动配置类（如 DataSourceAutoConfiguration）
     ↓
5. @ConditionalOnClass / @ConditionalOnMissingBean 等条件注解判断
     ↓
6. 满足条件则创建对应的 Bean
```

### 自定义 Starter

1. 创建 `xxx-spring-boot-autoconfigure` 模块
2. 编写自动配置类（`@AutoConfiguration` + `@ConditionalOnXxx`）
3. 在 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 中注册

---

## 6. Spring 循环依赖

### 三级缓存解决循环依赖

```
Map<String, Object> singletonObjects          // 一级缓存：完全初始化的 Bean
Map<String, Object> earlySingletonObjects     // 二级缓存：提前暴露的 Bean（未完成属性填充）
Map<String, ObjectFactory<?>> singletonFactories  // 三级缓存：Bean 工厂（可生成代理对象）
```

**流程（A ↔ B 循环依赖）：**
```
1. 创建 A → 发现依赖 B → 提前曝光 A 的 ObjectFactory 到三级缓存
2. 创建 B → 发现依赖 A → 从三级缓存获取 A 的早期引用 → B 完成创建
3. 回到 A → 注入 B → A 完成创建
```

**无法解决的循环依赖：**
- 构造器注入（Spring 要求构造器参数必须可用）
- prototype 作用域的循环依赖
- 使用 `@Async` 的循环依赖（代理对象生成时序问题）

解决建议：重构代码，消除循环依赖（使用 Setter 注入或 `@Lazy`）。

---

## 7. Spring MVC 请求处理流程

```
Request → DispatcherServlet
              ↓
     HandlerMapping（找到 Handler）
              ↓
     HandlerAdapter（执行 Handler）
              ↓
     Handler（Controller 方法）
              ↓
     ViewResolver（解析视图）
              ↓
     Response
```

对于 REST 接口（`@RestController` + `@ResponseBody`），`HttpMessageConverter` 直接将返回值序列化为 JSON，不需要 ViewResolver。
