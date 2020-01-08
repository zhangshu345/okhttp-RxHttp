package rxhttp.wrapper.param

/**
 * 请求方法
 * User: ljx
 * Date: 2019-09-10
 * Time: 23:18
 */
enum class Method {
    GET, HEAD, POST, PUT, PATCH, DELETE;

    val isGet: Boolean
        get() = name == "GET"

    val isPost: Boolean
        get() = name == "POST"

    val isHead: Boolean
        get() = name == "HEAD"

    val isPut: Boolean
        get() = name == "PUT"

    val isPatch: Boolean
        get() = name == "PATCH"

    val isDelete: Boolean
        get() = name == "DELETE"
}