package rxhttp.wrapper.param

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.functions.Consumer
import rxhttp.HttpSender
import rxhttp.wrapper.entity.Progress
import rxhttp.wrapper.entity.UpFile
import rxhttp.wrapper.parse.Parser
import rxhttp.wrapper.parse.SimpleParser
import java.io.File
import kotlin.String

/**
 * Github
 * https://github.com/liujingxing/RxHttp
 * https://github.com/liujingxing/RxLife
 */
open class RxHttp_FormParam : RxHttp<FormParam, RxHttp_FormParam> {
    constructor(param: FormParam) : super(param)

    fun add(key: String, value: Any?): RxHttp_FormParam {
        param.add(key, value)
        return this
    }

    fun addEncoded(key: String, value: Any?): RxHttp_FormParam {
        param.addEncoded(key, value)
        return this
    }

    fun add(
        key: String,
        value: Any?,
        isAdd: Boolean
    ): RxHttp_FormParam {
        if (isAdd) {
            param.add(key, value)
        }
        return this
    }

    fun addAll(map: Map<out String, *>): RxHttp_FormParam {
        param.addAll(map)
        return this
    }

    fun removeAllBody(): RxHttp_FormParam {
        param.removeAllBody()
        return this
    }

    fun removeAllBody(key: String): RxHttp_FormParam {
        param.removeAllBody(key)
        return this
    }

    fun set(key: String, value: Any?): RxHttp_FormParam {
        param.set(key, value)
        return this
    }

    fun setEncoded(key: String, value: Any?): RxHttp_FormParam {
        param.setEncoded(key, value)
        return this
    }

    fun queryValue(key: String): Any? = param.queryValue(key)

    fun queryValues(key: String): List<Any> = param.queryValues(key)

    fun add(key: String, file: File): RxHttp_FormParam {
        param.add(key, file)
        return this
    }

    fun addFile(key: String, file: File): RxHttp_FormParam {
        param.addFile(key, file)
        return this
    }

    fun addFile(key: String, filePath: String): RxHttp_FormParam {
        param.addFile(key, filePath)
        return this
    }

    fun addFile(
        key: String,
        value: String?,
        filePath: String
    ): RxHttp_FormParam {
        param.addFile(key, value, filePath)
        return this
    }

    fun addFile(
        key: String,
        value: String?,
        file: File
    ): RxHttp_FormParam {
        param.addFile(key, value, file)
        return this
    }

    fun addFile(file: UpFile): RxHttp_FormParam {
        param.addFile(file)
        return this
    }

    fun addFile(key: String, fileList: List<File>): RxHttp_FormParam {
        param.addFile(key, fileList)
        return this
    }

    fun addFile(fileList: List<UpFile>): RxHttp_FormParam {
        param.addFile(fileList)
        return this
    }

    fun removeFile(key: String): RxHttp_FormParam {
        param.removeFile(key)
        return this
    }

    fun setMultiForm(): RxHttp_FormParam {
        param.setMultiForm()
        return this
    }

    fun setUploadMaxLength(maxLength: Long): RxHttp_FormParam {
        param.setUploadMaxLength(maxLength)
        return this
    }

    fun asUpload(progressConsumer: Consumer<Progress<String>>) =
        asUpload(SimpleParser(String::class.java), progressConsumer, null)

    fun asUpload(progressConsumer: Consumer<Progress<String>>, observeOnScheduler: Scheduler) =
        asUpload(SimpleParser(String::class.java), progressConsumer, observeOnScheduler)

    fun <T> asUpload(
        parser: Parser<T>,
        progressConsumer: Consumer<Progress<T>>,
        observeOnScheduler: Scheduler?
    ): Observable<T> {
        setConverter(param)
        var observable = HttpSender
            .uploadProgress(addDefaultDomainIfAbsent(param), parser, scheduler)
        if (observeOnScheduler != null) {
            observable = observable.observeOn(observeOnScheduler)
        }
        return observable.doOnNext(progressConsumer)
            .filter { it.isCompleted }
            .map { it.result }
    }
}
