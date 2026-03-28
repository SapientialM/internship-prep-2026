package com.sap.collections;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.chart.ui.RectangleInsets;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.awt.Color;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.List;

/**
 * LinkedList vs ArrayDeque 性能对比测试
 * 测试内容：
 * 1. 两端添加/删除操作性能
 * 2. 遍历所有元素性能
 * 3. 内存占用情况
 * 
 * 梯度：100, 500, 1000, 5000, 10000, 50000, 100000, 500000, 1000000
 */
class DequePerformanceTest {

    // 测试梯度
    private static final int[] SIZES = {100, 500, 1000, 5000, 10000, 50000, 100000, 500000, 1000000};
    
    // 预热次数
    private static final int WARMUP_ITERATIONS = 3;
    
    // 实际测试次数（取平均值）
    private static final int TEST_ITERATIONS = 5;
    
    // 结果输出目录
    private static final String OUTPUT_DIR = "target/performance-results";

    @BeforeAll
    static void setup() {
        // 设置无头模式，用于无图形环境的服务器
        System.setProperty("java.awt.headless", "true");
    }

    @Test
    void runAllPerformanceTests() throws IOException {
        // 创建输出目录
        new File(OUTPUT_DIR).mkdirs();
        
        System.out.println("==============================================");
        System.out.println("LinkedList vs ArrayDeque 性能对比测试");
        System.out.println("==============================================\n");
        
        // 存储所有测试结果
        List<TestResult> addFirstResults = new LinkedList<>();
        List<TestResult> addLastResults = new LinkedList<>();
        List<TestResult> removeFirstResults = new LinkedList<>();
        List<TestResult> removeLastResults = new LinkedList<>();
        List<TestResult> iterateResults = new LinkedList<>();
        List<MemoryResult> memoryResults = new LinkedList<>();
        
        // 执行各项测试
        for (int size : SIZES) {
            System.out.println("\n--- 测试数据量: " + size + " ---");
            
            // 1. 两端添加测试
            addFirstResults.add(testAddFirst(size));
            addLastResults.add(testAddLast(size));
            
            // 2. 两端删除测试
            removeFirstResults.add(testRemoveFirst(size));
            removeLastResults.add(testRemoveLast(size));
            
            // 3. 遍历测试
            iterateResults.add(testIteration(size));
            
            // 4. 内存占用测试
            memoryResults.add(testMemoryUsage(size));
        }
        
        // 生成报告
        generateCSVReport(addFirstResults, addLastResults, removeFirstResults, 
                         removeLastResults, iterateResults, memoryResults);
        
        // 生成图表
        generateCharts(addFirstResults, addLastResults, removeFirstResults, 
                      removeLastResults, iterateResults, memoryResults);
        
        System.out.println("\n==============================================");
        System.out.println("测试完成！结果保存在: " + OUTPUT_DIR);
        System.out.println("==============================================");
    }

    // ==================== 性能测试方法 ====================

    /**
     * 测试头部添加性能
     */
    private TestResult testAddFirst(int size) {
        // 预热
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            LinkedList<Integer> ll = new LinkedList<>();
            ArrayDeque<Integer> ad = new ArrayDeque<>();
            performAddFirst(ll, size);
            performAddFirst(ad, size);
        }
        
        // 强制GC
        System.gc();
        
        long llTotalTime = 0;
        long adTotalTime = 0;
        
        for (int iter = 0; iter < TEST_ITERATIONS; iter++) {
            // 测试 LinkedList
            LinkedList<Integer> ll = new LinkedList<>();
            long start = System.nanoTime();
            performAddFirst(ll, size);
            llTotalTime += System.nanoTime() - start;
            
            // 测试 ArrayDeque
            ArrayDeque<Integer> ad = new ArrayDeque<>();
            start = System.nanoTime();
            performAddFirst(ad, size);
            adTotalTime += System.nanoTime() - start;
        }
        
