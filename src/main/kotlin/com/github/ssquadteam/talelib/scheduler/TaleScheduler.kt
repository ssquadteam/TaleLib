package com.github.ssquadteam.talelib.scheduler

import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.server.core.plugin.JavaPlugin
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class TaleScheduler(private val plugin: JavaPlugin) {

    companion object {
        private val LOGGER: HytaleLogger = HytaleLogger.forEnclosingClass()
    }

    private val asyncExecutor = Executors.newCachedThreadPool { runnable ->
        Thread(runnable, "TaleLib-Async-${plugin.name}").apply { isDaemon = true }
    }

    private val scope = CoroutineScope(SupervisorJob() + asyncExecutor.asCoroutineDispatcher())
    private val ioDispatcher = Dispatchers.IO
    private val runningTasks = ConcurrentHashMap<Long, Job>()
    private val taskIdCounter = AtomicLong(0)

    fun async(block: suspend AsyncContext.() -> Unit): Long {
        val taskId = taskIdCounter.incrementAndGet()
        val job = scope.launch {
            try {
                AsyncContext(this@TaleScheduler).block()
            } catch (e: CancellationException) {
            } catch (e: Exception) {
                LOGGER.atSevere().withCause(e).log("Error in async task $taskId")
            } finally {
                runningTasks.remove(taskId)
            }
        }
        runningTasks[taskId] = job
        return taskId
    }

    fun io(block: suspend () -> Unit): Long {
        val taskId = taskIdCounter.incrementAndGet()
        val job = scope.launch(ioDispatcher) {
            try {
                block()
            } catch (e: CancellationException) {
            } catch (e: Exception) {
                LOGGER.atSevere().withCause(e).log("Error in IO task $taskId")
            } finally {
                runningTasks.remove(taskId)
            }
        }
        runningTasks[taskId] = job
        return taskId
    }

    fun delay(duration: Duration, block: suspend () -> Unit): Long {
        val taskId = taskIdCounter.incrementAndGet()
        val job = scope.launch {
            try {
                delay(duration.inWholeMilliseconds)
                block()
            } catch (e: CancellationException) {
            } catch (e: Exception) {
                LOGGER.atSevere().withCause(e).log("Error in delayed task $taskId")
            } finally {
                runningTasks.remove(taskId)
            }
        }
        runningTasks[taskId] = job
        return taskId
    }

    fun delayTicks(ticks: Long, block: suspend () -> Unit): Long =
        delay((ticks * 50).milliseconds, block)

    fun repeat(
        interval: Duration,
        initialDelay: Duration = Duration.ZERO,
        times: Int = Int.MAX_VALUE,
        block: suspend (iteration: Int) -> Unit
    ): Long {
        val taskId = taskIdCounter.incrementAndGet()
        val job = scope.launch {
            try {
                if (initialDelay > Duration.ZERO) {
                    delay(initialDelay.inWholeMilliseconds)
                }
                var iteration = 0
                while (iteration < times && isActive) {
                    block(iteration)
                    iteration++
                    if (iteration < times) {
                        delay(interval.inWholeMilliseconds)
                    }
                }
            } catch (e: CancellationException) {
            } catch (e: Exception) {
                LOGGER.atSevere().withCause(e).log("Error in repeating task $taskId")
            } finally {
                runningTasks.remove(taskId)
            }
        }
        runningTasks[taskId] = job
        return taskId
    }

    fun repeatTicks(
        intervalTicks: Long,
        initialDelayTicks: Long = 0,
        times: Int = Int.MAX_VALUE,
        block: suspend (iteration: Int) -> Unit
    ): Long = repeat(
        interval = (intervalTicks * 50).milliseconds,
        initialDelay = (initialDelayTicks * 50).milliseconds,
        times = times,
        block = block
    )

    fun cancel(taskId: Long): Boolean {
        val job = runningTasks.remove(taskId) ?: return false
        job.cancel()
        return true
    }

    fun cancelAll() {
        runningTasks.forEach { (_, job) -> job.cancel() }
        runningTasks.clear()
    }

    fun isRunning(taskId: Long): Boolean = runningTasks[taskId]?.isActive == true
    fun runningTaskCount(): Int = runningTasks.count { it.value.isActive }

    fun shutdown() {
        cancelAll()
        scope.cancel()
        asyncExecutor.shutdown()
        LOGGER.atInfo().log("TaleScheduler shutdown for plugin: ${plugin.name}")
    }
}

class AsyncContext(private val scheduler: TaleScheduler) {
    suspend fun sync(block: () -> Unit) {
        withContext(Dispatchers.Default) { block() }
    }

    suspend fun delay(duration: Duration) {
        kotlinx.coroutines.delay(duration.inWholeMilliseconds)
    }
}

val Int.ticks: Duration get() = (this * 50).milliseconds
val Long.ticks: Duration get() = (this * 50).milliseconds
