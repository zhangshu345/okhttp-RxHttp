package rxhttp.wrapper.param

import okhttp3.Headers

/**
 * User: ljx
 * Date: 2019/1/22
 * Time: 13:58
 */
interface IHeaders<P : Param<P>> {
    fun getHeaders(): Headers?
    fun getHeader(key: String): String?
    fun getHeadersBuilder(): Headers.Builder
    fun setHeadersBuilder(builder: Headers.Builder): P
    fun addHeader(key: String, value: String): P
    fun addHeader(line: String): P
    fun setHeader(key: String, value: String): P
    fun removeAllHeader(key: String): P

    /**
     * 设置断点下载范围
     * 注：
     * 1、开始位置小于0，及代表下载完整文件
     * 2、结束位置要大于开始位置，否则结束位置默认为文件末尾
     *
     * @param startIndex 开始位置
     * @param endIndex   结束位置
     * @return Param
     */
    fun setRangeHeader(startIndex: Long, endIndex: Long = -1L): P {
        val toIndex = if (endIndex > startIndex) endIndex else -1
        var headerValue = "bytes=$startIndex-"
        if (toIndex >= 0) {
            headerValue += toIndex
        }
        return addHeader("RANGE", headerValue)
    }
}