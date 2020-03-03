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
                    lifecycleOwner.lifecycle.removeObserver(this)
                }
            }
        })
    }

    //协程异常回调
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        catch(throwable)
    }

    private val coroutineScope = CoroutineScope(
        Dispatchers.Main + exceptionHandler + SupervisorJob())

    private var onError: ((Throwable) -> Unit)? = null

    private var job: Job? = null

    fun launch(
        block: suspend CoroutineScope.() -> Unit,
        error: (Throwable) -> Unit
    ): Job {
        onError = error
        val job = coroutineScope.launch { block() }
        this.job = job
        return job
    }

    private fun onCancel() {
        job?.cancel()
    }

    private fun catch(e: Throwable) {
        onError?.invoke(e)
    }
}