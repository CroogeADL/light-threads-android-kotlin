package ua.com.crooge.light.threads

import android.os.Handler
import android.text.TextUtils
import android.os.Looper
import java.util.concurrent.BlockingQueue
import java.util.concurrent.CancellationException
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger


/**
 * This file provides methods for creating [ExecutorService] or [ScheduledExecutorService] and methods for running tasks in the background or foreground.
 */

private val DEVICE_NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors()

private val EXCEPTION_HANDLER = ExceptionHandler()

private val RANDOM_THREAD_EXECUTOR = newExecutor(4, "BackgroundExecutor")
private val SCHEDULED_EXECUTOR = newScheduledExecutor(4, "ScheduledExecutor")

/**
 * Run task in the background. If current thread isn't UI thread, task will be executed on this thread.
 *
 * @param immediately flag indicates that the task should be run immediately if current thread isn't UI thread
 * @param function        command to run
 * @return future for task
 */
fun runInBackground(immediately: Boolean = true, function: () -> Unit) =
    runInBackground(immediately, Runnable { function() })

/**
 * Run task in the background. If current thread isn't UI thread, task will be executed on this thread.
 *
 * @param immediately flag indicates that the task should be run immediately if current thread isn't UI thread
 * @param task        command to run
 * @return future for task
 */
fun runInBackground(immediately: Boolean = true, task: Runnable): Future<*>? {
    return if (immediately && !isUiThread()) {
        task.run()
        null
    } else {
        RANDOM_THREAD_EXECUTOR.submit(task)
    }
}

/**
 * Run task in the background after delay.
 *
 * @param delayMillis delay in milliseconds
 * @param function        command to run
 * @return future for task
 */
fun runInBackground(delayMillis: Long, function: () -> Unit) = runInBackground(delayMillis, Runnable { function() })

/**
 * Run task in the background after delay.
 *
 * @param delayMillis delay in milliseconds
 * @param task        command to run
 * @return future for task
 */
fun runInBackground(delayMillis: Long, task: Runnable): Future<*> =
    SCHEDULED_EXECUTOR.schedule(task, delayMillis, TimeUnit.MILLISECONDS)

/**
 * Schedule task to run in the background.
 *
 * @param delay delay before executing task
 * @param unit  time unit
 * @param function  command to run
 * @return scheduled future for task
 */
fun schedule(delay: Long, unit: TimeUnit, function: () -> Unit) = schedule(delay, unit, Runnable { function() })

/**
 * Schedule task to run in the background.
 *
 * @param delay delay before executing task
 * @param unit  time unit
 * @param task  command to run
 * @return scheduled future for task
 */
fun schedule(delay: Long, unit: TimeUnit, task: Runnable): ScheduledFuture<*> =
    SCHEDULED_EXECUTOR.schedule(task, delay, unit)

/**
 * Schedule task to run in the background periodically.
 *
 * @param delay  delay before executing task
 * @param period period between successive executions
 * @param unit   time unit
 * @param function   command to run
 * @return scheduled future for task
 */
fun schedule(delay: Long, period: Long, unit: TimeUnit, function: () -> Unit) =
    schedule(delay, period, unit, Runnable { function() })

/**
 * Schedule task to run in the background periodically.
 *
 * @param delay  delay before executing task
 * @param period period between successive executions
 * @param unit   time unit
 * @param task   command to run
 * @return scheduled future for task
 */
fun schedule(delay: Long, period: Long, unit: TimeUnit, task: Runnable): ScheduledFuture<*> =
    SCHEDULED_EXECUTOR.scheduleAtFixedRate(task, delay, period, unit)

private fun getMainHandler() = MainThreadHandlerHolder.INSTANCE

/**
 * Run task on UI thread. If current thread is UI thread, task will be executed immediately on this thread.
 *
 * @param function command to run
 */
fun runInForeground(function: () -> Unit) = runInForeground(Runnable { function() })

/**
 * Run task on UI thread. If current thread is UI thread, task will be executed immediately on this thread.
 *
 * @param task command to run
 */
fun runInForeground(task: Runnable) {
    if (isUiThread()) {
        task.run()
    } else {
        getMainHandler().post(task)
    }
}

/**
 * Run a task on UI thread after delay.
 *
 * @param delayMillis delay in milliseconds
 * @param function        command to run
 */
fun runInForeground(delayMillis: Long, function: () -> Unit) = runInForeground(delayMillis, Runnable { function() })

/**
 * Run a task on UI thread after delay.
 *
 * @param delayMillis delay in milliseconds
 * @param task        command to run
 */
fun runInForeground(delayMillis: Long, task: Runnable) {
    getMainHandler().postDelayed(task, delayMillis)
}

private object MainThreadHandlerHolder {
    internal val INSTANCE = Handler(Looper.getMainLooper())
}

/**
 * Check if current thread is UI thread.
 *
 * @return true if the thread is UI thread, otherwise - false
 */
fun isUiThread() = Thread.currentThread() == Looper.getMainLooper().thread

/**
 * Create new [ExecutorService].
 *
 * @param multiplier multiplier for number of cores. Core pool size calculated by this formula: <br></br>`corePoolSize = numberOfCores * multiplier`
 * @param threadName name used for generating threads
 * @return new instance of ExecutorService
 */
