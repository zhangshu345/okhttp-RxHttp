package com.example.httpsender

import android.util.Log
import androidx.lifecycle.LifecycleOwner
import com.rxjava.rxlife.BaseScope
import com.rxjava.rxlife.RxLife
import io.reactivex.Observable
import java.util.concurrent.TimeUnit

/**
 * User: ljx
 * Date: 2019-05-26
 * Time: 15:20
 */
class Presenter(owner: LifecycleOwner?) : BaseScope(owner) {
    fun test() {
        Observable.interval(1, 1, TimeUnit.SECONDS)
            .`as`(RxLife.`as`(this)) //这里的this 为Scope接口对象
            .subscribe { aLong: Long -> Log.e("LJX", "accept aLong=$aLong") }
    }
}