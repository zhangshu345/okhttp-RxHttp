package rxhttp.wrapper.param

/**
 * Github
 * https://github.com/liujingxing/RxHttp
 * https://github.com/liujingxing/RxLife
 */
open class RxHttp_NoBodyParam : RxHttp<NoBodyParam, RxHttp_NoBodyParam> {
    constructor(param: NoBodyParam) : super(param)

    fun add(key: String, value: Any?): RxHttp_NoBodyParam {
        param.add(key, value)
        return this
    }

    fun addEncoded(key: String, value: Any?): RxHttp_NoBodyParam {
        param.addEncoded(key, value)
        return this
    }

    fun add(
        key: String,
        value: Any?,
        isAdd: Boolean
    ): RxHttp_NoBodyParam {
        if (isAdd) {
            param.add(key, value)
        }
        return this
    }

    fun addAll(map: Map<out String, *>): RxHttp_NoBodyParam {
        param.addAll(map)
        return this
    }

    fun removeAllBody(): RxHttp_NoBodyParam {
        param.removeAllBody()
        return this
    }

    fun removeAllBody(key: String): RxHttp_NoBodyParam {
        param.removeAllBody(key)
        return this
    }

    fun set(key: String, value: Any?): RxHttp_NoBodyParam {
        param.set(key, value)
        return this
    }

    fun setEncoded(key: String, value: Any?): RxHttp_NoBodyParam {
        param.setEncoded(key, value)
        return this
    }

    fun queryValue(key: String): Any? = param.queryValue(key)

    fun queryValues(key: String): List<Any> = param.queryValues(key)
}
