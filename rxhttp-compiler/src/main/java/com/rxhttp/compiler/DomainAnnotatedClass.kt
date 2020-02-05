package com.rxhttp.compiler

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.asTypeName
import rxhttp.wrapper.annotation.Domain
import java.util.*
import javax.lang.model.element.VariableElement

class DomainAnnotatedClass {

    private val mElementMap = LinkedHashMap<String, VariableElement>()

    fun add(variableElement: VariableElement) {
        val annotation = variableElement.getAnnotation(Domain::class.java)
        var name: String = annotation.name
        if (name.isEmpty()) {
            name = variableElement.simpleName.toString()
        }
        mElementMap[name] = variableElement
    }

    fun getMethodList(companionMethodList: MutableList<FunSpec>): List<FunSpec> {
        val funList = ArrayList<FunSpec>()
        for ((key, value) in mElementMap) {
            funList.add(
                FunSpec.builder("setDomainTo" + key + "IfAbsent")
                .addModifiers(KModifier.PUBLIC)
                .addStatement("val newUrl = addDomainIfAbsent(param.getSimpleUrl(), %T.%L)",
                    value.enclosingElement.asType().asTypeName(),
                    value.simpleName.toString())
                .addStatement("param.setUrl(newUrl)")
                .addStatement("return this as R")
                .returns(RxHttpGenerator.r)
                    .build())
        }
        companionMethodList.add( //对url添加域名方法
            FunSpec.builder("addDomainIfAbsent")
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
                .build())
        return funList
    }
}