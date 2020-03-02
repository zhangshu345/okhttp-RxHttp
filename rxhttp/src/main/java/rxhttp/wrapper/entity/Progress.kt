package rxhttp.wrapper.entity

/**
 * User: ljx
 * Date: 2019/1/20
 * Time: 18:15
 *
 * It is NOT thread safe.
 */
class Progress<T> {
    var progress = 0//当前进度 0-100
    var currentSize = 0L   //当前已完成的字节大小
    var totalSize = 0L   //总字节大小
    var result: T? = null  //http返回结果,上传/下载完成时调用

    constructor()
    constructor(progress: Int, currentSize: Long, totalSize: Long) {
        this.progress = progress
        this.currentSize = currentSize
        this.totalSize = totalSize
    }

    fun set(progress: Progress<*>) {
        this.progress = progress.progress
        currentSize = progress.currentSize
        totalSize = progress.totalSize
    }

    /**
     * 此方法的前身是isCompleted()
     * 在v1.4.3版本中，对上传/下载进度的回调有改动，不会存在进度为-1的情况；
     * 故无需在上传/下载回调中调用该方法，开发者可直接删除相关代码
     * @return 上传/下载是否完成
     */
    val isFinish: Boolean
        get() = progress == 100

    fun updateProgress() {
        progress = (currentSize * 100 / totalSize).toInt()
    }

    fun addTotalSize(addSize: Long) {
        totalSize += addSize
    }

    fun addCurrentSize(addSize: Long) {
        currentSize += addSize
    }

    override fun toString(): String {
        return "Progress{" +
            "progress=" + progress +
            ", currentSize=" + currentSize +
            ", totalSize=" + totalSize +
            ", mResult=" + result +
            '}'
    }
}