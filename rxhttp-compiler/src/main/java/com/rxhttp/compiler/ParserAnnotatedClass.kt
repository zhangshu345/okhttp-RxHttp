package com.rxhttp.compiler

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import rxhttp.wrapper.annotation.Parser
import java.io.IOException
import java.util.*
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.element.TypeParameterElement
import javax.lang.model.type.TypeMirror

class ParserAnnotatedClass {

    private val mElementMap = LinkedHashMap<String, TypeElement>()

    fun add(typeElement: TypeElement) {
        val annotation = typeElement.getAnnotation(Parser::class.java)
        val name: String = annotation.name
        require(name.isNotEmpty()) {
            String.format("methodName() in @%s for class %s is null or empty! that's not allowed",
                Parser::class.java.simpleName, typeElement.qualifiedName.toString())
        }
        mElementMap[name] = typeElement
    }

    fun getMethodList(platform: String): List<FunSpec> {
        val t = TypeVariableName("T")
        val k = TypeVariableName("K")
        val v = TypeVariableName("V")
        val responseName = ClassName("okhttp3", "Response")
        val schedulerName = ClassName("io.reactivex", "Scheduler")
        val observableName = ClassName("io.reactivex", "Observable")
        val consumerName = ClassName("io.reactivex.functions", "Consumer")
        val httpSenderName = ClassName("rxhttp", "HttpSender")
        val parserName = ClassName("rxhttp.wrapper.parse", "Parser")
        val progressName = ClassName("rxhttp.wrapper.entity", "Progress")
        val simpleParserName = ClassName("rxhttp.wrapper.parse", "SimpleParser")
        val mapParserName = ClassName("rxhttp.wrapper.parse", "MapParser")
        val listParserName = ClassName("rxhttp.wrapper.parse", "ListParser")
        val downloadParserName = ClassName("rxhttp.wrapper.parse", "DownloadParser")
        val bitmapParserName = ClassName("rxhttp.wrapper.parse", "BitmapParser")
        val okResponseParserName = ClassName("rxhttp.wrapper.parse", "OkResponseParser")
        val stringTypeName = String::class.asTypeName()
        val classTypeName = Class::class.asClassName()
        val classTName = classTypeName.parameterizedBy(t)
        val classKName = classTypeName.parameterizedBy(k)
        val classVName = classTypeName.parameterizedBy(v)
        val progressStringName = progressName.parameterizedBy(stringTypeName)
        val observableTName = observableName.parameterizedBy(t)
        val observableStringName = observableName.parameterizedBy(stringTypeName)
        val consumerProgressStringName = consumerName.parameterizedBy(progressStringName)
        val parserTName = parserName.parameterizedBy(t)
        val funList = ArrayList<FunSpec>()
        funList.add(
            FunSpec.builder("execute")
                .addAnnotation(AnnotationSpec.builder(Throws::class)
                    .addMember("%T::class", IOException::class).build())
                .addStatement("setConverter(param)")
                .addStatement("return %T.execute(addDefaultDomainIfAbsent(param))", httpSenderName)
                .returns(responseName)
                .build())
        funList.add(
            FunSpec.builder("execute")
                .addTypeVariable(t)
                .addAnnotation(AnnotationSpec.builder(Throws::class)
                    .addMember("%T::class", IOException::class).build())
                .addParameter("parser", parserTName)
                .addStatement("return parser.onParse(execute())", httpSenderName)
                .returns(t)
                .build())

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
            FunSpec.builder("asParser")
                .addTypeVariable(t)
                .addParameter("parser", parserTName)
                .addStatement("setConverter(param)")
                .addStatement("var observable=%T.syncFrom(addDefaultDomainIfAbsent(param),parser)", httpSenderName)
                .beginControlFlow("if(scheduler!=null)")
                .addStatement("observable=observable.subscribeOn(scheduler)")
                .endControlFlow()
                .addStatement("return observable")
                .returns(observableTName)
                .build())

        funList.add(
            FunSpec.builder("asObject")
                .addTypeVariable(t)
                .addParameter("type", classTName)
                .addStatement("return asParser(%T(type))", simpleParserName)
                .build())


