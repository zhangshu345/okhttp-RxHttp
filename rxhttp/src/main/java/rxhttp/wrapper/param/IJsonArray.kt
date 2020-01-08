@file:Suppress("UNCHECKED_CAST")

package rxhttp.wrapper.param

import com.google.gson.JsonArray
import com.google.gson.JsonParser

/**
 * User: ljx
 * Date: 2019-11-10
 * Time: 11:54
 */
interface IJsonArray<P : Param<P>> : IJsonObject<P> {
    /**
     * 添加一个对象，JsonArray类型请求，所有add系列方法内部最终都会调用此方法
     *
     * @param any Object
     * @return P
     */
    fun add(any: Any): P

    /**
     * 添加多个对象
     *
     * @param list List
     * @return P
     */
    fun addAll(list: List<Any>): P { //TODO  List<*>
        for (any in list) {
            add(any)
        }
        return this as P
    }

    /**
     * 添加多个对象，将字符串转JsonElement对象,并根据不同类型,执行不同操作
     *
     * @param jsonElement 可输入任意非空字符串
     * @return P
     */
    override fun addAll(jsonElement: String): P {
        val parse = JsonParser().parse(jsonElement)
        if (parse.isJsonArray) {
            return addAll(parse.asJsonArray)
        } else if (parse.isJsonObject) {
            return addAll(parse.asJsonObject)
        }
        return add(parse)
    }

    /**
     * 添加多个对象
     *
     * @param jsonArray JsonArray
     * @return P
     */
    fun addAll(jsonArray: JsonArray): P {
        for (next in jsonArray) {
            add(next)
        }
        return this as P
    }

    /**
     * 添加一个JsonElement对象(Json对象、json数组等)
     *
     * @param jsonElement 可输入任意非空字符串
     * @return P
     */
    fun addJsonElement(jsonElement: String): P {
        return add(JsonParser().parse(jsonElement))
    }
}