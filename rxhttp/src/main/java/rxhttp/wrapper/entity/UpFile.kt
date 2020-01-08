package rxhttp.wrapper.entity

import java.io.File
import java.net.URI

/**
 * User: ljx
 * Date: 2018/12/21
 * Time: 09:21
 */
class UpFile : File {

    var key: String

    var value: String? = null
        get() = if (field == null) name else field

    constructor(key: String, pathname: String) : super(pathname) {
        this.key = key
    }

    constructor(key: String, parent: String?, child: String) : super(parent, child) {
        this.key = key
    }

    constructor(key: String, parent: File?, child: String) : super(parent, child) {
        this.key = key
    }

    constructor(key: String, uri: URI) : super(uri) {
        this.key = key
    }

}