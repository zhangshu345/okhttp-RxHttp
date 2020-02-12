package rxhttp.wrapper.param

import okhttp3.RequestBody
import rxhttp.wrapper.callback.ProgressCallback
import rxhttp.wrapper.entity.KeyValuePair
import rxhttp.wrapper.entity.UpFile
import rxhttp.wrapper.progress.ProgressRequestBody
import rxhttp.wrapper.utils.BuildUtil
import rxhttp.wrapper.utils.CacheUtil
import java.io.IOException
import java.util.*

/**
 * post、put、patch、delete请求
 * 参数以{application/x-www-form-urlencoded}形式提交
 * 当带有文件时，自动以{multipart/form-data}形式提交
 * 当调用[.setMultiForm]方法，强制以{multipart/form-data}形式提交
 *
 *
 * User: ljx
 * Date: 2019-09-09
 * Time: 21:08
 */
open class FormParam(url: String, method: Method) :
    AbstractParam<FormParam>(url, method), IUploadLengthLimit, IFile<FormParam> {

    private var callback: ProgressCallback? = null //上传进度回调
    private var mFileList: MutableList<UpFile>? = null  //附件集合
    private var mKeyValuePairs: MutableList<KeyValuePair>? = null  //请求参数
    private var uploadMaxLength = Int.MAX_VALUE.toLong() //文件上传最大长度
    private var isMultiForm = false

    override fun add(key: String, value: Any?): FormParam {
        return add(KeyValuePair(key, value ?: ""))
    }

    fun addEncoded(key: String, value: Any?): FormParam {
        return add(KeyValuePair(key, value ?: "", true))
    }

    fun removeAllBody(key: String): FormParam {
        val keyValuePairs = mKeyValuePairs ?: return this
        val iterator = keyValuePairs.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            if (next.equals(key)) iterator.remove()
        }
        return this
    }

    fun removeAllBody(): FormParam {
        mKeyValuePairs?.clear()
        return this
    }

    operator fun set(key: String, value: Any?): FormParam {
        removeAllBody(key)
        return add(key, value)
    }

    fun setEncoded(key: String, value: Any?): FormParam {
        removeAllBody(key)
        return addEncoded(key, value)
    }

    fun queryValue(key: String): Any? {
        val keyValuePairs = mKeyValuePairs ?: return null
        for (pair in keyValuePairs) {
            if (pair.equals(key)) return pair.value
        }
        return null
    }

    fun queryValues(key: String): List<Any> {
        val keyValuePairs = mKeyValuePairs ?: return emptyList()
        val values: MutableList<Any> = ArrayList()
        for (pair in keyValuePairs) {
            if (pair.equals(key)) values.add(pair.value)
        }
        return Collections.unmodifiableList(values)
    }

    private fun add(keyValuePair: KeyValuePair): FormParam {
        val keyValuePairs = mKeyValuePairs ?: ArrayList()
        if (keyValuePairs !== mKeyValuePairs) {
            mKeyValuePairs = keyValuePairs
        }
        keyValuePairs.add(keyValuePair)
        return this
    }

    override fun addFile(upFile: UpFile): FormParam {
        val fileList = mFileList ?: ArrayList()
        if (fileList !== mFileList) {
            mFileList = fileList
        }
        fileList.add(upFile)
        return this
    }

    override fun removeFile(key: String): FormParam {
        val fileList = mFileList ?: return this
        val it = fileList.iterator()
        while (it.hasNext()) {
            val upFile = it.next()
            if (key == upFile.key) {
                it.remove()
            }
        }
        return this
    }

    private fun hasFile(): Boolean {
        return mFileList?.isNotEmpty() ?: false
    }

    private val totalFileLength: Long
        get() {
            if (mFileList == null) return 0
            var totalLength: Long = 0
            for (upFile in mFileList!!) {
                totalLength += upFile.length()
            }
            return totalLength
        }

    override fun setUploadMaxLength(maxLength: Long): FormParam {
        uploadMaxLength = maxLength
        return this
    }

    /**
     * 设置提交方式为{multipart/form-data}
     *
     * @return FormParam
     */
    fun setMultiForm(): FormParam {
        isMultiForm = true
        return this
    }

    override fun checkLength() {
        val totalFileLength = totalFileLength
        if (totalFileLength > uploadMaxLength) throw IllegalArgumentException("The current total file length is " + totalFileLength + " byte, " +
            "this length cannot be greater than " + uploadMaxLength + " byte")
    }

    /**
     * 设置上传进度监听器
     *
     * @param callback 进度回调对象
     * @return FormParam
     */
    override fun setProgressCallback(callback: ProgressCallback): FormParam {
        this.callback = callback
        return this
    }

    override fun getRequestBody(): RequestBody {
        val keyValuePairs = mKeyValuePairs
        val requestBody = if (isMultiForm || hasFile())
            BuildUtil.buildFormRequestBody(keyValuePairs, mFileList)
        else BuildUtil.buildFormRequestBody(keyValuePairs)
        val callback = callback
        return callback?.let {
            //如果设置了进度回调，则对RequestBody进行装饰
            ProgressRequestBody(requestBody, it)
        } ?: requestBody
    }

    val fileList: List<UpFile>?
        get() = mFileList

    val keyValuePairs: List<KeyValuePair>?
        get() = mKeyValuePairs

    override fun getCacheKey(): String {
        val cacheKey = super.getCacheKey()
        if (cacheKey != null) return cacheKey
        val keyValuePairs = CacheUtil.excludeCacheKey(mKeyValuePairs)
        return BuildUtil.getHttpUrl(getSimpleUrl(), keyValuePairs).toString()
    }

    override fun toString(): String {
        return BuildUtil.getHttpUrl(getSimpleUrl(), mKeyValuePairs).toString()
    }
}