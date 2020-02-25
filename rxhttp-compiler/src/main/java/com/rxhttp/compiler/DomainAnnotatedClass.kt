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
                .addCode("""
                        return 
                        if (url.startsWith("http")) {     
                            url                           
                        } else if (url.startsWith("/")) {  
                            domain + if (domain.endsWith("/")) url.substring(1) else url             
                        } else if (domain.endsWith("/")) {
                            domain + url              
                        } else {                          
                            domain + "/" + url            
                        }                                 
                """.trimIndent())
                .build())
        return funList
    }
}