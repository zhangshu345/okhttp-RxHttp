package com.rxhttp.compiler

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import rxhttp.wrapper.annotation.Parser
import java.io.IOException
import java.util.*
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror
import kotlin.reflect.KClass

class ParserAnnotatedClass {

    private val mElementMap = LinkedHashMap<String, TypeElement>()
    private val anyTypeName = Any::class.asTypeName()
    private val t = TypeVariableName("T")
    private val k = TypeVariableName("K")
    private val v = TypeVariableName("V")
    private val anyT = TypeVariableName("T", anyTypeName)
    private val anyK = TypeVariableName("K", anyTypeName)
    private val anyV = TypeVariableName("V", anyTypeName)
    private val responseName = ClassName("okhttp3", "Response")
    private val callName = ClassName("okhttp3", "Call")
    private val schedulerName = ClassName("io.reactivex", "Scheduler")
    private val observableName = ClassName("io.reactivex", "Observable")
    private val consumerName = ClassName("io.reactivex.functions", "Consumer")
    private val httpSenderName = ClassName("rxhttp", "HttpSender")
    private val awaitName = ClassName("rxhttp", "await")
    private val parserName = ClassName("rxhttp.wrapper.parse", "Parser")
    private val progressName = ClassName("rxhttp.wrapper.entity", "Progress")
    private val simpleParserName = ClassName("rxhttp.wrapper.parse", "SimpleParser")
    private val mapParserName = ClassName("rxhttp.wrapper.parse", "MapParser")
    private val listParserName = ClassName("rxhttp.wrapper.parse", "ListParser")
    private val downloadParserName = ClassName("rxhttp.wrapper.parse", "DownloadParser")
    private val bitmapParserName = ClassName("rxhttp.wrapper.parse", "BitmapParser")
    private val okResponseParserName = ClassName("rxhttp.wrapper.parse", "OkResponseParser")
    private val stringTypeName = String::class.asTypeName()
    private val classTypeName = Class::class.asClassName()
    private val kClassTypeName = KClass::class.asClassName()
    private val classTName = classTypeName.parameterizedBy(t)
    private val classKName = classTypeName.parameterizedBy(k)
    private val classVName = classTypeName.parameterizedBy(v)
    private val kClassTName = kClassTypeName.parameterizedBy(t)
    private val kClassKName = kClassTypeName.parameterizedBy(k)
    private val kClassVName = kClassTypeName.parameterizedBy(v)
    private val progressStringName = progressName.parameterizedBy(stringTypeName)
    private val observableTName = observableName.parameterizedBy(t)
    private val observableStringName = observableName.parameterizedBy(stringTypeName)
    private val consumerProgressStringName = consumerName.parameterizedBy(progressStringName)
    private val parserTName = parserName.parameterizedBy(t)
    private val okHttpClientName = ClassName("okhttp3", "OkHttpClient")

    fun add(typeElement: TypeElement) {
        val annotation = typeElement.getAnnotation(Parser::class.java)
        val name: String = annotation.name
        require(name.isNotEmpty()) {
            String.format("methodName() in @%s for class %s is null or empty! that's not allowed",
                Parser::class.java.simpleName, typeElement.qualifiedName.toString())
        }
        mElementMap[name] = typeElement
    }

    var awaitEndIndex = 0 //最后一个awaitXxx方法下标