@Throws(IllegalArgumentException::class)
fun newExecutor(multiplier: Int, threadName: String): ExecutorService {
    if (TextUtils.isEmpty(threadName)) {
        throw IllegalArgumentException()
    }
    return DefaultExecutor(DEVICE_NUMBER_OF_CORES * multiplier, DefaultThreadFactory(threadName))
}

/**
 * Create new [ExecutorService].
 *
 * @param threadName      name used for generating threads
 * @param maximumPoolSize the maximum number of threads to allow in the pool
 * @return new instance of ExecutorService
 */
@Throws(IllegalArgumentException::class)
fun newCacheExecutor(threadName: String, maximumPoolSize: Int): ExecutorService {
    if (TextUtils.isEmpty(threadName)) {
        throw IllegalArgumentException()
    }
    return DefaultCachedExecutor(DefaultThreadFactory(threadName), maximumPoolSize)
}

/**
 * Create new [ScheduledExecutorService].
 *
 * @param multiplier multiplier for number of cores. Core pool size calculated by this formula: <br></br>`corePoolSize = numberOfCores * multiplier`
 * @param threadName name used for generating threads
 * @return new instance of ScheduledExecutorService
 */
@Throws(IllegalArgumentException::class)
fun newScheduledExecutor(multiplier: Int, threadName: String): ScheduledExecutorService {
    if (TextUtils.isEmpty(threadName)) {
        throw IllegalArgumentException()
    }
    return DefaultScheduledExecutor(DEVICE_NUMBER_OF_CORES * multiplier, DefaultThreadFactory(threadName))
}

private abstract class CustomExecutor internal constructor(
    corePoolSize: Int,
    maximumPoolSize: Int,
    keepAliveTime: Long,
    unit: TimeUnit,
    workQueue: BlockingQueue<Runnable>,
    threadFactory: DefaultThreadFactory
) : ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory)

private abstract class CustomScheduledExecutor internal constructor(
    corePoolSize: Int,
    threadFactory: DefaultThreadFactory
) : ScheduledThreadPoolExecutor(corePoolSize, threadFactory)

private class DefaultExecutor internal constructor(corePoolSize: Int, threadFactory: DefaultThreadFactory) :
    CustomExecutor(corePoolSize, corePoolSize, 0L, TimeUnit.SECONDS, LinkedBlockingQueue<Runnable>(), threadFactory) {

    override fun afterExecute(task: Runnable, t: Throwable?) {
        super.afterExecute(task, t)
        EXCEPTION_HANDLER.afterExecute(task, t)
    }
}

private class DefaultCachedExecutor internal constructor(threadFactory: DefaultThreadFactory, maximumPoolSize: Int) :
    CustomExecutor(
        maximumPoolSize,
        maximumPoolSize,
        60L,
        TimeUnit.SECONDS,
        SynchronousQueue<Runnable>(),
        threadFactory
    ) {

    override fun afterExecute(task: Runnable, t: Throwable?) {
        super.afterExecute(task, t)
        EXCEPTION_HANDLER.afterExecute(task, t)
    }
}

private class DefaultScheduledExecutor internal constructor(corePoolSize: Int, threadFactory: DefaultThreadFactory) :
    CustomScheduledExecutor(corePoolSize, threadFactory) {

    override fun afterExecute(task: Runnable, t: Throwable?) {
        super.afterExecute(task, t)
        EXCEPTION_HANDLER.afterExecute(task, t)
    }
}

private class DefaultThreadFactory internal constructor(poolName: String) : ThreadFactory {

    private val group: ThreadGroup
    private val threadNumber = AtomicInteger(1)
    private val namePrefix: String

    init {
        val s = System.getSecurityManager()
        group = if (s != null) s.threadGroup else Thread.currentThread().threadGroup
        namePrefix = poolName + "-" + poolNumber.getAndIncrement() + "-thread"
    }

    override fun newThread(task: Runnable): Thread {
        val t = Thread(group, task, namePrefix + threadNumber.getAndIncrement(), 0)
        if (t.isDaemon) {
            t.isDaemon = false
        }

        if (task is PriorityRunnable) {
            t.priority = task.getPriority()
        } else if (t.priority != Thread.NORM_PRIORITY) {
            t.priority = Thread.NORM_PRIORITY
        }
        t.uncaughtExceptionHandler = EXCEPTION_HANDLER
        return t
    }

    companion object {
        private val poolNumber = AtomicInteger(1)
    }
}

private class ExceptionHandler : Thread.UncaughtExceptionHandler {

    internal fun afterExecute(task: Runnable, ex: Throwable?) {
        var t = ex
        if (t == null && task is Future<*>) {
            try {
                if (task.isDone && !task.isCancelled) {
                    task.get()
                }
            } catch (ce: CancellationException) {
                t = ce
            } catch (ee: ExecutionException) {
                t = ee.cause
            } catch (ie: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }

        t?.let {
            uncaughtException(Thread.currentThread(), it)
        }
    }

    override fun uncaughtException(thread: Thread, ex: Throwable?) {
        ex?.printStackTrace()
    }
}