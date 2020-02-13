package com.example.httpsender

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.*

/**
 * User: ljx
 * Date: 2020-02-05
 * Time: 20:30
 */
open class AndroidScope() {


    constructor(
        lifecycleOwner: LifecycleOwner,
        lifeEvent: Lifecycle.Event = Lifecycle.Event.ON_DESTROY
    ) : this() {
        lifecycleOwner.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (lifeEvent == event) {
                    onCancel()
                }
            }
        })
    }

    private val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
        catch(throwable)
    }

    private val coroutineScope = CoroutineScope(
        Dispatchers.Main + exceptionHandler + SupervisorJob())

    protected open fun catch(e: Throwable) {
        onError?.invoke(e)
    }

    private var onError: ((Throwable) -> Unit)? = null

    private var job: Job? = null

    fun launch(
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        val job = coroutineScope.launch(start = start) {
            block()
        }
        this.job = job
        return job
    }

    fun onError(error: (Throwable) -> Unit): AndroidScope {
        onError = error
        return this
    }

    fun onCancel() {
        job?.cancel()

    }
}