    fun getMethodList(platform: String): List<FunSpec> {

        val funList = ArrayList<FunSpec>()
        funList.add(
            FunSpec.builder("subscribeOn")
                .addParameter("scheduler", schedulerName)
                .addStatement("this.scheduler=scheduler")
                .addStatement("return this as R")
                .returns(RxHttpGenerator.r)
                .build())

        funList.add(
            FunSpec.builder("subscribeOnCurrent")
                .addKdoc("设置在当前线程发请求\n")
                .addStatement("this.scheduler=null")
                .addStatement("return this as R")
                .returns(RxHttpGenerator.r)
                .build())

        funList.add(
            FunSpec.builder("subscribeOnIo")
                .addStatement("this.scheduler=Schedulers.io()")
                .addStatement("return this as R")
                .returns(RxHttpGenerator.r)
                .build())

        funList.add(
            FunSpec.builder("subscribeOnComputation")
                .addStatement("this.scheduler=Schedulers.computation()")
                .addStatement("return this as R")
                .returns(RxHttpGenerator.r)
                .build())

        funList.add(
            FunSpec.builder("subscribeOnNewThread")
                .addStatement("this.scheduler=Schedulers.newThread()")
                .addStatement("return this as R")
                .returns(RxHttpGenerator.r)
                .build())

        funList.add(
            FunSpec.builder("subscribeOnSingle")
                .addStatement("this.scheduler=Schedulers.single()")
                .addStatement("return this as R")
                .returns(RxHttpGenerator.r)
                .build())

        funList.add(
            FunSpec.builder("subscribeOnTrampoline")
                .addStatement("this.scheduler=Schedulers.trampoline()")
                .addStatement("return this as R")
                .returns(RxHttpGenerator.r)
                .build())

        funList.add(
            FunSpec.builder("execute")
                .addAnnotation(AnnotationSpec.builder(Throws::class)
                    .addMember("%T::class", IOException::class).build())
                .addStatement("return newCall().execute()")
                .returns(responseName)
                .build())
        funList.add(
            FunSpec.builder("execute")
                .addTypeVariable(anyT)
                .addAnnotation(AnnotationSpec.builder(Throws::class)
                    .addMember("%T::class", IOException::class).build())
                .addParameter("parser", parserTName)
                .addStatement("return parser.onParse(execute())", httpSenderName)
                .returns(t)
                .build())

        funList.add(
            FunSpec.builder("newCall")
                .addStatement("return newCall(getOkHttpClient())")
                .returns(callName)
                .build())

        funList.add(
            FunSpec.builder("newCall")
                .addParameter("okHttp", okHttpClientName)
                .addStatement("setConverter(param)")
                .addStatement("return %T.newCall(okHttp, addDefaultDomainIfAbsent(param))", httpSenderName)
                .returns(callName)
                .build())

        funList.add(
            FunSpec.builder("awaitBoolean")
                .addModifiers(KModifier.SUSPEND)
                .addStatement("return await(Boolean::class.java)")
                .build())

        funList.add(
            FunSpec.builder("awaitByte")
                .addModifiers(KModifier.SUSPEND)
                .addStatement("return await(Byte::class.java)")
                .build())

        funList.add(
            FunSpec.builder("awaitShort")
                .addModifiers(KModifier.SUSPEND)
                .addStatement("return await(Short::class.java)")
                .build())

        funList.add(
            FunSpec.builder("awaitInt")
                .addModifiers(KModifier.SUSPEND)
                .addStatement("return await(Int::class.java)")
                .build())

        funList.add(
            FunSpec.builder("awaitLong")
                .addModifiers(KModifier.SUSPEND)
                .addStatement("return await(Long::class.java)")
                .build())

        funList.add(
            FunSpec.builder("awaitFloat")
                .addModifiers(KModifier.SUSPEND)
                .addStatement("return await(Float::class.java)")
                .build())
        funList.add(
            FunSpec.builder("awaitDouble")
                .addModifiers(KModifier.SUSPEND)
                .addStatement("return await(Double::class.java)")
                .build())

        funList.add(
            FunSpec.builder("awaitString")
                .addModifiers(KModifier.SUSPEND)
                .addStatement("return await(String::class.java)")
                .build())

        funList.add(
            FunSpec.builder("await")
                .addModifiers(KModifier.SUSPEND)
                .addStatement("return await(Any::class.java)")
                .build())

        funList.add(
            FunSpec.builder("await")
                .addModifiers(KModifier.SUSPEND)
                .addTypeVariable(anyT)
                .addParameter("any", kClassTName)
                .addStatement("return await(any.java)")
                .build())

        funList.add(
            FunSpec.builder("await")
                .addModifiers(KModifier.SUSPEND)
                .addTypeVariable(anyT)
                .addParameter("any", classTName)
                .addStatement("return await(SimpleParser(any))")
                .build())

        funList.add(
            FunSpec.builder("awaitList")
                .addModifiers(KModifier.SUSPEND)
                .addStatement("return awaitList(Any::class.java)")
                .build())

        funList.add(
            FunSpec.builder("awaitList")
                .addModifiers(KModifier.SUSPEND)
                .addTypeVariable(anyT)
                .addParameter("any", kClassTName)
                .addStatement("return awaitList(any.java)")
                .build())

        funList.add(
            FunSpec.builder("awaitList")
                .addModifiers(KModifier.SUSPEND)
                .addTypeVariable(anyT)
                .addParameter("any", classTName)
                .addStatement("return await(ListParser(any))")
                .build())

        funList.add(
            FunSpec.builder("awaitMap")
                .addModifiers(KModifier.SUSPEND)
                .addStatement("return awaitMap(Any::class.java,Any::class.java)")
                .build())

        funList.add(
            FunSpec.builder("awaitMap")
                .addModifiers(KModifier.SUSPEND)
                .addTypeVariable(anyK)
                .addParameter("kType", classKName)
                .addStatement("return awaitMap(kType, kType)")
                .build())

        funList.add(
            FunSpec.builder("awaitMap")
                .addModifiers(KModifier.SUSPEND)
                .addTypeVariable(anyK)
                .addParameter("kType", kClassKName)
                .addStatement("return awaitMap(kType.java,kType.java)")
                .build())

        funList.add(
            FunSpec.builder("awaitMap")
                .addModifiers(KModifier.SUSPEND)
                .addTypeVariable(anyK)
                .addTypeVariable(anyV)
                .addParameter("kType", kClassKName)
                .addParameter("vType", kClassVName)
                .addStatement("return awaitMap(kType.java,vType.java)")
                .build())

        funList.add(
            FunSpec.builder("awaitMap")
                .addModifiers(KModifier.SUSPEND)
                .addTypeVariable(anyK)
                .addTypeVariable(anyV)
                .addParameter("kType", classKName)
                .addParameter("vType", classVName)
                .addStatement("return await(MapParser(kType,vType))")
                .build())

        funList.add(
            FunSpec.builder("awaitHeaders")
                .addModifiers(KModifier.SUSPEND)
                .addStatement("return awaitOkResponse().headers()")
                .build())

        funList.add(
            FunSpec.builder("awaitOkResponse")
                .addModifiers(KModifier.SUSPEND)
                .addStatement("return await(OkResponseParser())")
                .build())

        funList.add(
            FunSpec.builder("await")
                .addKdoc("所有的awaitXxx方法,最终都会调用本方法\n")
                .addModifiers(KModifier.SUSPEND)
                .addTypeVariable(anyT)
                .addParameter("parser", parserTName)
                .addStatement("return newCall().%T(parser)", awaitName)
                .build())

        awaitEndIndex = funList.size

        if ("Android" == platform) {
            funList.add(
                FunSpec.builder("awaitBitmap")
                    .addModifiers(KModifier.SUSPEND)
                    .addStatement("return await(BitmapParser())")
                    .build())

            funList.add(
                FunSpec.builder("asBitmap")
                    .addStatement("return asParser(%T())", bitmapParserName)
                    .build())
        }

        funList.add(
            FunSpec.builder("asString")
                .addStatement("return asObject(String::class.java)")
                .build())

        funList.add(
            FunSpec.builder("asBoolean")
                .addStatement("return asObject(Boolean::class.java)")
                .build())

        funList.add(
            FunSpec.builder("asByte")
                .addStatement("return asObject(Byte::class.java)")
                .build())

        funList.add(
            FunSpec.builder("asShort")
                .addStatement("return asObject(Short::class.java)")
                .build())

        funList.add(
            FunSpec.builder("asInt")
                .addStatement("return asObject(Int::class.java)")
                .build())

        funList.add(
            FunSpec.builder("asLong")
                .addStatement("return asObject(Long::class.java)")
                .build())

        funList.add(
            FunSpec.builder("asFloat")
                .addStatement("return asObject(Float::class.java)")
                .build())

        funList.add(
            FunSpec.builder("asDouble")
                .addStatement("return asObject(Double::class.java)")
                .build())

        funList.add(
            FunSpec.builder("asObject")
                .addStatement("return asObject(Any::class.java)")
                .build())

        funList.add(
            FunSpec.builder("asObject")
                .addTypeVariable(anyT)
                .addParameter("type", kClassTName)
                .addStatement("return asObject((type.java))")
                .build())

        funList.add(
            FunSpec.builder("asObject")
                .addTypeVariable(anyT)
                .addParameter("type", classTName)
                .addStatement("return asParser(%T(type))", simpleParserName)
                .build())

        funList.add(FunSpec.builder("asMap")
            .addStatement("return asObject(Map::class.java)")
            .build())

        funList.add(
            FunSpec.builder("asMap")
                .addTypeVariable(anyT)
                .addParameter("type", classTName)
                .addStatement("return asParser(%T(type,type))", mapParserName)
                .build())

        funList.add(
            FunSpec.builder("asMap")
                .addTypeVariable(anyT)
                .addParameter("type", kClassTName)
                .addStatement("return asParser(%T(type.java,type.java))", mapParserName)
                .build())

        funList.add(
            FunSpec.builder("asMap")
                .addTypeVariable(anyK)
                .addTypeVariable(anyV)
                .addParameter("kType", classKName)
                .addParameter("vType", classVName)
                .addStatement("return asParser(%T(kType,vType))", mapParserName)
                .build())

        funList.add(
            FunSpec.builder("asMap")
                .addTypeVariable(anyK)
                .addTypeVariable(anyV)
                .addParameter("kType", kClassKName)
                .addParameter("vType", kClassVName)
                .addStatement("return asParser(%T(kType.java,vType.java))", mapParserName)
                .build())

        funList.add(
            FunSpec.builder("asList")
                .addStatement("return asList(Any::class.java)")
                .build())

        funList.add(
            FunSpec.builder("asList")
                .addTypeVariable(anyT)
                .addParameter("type", kClassTName)
                .addStatement("return asList((type.java))")
                .build())

        funList.add(
            FunSpec.builder("asList")
                .addTypeVariable(anyT)
                .addParameter("type", classTName)
                .addStatement("return asParser(%T(type))", listParserName)
                .build())

        funList.add(
            FunSpec.builder("asHeaders")
                .addKdoc("调用此方法，订阅回调时，返回 [okhttp3.Headers] 对象\n")
                .addStatement("return asOkResponse().map(Response::headers)")
                .build())

        funList.add(
            FunSpec.builder("asOkResponse")
                .addKdoc("调用此方法，订阅回调时，返回 [okhttp3.Response] 对象\n")
                .addStatement("return asParser(%T())", okResponseParserName)
                .build())

        funList.add(
            FunSpec.builder("asParser")
                .addKdoc("所有的asXxx方法,最终都会调用本方法\n")
                .addTypeVariable(anyT)
                .addParameter("parser", parserTName)
                .addStatement("setConverter(param)")
                .addStatement("var observable = %T.syncFrom(addDefaultDomainIfAbsent(param),parser)", httpSenderName)
                .beginControlFlow("if(scheduler!=null)")
                .addStatement("observable=observable.subscribeOn(scheduler)")
                .endControlFlow()
                .addStatement("return observable")
                .returns(observableTName)
                .build())

        //遍历@Parser注解，生成对应的方法
        for ((funName, typeElement) in mElementMap) {
            var returnType: TypeMirror? = null //获取onParse方法的返回类型
            for (element in typeElement.enclosedElements) {
                if (element !is ExecutableElement
                    || !element.getModifiers().contains(Modifier.PUBLIC)
                    || element.getModifiers().contains(Modifier.STATIC))
                    continue
                if (element.simpleName.toString() == "onParse"
                    && element.parameters.size == 1
                    && element.parameters[0].asType().toString() == "okhttp3.Response") {
                    returnType = element.returnType
                    break
                }
            }
            if (returnType == null) continue
            //一个@Parser注解，会生成3个asXxx重载方法
            generateNoParamFun(typeElement, funName, funList)  //生成无参的asXxx方法
            generateKClassFun(typeElement, funName, funList)   //生成asXxx(KClass<T>)类型方法
            generateClassFun(typeElement, funName, funList)    //生成asXxx(Class<T>)类型方法
        }

        funList.add(
            FunSpec.builder("asDownload")
                .addParameter("destPath", String::class)
                .addStatement("return asParser(%T(destPath))", downloadParserName)
                .build())

        funList.add(
            FunSpec.builder("asDownload")
                .addParameter("destPath", String::class)
                .addParameter("progressConsumer", consumerProgressStringName)
                .addParameter("observeOnScheduler", schedulerName)
                .addStatement("return asDownload(destPath, 0, progressConsumer, observeOnScheduler)")
                .build())

        val offsetSize = ParameterSpec.builder("offsetSize", Long::class)
            .defaultValue("0L")
            .build()
        val observeOnScheduler = ParameterSpec.builder("observeOnScheduler", schedulerName.copy(nullable = true))
            .defaultValue("null")
            .build()

        funList.add(
            FunSpec.builder("asDownload")
                .addAnnotation(JvmOverloads::class)
                .addParameter("destPath", String::class)
                .addParameter(offsetSize)
                .addParameter("progressConsumer", consumerProgressStringName)
                .addParameter(observeOnScheduler)
                .addCode("""
                    setConverter(param)                                                                    
                    var observable = HttpSender                                                            
                        .downloadProgress(addDefaultDomainIfAbsent(param), destPath, offsetSize, scheduler)
                    if (observeOnScheduler != null) {                                                      
                        observable = observable.observeOn(observeOnScheduler)                              
                    }                                                                                      
                    return observable.doOnNext(progressConsumer)                                           
                        .filter { it.isFinish }                                                         
                        .map { it.result }                                                                 
                """.trimIndent())
                .returns(observableStringName)
                .build())
        return funList
    }

