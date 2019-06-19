package ua.com.crooge.light.threads

import android.os.Process

/**
 * Runnable that sets priority of thread before execution
 */
abstract class PriorityRunnable() : Runnable {

    private var threadPriority = Process.THREAD_PRIORITY_BACKGROUND

    constructor(threadPriority: Int) : this() {
        this.threadPriority = threadPriority
    }

    override fun run() {
        Process.setThreadPriority(threadPriority)
        runImpl()
    }

    protected abstract fun runImpl()

    fun getPriority() = threadPriority
}