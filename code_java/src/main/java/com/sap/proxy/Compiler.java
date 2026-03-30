package com.sap.proxy;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.util.Collections;

/**
 * 动态 Java 编译器
 * 
 * 本类负责将动态生成的 Java 源文件编译为字节码(.class)文件
 * 编译后的类文件会被输出到 ./target/classes 目录下
 * 
 * 核心流程：
 * 1. 获取系统 Java 编译器 (ToolProvider.getSystemJavaCompiler())
 * 2. 配置编译选项（输出目录等）
 * 3. 执行编译
 * 4. 处理编译结果和错误
 * 
 * @author SapientialM
 * @date 2026/3/29 23:13
 * @since 1.0
 */
public class Compiler {

    /**
     * 编译 Java 源文件到 ./target/classes 目录
     * 
     * 注意：运行此方法需要 JDK（不是 JRE），因为需要使用其中的 Java 编译器
     *
     * @param javaFile 要编译的 Java 源文件
     * @throws RuntimeException 当编译失败时抛出，包含详细的错误信息
     */
    public static void compile(File javaFile) {
        // 参数校验：确保文件存在且有效
        if (javaFile == null || !javaFile.exists()) {
            throw new IllegalArgumentException("Java file does not exist: " + javaFile);
        }

        // 获取系统 Java 编译器（需要 JDK 环境）
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new RuntimeException("System Java compiler not available. " +
                    "Please run with JDK (not JRE) and ensure tools.jar is in classpath.");
        }

        // 诊断收集器：用于收集编译过程中的错误和警告信息
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

        // 使用 try-with-resources 自动关闭文件管理器
        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(
                diagnostics, null, null)) {

            // 设置编译输出目录为 target/classes（Maven 标准输出目录）
            File outputDir = new File("./target/classes");
            if (!outputDir.exists()) {
                outputDir.mkdirs();  // 递归创建目录
            }

            // 获取要编译的源文件对象
            Iterable<? extends JavaFileObject> compilationUnits =
                    fileManager.getJavaFileObjectsFromFiles(Collections.singletonList(javaFile));

            // 配置编译选项：-d 指定输出目录
            // 还可以添加 -classpath 选项来指定依赖类的路径
            Iterable<String> options = java.util.Arrays.asList(
                    "-d", outputDir.getAbsolutePath(),
                    "-cp", outputDir.getAbsolutePath()  // 添加类路径，确保能找到依赖的类
            );

            // 创建编译任务
            JavaCompiler.CompilationTask task = compiler.getTask(
                    null,           // 输出 Writer（null 表示使用 System.err）
                    fileManager,    // 文件管理器
                    diagnostics,    // 诊断收集器
                    options,        // 编译选项
                    null,           // 类名（用于注解处理，这里不需要）
                    compilationUnits // 要编译的源文件列表
            );

            // 执行编译
            boolean success = task.call();

            if (!success) {
                // 编译失败：收集所有错误信息并抛出异常
                StringBuilder errorMsg = new StringBuilder("Compilation failed:\n");
                diagnostics.getDiagnostics().forEach(diagnostic ->
                        errorMsg.append(diagnostic.getMessage(null)).append("\n")
                );
                throw new RuntimeException(errorMsg.toString());
            }

            System.out.println("Compilation successful: " + javaFile.getName());

        } catch (Exception e) {
            // 统一异常处理
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException("Compilation error: " + e.getMessage(), e);
        }
    }
}