    //根据@Parser注解，生成无参数asXxx方法
    private fun generateNoParamFun(typeElement: TypeElement, key: String, funList: ArrayList<FunSpec>) {
        val typeVariableNames = ArrayList<TypeVariableName>()

        typeElement.typeParameters.forEach {
            val typeVariableName = it.asTypeVariableName()
            typeVariableNames.add(typeVariableName)
        }

        typeVariableNames.forEach {
            it.bounds.forEach { typeName ->
                //遍历泛型的所有边界，若设置了边界，则不生成对应的无参方法
                if (typeName.toString() != "java.lang.Object") {
                    return //泛型有边界，直接返回
                }
            }
        }

        //自定义解析器对应的asXxx方法里面的语句
        var statementBuilder = StringBuilder("return as$key(")
        repeat(typeVariableNames.size) {
            statementBuilder.append("Any::class.java,")
        }
        if (statementBuilder.last() == ',') {
            statementBuilder.deleteCharAt(statementBuilder.length - 1)
        }
        statementBuilder.append(")")
        val asFunBuilder = FunSpec.builder("as$key")
            .addStatement(statementBuilder.toString())
        funList.add(asFunBuilder.build())

        //自定义解析器对应的awaitXxx方法里面的语句
        statementBuilder = StringBuilder("return await$key(")
        repeat(typeVariableNames.size) {
            statementBuilder.append("Any::class.java,")
        }
        if (statementBuilder.last() == ',') {
            statementBuilder.deleteCharAt(statementBuilder.length - 1)
        }
        statementBuilder.append(")")
        val awaitFunBuilder = FunSpec.builder("await$key")
            .addModifiers(KModifier.SUSPEND)
            .addStatement(statementBuilder.toString())

        funList.add(awaitEndIndex++, awaitFunBuilder.build())
    }

