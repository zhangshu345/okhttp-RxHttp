package rxhttp.wrapper.param

import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.Request
import okhttp3.RequestBody

/**
 * 用于构建一个[Request]
 * User: ljx
 * Date: 2019/1/19
 * Time: 17:24
 */
interface IRequest {
    /**
     * @return 带参数的url 仅[NoBodyParam]请求会将参数以 ?key=value... 的形式拼接在url后面
     */
    fun getUrl(): String {
        return getSimpleUrl()
    }

    /**
     * @return 不带参数的url
     */
    fun getSimpleUrl(): String

    /**
     * @return HttpUrl
     */
    fun getHttpUrl(): HttpUrl

    /**
     * @return 请求方法，GET、POST等
     */
    fun getMethod(): Method

    /**
     * @return 请求体
     * GET、HEAD不能有请求体，
     * POST、PUT、PATCH、PROPPATCH、REPORT请求必须要有请求体
     * 其它请求可有可无
     */
    fun getRequestBody(): RequestBody?

    /**
     * @return 请求头信息
     */
    fun getHeaders(): Headers?

    /**
     * @return 根据以上定义的方法构建一个请求
     */
    fun buildRequest(): Request
}