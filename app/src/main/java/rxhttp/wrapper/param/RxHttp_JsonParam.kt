package rxhttp.wrapper.param

import com.google.gson.JsonObject

/**
 * Github
 * https://github.com/liujingxing/RxHttp
 * https://github.com/liujingxing/RxLife
 */
open class RxHttp_JsonParam : RxHttp<JsonParam, RxHttp_JsonParam> {
    constructor(param: JsonParam) : super(param)

    fun add(key: String, value: Any?): RxHttp_JsonParam {
        param.add(key, value)
        return this
    }

    fun add(
        key: String,
        value: Any?,
        isAdd: Boolean
    ): RxHttp_JsonParam {
        if (isAdd) {
            param.add(key, value)
        }
        return this
    }

    fun addAll(map: Map<out String, *>): RxHttp_JsonParam {
        param.addAll(map)
        return this
    }

    /**
     * 将Json对象里面的key-value逐一取出，添加到另一个Json对象中，
     * 输入非Json对象将抛出 [IllegalStateException] 异常
     */
    fun addAll(jsonObject: String): RxHttp_JsonParam {
        param.addAll(jsonObject)
        return this
    }

    /**
     * 将Json对象里面的key-value逐一取出，添加到另一个Json对象中
     */
    fun addAll(jsonObject: JsonObject): RxHttp_JsonParam {
        param.addAll(jsonObject)
        return this
    }

    /**
     * 添加一个JsonElement对象(Json对象、json数组等)
     */
    fun addJsonElement(key: String, jsonElement: String): RxHttp_JsonParam {
        param.addJsonElement(key, jsonElement)
        return this
    }
}
