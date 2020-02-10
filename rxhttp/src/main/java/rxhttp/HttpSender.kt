package rxhttp

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.plugins.RxJavaPlugins
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import rxhttp.wrapper.callback.ProgressCallback
import rxhttp.wrapper.entity.Progress
import rxhttp.wrapper.param.IUploadLengthLimit
import rxhttp.wrapper.param.Param
import rxhttp.wrapper.parse.Parser
import rxhttp.wrapper.progress.ProgressInterceptor
import rxhttp.wrapper.ssl.SSLSocketFactoryImpl
import rxhttp.wrapper.ssl.X509TrustManagerImpl
import rxhttp.wrapper.utils.LogUtil
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

/**
 * 有公共参数的请求，用此类
 * User: ljx
 * Date: 2017/12/2
 * Time: 11:13
 */
object HttpSender {

    private var mOkHttpClient: OkHttpClient? = null  //只能初始化一次,第二次将抛出异常

    init {
        val errorHandler = RxJavaPlugins.getErrorHandler()
        if (errorHandler == null) {
            /*
            RxJava2的一个重要的设计理念是：不吃掉任何一个异常, 即抛出的异常无人处理，便会导致程序崩溃
            这就会导致一个问题，当RxJava2“downStream”取消订阅后，“upStream”仍有可能抛出异常，
            这时由于已经取消订阅，“downStream”无法处理异常，此时的异常无人处理，便会导致程序崩溃
            */
            RxJavaPlugins.setErrorHandler { LogUtil.log(it) }
        }
    }

    @JvmStatic
    fun init(okHttpClient: OkHttpClient, debug: Boolean = false) {
        setDebug(debug)
        require(mOkHttpClient == null) { "OkHttpClient can only be initialized once" }
        mOkHttpClient = okHttpClient
    }

    @JvmStatic
    fun getOkHttpClient(): OkHttpClient {
        val client = mOkHttpClient ?: getDefaultOkHttpClient()
        if (mOkHttpClient !== client) mOkHttpClient = client
        return client
    }

    @JvmStatic
    fun setDebug(debug: Boolean) {
        LogUtil.setDebug(debug)
    }

    /**
     * 同步发送一个请求
     *
     * 支持任意请求方式，如：Get、Head、Post、Put等
     *
     * @param param 请求参数
     * @return Response
     * @throws IOException 数据解析异常、网络异常等
     */
    @JvmStatic
    @Throws(IOException::class)
    fun execute(param: Param<*>): Response {
        return newCall(param).execute()
    }

    /**
     * 同步发送一个请求
     *
     * 支持任意请求方式，如：Get、Head、Post、Put等
     *
     * @param param  请求参数
     * @param parser 数据解析器
     * @param <T>    要转换的目标数据类型
     * @return T
     * @throws IOException 数据解析异常、网络异常等
     */
    @JvmStatic
    @Throws(IOException::class)
    fun <T> execute(param: Param<*>, parser: Parser<T>): T {
        return parser.onParse(execute(param))
    }

    /**
     * 同步发送一个请求
     *
     * 支持任意请求方式，如：Get、Head、Post、Put等
     *
     * @param param  请求参数
     * @param parser 数据解析器
     * @param <T>    要转换的目标数据类型
     * @return Observable
     */
    @JvmStatic
    fun <T> syncFrom(param: Param<*>, parser: Parser<T>): Observable<T> {
        return ObservableHttp(param, parser)
    }

    /**
     * 异步文件下载，带进度回调
     *
     * @param param      请求参数
     * @param destPath   目标路径
     * @param offsetSize 断点下载时,进度偏移量,仅断点下载时有效
     * @param scheduler  线程调度器
     * @return Observable
     */
    @JvmStatic
    fun downloadProgress(param: Param<*>, destPath: String, offsetSize: Long, scheduler: Scheduler?): Observable<Progress<String>> {
        val observableDownload = ObservableDownload(param, destPath, offsetSize)
        return if (scheduler != null) observableDownload.subscribeOn(scheduler) else observableDownload
    }

    /**
     * 异步发送一个请求,信息上传(支持文件上传,带进度回调)
     * 支持实现了[Param]接口的请求
     *
     * @param param     请求参数，
     * @param parser    数据解析器
     * @param scheduler 线程调度器
     * @param <T>       要转换的目标数据类型
     * @return Observable
    </T> */
    @JvmStatic
    fun <T> uploadProgress(param: Param<*>, parser: Parser<T>, scheduler: Scheduler?): Observable<Progress<T>> {
        val observableUpload = ObservableUpload(param, parser)
        return if (scheduler != null) observableUpload.subscribeOn(scheduler) else observableUpload
    }

    @JvmStatic
    @Throws(IOException::class)
    fun newCall(param: Param<*>): Call {
        return newCall(getOkHttpClient(), param)
    }

    //所有的请求，最终都会调此方法拿到Call对象，然后执行请求
    @JvmStatic
    @Throws(IOException::class)
    fun newCall(client: OkHttpClient, param: Param<*>): Call {
        val request = newRequest(param)
        return client.newCall(request)
    }

    @JvmStatic
    @Throws(IOException::class)
    fun newRequest(param: Param<*>): Request {
        val onParamAssembly = RxHttpPlugins.onParamAssembly(param)
        if (onParamAssembly is IUploadLengthLimit) {
            onParamAssembly.checkLength()
        }
        val request = onParamAssembly.buildRequest()
        LogUtil.log(request)
        return request
    }

    @JvmStatic
    @Throws(IOException::class)
    fun execute(request: Request): Call {
        return getOkHttpClient().newCall(request)
    }

    /**
     * 克隆一个OkHttpClient对象,用于监听下载进度
     *
     * @param progressCallback 进度回调
     * @return 克隆的OkHttpClient对象
     */
    @JvmStatic
    fun clone(progressCallback: ProgressCallback): OkHttpClient { //克隆一个OkHttpClient后,增加拦截器,拦截下载进度
        return getOkHttpClient().newBuilder()
            .addNetworkInterceptor(ProgressInterceptor(progressCallback))
            .build()
    }

    /**
     * 连接、读写超时均为10s、添加信任证书并忽略host验证
     *
     * @return 返回默认的OkHttpClient对象
     */
    private fun getDefaultOkHttpClient(): OkHttpClient {
        val trustAllCert: X509TrustManager = X509TrustManagerImpl()
        val sslSocketFactory: SSLSocketFactory = SSLSocketFactoryImpl(trustAllCert)
        return OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .sslSocketFactory(sslSocketFactory, trustAllCert) //添加信任证书
            .hostnameVerifier { _, _ -> true } //忽略host验证
            .build()
    }
}