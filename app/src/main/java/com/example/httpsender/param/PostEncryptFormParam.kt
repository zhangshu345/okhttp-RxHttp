package com.example.httpsender.param

import rxhttp.wrapper.annotation.Param
import rxhttp.wrapper.param.FormParam
import rxhttp.wrapper.param.Method

/**
 * 此类中自己声明的所有public方法(构造方法除外)都会在RxHttp$PostEncryptFormParam类中一一生成，
 * 并一一对应调用。如: RxHttp$PostEncryptFormParam.test(int,int)方法内部会调用本类的test(int,int)方法
 * User: ljx
 * Date: 2019-09-11
 * Time: 11:52
 */
@Param(methodName = "postEncryptForm")
class PostEncryptFormParam(url: String) : FormParam(url, Method.POST) {
    fun test(): PostEncryptFormParam {
        return this
    }

    //此方法会在
    fun test2(a: Long, b: Float): PostEncryptFormParam {
        return this
    }

    fun add(a: Int, b: Int): Int {
        return a + b
    }
}