package rxhttp.wrapper.param

import com.example.httpsender.RxHttpManager
import com.example.httpsender.entity.Url
import com.example.httpsender.param.GetEncryptParam
import com.example.httpsender.param.PostEncryptFormParam
import com.example.httpsender.param.PostEncryptJsonParam
import com.example.httpsender.param.PostEncryptJsonParam1
import com.example.httpsender.parser.ResponseListParser
import com.example.httpsender.parser.ResponsePageListParser
import com.example.httpsender.parser.ResponseParser
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.functions.Consumer
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import okhttp3.CacheControl
import okhttp3.Headers.Builder
import okhttp3.OkHttpClient
import okhttp3.Response
import rxhttp.HttpSender
import rxhttp.RxHttpPlugins
import rxhttp.wrapper.cahce.CacheMode
import rxhttp.wrapper.callback.IConverter
import rxhttp.wrapper.entity.Progress
import rxhttp.wrapper.parse.*
import java.io.IOException
import kotlin.Boolean
import kotlin.Long
import kotlin.String
import kotlin.reflect.KClass

/**
 * Github
 * https://github.com/liujingxing/RxHttp
 * https://github.com/liujingxing/RxLife
 */
@Suppress("UNCHECKED_CAST")
open class RxHttp<P : Param<P>, R : RxHttp<P, R>> {
    protected var param: P
        get() = field

    /**
     * The request is executed on the IO thread by default
     */
    protected var scheduler: Scheduler? = Schedulers.io()

    protected var localConverter: IConverter = RxHttpPlugins.getConverter()

    protected constructor(param: P) {
        this.param = param
    }

    fun setParam(param: P): R {
        this.param = param
        return this as R
    }

    fun setUrl(url: String): R {
        param.setUrl(url)
        return this as R
    }

    fun addHeader(line: String): R {
        param.addHeader(line)
        return this as R
    }

    fun addHeader(line: String, isAdd: Boolean): R {
        if (isAdd) {
            param.addHeader(line)
        }
        return this as R
    }

    fun addHeader(key: String, value: String): R {
        param.addHeader(key, value)
        return this as R
    }

    fun addHeader(
        key: String,
        value: String,
        isAdd: Boolean
    ): R {
        if (isAdd) {
            param.addHeader(key, value)
        }
        return this as R
    }

    fun setHeader(key: String, value: String): R {
        param.setHeader(key, value)
        return this as R
    }

    fun setRangeHeader(startIndex: Long): R {
        param.setRangeHeader(startIndex)
        return this as R
    }

    fun setRangeHeader(startIndex: Long, endIndex: Long): R {
        param.setRangeHeader(startIndex, endIndex)
        return this as R
    }

    fun removeAllHeader(key: String): R {
        param.removeAllHeader(key)
        return this as R
    }

    fun setHeadersBuilder(builder: Builder): R {
        param.setHeadersBuilder(builder)
        return this as R
    }

    /**
     * 设置单个接口是否需要添加公共参数,
     * 即是否回调通过 [setOnParamAssembly] 方法设置的接口,默认为true
     */
    fun setAssemblyEnabled(enabled: Boolean): R {
        param.setAssemblyEnabled(enabled)
        return this as R
    }

    /**
     * @deprecated please user [setDecoderEnabled] instead
     */
    @Deprecated(
        "please user [setDecoderEnabled] instead"
        , ReplaceWith("setDecoderEnabled(enabled)", "RxHttp.setDecoderEnabled"))
    fun setConverterEnabled(enabled: Boolean): R = setDecoderEnabled(enabled)

    /**
     * 设置单个接口是否需要对Http返回的数据进行解码/解密,
     * 即是否回调通过 [setResultDecoder] 方法设置的接口,默认为true
     */
    fun setDecoderEnabled(enabled: Boolean): R {
        param.addHeader(Param.DATA_DECRYPT, enabled.toString())
        return this as R
    }

    fun isAssemblyEnabled() = param.isAssemblyEnabled()

    fun getUrl() = param.getUrl()

    fun getSimpleUrl() = param.getSimpleUrl()

    fun getHeader(key: String) = param.getHeader(key)

    fun getHeaders() = param.getHeaders()

    fun getHeadersBuilder() = param.getHeadersBuilder()

    fun buildRequest() = param.buildRequest()

