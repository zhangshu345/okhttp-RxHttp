package rxhttp.wrapper.param

import com.example.httpsender.param.GetEncryptParam

/**
 * Github
 * https://github.com/liujingxing/RxHttp
 * https://github.com/liujingxing/RxLife
 */
open class RxHttp_GetEncryptParam : RxHttp_NoBodyParam {
    constructor(param: GetEncryptParam) : super(param)

    fun test(): Unit = (param as GetEncryptParam).test()
}