        TestResult result = new TestResult("AddFirst", size, 
            llTotalTime / TEST_ITERATIONS, adTotalTime / TEST_ITERATIONS);
        System.out.printf("AddFirst - Size: %d, LinkedList: %,d ns, ArrayDeque: %,d ns%n",
            size, result.linkedListTime, result.arrayDequeTime);
        return result;
    }
    
    private void performAddFirst(LinkedList<Integer> list, int size) {
        for (int i = 0; i < size; i++) {
            list.addFirst(i);
        }
    }
    
    private void performAddFirst(ArrayDeque<Integer> deque, int size) {
        for (int i = 0; i < size; i++) {
            deque.addFirst(i);
        }
    }

    /**
     * 测试尾部添加性能
     */
    private TestResult testAddLast(int size) {
        // 预热
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            LinkedList<Integer> ll = new LinkedList<>();
            ArrayDeque<Integer> ad = new ArrayDeque<>();
            performAddLast(ll, size);
            performAddLast(ad, size);
        }
        
        System.gc();
        
        long llTotalTime = 0;
        long adTotalTime = 0;
        
        for (int iter = 0; iter < TEST_ITERATIONS; iter++) {
            LinkedList<Integer> ll = new LinkedList<>();
            long start = System.nanoTime();
            performAddLast(ll, size);
            llTotalTime += System.nanoTime() - start;
            
            ArrayDeque<Integer> ad = new ArrayDeque<>();
            start = System.nanoTime();
            performAddLast(ad, size);
            adTotalTime += System.nanoTime() - start;
        }
        
        TestResult result = new TestResult("AddLast", size, 
            llTotalTime / TEST_ITERATIONS, adTotalTime / TEST_ITERATIONS);
        System.out.printf("AddLast - Size: %d, LinkedList: %,d ns, ArrayDeque: %,d ns%n",
            size, result.linkedListTime, result.arrayDequeTime);
        return result;
    }
    
    private void performAddLast(LinkedList<Integer> list, int size) {
        for (int i = 0; i < size; i++) {
            list.addLast(i);
        }
    }
    
    private void performAddLast(ArrayDeque<Integer> deque, int size) {
        for (int i = 0; i < size; i++) {
            deque.addLast(i);
        }
    }

    /**
     * 测试头部删除性能
     */
    private TestResult testRemoveFirst(int size) {
        // 预热
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            LinkedList<Integer> ll = createLinkedList(size);
            ArrayDeque<Integer> ad = createArrayDeque(size);
            performRemoveFirst(ll);
            performRemoveFirst(ad);
        }
        
        System.gc();
        
        long llTotalTime = 0;
        long adTotalTime = 0;
        
        for (int iter = 0; iter < TEST_ITERATIONS; iter++) {
            LinkedList<Integer> ll = createLinkedList(size);
            long start = System.nanoTime();
            performRemoveFirst(ll);
            llTotalTime += System.nanoTime() - start;
            
            ArrayDeque<Integer> ad = createArrayDeque(size);
            start = System.nanoTime();
            performRemoveFirst(ad);
            adTotalTime += System.nanoTime() - start;
        }
        
        TestResult result = new TestResult("RemoveFirst", size, 
            llTotalTime / TEST_ITERATIONS, adTotalTime / TEST_ITERATIONS);
        System.out.printf("RemoveFirst - Size: %d, LinkedList: %,d ns, ArrayDeque: %,d ns%n",
            size, result.linkedListTime, result.arrayDequeTime);
        return result;
    }
    
    private void performRemoveFirst(LinkedList<Integer> list) {
        while (!list.isEmpty()) {
            list.removeFirst();
        }
    }
    
    private void performRemoveFirst(ArrayDeque<Integer> deque) {
        while (!deque.isEmpty()) {
            deque.removeFirst();
        }
    }

    /**
     * 测试尾部删除性能
     */
    private TestResult testRemoveLast(int size) {
        // 预热
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            LinkedList<Integer> ll = createLinkedList(size);
            ArrayDeque<Integer> ad = createArrayDeque(size);
            performRemoveLast(ll);
            performRemoveLast(ad);
        }
        
        System.gc();
        
        long llTotalTime = 0;
        long adTotalTime = 0;
        
        for (int iter = 0; iter < TEST_ITERATIONS; iter++) {
            LinkedList<Integer> ll = createLinkedList(size);
            long start = System.nanoTime();
            performRemoveLast(ll);
            llTotalTime += System.nanoTime() - start;
            
            ArrayDeque<Integer> ad = createArrayDeque(size);
            start = System.nanoTime();
            performRemoveLast(ad);
            adTotalTime += System.nanoTime() - start;
        }
        
        TestResult result = new TestResult("RemoveLast", size, 
            llTotalTime / TEST_ITERATIONS, adTotalTime / TEST_ITERATIONS);
        System.out.printf("RemoveLast - Size: %d, LinkedList: %,d ns, ArrayDeque: %,d ns%n",
            size, result.linkedListTime, result.arrayDequeTime);
        return result;
    }
    
    private void performRemoveLast(LinkedList<Integer> list) {
        while (!list.isEmpty()) {
            list.removeLast();
        }
    }
    
    private void performRemoveLast(ArrayDeque<Integer> deque) {
        while (!deque.isEmpty()) {
            deque.removeLast();
        }
    }

    /**
     * 测试遍历性能
     */
    private TestResult testIteration(int size) {
        // 预热
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            LinkedList<Integer> ll = createLinkedList(size);
            ArrayDeque<Integer> ad = createArrayDeque(size);
            performIteration(ll);
            performIteration(ad);
        }
        
        System.gc();
        
        long llTotalTime = 0;
        long adTotalTime = 0;
        
        for (int iter = 0; iter < TEST_ITERATIONS; iter++) {
            LinkedList<Integer> ll = createLinkedList(size);
            long start = System.nanoTime();
            performIteration(ll);
            llTotalTime += System.nanoTime() - start;
            
            ArrayDeque<Integer> ad = createArrayDeque(size);
            start = System.nanoTime();
            performIteration(ad);
            adTotalTime += System.nanoTime() - start;
        }
        
        TestResult result = new TestResult("Iteration", size, 
            llTotalTime / TEST_ITERATIONS, adTotalTime / TEST_ITERATIONS);
        System.out.printf("Iteration - Size: %d, LinkedList: %,d ns, ArrayDeque: %,d ns%n",
            size, result.linkedListTime, result.arrayDequeTime);
        return result;
    }
    
    private long performIteration(LinkedList<Integer> list) {
        long sum = 0;
        for (Integer val : list) {
            sum += val;
        }
        return sum;
    }
    
    private long performIteration(ArrayDeque<Integer> deque) {
        long sum = 0;
        for (Integer val : deque) {
            sum += val;
        }
        return sum;
    }

    /**
     * 测试内存占用
     */
    private MemoryResult testMemoryUsage(int size) {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        
        // 获取GC前的内存信息
        System.gc();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        MemoryUsage beforeHeap = memoryMXBean.getHeapMemoryUsage();
        
        // 测试 LinkedList 内存
        LinkedList<Integer> ll = new LinkedList<>();
        for (int i = 0; i < size; i++) {
            ll.add(i);
        }
        MemoryUsage llHeap = memoryMXBean.getHeapMemoryUsage();
        long llUsed = llHeap.getUsed() - beforeHeap.getUsed();
        
        // 清理
        ll = null;
        System.gc();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        beforeHeap = memoryMXBean.getHeapMemoryUsage();
        
        // 测试 ArrayDeque 内存
        ArrayDeque<Integer> ad = new ArrayDeque<>();
        for (int i = 0; i < size; i++) {
            ad.add(i);
        }
        MemoryUsage adHeap = memoryMXBean.getHeapMemoryUsage();
        long adUsed = adHeap.getUsed() - beforeHeap.getUsed();
        
        // 计算每个元素平均内存占用（字节）
        double llAvgBytes = (double) llUsed / size;
        double adAvgBytes = (double) adUsed / size;
        
        MemoryResult result = new MemoryResult(size, llUsed, adUsed, llAvgBytes, adAvgBytes);
        System.out.printf("Memory - Size: %d, LinkedList: %,d bytes (%.2f/elem), ArrayDeque: %,d bytes (%.2f/elem)%n",
            size, llUsed, llAvgBytes, adUsed, adAvgBytes);
        return result;
    }

    // ==================== 辅助方法 ====================

    private LinkedList<Integer> createLinkedList(int size) {
        LinkedList<Integer> list = new LinkedList<>();
        for (int i = 0; i < size; i++) {
            list.add(i);
        }
        return list;
    }

    private ArrayDeque<Integer> createArrayDeque(int size) {
        ArrayDeque<Integer> deque = new ArrayDeque<>();
        for (int i = 0; i < size; i++) {
            deque.add(i);
        }
        return deque;
    }

    // ==================== 报告生成 ====================

    private void generateCSVReport(List<TestResult> addFirst, List<TestResult> addLast,
                                   List<TestResult> removeFirst, List<TestResult> removeLast,
                                   List<TestResult> iterate, List<MemoryResult> memory) throws IOException {
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(OUTPUT_DIR + "/performance_report.csv"))) {
            // 写入表头
            writer.println("测试项目,数据量,LinkedList耗时(ns),ArrayDeque耗时(ns),LinkedList内存(bytes),ArrayDeque内存(bytes)");
            
            // 写入AddFirst
            for (TestResult r : addFirst) {
                writer.printf("AddFirst,%d,%d,%d,,%n", r.size, r.linkedListTime, r.arrayDequeTime);
            }
            
            // 写入AddLast
            for (TestResult r : addLast) {
                writer.printf("AddLast,%d,%d,%d,,%n", r.size, r.linkedListTime, r.arrayDequeTime);
            }
            
            // 写入RemoveFirst
            for (TestResult r : removeFirst) {
                writer.printf("RemoveFirst,%d,%d,%d,,%n", r.size, r.linkedListTime, r.arrayDequeTime);
            }
            
            // 写入RemoveLast
            for (TestResult r : removeLast) {
                writer.printf("RemoveLast,%d,%d,%d,,%n", r.size, r.linkedListTime, r.arrayDequeTime);
            }
            
            // 写入Iteration
            for (TestResult r : iterate) {
                writer.printf("Iteration,%d,%d,%d,,%n", r.size, r.linkedListTime, r.arrayDequeTime);
            }
            
            // 写入Memory
            for (MemoryResult r : memory) {
                writer.printf("Memory,%d,,,%d,%d%n", r.size, r.linkedListMemory, r.arrayDequeMemory);
            }
        }
        
        System.out.println("\nCSV报告已生成: " + OUTPUT_DIR + "/performance_report.csv");
    }

    private void generateCharts(List<TestResult> addFirst, List<TestResult> addLast,
                               List<TestResult> removeFirst, List<TestResult> removeLast,
                               List<TestResult> iterate, List<MemoryResult> memory) throws IOException {
        
        // 1. 添加操作对比图
        createTimeChart("AddFirst", addFirst, "addFirst_chart.png");
        createTimeChart("AddLast", addLast, "addLast_chart.png");
        
        // 2. 删除操作对比图
        createTimeChart("RemoveFirst", removeFirst, "removeFirst_chart.png");
        createTimeChart("RemoveLast", removeLast, "removeLast_chart.png");
        
        // 3. 遍历操作对比图
        createTimeChart("Iteration", iterate, "iteration_chart.png");
        
        // 4. 内存占用对比图
        createMemoryChart(memory);
        
        // 5. 综合对比图（所有操作）
        createCombinedTimeChart(addFirst, addLast, removeFirst, removeLast, iterate);
        
        // 6. 仪表板式综合对比图（4合1）
        createDashboardChart(addFirst, addLast, removeFirst, removeLast, iterate, memory);
    }

    private void createTimeChart(String title, List<TestResult> results, String filename) throws IOException {
        XYSeries llSeries = new XYSeries("LinkedList");
        XYSeries adSeries = new XYSeries("ArrayDeque");
        
        for (TestResult r : results) {
            llSeries.add(r.size, r.linkedListTime / 1000.0); // 转换为微秒
            adSeries.add(r.size, r.arrayDequeTime / 1000.0);
        }
        
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(llSeries);
        dataset.addSeries(adSeries);
        
        JFreeChart chart = ChartFactory.createXYLineChart(
            title + " Performance Comparison",
            "Data Size",
            "Time (microseconds)",
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        );
        
        ChartUtils.saveChartAsPNG(new File(OUTPUT_DIR + "/" + filename), chart, 800, 600);
        System.out.println("图表已生成: " + OUTPUT_DIR + "/" + filename);
    }

    private void createMemoryChart(List<MemoryResult> results) throws IOException {
        // 总内存占用图
        XYSeries llSeries = new XYSeries("LinkedList");
        XYSeries adSeries = new XYSeries("ArrayDeque");
        
        for (MemoryResult r : results) {
            llSeries.add(r.size, r.linkedListMemory / 1024.0); // 转换为KB
            adSeries.add(r.size, r.arrayDequeMemory / 1024.0);
        }
        
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(llSeries);
        dataset.addSeries(adSeries);
        
        JFreeChart chart = ChartFactory.createXYLineChart(
            "Memory Usage Comparison",
            "Data Size",
            "Memory (KB)",
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        );
        
        ChartUtils.saveChartAsPNG(new File(OUTPUT_DIR + "/memory_chart.png"), chart, 800, 600);
        System.out.println("图表已生成: " + OUTPUT_DIR + "/memory_chart.png");
        
        // 每个元素平均内存占用图
        XYSeries llAvgSeries = new XYSeries("LinkedList");
        XYSeries adAvgSeries = new XYSeries("ArrayDeque");
        
        for (MemoryResult r : results) {
            llAvgSeries.add(r.size, r.linkedListAvgBytes);
            adAvgSeries.add(r.size, r.arrayDequeAvgBytes);
        }
        
        XYSeriesCollection avgDataset = new XYSeriesCollection();
        avgDataset.addSeries(llAvgSeries);
        avgDataset.addSeries(adAvgSeries);
        
        JFreeChart avgChart = ChartFactory.createXYLineChart(
            "Average Memory per Element",
            "Data Size",
            "Bytes per Element",
            avgDataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        );
        
        ChartUtils.saveChartAsPNG(new File(OUTPUT_DIR + "/memory_avg_chart.png"), avgChart, 800, 600);
        System.out.println("图表已生成: " + OUTPUT_DIR + "/memory_avg_chart.png");
    }

    private void createCombinedTimeChart(List<TestResult> addFirst, List<TestResult> addLast,
                                        List<TestResult> removeFirst, List<TestResult> removeLast,
                                        List<TestResult> iterate) throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        // 取最后一个数据点（最大数据量）进行对比
        int lastIdx = addFirst.size() - 1;
        
        dataset.addValue(addFirst.get(lastIdx).linkedListTime / 1000000.0, "LinkedList", "AddFirst");
        dataset.addValue(addFirst.get(lastIdx).arrayDequeTime / 1000000.0, "ArrayDeque", "AddFirst");
        
        dataset.addValue(addLast.get(lastIdx).linkedListTime / 1000000.0, "LinkedList", "AddLast");
        dataset.addValue(addLast.get(lastIdx).arrayDequeTime / 1000000.0, "ArrayDeque", "AddLast");
        
        dataset.addValue(removeFirst.get(lastIdx).linkedListTime / 1000000.0, "LinkedList", "RemoveFirst");
        dataset.addValue(removeFirst.get(lastIdx).arrayDequeTime / 1000000.0, "ArrayDeque", "RemoveFirst");
        
        dataset.addValue(removeLast.get(lastIdx).linkedListTime / 1000000.0, "LinkedList", "RemoveLast");
        dataset.addValue(removeLast.get(lastIdx).arrayDequeTime / 1000000.0, "ArrayDeque", "RemoveLast");
        
        dataset.addValue(iterate.get(lastIdx).linkedListTime / 1000000.0, "LinkedList", "Iteration");
        dataset.addValue(iterate.get(lastIdx).arrayDequeTime / 1000000.0, "ArrayDeque", "Iteration");
        
        JFreeChart chart = ChartFactory.createBarChart(
            "Operations Comparison (Size=" + addFirst.get(lastIdx).size + ")",
            "Operation",
            "Time (ms)",
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        );
        
        ChartUtils.saveChartAsPNG(new File(OUTPUT_DIR + "/combined_chart.png"), chart, 1000, 600);
        System.out.println("图表已生成: " + OUTPUT_DIR + "/combined_chart.png");
    }

    /**
     * 创建仪表板式综合图表（2x2布局）
     */
    private void createDashboardChart(List<TestResult> addFirst, List<TestResult> addLast,
                                     List<TestResult> removeFirst, List<TestResult> removeLast,
                                     List<TestResult> iterate, List<MemoryResult> memory) throws IOException {
        
        // 创建4个子图
        XYPlot addPlot = createSubPlot(addFirst, addLast, "Add Operations", "Time (μs)");
        XYPlot removePlot = createSubPlot(removeFirst, removeLast, "Remove Operations", "Time (μs)");
        XYPlot iteratePlot = createSingleSubPlot(iterate, "Iteration Performance", "Time (μs)");
        XYPlot memoryPlot = createMemorySubPlot(memory);
        
        // 创建组合图 - 上半部分（添加和删除）
        CombinedDomainXYPlot topPlot = new CombinedDomainXYPlot(new NumberAxis("Data Size"));
        topPlot.add(addPlot, 1);
        topPlot.add(removePlot, 1);
        topPlot.setGap(10.0);
        
        // 创建组合图 - 下半部分（遍历和内存）
        CombinedDomainXYPlot bottomPlot = new CombinedDomainXYPlot(new NumberAxis("Data Size"));
        bottomPlot.add(iteratePlot, 1);
        bottomPlot.add(memoryPlot, 1);
        bottomPlot.setGap(10.0);
        
        // 最终组合 - 垂直布局
        CombinedDomainXYPlot finalPlot = new CombinedDomainXYPlot();
        finalPlot.add(topPlot, 1);
        finalPlot.add(bottomPlot, 1);
        finalPlot.setOrientation(PlotOrientation.VERTICAL);
        finalPlot.setGap(15.0);
        
        JFreeChart chart = new JFreeChart("LinkedList vs ArrayDeque - Comprehensive Comparison", 
                                         JFreeChart.DEFAULT_TITLE_FONT, finalPlot, true);
        
        ChartUtils.saveChartAsPNG(new File(OUTPUT_DIR + "/dashboard_chart.png"), chart, 1400, 1100);
        System.out.println("图表已生成: " + OUTPUT_DIR + "/dashboard_chart.png");
        
        // 额外生成一个更简洁的2x2图表，每个子图有独立标题
        createCleanDashboard(addFirst, addLast, removeFirst, removeLast, iterate, memory);
    }
    
    /**
     * 创建更简洁的仪表板图表
     */
    private void createCleanDashboard(List<TestResult> addFirst, List<TestResult> addLast,
                                     List<TestResult> removeFirst, List<TestResult> removeLast,
                                     List<TestResult> iterate, List<MemoryResult> memory) throws IOException {
        
        // 为每个操作创建单独的图表
        JFreeChart chart1 = createStandaloneChart(addFirst, addLast, "Add Operations (First vs Last)", "Time (μs)");
        JFreeChart chart2 = createStandaloneChart(removeFirst, removeLast, "Remove Operations (First vs Last)", "Time (μs)");
        JFreeChart chart3 = createSimpleChart(iterate, "Iteration Performance", "Time (μs)");
        JFreeChart chart4 = createMemoryChartStandalone(memory);
        
        // 创建组合域XYPlot（共享X轴）
        CombinedDomainXYPlot combinedPlot = new CombinedDomainXYPlot(new NumberAxis("Data Size"));
        
        combinedPlot.add((XYPlot) chart1.getPlot(), 1);
        combinedPlot.add((XYPlot) chart2.getPlot(), 1);
        combinedPlot.add((XYPlot) chart3.getPlot(), 1);
        combinedPlot.add((XYPlot) chart4.getPlot(), 1);
        combinedPlot.setOrientation(PlotOrientation.VERTICAL);
        combinedPlot.setGap(15.0);
        
        JFreeChart finalChart = new JFreeChart(
            "LinkedList vs ArrayDeque - Full Comparison Dashboard", 
            JFreeChart.DEFAULT_TITLE_FONT, combinedPlot, true);
        
        ChartUtils.saveChartAsPNG(new File(OUTPUT_DIR + "/full_dashboard.png"), finalChart, 1400, 1200);
        System.out.println("图表已生成: " + OUTPUT_DIR + "/full_dashboard.png");
    }
    
    private JFreeChart createStandaloneChart(List<TestResult> result1, List<TestResult> result2, 
                                              String title, String yLabel) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        
        XYSeries llSeries1 = new XYSeries("LinkedList-" + result1.get(0).operation);
        XYSeries adSeries1 = new XYSeries("ArrayDeque-" + result1.get(0).operation);
        XYSeries llSeries2 = new XYSeries("LinkedList-" + result2.get(0).operation);
        XYSeries adSeries2 = new XYSeries("ArrayDeque-" + result2.get(0).operation);
        
        for (int i = 0; i < result1.size(); i++) {
            llSeries1.add(result1.get(i).size, result1.get(i).linkedListTime / 1000.0);
            adSeries1.add(result1.get(i).size, result1.get(i).arrayDequeTime / 1000.0);
            llSeries2.add(result2.get(i).size, result2.get(i).linkedListTime / 1000.0);
            adSeries2.add(result2.get(i).size, result2.get(i).arrayDequeTime / 1000.0);
        }
        
        dataset.addSeries(llSeries1);
        dataset.addSeries(adSeries1);
        dataset.addSeries(llSeries2);
        dataset.addSeries(adSeries2);
        
        JFreeChart chart = ChartFactory.createXYLineChart(title, "Data Size", yLabel, dataset,
            PlotOrientation.VERTICAL, true, true, false);
        
        // 设置颜色：LinkedList 红色，ArrayDeque 蓝色
        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, Color.RED);
        renderer.setSeriesPaint(1, Color.BLUE);
        renderer.setSeriesPaint(2, new Color(200, 0, 0)); // 深红
        renderer.setSeriesPaint(3, new Color(0, 0, 200)); // 深蓝
        
        return chart;
    }
    
    private JFreeChart createSimpleChart(List<TestResult> result, String title, String yLabel) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        
        XYSeries llSeries = new XYSeries("LinkedList");
        XYSeries adSeries = new XYSeries("ArrayDeque");
        
        for (TestResult r : result) {
            llSeries.add(r.size, r.linkedListTime / 1000.0);
            adSeries.add(r.size, r.arrayDequeTime / 1000.0);
        }
        
        dataset.addSeries(llSeries);
        dataset.addSeries(adSeries);
        
        JFreeChart chart = ChartFactory.createXYLineChart(title, "Data Size", yLabel, dataset,
            PlotOrientation.VERTICAL, true, true, false);
        
        // 设置颜色：LinkedList 红色，ArrayDeque 蓝色
        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, Color.RED);
        renderer.setSeriesPaint(1, Color.BLUE);
        
        return chart;
    }
    
    private JFreeChart createMemoryChartStandalone(List<MemoryResult> memory) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        
        XYSeries llSeries = new XYSeries("LinkedList");
        XYSeries adSeries = new XYSeries("ArrayDeque");
        
        for (MemoryResult r : memory) {
            if (r.linkedListMemory > 0) {
                llSeries.add(r.size, r.linkedListMemory / 1024.0);
                adSeries.add(r.size, r.arrayDequeMemory / 1024.0);
            }
        }
        
        dataset.addSeries(llSeries);
        dataset.addSeries(adSeries);
        
        JFreeChart chart = ChartFactory.createXYLineChart("Memory Usage", "Data Size", "Memory (KB)", dataset,
            PlotOrientation.VERTICAL, true, true, false);
        
        // 设置颜色：LinkedList 红色，ArrayDeque 蓝色
        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, Color.RED);
        renderer.setSeriesPaint(1, Color.BLUE);
        
        return chart;
    }
    
    private XYPlot createSubPlot(List<TestResult> result1, List<TestResult> result2, 
                                 String title, String yLabel) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        
        XYSeries llSeries1 = new XYSeries("LinkedList-" + result1.get(0).operation);
        XYSeries adSeries1 = new XYSeries("ArrayDeque-" + result1.get(0).operation);
        XYSeries llSeries2 = new XYSeries("LinkedList-" + result2.get(0).operation);
        XYSeries adSeries2 = new XYSeries("ArrayDeque-" + result2.get(0).operation);
        
        for (int i = 0; i < result1.size(); i++) {
            llSeries1.add(result1.get(i).size, result1.get(i).linkedListTime / 1000.0);
            adSeries1.add(result1.get(i).size, result1.get(i).arrayDequeTime / 1000.0);
            llSeries2.add(result2.get(i).size, result2.get(i).linkedListTime / 1000.0);
            adSeries2.add(result2.get(i).size, result2.get(i).arrayDequeTime / 1000.0);
        }
        
        dataset.addSeries(llSeries1);
        dataset.addSeries(adSeries1);
        dataset.addSeries(llSeries2);
        dataset.addSeries(adSeries2);
        
        NumberAxis xAxis = new NumberAxis("Data Size");
        NumberAxis yAxis = new NumberAxis(yLabel);
        
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, true);
        // LinkedList 系列 (0, 2) 统一用红色
        renderer.setSeriesPaint(0, Color.RED);
        renderer.setSeriesPaint(2, new Color(200, 0, 0)); // 深红
        // ArrayDeque 系列 (1, 3) 统一用蓝色
        renderer.setSeriesPaint(1, Color.BLUE);
        renderer.setSeriesPaint(3, new Color(0, 0, 200)); // 深蓝
        
        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
        plot.setDomainGridlinesVisible(true);
        plot.setRangeGridlinesVisible(true);
        
        return plot;
    }
    
    private XYPlot createSingleSubPlot(List<TestResult> result, String title, String yLabel) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        
        XYSeries llSeries = new XYSeries("LinkedList");
        XYSeries adSeries = new XYSeries("ArrayDeque");
        
        for (TestResult r : result) {
            llSeries.add(r.size, r.linkedListTime / 1000.0);
            adSeries.add(r.size, r.arrayDequeTime / 1000.0);
        }
        
        dataset.addSeries(llSeries);
        dataset.addSeries(adSeries);
        
        NumberAxis xAxis = new NumberAxis("Data Size");
        NumberAxis yAxis = new NumberAxis(yLabel);
        
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, true);
        // LinkedList 红色，ArrayDeque 蓝色
        renderer.setSeriesPaint(0, Color.RED);
        renderer.setSeriesPaint(1, Color.BLUE);
        
        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
        plot.setDomainGridlinesVisible(true);
        plot.setRangeGridlinesVisible(true);
        
        return plot;
    }
    
    private XYPlot createMemorySubPlot(List<MemoryResult> memory) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        
        XYSeries llSeries = new XYSeries("LinkedList");
        XYSeries adSeries = new XYSeries("ArrayDeque");
        
        for (MemoryResult r : memory) {
            if (r.linkedListMemory > 0) {
                llSeries.add(r.size, r.linkedListMemory / 1024.0);
                adSeries.add(r.size, r.arrayDequeMemory / 1024.0);
            }
        }
        
        dataset.addSeries(llSeries);
        dataset.addSeries(adSeries);
        
        NumberAxis xAxis = new NumberAxis("Data Size");
        NumberAxis yAxis = new NumberAxis("Memory (KB)");
        
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, true);
        // LinkedList 红色，ArrayDeque 蓝色
        renderer.setSeriesPaint(0, Color.RED);
        renderer.setSeriesPaint(1, Color.BLUE);
        
        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
        plot.setDomainGridlinesVisible(true);
        plot.setRangeGridlinesVisible(true);
        
        return plot;
    }

    // ==================== 结果类 ====================

    private static class TestResult {
        final String operation;
        final int size;
        final long linkedListTime;
        final long arrayDequeTime;
        
        TestResult(String operation, int size, long linkedListTime, long arrayDequeTime) {
            this.operation = operation;
            this.size = size;
            this.linkedListTime = linkedListTime;
            this.arrayDequeTime = arrayDequeTime;
        }
    }

    private static class MemoryResult {
        final int size;
        final long linkedListMemory;
        final long arrayDequeMemory;
        final double linkedListAvgBytes;
        final double arrayDequeAvgBytes;
        
        MemoryResult(int size, long linkedListMemory, long arrayDequeMemory, 
                     double linkedListAvgBytes, double arrayDequeAvgBytes) {
            this.size = size;
            this.linkedListMemory = linkedListMemory;
            this.arrayDequeMemory = arrayDequeMemory;
            this.linkedListAvgBytes = linkedListAvgBytes;
            this.arrayDequeAvgBytes = arrayDequeAvgBytes;
        }
    }
}
