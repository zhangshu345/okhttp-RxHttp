@file:Suppress("UNCHECKED_CAST")

package rxhttp.wrapper.param

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import okhttp3.MediaType

/**
 * User: ljx
 * Date: 2019-11-10
 * Time: 11:54
 */
interface IJsonObject<P : Param<P>> {
    fun add(key: String, value: Any?): P
    /**
     * 将Json对象里面的key-value逐一取出，添加到另一个Json对象中，
     * 输入非Json对象将抛出[IllegalStateException]异常
     *
     * @param jsonElement 字符串Json对象
     * @return P
     */
    fun addAll(jsonElement: String): P {
        return addAll(JsonParser().parse(jsonElement).asJsonObject)
    }

    /**
     * 将Json对象里面的key-value逐一取出，添加到另一个Json对象中
     *
     * @param jsonObject Json对象
     * @return P
     */
    fun addAll(jsonObject: JsonObject): P {
        for ((key, value) in jsonObject.entrySet()) {
            add(key, value)
        }
        return this as P
    }

    /**
     * 添加一个JsonElement对象(Json对象、json数组等)
     * @param key key
     * @param jsonElement 可输入任意非空字符串
     * @return P
     */
    fun addJsonElement(key: String, jsonElement: String): P {
        return add(key, JsonParser().parse(jsonElement))
    }

    companion object {
        @JvmStatic
        val MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8")
    }
}