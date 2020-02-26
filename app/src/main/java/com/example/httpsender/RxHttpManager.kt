package com.example.httpsender

import android.app.Application
import io.reactivex.functions.Function
import okhttp3.OkHttpClient
import rxhttp.wrapper.annotation.Converter
import rxhttp.wrapper.callback.IConverter
import rxhttp.wrapper.converter.FastJsonConverter
import rxhttp.wrapper.converter.XmlConverter
import rxhttp.wrapper.cookie.CookieStore
import rxhttp.wrapper.param.Method
import rxhttp.wrapper.param.RxHttp
import rxhttp.wrapper.ssl.SSLSocketFactoryImpl
import rxhttp.wrapper.ssl.X509TrustManagerImpl
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * User: ljx
 * Date: 2020/2/26
 * Time: 15:22
 */
object RxHttpManager {

    @JvmField
    @Converter(name = "FastJsonConverter")
    val fastJsonConverter: IConverter = FastJsonConverter.create()

    @JvmField
    @Converter(name = "XmlConverter")
    val xmlConverter: IConverter = XmlConverter.create()

    @JvmStatic
    fun init(context: Application) {
        val file = File(context.externalCacheDir, "RxHttpCookie")
        val trustAllCert = X509TrustManagerImpl()
        val sslSocketFactory = SSLSocketFactoryImpl(trustAllCert)
        val client = OkHttpClient.Builder()
            .cookieJar(CookieStore(file))
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .sslSocketFactory(sslSocketFactory, trustAllCert) //添加信任证书
            .hostnameVerifier { _, _ -> true } //忽略host验证
            .build()
        RxHttp.init(client, BuildConfig.DEBUG)

        //设置缓存策略，非必须
//        val cacheFile = File(context.getExternalCacheDir(), "RxHttpCache")
//        RxHttpPlugins.setCache(cacheFile, 1000 * 100, CacheMode.REQUEST_NETWORK_FAILED_READ_CACHE);
//        RxHttpPlugins.setExcludeCacheKeys("time") //设置一些key，不参与cacheKey的组拼

        //设置数据解密/解码器，非必须
//        RxHttp.setResultDecoder(Function { it })

        //设置全局的转换器，非必须
//        RxHttp.setConverter(FastJsonConverter.create())

        //设置公共参数，非必须
        RxHttp.setOnParamAssembly(Function {
            /*
              根据不同请求添加不同参数，子线程执行，每次发送请求前都会被回调
              如果希望部分请求不回调这里，发请求前调用Param.setAssemblyEnabled(false)即可
             */
            val method: Method = it.getMethod()
            if (method.isGet) { //Get请求
            } else if (method.isPost) { //Post请求
            }
            it.add("versionName", "1.0.0") //添加公共参数
                .add("time", System.currentTimeMillis())
                .addHeader("deviceType", "android") //添加公共请求头

        })
    }
}