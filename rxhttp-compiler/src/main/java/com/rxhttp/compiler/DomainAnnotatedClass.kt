package com.rxhttp.compiler

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.asTypeName
import rxhttp.wrapper.annotation.Domain
import java.util.*
import javax.lang.model.element.VariableElement

class DomainAnnotatedClass {
    private val mElementMap: MutableMap<String, VariableElement>
    fun add(variableElement: VariableElement) {
        val annotation = variableElement.getAnnotation(Domain::class.java)
        var name: String = annotation.name
        if (name.length <= 0) {
            name = variableElement.simpleName.toString()
        }
        mElementMap[name] = variableElement
    }

    fun getMethodList(companionMethodList: MutableList<FunSpec>): List<FunSpec> {
        val methodList: MutableList<FunSpec> = ArrayList()
        var method: FunSpec.Builder
        for ((key, value) in mElementMap) {
            method = FunSpec.builder("setDomainTo" + key + "IfAbsent")
                .addModifiers(KModifier.PUBLIC)
                .addStatement("val newUrl = addDomainIfAbsent(param.getSimpleUrl(), %T.%L)",
                    value.enclosingElement.asType().asTypeName(),
                    value.simpleName.toString())
                .addStatement("param.setUrl(newUrl)")
                .addStatement("return this as R")
                .returns(RxHttpGenerator.r)
            methodList.add(method.build())
        }
        //对url添加域名方法
        method = FunSpec.builder("addDomainIfAbsent")
            .addModifiers(KModifier.PRIVATE)
            .addParameter("url", String::class)
            .addParameter("domain", String::class)
            .addCode("if (url.startsWith(\"http\")) return url;\n" +
                "var newUrl : String\n" +
                "if (url.startsWith(\"/\")) {\n" +
                "    if (domain.endsWith(\"/\"))\n" +
                "        newUrl = domain + url.substring(1);\n" +
                "    else\n" +
                "        newUrl = domain + url;\n" +
                "} else if (domain.endsWith(\"/\")) {\n" +
                "    newUrl = domain + url;\n" +
                "} else {\n" +
                "    newUrl = domain + \"/\" + url;\n" +
                "}\n" +
                "return newUrl;\n")
            .returns(String::class)
        companionMethodList.add(method.build())
        return methodList
    }

    init {
        mElementMap = LinkedHashMap()
    }
}