package rxhttp

import okhttp3.Call
import okhttp3.OkHttpClient

/**
 * User: ljx
 * Date: 2020/3/3
 * Time: 22:04
 */
interface IRxHttp {
    //断点下载进度偏移量，进在带进度断点下载时生效
    val breakDownloadOffSize: Long

    fun newCall(): Call
    fun newCall(okHttp: OkHttpClient): Call
}