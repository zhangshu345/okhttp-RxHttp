package rxhttp.wrapper.param

import rxhttp.wrapper.cahce.CacheMode

/**
 * User: ljx
 * Date: 2019-12-15
 * Time: 14:08
 */
interface ICache<P : Param<P>> {
    fun setCacheKey(cacheKey: String): P
    fun setCacheValidTime(cacheTime: Long): P
    fun setCacheMode(cacheMode: CacheMode): P
    fun getCacheKey(): String?
    fun getCacheValidTime(): Long
    fun getCacheMode(): CacheMode?
}