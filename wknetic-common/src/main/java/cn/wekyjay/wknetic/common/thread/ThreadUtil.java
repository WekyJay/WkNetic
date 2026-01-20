package cn.wekyjay.wknetic.common.thread;
import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.*;

/**
 * @Description: 基于 Java 21 虚拟线程特性
 * @Title: WkNetic 全局线程调度工具
 * @author WekyJay
 * @Github: <a href="https://github.com/WekyJay">https://github.com/WekyJay</a>
 * @Date: 2026/1/20 10:17
 */
@Slf4j
public class ThreadUtil {

    // 1. 虚拟线程执行器（不限数量，每个任务一个新线程）
    private static final ExecutorService VIRTUAL_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    // 2. 调度执行器（用于延迟任务或定时任务，虚拟线程不擅长定时，仍需平台线程）
    private static final ScheduledExecutorService SCHEDULED_EXECUTOR = Executors.newScheduledThreadPool(
            Runtime.getRuntime().availableProcessors(),
            Thread.ofPlatform().daemon().name("wk-scheduled-", 0).factory()
    );

    /**
     * 在虚拟线程中执行异步任务（适合处理 IO 密集型任务，如数据库、Redis 操作）
     */
    public static void runAsync(Runnable task) {
        VIRTUAL_EXECUTOR.execute(() -> {
            try {
                task.run();
            } catch (Exception e) {
                log.error("虚拟线程执行异常: ", e);
            }
        });
    }

    /**
     * 在虚拟线程中执行并返回结果
     */
    public static <T> Future<T> submitAsync(Callable<T> task) {
        return VIRTUAL_EXECUTOR.submit(task);
    }

    /**
     * 执行延迟任务
     */
    public static void schedule(Runnable task, long delay, TimeUnit unit) {
        SCHEDULED_EXECUTOR.schedule(() -> runAsync(task), delay, unit);
    }

    /**
     * 获取通用的虚拟线程工厂（可供 Netty 或其他框架使用）
     */
    public static ThreadFactory virtualThreadFactory(String namePrefix) {
        return Thread.ofVirtual().name(namePrefix, 0).factory();
    }

    /**
     * 优雅关闭执行器
     */
    public static void shutdown() {
        VIRTUAL_EXECUTOR.shutdown();
        SCHEDULED_EXECUTOR.shutdown();
    }
}