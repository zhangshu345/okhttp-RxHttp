@file:Suppress("UNCHECKED_CAST")

package rxhttp.wrapper.param

import rxhttp.wrapper.callback.ProgressCallback
import rxhttp.wrapper.entity.UpFile
import java.io.File

/**
 * User: ljx
 * Date: 2019-05-19
 * Time: 18:18
 */
interface IFile<P : Param<P>> {
    fun add(key: String, file: File): P {
        return addFile(key, file.absolutePath)
    }

    fun addFile(key: String, file: File): P {
        return addFile(key, file.absolutePath)
    }

    fun addFile(key: String, filePath: String): P {
        return addFile(UpFile(key, filePath))
    }

    fun addFile(key: String, value: String?, filePath: String): P {
        val upFile = UpFile(key, filePath)
        upFile.value = value
        return addFile(upFile)
    }

    fun addFile(key: String, value: String?, file: File): P {
        return addFile(key, value, file.absolutePath)
    }

    fun addFile(key: String, fileList: List<File>): P {
        for (file in fileList) {
            addFile(UpFile(key, file.absolutePath))
        }
        return this as P
    }

    fun addFile(upFileList: List<UpFile>): P {
        for (upFile in upFileList) {
            addFile(upFile)
        }
        return this as P
    }

    /**
     *
     * 添加文件对象
     *
     * @param upFile UpFile
     * @return Param
     */
    fun addFile(upFile: UpFile): P

    /**
     * 根据key 移除已添加的文件
     *
     * @param key String
     * @return Param
     */
    fun removeFile(key: String): P

    fun setUploadMaxLength(maxLength: Long): P
    /**
     *
     * 设置上传进度监听器
     *
     * @param callback 进度回调对象
     * @return Param
     */
    fun setProgressCallback(callback: ProgressCallback): P
}