    fun tag(tag: Any?): R {
        param.tag(tag)
        return this as R
    }

    fun <T> tag(type: Class<in T>, tag: T?): R {
        param.tag(type, tag)
        return this as R
    }

    fun cacheControl(cacheControl: CacheControl): R {
        param.cacheControl(cacheControl)
        return this as R
    }

    fun setCacheKey(cacheKey: String): R {
        param.setCacheKey(cacheKey)
        return this as R
    }

    fun setCacheValidTime(cacheValidTime: Long): R {
        param.setCacheValidTime(cacheValidTime)
        return this as R
    }

    fun setCacheMode(cacheMode: CacheMode): R {
        param.setCacheMode(cacheMode)
        return this as R
    }

    @Throws(IOException::class)
    fun execute(): Response {
        setConverter(param)
        return HttpSender.execute(addDefaultDomainIfAbsent(param))
    }

    @Throws(IOException::class)
    fun <T> execute(parser: Parser<T>): T = parser.onParse(execute())


    fun subscribeOn(scheduler: Scheduler): R {
        this.scheduler = scheduler
        return this as R
    }

    /**
     * 设置在当前线程发请求
     */
    fun subscribeOnCurrent(): R {
        this.scheduler = null
        return this as R
    }

    fun subscribeOnIo(): R {
        this.scheduler = Schedulers.io()
        return this as R
    }

    fun subscribeOnComputation(): R {
        this.scheduler = Schedulers.computation()
        return this as R
    }

    fun subscribeOnNewThread(): R {
        this.scheduler = Schedulers.newThread()
        return this as R
    }

    fun subscribeOnSingle(): R {
        this.scheduler = Schedulers.single()
        return this as R
    }

    fun subscribeOnTrampoline(): R {
        this.scheduler = Schedulers.trampoline()
        return this as R
    }

    fun asBitmap() = asParser(BitmapParser())

    fun asString() = asObject(String::class.java)

    fun asBoolean() = asObject(Boolean::class.java)

    fun asByte() = asObject(Byte::class.java)

    fun asShort() = asObject(Short::class.java)

    fun asInteger() = asObject(Int::class.java)

    fun asLong() = asObject(Long::class.java)

    fun asFloat() = asObject(Float::class.java)

    fun asDouble() = asObject(Double::class.java)

    fun asObject() = asObject(Any::class.java)

    fun <T : Any> asObject(type: KClass<T>) = asObject((type.java))

    fun <T> asObject(type: Class<T>) = asParser(SimpleParser(type))

    fun asMap() = asObject(Map::class.java)

    fun <T> asMap(type: Class<T>) = asParser(MapParser(type, type))

    fun <T : Any> asMap(type: KClass<T>) = asParser(MapParser(type.java, type.java))

    fun <K, V> asMap(kType: Class<K>, vType: Class<V>) = asParser(MapParser(kType, vType))

    fun <K : Any, V : Any> asMap(kType: KClass<K>, vType: KClass<V>) =
        asParser(MapParser(kType.java, vType.java))

    fun asList() = asList(Any::class.java)

    fun <T : Any> asList(type: KClass<T>) = asList((type.java))

    fun <T> asList(type: Class<T>) = asParser(ListParser(type))

    /**
     * 调用此方法，订阅回调时，返回 [okhttp3.Headers] 对象
     */
    fun asHeaders() = asOkResponse().map(Response::headers)

    /**
     * 调用此方法，订阅回调时，返回 [okhttp3.Response] 对象
     */
    fun asOkResponse() = asParser(OkResponseParser())

    fun <T> asParser(parser: Parser<T>): Observable<T> {
        setConverter(param)
        var observable = HttpSender.syncFrom(addDefaultDomainIfAbsent(param), parser)
        if (scheduler != null) {
            observable = observable.subscribeOn(scheduler)
        }
        return observable
    }

    fun asResponse() = asResponse(Any::class.java)

    fun <T : Any> asResponse(tType: KClass<T>) = asResponse(tType.java)

    fun <T> asResponse(tType: Class<T>) = asParser(ResponseParser<T>(tType))

    fun asResponsePageList() = asResponsePageList(Any::class.java)

    fun <T : Any> asResponsePageList(tType: KClass<T>) = asResponsePageList(tType.java)

    fun <T> asResponsePageList(tType: Class<T>) = asParser(ResponsePageListParser<T>(tType))

