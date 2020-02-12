package rxhttp.wrapper.parse

import okhttp3.Response
import kotlin.reflect.KClass

/**
 * User: ljx
 * Date: 2020-01-07
 * Time: 14:31
 */
open class SimpleParser<T : Any> : AbstractParser<T> {

    protected constructor() : super()

    constructor(type: Class<T>) : super(type)
    constructor(type: KClass<T>) : super(type.java)

    override fun onParse(response: Response): T {
        return convert(response, mType)
    }
}