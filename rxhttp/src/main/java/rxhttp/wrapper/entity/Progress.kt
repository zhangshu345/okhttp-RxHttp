package rxhttp.wrapper.entity

/**
 * User: ljx
 * Date: 2019/1/20
 * Time: 18:15
 *
 * It is NOT thread safe.
 */
data class Progress<T>(var progress: Int, //当前进度 0-100
                       var currentSize: Long, //当前已完成的字节大小
                       var totalSize: Long) { //总字节大小

    var result: T? = null //http返回结果,上传/下载完成时调用
        private set

    /**
     * 上传/下载完成时调用,并将进度设置为-1
     *
     * @param result http执行结果
     */
    constructor(result: T) : this(-1, -1, -1) {
        this.result = result
    }

    /**
     * @return 上传/下载是否完成
     */
    val isCompleted: Boolean
        get() = progress == -1

    fun updateProgress() {
        progress = (currentSize * 100 / totalSize).toInt()
    }

    fun addTotalSize(addSize: Long) {
        totalSize += addSize
    }

    fun addCurrentSize(addSize: Long) {
        currentSize += addSize
    }
}