        if ("Android" == platform) {
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
            FunSpec.builder("asInteger")
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

        funList.add(FunSpec.builder("asMap")
            .addStatement("return asObject(Map::class.java)")
            .build())

        funList.add(
            FunSpec.builder("asMap")
                .addTypeVariable(t)
                .addParameter("type", classTName)
                .addStatement("return asParser(%T(type,type))", mapParserName)
                .build())

        funList.add(
            FunSpec.builder("asMap")
                .addTypeVariable(k)
                .addTypeVariable(v)
                .addParameter("kType", classKName)
                .addParameter("vType", classVName)
                .addStatement("return asParser(%T(kType,vType))", mapParserName)
                .build())

        funList.add(
            FunSpec.builder("asList")
                .addTypeVariable(t)
                .addParameter("type", classTName)
                .addStatement("return asParser(%T(type))", listParserName)
                .build())


        funList.add(
            FunSpec.builder("asHeaders")
                .addKdoc("调用此方法，订阅回调时，返回 {@link okhttp3.Headers} 对象\n")
                .addStatement("return asOkResponse().map(Response::headers)")
                .build())

        funList.add(
            FunSpec.builder("asOkResponse")
                .addKdoc("调用此方法，订阅回调时，返回 {@link okhttp3.Response} 对象\n")
                .addStatement("return asParser(%T())", okResponseParserName)
                .build())

        //获取自定义解析器，并生成对应的方法
        for ((key, typeElement) in mElementMap) {
            var returnType: TypeMirror? = null //获取onParse方法的返回类型
            for (element in typeElement.enclosedElements) {
                if (element !is ExecutableElement) continue
                if (!element.getModifiers().contains(Modifier.PUBLIC)
                    || element.getModifiers().contains(Modifier.STATIC)) continue
                val executableElement = element
                if (executableElement.simpleName.toString() == "onParse" && executableElement.parameters.size == 1 && executableElement.parameters[0].asType().toString() == "okhttp3.Response") {
                    returnType = executableElement.returnType
                    break
                }
            }
            if (returnType == null) continue
            val typeVariableNames: MutableList<TypeVariableName> = ArrayList()
            val parameterSpecs: MutableList<ParameterSpec> = ArrayList()
            val typeParameters: List<TypeParameterElement> = typeElement.getTypeParameters()
            for (element in typeParameters) {

                val typeVariableName = element.asTypeVariableName()
                typeVariableNames.add(typeVariableName)
                val parameterSpec: ParameterSpec = ParameterSpec.builder(
                    element.asType().toString().toLowerCase() + "Type",
                    Class::class.asClassName().parameterizedBy(typeVariableName)).build()
                parameterSpecs.add(parameterSpec)
            }

            //自定义解析器对应的asXxx方法里面的语句
            //自定义解析器对应的asXxx方法里面的语句
            val statementBuilder = StringBuilder("return asParser(%T")
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
            if (parameterSpecs.size > 0) { //添加参数
                var i = 0
                val size = parameterSpecs.size
                while (i < size) {
                    val parameterSpec = parameterSpecs[i]
                    statementBuilder.append(parameterSpec.name)
                    if (i < size - 1) statementBuilder.append(",")
                    i++
                }
            }
            statementBuilder.append("))")
            val funBuilder = FunSpec.builder("as$key")
                .addParameters(parameterSpecs)
                .addStatement(statementBuilder.toString(), typeElement.asClassName())

            typeVariableNames.forEach {
                funBuilder.addTypeVariable(it.toKClassTypeName() as TypeVariableName)
            }
            funList.add(funBuilder.build())
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
                .addStatement("return asDownload(destPath, 0, progressConsumer, null)")
                .build())

        funList.add(
            FunSpec.builder("asDownload")
                .addParameter("destPath", String::class)
                .addParameter("progressConsumer", consumerProgressStringName)
                .addParameter("observeOnScheduler", schedulerName)
                .addStatement("return asDownload(destPath, 0, progressConsumer, observeOnScheduler)")
                .build())

        funList.add(
            FunSpec.builder("asDownload")
                .addParameter("destPath", String::class)
                .addParameter("offsetSize", Long::class)
                .addParameter("progressConsumer", consumerProgressStringName)
                .addStatement("return asDownload(destPath, offsetSize, progressConsumer, null)")
                .build())


        val offsetSize = ParameterSpec.builder("offsetSize", Long::class)
            .build()
        val observeOnScheduler = ParameterSpec.builder("observeOnScheduler", schedulerName.copy(nullable = true))
            .build()

        funList.add(
            FunSpec.builder("asDownload")
                .addParameter("destPath", String::class)
                .addParameter(offsetSize)
                .addParameter("progressConsumer", consumerProgressStringName)
                .addParameter(observeOnScheduler)
                .addStatement("setConverter(param)")
                .addStatement("var observable = %T\n" +
                    ".downloadProgress(addDefaultDomainIfAbsent(param),destPath,offsetSize,scheduler)", httpSenderName)
                .beginControlFlow("if(observeOnScheduler != null)")
                .addStatement("observable=observable.observeOn(observeOnScheduler)")
                .endControlFlow()
                .addStatement("return observable.doOnNext(progressConsumer)\n" +
                    ".filter { it.isCompleted }\n" +
                    ".map { it.result }")
                .returns(observableStringName)
                .build())
        return funList
    }
}