    fun asResponseList() = asResponseList(Any::class.java)

    fun <T : Any> asResponseList(tType: KClass<T>) = asResponseList(tType.java)

    fun <T> asResponseList(tType: Class<T>) = asParser(ResponseListParser<T>(tType))

    fun asDownload(destPath: String) = asParser(DownloadParser(destPath))

    fun asDownload(destPath: String, progressConsumer: Consumer<Progress<String>>) =
        asDownload(destPath, 0, progressConsumer, null)

    fun asDownload(
        destPath: String,
        progressConsumer: Consumer<Progress<String>>,
        observeOnScheduler: Scheduler
    ) = asDownload(destPath, 0, progressConsumer, observeOnScheduler)

    fun asDownload(
        destPath: String,
        offsetSize: Long,
        progressConsumer: Consumer<Progress<String>>
    ) = asDownload(destPath, offsetSize, progressConsumer, null)

    fun asDownload(
        destPath: String,
        offsetSize: Long,
        progressConsumer: Consumer<Progress<String>>,
        observeOnScheduler: Scheduler?
    ): Observable<String> {
        setConverter(param)
        var observable = HttpSender
            .downloadProgress(addDefaultDomainIfAbsent(param), destPath, offsetSize, scheduler)
        if (observeOnScheduler != null) {
            observable = observable.observeOn(observeOnScheduler)
        }
        return observable.doOnNext(progressConsumer)
            .filter { it.isCompleted }
            .map { it.result }
    }

    fun setFastJsonConverter(): R {
        if (RxHttpManager.fastJsonConverter == null)
            throw IllegalArgumentException("converter can not be null");
        this.localConverter = RxHttpManager.fastJsonConverter
        return this as R
    }

    fun setXmlConverter(): R {
        if (RxHttpManager.xmlConverter == null)
            throw IllegalArgumentException("converter can not be null");
        this.localConverter = RxHttpManager.xmlConverter
        return this as R
    }

    /**
     * 给Param设置转换器，此方法会在请求发起前，被RxHttp内部调用
     */
    protected fun setConverter(param: P): R {
        param.tag(IConverter::class.java, localConverter)
        return this as R
    }

    /**
     * 给Param设置默认域名(如何缺席的话)，此方法会在请求发起前，被RxHttp内部调用
     */
    protected fun addDefaultDomainIfAbsent(param: P): P {
        val newUrl = addDomainIfAbsent(param.getSimpleUrl(), Url.baseUrl)
        param.setUrl(newUrl)
        return param
    }

    fun setDomainToUpdateIfAbsent(): R {
        val newUrl = addDomainIfAbsent(param.getSimpleUrl(), Url.update)
        param.setUrl(newUrl)
        return this as R
    }

