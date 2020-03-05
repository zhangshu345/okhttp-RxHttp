package rxhttp.wrapper.param

import okhttp3.HttpUrl
import okhttp3.RequestBody
import rxhttp.wrapper.utils.CacheUtil
import rxhttp.wrapper.utils.GsonUtil
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * post、put、patch、delete请求，参数以{application/json; charset=utf-8}形式提交
 * User: ljx
 * Date: 2019-09-09
 * Time: 21:08
 */
class JsonArrayParam(url: String, method: Method) : AbstractParam<JsonArrayParam>(url, method), IJsonArray<JsonArrayParam> {
    private var mList: MutableList<Any>? = null


    override fun getRequestBody(): RequestBody {
        val jsonArray = mList ?: return RequestBody.create(null, ByteArray(0))
        return convert(jsonArray)
    }

    /**
     * JsonArray类型请求，所有add系列方法内部最终都会调用此方法
     *
     * @param any Object
     * @return JsonArrayParam
     */
    override fun add(any: Any): JsonArrayParam {
        val list = mList ?: ArrayList()
        if (list !== mList) mList = list
        list.add(any)
        return this
    }

    override fun add(key: String, value: Any?): JsonArrayParam {
        val map = HashMap<String, Any?>().apply { this[key] = value }
        return add(map)
    }

    val list: List<Any>?
        get() = mList

    override fun getCacheKey(): String? {
        val cacheKey = super.getCacheKey()
        if (cacheKey != null) return cacheKey
        val list = CacheUtil.excludeCacheKey(mList)
        val json = GsonUtil.toJson(list)
        val httpUrl = HttpUrl.get(getSimpleUrl())
        val builder = httpUrl.newBuilder().addQueryParameter("json", json)
        return builder.toString()
    }

    override fun toString(): String {
        return "JsonArrayParam{" +
            "url=" + getUrl() +
            "mList=" + mList +
            '}'
    }
}