package rxhttp.wrapper.param

import com.example.httpsender.param.PostEncryptFormParam

/**
 * Github
 * https://github.com/liujingxing/RxHttp
 * https://github.com/liujingxing/RxLife
 */
open class RxHttp_PostEncryptFormParam : RxHttp_FormParam {
    constructor(param: PostEncryptFormParam) : super(param)

    fun test(): RxHttp_PostEncryptFormParam {
        (param as PostEncryptFormParam).test()
        return this
    }

    fun test2(a: Long, b: Float): RxHttp_PostEncryptFormParam {
        (param as PostEncryptFormParam).test2(a, b)
        return this
    }

    fun add(a: Int, b: Int): Int = (param as PostEncryptFormParam).add(a, b)
}
