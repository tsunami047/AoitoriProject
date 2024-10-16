package io.aoitori043.aoitoriproject.utils.lock;

import io.aoitori043.aoitoriproject.database.orm.impl.CacheImplUtil;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@Ignore
@RunWith(MockitoJUnitRunner.class)
public class SemaphoreLockTest {

    @InjectMocks
    private SemaphoreLock semaphoreLock;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    private static final int THREAD_COUNT = 1000; // 模拟的线程数量
    private static final String RESOURCE_ID = "testResource";
    private static volatile boolean isLocked = false;

    @Test
    public void testLockUnderHighPressure() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);  // 用于同步所有线程执行
//        CacheImplUtil.Lock lock = new CacheImplUtil.Lock();
        AtomicInteger successCounter = new AtomicInteger(0);  // 统计成功运行的次数
        AtomicLong total = new AtomicLong();
//        for (int i = 0; i < THREAD_COUNT; i++) {
//            int finalI = i;
//            executor.execute(() -> {
//                try {
//                    long start = System.nanoTime();
//                    SemaphoreLock.lock(RESOURCE_ID, 50000, ()->{
//                        System.out.println(finalI);
//                        try {
//                            // 模拟长时间运行的任务，保持线程在临界区一段时间
//                            Thread.sleep(0,10);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    });  // 超时时间设为500ms
//                    long end = System.nanoTime();
//                    successCounter.incrementAndGet();  // 成功运行一次加1
//                } catch (Exception e) {
//                    fail("Lock failed: " + e.getMessage());
//                } finally {
//                    latch.countDown();  // 完成一个线程后，减少倒计数
//                }
//            });
//        }
        for (int i = 0; i < THREAD_COUNT; i++) {
            int finalI = i;
            executor.execute(() -> {
                try {
                    long start = System.nanoTime();
                    SemaphoreLock.lock(RESOURCE_ID, 50000, ()->{
                        System.out.println(finalI);
                        try {
                            // 模拟长时间运行的任务，保持线程在临界区一段时间
                            Thread.sleep(0,10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    });  // 超时时间设为500ms
                    long end = System.nanoTime();
                    total.addAndGet(end - start);
                    successCounter.incrementAndGet();  // 成功运行一次加1
                } catch (Exception e) {
                    fail("Lock failed: " + e.getMessage());
                } finally {
                    latch.countDown();  // 完成一个线程后，减少倒计数
                }
            });
        }
        latch.await();  // 等待所有线程执行完
        executor.shutdown();
        System.out.println("sb "+total.get());
        System.out.println(successCounter.get());
        System.out.println(THREAD_COUNT);
        // 确保所有线程都成功运行
//        assertEquals(THREAD_COUNT, successCounter.get(), "Not all threads completed successfully");
    }

    @Test
    public void testSemaphoreLock_AcquireAndRelease() throws InterruptedException {
        String resourceId = "resourceId";
        int timeout = 1000;

        Semaphore semaphore = new Semaphore(1);
        semaphore.acquire();

        SemaphoreLock.semaphoreMap.put(resourceId, semaphore);

        CacheImplUtil.Lock lock = mock(CacheImplUtil.Lock.class);
        doNothing().when(lock).run();

        SemaphoreLock.lock(resourceId, timeout, lock);

        verify(lock).run();
        assertTrue(semaphore.availablePermits() == 1);
    }

    @Test
    public void testSemaphoreLock_ExceptionHandling() {
        String resourceId = "resourceId";
        int timeout = 1000;

        CacheImplUtil.Lock lock = mock(CacheImplUtil.Lock.class);
        doThrow(new RuntimeException("Test Exception")).when(lock).run();

        SemaphoreLock.lock(resourceId, timeout, lock);

        verify(lock).run();
    }

    @Test
    public void testSemaphoreLock_AcquireWriteLock() {
        String resourceId = "resourceId";
        int timeout = 1000;

        Semaphore semaphore = new Semaphore(1);
        SemaphoreLock.semaphoreMap.put(resourceId, semaphore);

        boolean result = SemaphoreLock.acquireWriteLock(resourceId, timeout);

        assertTrue(result);
        assertEquals(0, semaphore.availablePermits());
    }

    @Test
    public void testSemaphoreLock_AcquireWriteLock_Exception() throws InterruptedException {
        String resourceId = "resourceId";
        int timeout = 1000;

        Semaphore semaphore = mock(Semaphore.class);
        when(semaphore.tryAcquire(timeout, TimeUnit.MILLISECONDS)).thenThrow(new InterruptedException("Test Exception"));
        SemaphoreLock.semaphoreMap.put(resourceId, semaphore);

        boolean result = SemaphoreLock.acquireWriteLock(resourceId, timeout);

        assertFalse(result);
        verify(semaphore).tryAcquire(timeout, TimeUnit.MILLISECONDS);
    }

    @Test
    public void testSemaphoreLock_ReleaseWriteLock() {
        String resourceId = "resourceId";
        Semaphore semaphore = new Semaphore(0);
        SemaphoreLock.semaphoreMap.put(resourceId, semaphore);

        SemaphoreLock.releaseWriteLock(resourceId);

        assertEquals(1, semaphore.availablePermits());
    }

    @Test
    public void testSemaphoreLock_Submit() {
        String resourceId = "resourceId";
        int timeout = 1000;

        Semaphore semaphore = new Semaphore(1);
        SemaphoreLock.semaphoreMap.put(resourceId, semaphore);

        CacheImplUtil.SubmitLock<String> lock = mock(CacheImplUtil.SubmitLock.class);
        when(lock.run()).thenReturn("Test Result");

        String result = SemaphoreLock.submit(resourceId, timeout, lock);

        assertEquals("Test Result", result);
        assertEquals(1, semaphore.availablePermits());
    }

    @Test
    public void testSemaphoreLock_Submit_Exception() {
        String resourceId = "resourceId";
        int timeout = 1000;

        Semaphore semaphore = new Semaphore(1);
        SemaphoreLock.semaphoreMap.put(resourceId, semaphore);

        CacheImplUtil.SubmitLock<String> lock = mock(CacheImplUtil.SubmitLock.class);
        when(lock.run()).thenThrow(new RuntimeException("Test Exception"));

        String result = SemaphoreLock.submit(resourceId, timeout, lock);

        assertNull(result);
        assertEquals(1, semaphore.availablePermits());
    }
}