    //根据@Parser注解，生成asXxx(KClass<T>)类型方法 ，kotlin专用
    private fun generateKClassFun(typeElement: TypeElement, key: String, funList: ArrayList<FunSpec>) {
        val typeVariableNames = ArrayList<TypeVariableName>()
        val parameterSpecs = ArrayList<ParameterSpec>()

        typeElement.typeParameters.forEach {
            val typeVariableName = it.asTypeVariableName()
            typeVariableNames.add(typeVariableName)
            val parameterSpec = ParameterSpec.builder(
                it.asType().toString().toLowerCase() + "Type",
                kClassTypeName.parameterizedBy(typeVariableName)).build()
            parameterSpecs.add(parameterSpec)
        }

        //自定义解析器对应的asXxx方法里面的语句
        var statementBuilder = StringBuilder("return as$key(") //方法里面的表达式
        parameterSpecs.forEach {
            statementBuilder.append(it.name).append(".java,")
        }
        if (statementBuilder.last() == ',') {
            statementBuilder.deleteCharAt(statementBuilder.length - 1)
        }

        statementBuilder.append(")")
        var funBuilder = FunSpec.builder("as$key")
            .addParameters(parameterSpecs)
            .addStatement(statementBuilder.toString())

        typeVariableNames.forEach {
            if (it.bounds.isEmpty()
                || (it.bounds.size == 1 && it.bounds[0].toString() == "java.lang.Object")) {
                funBuilder.addTypeVariable(TypeVariableName(it.name, anyTypeName))
            } else {
                funBuilder.addTypeVariable(it.toKClassTypeName() as TypeVariableName)
            }
        }
        funList.add(funBuilder.build())

        //自定义解析器对应的awaitXxx方法里面的语句
        statementBuilder = StringBuilder("return await$key(") //方法里面的表达式
        parameterSpecs.forEach {
            statementBuilder.append(it.name).append(".java,")
        }
        if (statementBuilder.last() == ',') {
            statementBuilder.deleteCharAt(statementBuilder.length - 1)
        }

        statementBuilder.append(")")
        funBuilder = FunSpec.builder("await$key")
            .addModifiers(KModifier.SUSPEND)
            .addParameters(parameterSpecs)
            .addStatement(statementBuilder.toString())

        typeVariableNames.forEach {
            if (it.bounds.isEmpty()
                || (it.bounds.size == 1 && it.bounds[0].toString() == "java.lang.Object")) {
                funBuilder.addTypeVariable(TypeVariableName(it.name, anyTypeName))
            } else {
                funBuilder.addTypeVariable(it.toKClassTypeName() as TypeVariableName)
            }
        }
        funList.add(awaitEndIndex++, funBuilder.build())
    }

