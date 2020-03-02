package com.example.httpsender.parser


import com.example.httpsender.entity.Response
import rxhttp.wrapper.annotation.Parser
import rxhttp.wrapper.entity.ParameterizedTypeImpl
import rxhttp.wrapper.exception.ParseException
import rxhttp.wrapper.parse.AbstractParser
import java.io.IOException

/**
 * 输入T，输出List<T>，并对code统一判断
 * User: ljx
 * Date: 2018/10/23
 * Time: 13:49
 */
@Parser(name = "ResponseList")
class ResponseListParser<T : Any> : AbstractParser<MutableList<T>> {

    protected constructor() : super()

    constructor(type: Class<T>) : super(type)

    @Throws(IOException::class)
    override fun onParse(response: okhttp3.Response): MutableList<T> {
        val type = ParameterizedTypeImpl[Response::class.java, MutableList::class.java, mType] //获取泛型类型
        val data = convert<Response<MutableList<T>>>(response, type)
        val list = data.data //获取data字段
        if (data.code != 0 || list == null) {  //code不等于0，说明数据不正确，抛出异常
            throw ParseException(data.code.toString(), data.msg, response)
        }
        return list
    }
}
