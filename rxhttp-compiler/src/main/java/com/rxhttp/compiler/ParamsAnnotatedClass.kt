package com.rxhttp.compiler

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import rxhttp.wrapper.annotation.Param
import java.io.IOException
import java.util.*
import javax.annotation.processing.Filer
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import kotlin.Boolean
import kotlin.Long
import kotlin.String

class ParamsAnnotatedClass {
    private val mElementMap: MutableMap<String, TypeElement>
    fun add(typeElement: TypeElement) {
        val annotation = typeElement.getAnnotation(Param::class.java)
        val name: String = annotation.methodName
        require(name.isNotEmpty()) {
            String.format("methodName() in @%s for class %s is null or empty! that's not allowed",
                Param::class.java.simpleName, typeElement.qualifiedName.toString())
        }
        mElementMap[name] = typeElement
    }

    @Throws(IOException::class)
    fun getMethodList(filer: Filer?, companionMethodList: MutableList<FunSpec>): List<FunSpec> {
        val rxHttp = RxHttpGenerator.r
        val headerName = ClassName("okhttp3", "Headers")
        val headerBuilderName = ClassName("okhttp3", "Headers.Builder")
        val cacheControlName = ClassName("okhttp3", "CacheControl")
        val paramName = ClassName(RxHttpGenerator.packageName, "Param")
        val noBodyParamName = ClassName(RxHttpGenerator.packageName, "NoBodyParam")
        val formParamName = ClassName(RxHttpGenerator.packageName, "FormParam")
        val jsonParamName = ClassName(RxHttpGenerator.packageName, "JsonParam")
        val jsonArrayParamName = ClassName(RxHttpGenerator.packageName, "JsonArrayParam")
        val cacheModeName = ClassName("rxhttp.wrapper.cahce", "CacheMode")
        val methodList: MutableList<FunSpec> = ArrayList()
        val methodMap: MutableMap<String, String> = LinkedHashMap()
        methodMap["get"] = "RxHttp_NoBodyParam"
        methodMap["head"] = "RxHttp_NoBodyParam"
        methodMap["postForm"] = "RxHttp_FormParam"
        methodMap["putForm"] = "RxHttp_FormParam"
        methodMap["patchForm"] = "RxHttp_FormParam"
        methodMap["deleteForm"] = "RxHttp_FormParam"
        methodMap["postJson"] = "RxHttp_JsonParam"
        methodMap["putJson"] = "RxHttp_JsonParam"
        methodMap["patchJson"] = "RxHttp_JsonParam"
        methodMap["deleteJson"] = "RxHttp_JsonParam"
        methodMap["postJsonArray"] = "RxHttp_JsonArrayParam"
        methodMap["putJsonArray"] = "RxHttp_JsonArrayParam"
        methodMap["patchJsonArray"] = "RxHttp_JsonArrayParam"
        methodMap["deleteJsonArray"] = "RxHttp_JsonArrayParam"
        var method: FunSpec.Builder
        for ((key, value) in methodMap) {
            method = FunSpec.builder(key)
                .addModifiers(KModifier.PUBLIC)
                .addAnnotation(JvmStatic::class)
                .addParameter("url", String::class)
                .addParameter("formatArgs", Any::class, KModifier.VARARG)
                .addStatement("return \n    with(%T.%L(format(url, formatArgs)))", paramName, key)
//                .returns(ClassName(RxHttpGenerator.packageName, value))
            companionMethodList.add(method.build())
        }
        for ((key, typeElement) in mElementMap) {
            val param = ClassName.bestGuess(typeElement.qualifiedName.toString())
            val rxHttpName = "RxHttp_" + typeElement.simpleName
            val rxhttpParamName = ClassName(RxHttpGenerator.packageName, rxHttpName)
            method = FunSpec.builder(key)
                .addModifiers(KModifier.PUBLIC)
                .addAnnotation(JvmStatic::class)
                .addParameter("url", String::class)
                .addParameter("formatArgs", Any::class, KModifier.VARARG)
                .addStatement("return  \n    %T(%T(format(url, formatArgs)))", rxhttpParamName, param)
//                .returns(RxHttp_ParamName)
            companionMethodList.add(method.build())
            val superclass = typeElement.superclass
            var rxhttpParam: TypeName
            var prefix = "(param as " + param.simpleName + ")."
            when (superclass.toString()) {
                "rxhttp.wrapper.param.FormParam" -> rxhttpParam = ClassName(RxHttpGenerator.packageName, "RxHttp_FormParam")
                "rxhttp.wrapper.param.JsonParam" -> rxhttpParam = ClassName(RxHttpGenerator.packageName, "RxHttp_JsonParam")
                "rxhttp.wrapper.param.NoBodyParam" -> rxhttpParam = ClassName(RxHttpGenerator.packageName, "RxHttp_NoBodyParam")
                else -> {
                    prefix = "param."
                    rxhttpParam = RxHttpGenerator.RXHTTP.parameterizedBy(param, rxhttpParamName)
                }
            }
            val rxHttpPostEncryptFormParamMethod = ArrayList<FunSpec>()
            method = FunSpec.constructorBuilder()
                .addModifiers(KModifier.PUBLIC)
                .addParameter("param", param)
                .callSuperConstructor("param")
            rxHttpPostEncryptFormParamMethod.add(method.build())
            for (enclosedElement in typeElement.enclosedElements) {
                if (enclosedElement !is ExecutableElement) continue
                if (!enclosedElement.getModifiers().contains(Modifier.PUBLIC)) continue  //过滤非public修饰符
                if (enclosedElement.getKind() != ElementKind.METHOD) continue  //过滤非方法，
                if (enclosedElement.getAnnotation(Override::class.java) != null) continue  //过滤重写的方法
                val returnTypeMirror = enclosedElement.returnType
                var returnType = returnTypeMirror.asTypeName()
                if (returnType.toString() == param.toString()) {
                    returnType = rxhttpParamName
                }
                val parameterSpecs: MutableList<ParameterSpec> = ArrayList()
                val builder = StringBuilder()
                    .append(enclosedElement.getSimpleName().toString())
                    .append("(")
                enclosedElement.parameters.forEach {
                    val parameterSpec = ParameterSpec.get(it)
                    parameterSpecs.add(parameterSpec)
                    builder.append(parameterSpec.name).append(",")
                }
                if (builder.toString().endsWith(",")) {
                    builder.deleteCharAt(builder.length - 1)
                }
                builder.append(")")

                if (enclosedElement.isVarArgs) { //处理可变参数
                    if (enclosedElement.parameters.size == 1) {
                        builder.insert(builder.indexOf("(") + 1, "*")
                    } else if (enclosedElement.parameters.size > 1) {
                        val indexOf = builder.lastIndexOf(",")
                        builder.insert(indexOf + 1, "*")
                    }
                }
                method = enclosedElement.toFunSpecBuilder()
                if (returnType === rxhttpParamName) {
                    method.addStatement(prefix + builder, param)
                        .addStatement("return this")
                } else if (returnType.toString() == "void") {
                    method.addStatement(prefix + builder)
                } else {
                    method.addStatement("return $prefix$builder", param)
                }
                method.returns(returnType)
                rxHttpPostEncryptFormParamMethod.add(method.build())
            }
            val rxHttpPostEncryptFormParamSpec = TypeSpec.classBuilder(rxHttpName)
                .addKdoc("Github" +
                    "\nhttps://github.com/liujingxing/RxHttp" +
                    "\nhttps://github.com/liujingxing/RxLife\n")
                .addModifiers(KModifier.PUBLIC, KModifier.OPEN)
                .superclass(rxhttpParam)
                .addFunctions(rxHttpPostEncryptFormParamMethod)
                .build()
            FileSpec.builder(RxHttpGenerator.packageName, rxHttpName)
                .addType(rxHttpPostEncryptFormParamSpec)
                .build().writeTo(filer!!)
        }
        method = FunSpec.builder("with")
            .addModifiers(KModifier.PUBLIC)
            .addAnnotation(JvmStatic::class)
            .addParameter("noBodyParam", noBodyParamName)
            .addStatement("return %L(noBodyParam)", "RxHttp_NoBodyParam")
        companionMethodList.add(method.build())
        method = FunSpec.builder("with")
            .addModifiers(KModifier.PUBLIC)
            .addAnnotation(JvmStatic::class)
            .addParameter("formParam", formParamName)
            .addStatement("return %L(formParam)", "RxHttp_FormParam")
        companionMethodList.add(method.build())
        method = FunSpec.builder("with")
            .addModifiers(KModifier.PUBLIC)
            .addAnnotation(JvmStatic::class)
            .addParameter("jsonParam", jsonParamName)
            .addStatement("return %L(jsonParam)", "RxHttp_JsonParam")
        companionMethodList.add(method.build())
        method = FunSpec.builder("with")
            .addModifiers(KModifier.PUBLIC)
            .addAnnotation(JvmStatic::class)
            .addParameter("jsonArrayParam", jsonArrayParamName)
            .addStatement("return %L(jsonArrayParam)", "RxHttp_JsonArrayParam")
        companionMethodList.add(method.build())
        method = FunSpec.builder("setUrl")
            .addModifiers(KModifier.PUBLIC)
            .addParameter("url", String::class)
            .addStatement("param.setUrl(url)")
            .addStatement("return this as R")
            .returns(rxHttp)
        methodList.add(method.build())
        method = FunSpec.builder("addHeader")
            .addModifiers(KModifier.PUBLIC)
            .addParameter("line", String::class)
            .addStatement("param.addHeader(line)")
            .addStatement("return this as R")
            .returns(rxHttp)
        methodList.add(method.build())
        method = FunSpec.builder("addHeader")
            .addModifiers(KModifier.PUBLIC)
            .addParameter("line", String::class)
            .addParameter("isAdd", Boolean::class)
            .beginControlFlow("if(isAdd)")
            .addStatement("param.addHeader(line)")
            .endControlFlow()
            .addStatement("return this as R")
            .returns(rxHttp)
        methodList.add(method.build())
        method = FunSpec.builder("addHeader")
            .addModifiers(KModifier.PUBLIC)
            .addParameter("key", String::class)
            .addParameter("value", String::class)
            .addStatement("param.addHeader(key,value)")
            .addStatement("return this as R")
            .returns(rxHttp)
        methodList.add(method.build())
        method = FunSpec.builder("addHeader")
            .addModifiers(KModifier.PUBLIC)
            .addParameter("key", String::class)
            .addParameter("value", String::class)
            .addParameter("isAdd", Boolean::class)
            .beginControlFlow("if(isAdd)")
            .addStatement("param.addHeader(key,value)")
            .endControlFlow()
            .addStatement("return this as R")
            .returns(rxHttp)
        methodList.add(method.build())
        method = FunSpec.builder("setHeader")
            .addModifiers(KModifier.PUBLIC)
            .addParameter("key", String::class)
            .addParameter("value", String::class)
            .addStatement("param.setHeader(key,value)")
            .addStatement("return this as R")
            .returns(rxHttp)
        methodList.add(method.build())
        method = FunSpec.builder("setRangeHeader")
            .addModifiers(KModifier.PUBLIC)
            .addParameter("startIndex", Long::class)
            .addStatement("param.setRangeHeader(startIndex)")
            .addStatement("return this as R")
            .returns(rxHttp)
        methodList.add(method.build())
        method = FunSpec.builder("setRangeHeader")
            .addModifiers(KModifier.PUBLIC)
            .addParameter("startIndex", Long::class)
            .addParameter("endIndex", Long::class)
            .addStatement("param.setRangeHeader(startIndex,endIndex)")
            .addStatement("return this as R")
            .returns(rxHttp)
        methodList.add(method.build())
        method = FunSpec.builder("removeAllHeader")
            .addModifiers(KModifier.PUBLIC)
            .addParameter("key", String::class)
            .addStatement("param.removeAllHeader(key)")
            .addStatement("return this as R")
            .returns(rxHttp)
        methodList.add(method.build())
        method = FunSpec.builder("setHeadersBuilder")
            .addModifiers(KModifier.PUBLIC)
            .addParameter("builder", headerBuilderName)
            .addStatement("param.setHeadersBuilder(builder)")
            .addStatement("return this as R")
            .returns(rxHttp)
        methodList.add(method.build())
        method = FunSpec.builder("setAssemblyEnabled")
            .addKdoc("设置单个接口是否需要添加公共参数," +
                "\n即是否回调通过{@link #setOnParamAssembly(Function)}方法设置的接口,默认为true\n")
            .addModifiers(KModifier.PUBLIC)
            .addParameter("enabled", Boolean::class)
            .addStatement("param.setAssemblyEnabled(enabled)")
            .addStatement("return this as R")
            .returns(rxHttp)
        methodList.add(method.build())

        val annoDeprecated = AnnotationSpec.builder(kotlin.Deprecated::class)
            .addMember(
                "\n\"please user {@link #setDecoderEnabled(boolean)} instead\""
                    + "\n,ReplaceWith(\"setDecoderEnabled(enabled)\", \"RxHttp.setDecoderEnabled\")")
            .build()

        method = FunSpec.builder("setConverterEnabled")
            .addAnnotation(annoDeprecated)
            .addKdoc("@deprecated please user {@link #setDecoderEnabled(boolean)} instead\n")
            .addModifiers(KModifier.PUBLIC)
            .addParameter("enabled", Boolean::class)
            .addStatement("return setDecoderEnabled(enabled)")
            .returns(rxHttp)
        methodList.add(method.build())
        method = FunSpec.builder("setDecoderEnabled")
            .addKdoc("设置单个接口是否需要对Http返回的数据进行解码/解密," +
                "\n即是否回调通过{@link #setResultDecoder(Function)}方法设置的接口,默认为true\n")
            .addModifiers(KModifier.PUBLIC)
            .addParameter("enabled", Boolean::class)
            .addStatement("param.addHeader(%T.DATA_DECRYPT,enabled.toString())", paramName)
            .addStatement("return this as R")
            .returns(rxHttp)
        methodList.add(method.build())
        method = FunSpec.builder("isAssemblyEnabled")
            .addModifiers(KModifier.PUBLIC)
            .addStatement("return param.isAssemblyEnabled()")
//            .returns(Boolean::class)
        methodList.add(method.build())
        method = FunSpec.builder("getUrl")
            .addModifiers(KModifier.PUBLIC)
            .addStatement("return param.getUrl()")
//            .returns(String::class)
        methodList.add(method.build())
        method = FunSpec.builder("getSimpleUrl")
            .addModifiers(KModifier.PUBLIC)
            .addStatement("return param.getSimpleUrl()")
//            .returns(String::class)
        methodList.add(method.build())
        method = FunSpec.builder("getHeader")
            .addModifiers(KModifier.PUBLIC)
            .addParameter("key", String::class)
            .addStatement("return param.getHeader(key)")
//            .returns(String::class.asTypeName().copy(nullable = true))
        methodList.add(method.build())
        method = FunSpec.builder("getHeaders")
            .addModifiers(KModifier.PUBLIC)
            .addStatement("return param.getHeaders()")
            .returns(headerName.copy(nullable = true))
        methodList.add(method.build())
        method = FunSpec.builder("getHeadersBuilder")
            .addModifiers(KModifier.PUBLIC)
            .addStatement("return param.getHeadersBuilder()")
//            .returns(headerBuilderName)
        methodList.add(method.build())
        method = FunSpec.builder("buildRequest")
            .addModifiers(KModifier.PUBLIC)
            .addStatement("return param.buildRequest()")
//            .returns(requestName)
        methodList.add(method.build())
        method = FunSpec.builder("tag")
            .addModifiers(KModifier.PUBLIC)
            .addParameter("tag", Any::class.asTypeName().copy(nullable = true))
            .addStatement("param.tag(tag)")
            .addStatement("return this as R")
            .returns(rxHttp)
        methodList.add(method.build())
        val t = TypeVariableName("T")
        val superT = WildcardTypeName.consumerOf(t)
        val classTName: TypeName = Class::class.asClassName().parameterizedBy(superT)
        method = FunSpec.builder("tag")
            .addModifiers(KModifier.PUBLIC)
            .addTypeVariable(t)
            .addParameter("type", classTName)
            .addParameter("tag", t.copy(nullable = true))
            .addStatement("param.tag(type,tag)")
            .addStatement("return this as R")
            .returns(rxHttp)
        methodList.add(method.build())
        method = FunSpec.builder("cacheControl")
            .addModifiers(KModifier.PUBLIC)
            .addParameter("cacheControl", cacheControlName)
            .addStatement("param.cacheControl(cacheControl)")
            .addStatement("return this as R")
            .returns(rxHttp)
        methodList.add(method.build())
        method = FunSpec.builder("setCacheKey")
            .addModifiers(KModifier.PUBLIC)
            .addParameter("cacheKey", String::class)
            .addStatement("param.setCacheKey(cacheKey)")
            .addStatement("return this as R")
            .returns(rxHttp)
        methodList.add(method.build())
        method = FunSpec.builder("setCacheValidTime")
            .addModifiers(KModifier.PUBLIC)
            .addParameter("cacheValidTime", Long::class)
            .addStatement("param.setCacheValidTime(cacheValidTime)")
            .addStatement("return this as R")
            .returns(rxHttp)
        methodList.add(method.build())
        method = FunSpec.builder("setCacheMode")
            .addModifiers(KModifier.PUBLIC)
            .addParameter("cacheMode", cacheModeName)
            .addStatement("param.setCacheMode(cacheMode)")
            .addStatement("return this as R")
            .returns(rxHttp)
        methodList.add(method.build())
        return methodList
    }

    init {
        mElementMap = LinkedHashMap()
    }
}