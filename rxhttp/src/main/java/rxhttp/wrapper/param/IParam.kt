@file:Suppress("UNCHECKED_CAST")

package rxhttp.wrapper.param

import okhttp3.CacheControl

/**
 * User: ljx
 * Date: 2019/1/19
 * Time: 10:25
 */
interface IParam<P : Param<P>> {
    fun setUrl(url: String): P
    fun add(key: String, value: Any?): P
    fun addAll(map: Map<out String, *>): P {
        for ((key, value1) in map) {
            value1 ?: continue
            add(key, value1)
        }
        return this as P
    }

    /**
     * @return 判断是否对参数添加装饰，即是否添加公共参数
     */
    fun isAssemblyEnabled(): Boolean

    /**
     * 设置是否对参数添加装饰，即是否添加公共参数
     *
     * @param enabled true 是
     * @return Param
     */
    fun setAssemblyEnabled(enabled: Boolean): P

    fun tag(tag: Any?): P {
        return tag(Any::class.java, tag)
    }

    fun <T> tag(type: Class<in T>, tag: T?): P

    fun cacheControl(cacheControl: CacheControl): P
}