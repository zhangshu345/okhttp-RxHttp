package com.example.httpsender

import android.app.Application
import android.util.Log
import com.rxjava.rxlife.ScopeViewModel
import com.rxjava.rxlife.lifeOnMain
import io.reactivex.Observable
import java.util.concurrent.TimeUnit

/**
 * User: ljx
 * Date: 2019-05-31
 * Time: 21:50
 */
class MyViewModel(application: Application) : ScopeViewModel(application) {

    fun test() {
        Observable.interval(1, 1, TimeUnit.SECONDS)
            .lifeOnMain(this)
            .subscribe {
                Log.e("LJX", "MyViewModel aLong=$it")
            }
    }
}