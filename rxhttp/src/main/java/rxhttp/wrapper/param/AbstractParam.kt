package rxhttp.wrapper.param

import okhttp3.*
import rxhttp.RxHttpPlugins
import rxhttp.wrapper.cahce.CacheMode
import rxhttp.wrapper.cahce.CacheStrategy
import rxhttp.wrapper.callback.IConverter
import rxhttp.wrapper.utils.BuildUtil
import java.io.IOException

/**
 * 此类是唯一直接实现Param接口的类
 * User: ljx
 * Date: 2019/1/19
 * Time: 14:35
 */
@Suppress("UNCHECKED_CAST")
abstract class AbstractParam<P : Param<P>>(
    private var mUrl: String,    //链接地址
    private val mMethod: Method  //请求方法
) : Param<P> {

    private var mHBuilder: Headers.Builder? = null//请求头构造器
    private val requestBuilder = Request.Builder() //请求构造器
    private var mIsAssemblyEnabled = true //是否添加公共参数
    private val mCacheStrategy: CacheStrategy = RxHttpPlugins.getCacheStrategy()

    override fun setUrl(url: String): P {
        mUrl = url
        return this as P
    }

    override fun getHttpUrl(): HttpUrl {
        return HttpUrl.get(mUrl)
    }

    /**
     * @return 不带参数的url
     */
    override fun getSimpleUrl(): String {
        return mUrl
    }

    override fun getMethod(): Method {
        return mMethod
    }

    override fun getHeaders(): Headers? {
        return if (mHBuilder == null) null else mHBuilder!!.build()
    }

    override fun getHeadersBuilder(): Headers.Builder {
        if (mHBuilder == null) mHBuilder = Headers.Builder()
        return mHBuilder!!
    }

    override fun setHeadersBuilder(builder: Headers.Builder): P {
        mHBuilder = builder
        return this as P
    }

    override fun addHeader(key: String, value: String): P {
        getHeadersBuilder().add(key, value)
        return this as P
    }

    override fun addHeader(line: String): P {
        getHeadersBuilder().add(line)
        return this as P
    }

    override fun setHeader(key: String, value: String): P {
        getHeadersBuilder()[key] = value
        return this as P
    }

    override fun getHeader(key: String): String? {
        return getHeadersBuilder()[key]
    }

    override fun removeAllHeader(key: String): P {
        getHeadersBuilder().removeAll(key)
        return this as P
    }

    override fun cacheControl(cacheControl: CacheControl): P {
        requestBuilder.cacheControl(cacheControl)
        return this as P
    }

    override fun <T> tag(type: Class<in T>, tag: T?): P {
        requestBuilder.tag(type, tag)
        return this as P
    }

    override fun setAssemblyEnabled(enabled: Boolean): P {
        mIsAssemblyEnabled = enabled
        return this as P
    }

    override fun isAssemblyEnabled(): Boolean {
        return mIsAssemblyEnabled
    }

    override fun getCacheKey(): String? {
        return mCacheStrategy.cacheKey
    }

    override fun setCacheKey(cacheKey: String): P {
        mCacheStrategy.cacheKey = cacheKey
        return this as P
    }

    override fun getCacheValidTime(): Long {
        return mCacheStrategy.cacheValidTime
    }

    override fun setCacheValidTime(cacheTime: Long): P {
        mCacheStrategy.cacheValidTime = cacheTime
        return this as P
    }

    override fun getCacheMode(): CacheMode {
        return mCacheStrategy.cacheMode
    }

    override fun setCacheMode(cacheMode: CacheMode): P {
        mCacheStrategy.cacheMode = cacheMode
        return this as P
    }

    override fun buildRequest(): Request {
        return BuildUtil.buildRequest(this, requestBuilder)
    }

    protected fun getConverter(): IConverter? {
        val request = requestBuilder.build()
        return request.tag(IConverter::class.java)
    }

    protected fun convert(any: Any): RequestBody {
        val converter = getConverter() ?: throw NullPointerException("converter can not be null")
        return try {
            converter.convert(any)
        } catch (e: IOException) {
            throw IllegalArgumentException("Unable to convert $any to RequestBody", e)
        }
    }
}