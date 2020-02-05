package rxhttp.wrapper.param

import com.example.httpsender.param.PostEncryptJsonParam1

/**
 * Github
 * https://github.com/liujingxing/RxHttp
 * https://github.com/liujingxing/RxLife
 */
open class RxHttp_PostEncryptJsonParam1 : RxHttp<PostEncryptJsonParam1,
    RxHttp_PostEncryptJsonParam1> {
    constructor(param: PostEncryptJsonParam1) : super(param)

    fun test(): Unit = param.test()
}
