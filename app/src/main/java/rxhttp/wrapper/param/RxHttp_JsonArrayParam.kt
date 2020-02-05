package rxhttp.wrapper.param

import com.google.gson.JsonArray
import com.google.gson.JsonObject

/**
 * Github
 * https://github.com/liujingxing/RxHttp
 * https://github.com/liujingxing/RxLife
 */
open class RxHttp_JsonArrayParam : RxHttp<JsonArrayParam, RxHttp_JsonArrayParam> {
    constructor(param: JsonArrayParam) : super(param)

    fun add(any: Any): RxHttp_JsonArrayParam {
        param.add(any)
        return this
    }

    fun add(key: String, value: Any?): RxHttp_JsonArrayParam {
        param.add(key, value)
        return this
    }

    fun add(
        key: String,
        value: Any?,
        isAdd: Boolean
    ): RxHttp_JsonArrayParam {
        if (isAdd) {
            param.add(key, value)
        }
        return this
    }

    fun addAll(map: Map<out String, *>): RxHttp_JsonArrayParam {
        param.addAll(map)
        return this
    }

    fun addAll(list: List<Any>): RxHttp_JsonArrayParam {
        param.addAll(list)
        return this
    }

    /**
     * 添加多个对象，将字符串转JsonElement对象,并根据不同类型,执行不同操作,可输入任意非空字符串
     */
    fun addAll(jsonElement: String): RxHttp_JsonArrayParam {
        param.addAll(jsonElement)
        return this
    }

    fun addAll(jsonArray: JsonArray): RxHttp_JsonArrayParam {
        param.addAll(jsonArray)
        return this
    }

    /**
     * 将Json对象里面的key-value逐一取出，添加到Json数组中，成为单独的对象
     */
    fun addAll(jsonObject: JsonObject): RxHttp_JsonArrayParam {
        param.addAll(jsonObject)
        return this
    }

    fun addJsonElement(jsonElement: String): RxHttp_JsonArrayParam {
        param.addJsonElement(jsonElement)
        return this
    }

    /**
     * 添加一个JsonElement对象(Json对象、json数组等)
     */
    fun addJsonElement(key: String, jsonElement: String): RxHttp_JsonArrayParam {
        param.addJsonElement(key, jsonElement)
        return this
    }
}