    //根据@Parser注解，生成asXxx(Class<T>)类型方法
    private fun generateClassFun(typeElement: TypeElement, key: String, funList: ArrayList<FunSpec>) {
        val typeVariableNames = ArrayList<TypeVariableName>()
        val parameterSpecs = ArrayList<ParameterSpec>()

        typeElement.typeParameters.forEach {
            val typeVariableName = it.asTypeVariableName()
            typeVariableNames.add(typeVariableName)
            val parameterSpec = ParameterSpec.builder(
                it.asType().toString().toLowerCase() + "Type",
                classTypeName.parameterizedBy(typeVariableName)).build()
            parameterSpecs.add(parameterSpec)
        }

        //自定义解析器对应的asXxx方法里面的语句
        var statementBuilder = StringBuilder("return asParser(%T") //方法里面的表达式
        if (typeVariableNames.size > 0) { //添加泛型
            statementBuilder.append("<")
            var i = 0
            val size = typeVariableNames.size
            while (i < size) {
                val variableName = typeVariableNames[i]
                statementBuilder.append(variableName.name)
                    .append(if (i == size - 1) ">" else ",")
                i++
            }
        }

        statementBuilder.append("(")
        parameterSpecs.forEach {
            statementBuilder.append(it.name).append(",")
        }
        if (statementBuilder.last() == ',') {
            statementBuilder.deleteCharAt(statementBuilder.length - 1)
        }
        statementBuilder.append("))")
        var funBuilder = FunSpec.builder("as$key")
            .addParameters(parameterSpecs)
            .addStatement(statementBuilder.toString(), typeElement.asClassName())

        typeVariableNames.forEach {
            if (it.bounds.isEmpty()
                || (it.bounds.size == 1 && it.bounds[0].toString() == "java.lang.Object")) {
                funBuilder.addTypeVariable(TypeVariableName(it.name, anyTypeName))
            } else {
                funBuilder.addTypeVariable(it.toKClassTypeName() as TypeVariableName)
            }
        }
        funList.add(funBuilder.build())


        //自定义解析器对应的awaitXxx方法里面的语句
        statementBuilder = StringBuilder("return await(%T") //方法里面的表达式
        if (typeVariableNames.size > 0) { //添加泛型
            statementBuilder.append("<")
            var i = 0
            val size = typeVariableNames.size
            while (i < size) {
                val variableName = typeVariableNames[i]
                statementBuilder.append(variableName.name)
                    .append(if (i == size - 1) ">" else ",")
                i++
            }
        }

        statementBuilder.append("(")
        parameterSpecs.forEach {
            statementBuilder.append(it.name).append(",")
        }
        if (statementBuilder.last() == ',') {
            statementBuilder.deleteCharAt(statementBuilder.length - 1)
        }
        statementBuilder.append("))")
        funBuilder = FunSpec.builder("await$key")
            .addModifiers(KModifier.SUSPEND)
            .addParameters(parameterSpecs)
            .addStatement(statementBuilder.toString(), typeElement.asClassName())

        typeVariableNames.forEach {
            if (it.bounds.isEmpty()
                || (it.bounds.size == 1 && it.bounds[0].toString() == "java.lang.Object")) {
                funBuilder.addTypeVariable(TypeVariableName(it.name, anyTypeName))
            } else {
                funBuilder.addTypeVariable(it.toKClassTypeName() as TypeVariableName)
            }
        }
        funList.add(awaitEndIndex++, funBuilder.build())
    }
}