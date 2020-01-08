package rxhttp.wrapper.param

import okhttp3.HttpUrl
import okhttp3.RequestBody
import rxhttp.wrapper.entity.KeyValuePair
import rxhttp.wrapper.utils.BuildUtil
import rxhttp.wrapper.utils.CacheUtil
import java.util.*

/**
 * Get、Head没有body的请求调用此类
 * User: ljx
 * Date: 2019-09-09
 * Time: 21:08
 */
open class NoBodyParam(url: String, method: Method) : AbstractParam<NoBodyParam>(url, method) {
    private var mKeyValuePairs: MutableList<KeyValuePair>? = null //键值对数组

    override fun getUrl(): String {
        return getHttpUrl().toString()
    }

    override fun getHttpUrl(): HttpUrl {
        return BuildUtil.getHttpUrl(getSimpleUrl(), mKeyValuePairs)
    }

    override fun add(key: String, value: Any?): NoBodyParam {
        return add(KeyValuePair(key, value ?: ""))
    }

    fun addEncoded(key: String, value: Any?): NoBodyParam {
        return add(KeyValuePair(key, value ?: "", true))
    }

    fun removeAllBody(key: String): NoBodyParam {
        val keyValuePairs = mKeyValuePairs ?: return this
        val iterator = keyValuePairs.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            if (next.equals(key)) iterator.remove()
        }
        return this
    }

    fun removeAllBody(): NoBodyParam {
        val keyValuePairs = mKeyValuePairs
        keyValuePairs?.clear()
        return this
    }

    operator fun set(key: String, value: Any?): NoBodyParam {
        removeAllBody(key)
        return add(key, value)
    }

    fun setEncoded(key: String, value: Any?): NoBodyParam {
        removeAllBody(key)
        return addEncoded(key, value)
    }

    fun queryValue(key: String): Any? {
        val keyValuePairs = mKeyValuePairs ?: return this
        for (pair in keyValuePairs) {
            if (pair.equals(key)) return pair.value
        }
        return null
    }

    fun queryValues(key: String): List<Any> {
        val keyValuePairs = mKeyValuePairs ?: return emptyList()
        val values: MutableList<Any> = ArrayList()
        for (pair in keyValuePairs) {
            if (pair.equals(key)) values.add(pair.value)
        }
        return Collections.unmodifiableList(values)
    }

    private fun add(keyValuePair: KeyValuePair): NoBodyParam {
        var keyValuePairs = mKeyValuePairs
        if (keyValuePairs == null) {
            mKeyValuePairs = ArrayList()
            keyValuePairs = mKeyValuePairs
        }
        keyValuePairs!!.add(keyValuePair)
        return this
    }

    override fun getRequestBody(): RequestBody? {
        return null
    }

    override fun getCacheKey(): String {
        val cacheKey = super.getCacheKey()
        if (cacheKey != null) return cacheKey
        val keyValuePairs = CacheUtil.excludeCacheKey(mKeyValuePairs)
        return BuildUtil.getHttpUrl(getSimpleUrl(), keyValuePairs).toString()
    }

    override fun toString(): String {
        return getUrl()
    }
}