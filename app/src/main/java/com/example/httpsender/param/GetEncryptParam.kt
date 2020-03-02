package com.example.httpsender.param

import android.graphics.Point
import okhttp3.HttpUrl
import rxhttp.wrapper.annotation.Param
import rxhttp.wrapper.param.Method
import rxhttp.wrapper.param.NoBodyParam
import java.io.IOException

/**
 * 加密get请求
 * User: ljx
 * Date: 2019-09-12
 * Time: 17:25
 */
@Param(methodName = "getEncrypt")
class GetEncryptParam(url: String) : NoBodyParam(url, Method.GET) {
    @Throws(IOException::class, IllegalArgumentException::class)
    fun <T : Point, R : CharSequence> test(a: List<R>, map: Map<T, R>, vararg b: Array<T>): GetEncryptParam {
        return this
    }

    override fun getHttpUrl(): HttpUrl {
        val paramsBuilder = StringBuilder() //存储加密后的参数
        val keyValuePairs = keyValuePairs
        if (keyValuePairs != null) {
            for ((key, value1) in keyValuePairs) { //这里遍历所有添加的参数，可对参数进行加密操作
                val value = value1.toString()
                //加密逻辑自己写
            }
        }
        val simpleUrl = getSimpleUrl() //拿到请求Url
        return if (paramsBuilder.isEmpty()) HttpUrl.get(simpleUrl)
        else HttpUrl.get("$simpleUrl?$paramsBuilder") //将加密后的参数和url组拼成HttpUrl对象并返回

    }
}