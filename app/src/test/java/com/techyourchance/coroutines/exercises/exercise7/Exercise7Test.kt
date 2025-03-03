package com.techyourchance.coroutines.exercises.exercise7

import com.techyourchance.coroutines.common.TestUtils
import com.techyourchance.coroutines.common.TestUtils.printCoroutineScopeInfo
import com.techyourchance.coroutines.common.TestUtils.printJobsHierarchy
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import java.lang.Exception
import kotlin.coroutines.EmptyCoroutineContext

class Exercise7Test {

    /*
    Write nested withContext blocks, explore the resulting Job's hierarchy, test cancellation
    of the outer scope
     */
    @Test
    fun nestedWithContext() {
        runBlocking {
            val scopeJob = Job()
            val scope = CoroutineScope(scopeJob + CoroutineName("outer scope") + Dispatchers.IO)
            val job = scope.launch(CoroutineName("outer coroutine")) {
                printCoroutineScopeInfo()
                withContext(CoroutineName("outer withContext")) {
                    delay(20)
                    printCoroutineScopeInfo()
                    try {
                        withContext(CoroutineName("inner withContext")) {
                            try {
                                delay(50)
                                printCoroutineScopeInfo()
                                printJobsHierarchy(scopeJob)
                            } catch (e: CancellationException) {
                                println("inner withContext cancelled")
                            }
                        }
                    } catch (e: CancellationException) {
                        println("outer withContext cancelled")
                    }
                }
            }

            scope.launch(CoroutineName("outer coroutine sibling")) {
                delay(45)
                scopeJob.cancel()
            }

            job.join()
            println("test done")
        }
    }

    /*
    Launch new coroutine inside another coroutine, explore the resulting Job's hierarchy, test cancellation
    of the outer scope, explore structured concurrency
     */
    @Test
    fun nestedLaunchBuilders() {
        runBlocking {
            val scopeJob = Job()
            val scope = CoroutineScope(scopeJob + CoroutineName("outer scope") + Dispatchers.IO)
            val job = scope.launch(CoroutineName("outer coroutine")) {
                printCoroutineScopeInfo()
                withContext(CoroutineName("nested withContext")) {
                    try {
                        delay(50)
                        printCoroutineScopeInfo()

                        val innerJob = launch(CoroutineName("inner nested coroutine")) {
                            try {
                                delay(50)
                                printCoroutineScopeInfo()
                                printJobsHierarchy(scopeJob)
                            } catch (e: CancellationException) {
                                println("inner nested coroutine cancelled")
                            }
                        }
                        innerJob.join()
                        println("nested withContext done")
                    } catch (e: CancellationException) {
                        println("nested withContext cancelled")
                    }
                }
            }

            scope.launch(CoroutineName("outer coroutine sibling")) {
                delay(125)
                scopeJob.cancel()
            }
            job.join()
            println("test done")
        }
    }

    /*
    Launch new coroutine on "outer scope" inside another coroutine, explore the resulting Job's hierarchy,
    test cancellation of the outer scope, explore structured concurrency
     */
    @Test
    fun nestedCoroutineInOuterScope() {
        runBlocking {
            val scopeJob = Job()
            val scope = CoroutineScope(scopeJob + CoroutineName("outer scope") + Dispatchers.IO)
            val job = scope.launch(CoroutineName("outer coroutine")) {
                printCoroutineScopeInfo()
                withContext(CoroutineName("nested withContext")) {
                    try {
                        delay(50)
                        printCoroutineScopeInfo()

                        val innerJob = scope.launch(CoroutineName("nested outer scope")) {
                            try {
                                delay(50)
                                printCoroutineScopeInfo()
                                printJobsHierarchy(scopeJob)
                            } catch (e: CancellationException) {
                                println("nested outer scope cancelled")
                            }
                        }
                        innerJob.join()
                        println("nested withContext done")
                    } catch (e: CancellationException) {
                        println("nested withContext cancelled")
                    }
                }
            }

            scope.launch(CoroutineName("outer coroutine sibling")) {
                delay(150)
                scopeJob.cancel()
            }
            job.join()
            println("test done")
        }
    }
}