    companion object {
        @JvmStatic
        fun setDebug(debug: Boolean) {
            HttpSender.setDebug(debug)
        }

        @JvmStatic
        fun init(okHttpClient: OkHttpClient) {
            HttpSender.init(okHttpClient)
        }

        @JvmStatic
        fun init(okHttpClient: OkHttpClient, debug: Boolean) {
            HttpSender.init(okHttpClient, debug)
        }

        /**
         * @deprecated please user [setResultDecoder] instead
         */
        @JvmStatic
        @Deprecated(
            "please user [setResultDecoder] instead",
            ReplaceWith("setResultDecoder(decoder)", "RxHttp.setResultDecoder"))
        fun setOnConverter(decoder: Function<String, String>) {
            setResultDecoder(decoder)
        }

        /**
         * 设置统一数据解码/解密器，每次请求成功后会回调该接口并传入Http请求的结果
         * 通过该接口，可以统一对数据解密，并将解密后的数据返回即可
         * 若部分接口不需要回调该接口，发请求前，调用 [setDecoderEnabled] 方法设置false即可
         */
        @JvmStatic
        fun setResultDecoder(decoder: Function<String, String>) {
            RxHttpPlugins.setResultDecoder(decoder)
        }

        /**
         * 设置全局转换器
         */
        @JvmStatic
        fun setConverter(globalConverter: IConverter) {
            RxHttpPlugins.setConverter(globalConverter)
        }

        /**
         * 设置统一公共参数回调接口,通过该接口,可添加公共参数/请求头，每次请求前会回调该接口
         * 若部分接口不需要添加公共参数,发请求前，调用 [setAssemblyEnabled]方法设置false即可
         */
        @JvmStatic
        fun setOnParamAssembly(onParamAssembly: Function<Param<*>, Param<*>>) {
            RxHttpPlugins.setOnParamAssembly(onParamAssembly)
        }

        @JvmStatic
        fun getOkHttpClient() = HttpSender.getOkHttpClient()

        @JvmStatic
        fun get(url: String, vararg formatArgs: Any) =
            with(Param.get(format(url, formatArgs)))

        @JvmStatic
        fun head(url: String, vararg formatArgs: Any) =
            with(Param.head(format(url, formatArgs)))

        @JvmStatic
        fun postForm(url: String, vararg formatArgs: Any) =
            with(Param.postForm(format(url, formatArgs)))

        @JvmStatic
        fun putForm(url: String, vararg formatArgs: Any) =
            with(Param.putForm(format(url, formatArgs)))

        @JvmStatic
        fun patchForm(url: String, vararg formatArgs: Any) =
            with(Param.patchForm(format(url, formatArgs)))

        @JvmStatic
        fun deleteForm(url: String, vararg formatArgs: Any) =
            with(Param.deleteForm(format(url, formatArgs)))

        @JvmStatic
        fun postJson(url: String, vararg formatArgs: Any) =
            with(Param.postJson(format(url, formatArgs)))

        @JvmStatic
        fun putJson(url: String, vararg formatArgs: Any) =
            with(Param.putJson(format(url, formatArgs)))

        @JvmStatic
        fun patchJson(url: String, vararg formatArgs: Any) =
            with(Param.patchJson(format(url, formatArgs)))

        @JvmStatic
        fun deleteJson(url: String, vararg formatArgs: Any) =
            with(Param.deleteJson(format(url, formatArgs)))

        @JvmStatic
        fun postJsonArray(url: String, vararg formatArgs: Any) =
            with(Param.postJsonArray(format(url, formatArgs)))

        @JvmStatic
        fun putJsonArray(url: String, vararg formatArgs: Any) =
            with(Param.putJsonArray(format(url, formatArgs)))

        @JvmStatic
        fun patchJsonArray(url: String, vararg formatArgs: Any) =
            with(Param.patchJsonArray(format(url, formatArgs)))

        @JvmStatic
        fun deleteJsonArray(url: String, vararg formatArgs: Any) =
            with(Param.deleteJsonArray(format(url, formatArgs)))

        @JvmStatic
        fun postEncryptJson(url: String, vararg formatArgs: Any) =
            RxHttp_PostEncryptJsonParam(PostEncryptJsonParam(format(url, formatArgs)))

        @JvmStatic
        fun postEncryptForm(url: String, vararg formatArgs: Any) =
            RxHttp_PostEncryptFormParam(PostEncryptFormParam(format(url, formatArgs)))

        @JvmStatic
        fun getEncrypt(url: String, vararg formatArgs: Any) =
            RxHttp_GetEncryptParam(GetEncryptParam(format(url, formatArgs)))

        @JvmStatic
        fun postEncryptJson1(url: String, vararg formatArgs: Any) =
            RxHttp_PostEncryptJsonParam1(PostEncryptJsonParam1(format(url, formatArgs)))

        @JvmStatic
        fun with(noBodyParam: NoBodyParam) = RxHttp_NoBodyParam(noBodyParam)

        @JvmStatic
        fun with(formParam: FormParam) = RxHttp_FormParam(formParam)

        @JvmStatic
        fun with(jsonParam: JsonParam) = RxHttp_JsonParam(jsonParam)

        @JvmStatic
        fun with(jsonArrayParam: JsonArrayParam) = RxHttp_JsonArrayParam(jsonArrayParam)

        private fun addDomainIfAbsent(url: String, domain: String): String {
            if (url.startsWith("http")) return url;
            var newUrl: String
            if (url.startsWith("/")) {
                if (domain.endsWith("/"))
                    newUrl = domain + url.substring(1);
                else
                    newUrl = domain + url;
            } else if (domain.endsWith("/")) {
                newUrl = domain + url;
            } else {
                newUrl = domain + "/" + url;
            }
            return newUrl;
        }

        private fun format(url: String, vararg formatArgs: Any) =
            if (formatArgs.size == 0) url else String.format(url, formatArgs)
    }
}
