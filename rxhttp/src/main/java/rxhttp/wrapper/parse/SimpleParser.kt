package rxhttp.wrapper.parse

import okhttp3.Response

/**
 * User: ljx
 * Date: 2020-01-07
 * Time: 14:31
 */
open class SimpleParser<T> : AbstractParser<T> {

    protected constructor() : super()

    constructor(type: Class<T>) : super(type)

    override fun onParse(response: Response): T {
        return convert(response, mType)
    }
}