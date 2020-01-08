package rxhttp.wrapper.param

import java.io.IOException

/**
 * 文件上传长度限制接口
 * User: ljx
 * Date: 2019-05-19
 * Time: 18:18
 */
interface IUploadLengthLimit {
    //检查长度逻辑自行实现
    @Throws(IOException::class)
    fun checkLength()
}