package rxhttp.wrapper.param

import okhttp3.HttpUrl
import okhttp3.RequestBody
import rxhttp.wrapper.utils.CacheUtil
import rxhttp.wrapper.utils.GsonUtil
import java.util.*
import kotlin.collections.LinkedHashMap

/**
 * post、put、patch、delete请求，参数以{application/json; charset=utf-8}形式提交
 * User: ljx
 * Date: 2019-09-09
 * Time: 21:08
 */
open class JsonParam(url: String, method: Method) : AbstractParam<JsonParam>(url, method), IJsonObject<JsonParam> {
    private var mParam: MutableMap<String, Any?>? = null //请求参数

    override fun getRequestBody(): RequestBody {
        val params = mParam ?: return RequestBody.create(null, ByteArray(0))
        return convert(params)
    }

    override fun add(key: String, value: Any?): JsonParam {
        val param = mParam ?: LinkedHashMap()
        param[key] = value
        mParam = param
        return this
    }

    val params: Map<String, Any?>?
        get() = mParam

    override fun getCacheKey(): String? {
        val cacheKey = super.getCacheKey()
        if (cacheKey != null) return cacheKey
        val param = CacheUtil.excludeCacheKey(mParam)
        val json = GsonUtil.toJson(param)
        val httpUrl = HttpUrl.get(getSimpleUrl())
        val builder = httpUrl.newBuilder().addQueryParameter("json", json)
        return builder.toString()
    }

    override fun toString(): String {
        return "JsonParam{" +
            "url=" + getUrl() +
            "mParam=" + mParam +
            '}'
    }
}