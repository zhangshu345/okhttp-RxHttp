package com.rxhttp.compiler

import com.rxhttp.compiler.exception.ProcessingException
import rxhttp.wrapper.annotation.*
import java.io.IOException
import java.util.*
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeKind
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic

/**
 * User: ljx
 * Date: 2019/3/21
 * Time: 20:36
 */
class AnnotationProcessor : AbstractProcessor() {
    private var typeUtils: Types? = null
    private var messager: Messager? = null
    private var filer: Filer? = null
    private var elementUtils: Elements? = null
    private var processed = false
    private lateinit var platform: String
    @Synchronized
    override fun init(processingEnvironment: ProcessingEnvironment) {
        super.init(processingEnvironment)
        typeUtils = processingEnvironment.typeUtils
        messager = processingEnvironment.messager
        filer = processingEnvironment.filer
        elementUtils = processingEnvironment.elementUtils
        val map = processingEnvironment.options
        platform = map["platform"] ?: "Android"
    }

    override fun getSupportedAnnotationTypes(): Set<String> {
        val annotations: MutableSet<String> = LinkedHashSet()
        annotations.add(Param::class.java.canonicalName)
        annotations.add(Parser::class.java.canonicalName)
        annotations.add(Converter::class.java.canonicalName)
        annotations.add(Domain::class.java.canonicalName)
        annotations.add(DefaultDomain::class.java.canonicalName)
        annotations.add(Override::class.java.canonicalName)
        return annotations
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean { //        messager.printMessage(Kind.WARNING, "process start annotations" + annotations + " this=" + this);
        if (annotations.isEmpty() || processed) return true
        try {
            val rxHttpGenerator = RxHttpGenerator()
            val paramsAnnotatedClass = ParamsAnnotatedClass()
            for (element in roundEnv.getElementsAnnotatedWith(Param::class.java)) {
                val typeElement = element as TypeElement
                checkParamsValidClass(typeElement);
                paramsAnnotatedClass.add(typeElement)
            }
            val parserAnnotatedClass = ParserAnnotatedClass()
            for (element in roundEnv.getElementsAnnotatedWith(Parser::class.java)) {
                val typeElement = element as TypeElement
                checkParserValidClass(typeElement);
                parserAnnotatedClass.add(typeElement)
            }
            val converterAnnotatedClass = ConverterAnnotatedClass()
            for (annotatedElement in roundEnv.getElementsAnnotatedWith(Converter::class.java)) {
                val variableElement = annotatedElement as VariableElement
                checkConverterValidClass(variableElement);
                converterAnnotatedClass.add(variableElement)
            }
            val domainAnnotatedClass = DomainAnnotatedClass()
            for (annotatedElement in roundEnv.getElementsAnnotatedWith(Domain::class.java)) {
                val variableElement = annotatedElement as VariableElement
                checkVariableValidClass(variableElement);
                domainAnnotatedClass.add(variableElement)
            }
            val elementSet = roundEnv.getElementsAnnotatedWith(DefaultDomain::class.java)
            if (elementSet.size > 1) throw ProcessingException(elementSet.iterator().next(), "@DefaultDomain annotations can only be used once") else if (elementSet.iterator().hasNext()) {
                val variableElement = elementSet.iterator().next() as VariableElement
                checkVariableValidClass(variableElement);
                rxHttpGenerator.setAnnotatedClass(variableElement)
            }
            rxHttpGenerator.setAnnotatedClass(paramsAnnotatedClass)
            rxHttpGenerator.setAnnotatedClass(parserAnnotatedClass)
            rxHttpGenerator.setAnnotatedClass(converterAnnotatedClass)
            rxHttpGenerator.setAnnotatedClass(domainAnnotatedClass)
            // Generate code
            rxHttpGenerator.generateCode(elementUtils, filer, platform)
            processed = true
        } catch (e: ProcessingException) {
            error(e.element, e.message)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return true
    }

    @Throws(ProcessingException::class)
    private fun checkParamsValidClass(element: TypeElement) {
        if (!element.modifiers.contains(Modifier.PUBLIC)) {
            throw ProcessingException(element,
                "The class %s is not public",
                Param::class.java.simpleName)
        }
        if (element.modifiers.contains(Modifier.ABSTRACT)) {
            throw ProcessingException(element,
                "The class %s is abstract. You can't annotate abstract classes with @%",
                element.simpleName.toString(), Param::class.java.simpleName)
        }
        var currentClass = element
        while (true) {
            val interfaces = currentClass.interfaces
            for (typeMirror in interfaces) {
                if (typeMirror.toString() != "rxhttp.wrapper.param.Param<P>") continue
                return
            }
            val superClassType = currentClass.superclass
            if (superClassType.kind == TypeKind.NONE) {
                throw ProcessingException(element,
                    "The class %s annotated with @%s must inherit from %s",
                    element.qualifiedName.toString(), Param::class.java.simpleName,
                    "rxhttp.wrapper.param.Param")
            }
            currentClass = typeUtils!!.asElement(superClassType) as TypeElement
        }
    }

    @Throws(ProcessingException::class)
    private fun checkParserValidClass(element: TypeElement) {
        if (!element.modifiers.contains(Modifier.PUBLIC)) {
            throw ProcessingException(element,
                "The class %s is not public",
                Parser::class.java.simpleName)
        }
        if (element.modifiers.contains(Modifier.ABSTRACT)) {
            throw ProcessingException(element,
                "The class %s is abstract. You can't annotate abstract classes with @%",
                element.simpleName.toString(), Parser::class.java.simpleName)
        }
        var currentClass = element
        All@ while (true) {
            val interfaces = currentClass.interfaces
            for (typeMirror in interfaces) {
                if (typeMirror.toString() != "rxhttp.wrapper.parse.Parser<T>") continue
                break@All
            }
            val superClassType = currentClass.superclass
            if (superClassType.kind == TypeKind.NONE) {
                throw ProcessingException(element,
                    "The class %s annotated with @%s must inherit from %s",
                    element.qualifiedName.toString(), Parser::class.java.simpleName,
                    "rxhttp.wrapper.parse.Parser<T>")
            }
            currentClass = typeUtils!!.asElement(superClassType) as TypeElement
        }
        //        for (Element enclosedElement : element.getEnclosedElements()) {
//            if (!(enclosedElement instanceof ExecutableElement)) continue;
//            if (!enclosedElement.getModifiers().contains(KModifier.PUBLIC)
//                    || !enclosedElement.getModifiers().contains(KModifier.COMPANION)) continue;
//            if (!enclosedElement.toString().equals("<T>get(java.lang.Class<T>)")) continue;
//            ExecutableElement executableElement = (ExecutableElement) enclosedElement;
//            TypeMirror returnType = executableElement.getReturnType();
//            if (!typeUtils.asElement(returnType).toString()
//                    .equals(element.getQualifiedName().toString())) continue;
//            if (returnType instanceof DeclaredType) {
//                DeclaredType declaredType = (DeclaredType) returnType;
//                if (declaredType.getTypeArguments().size() == 1) return;
//            }
//        }
// No empty constructor found
//        throw  ProcessingException(element,
//                "The class %s must provide an public static <T> %s get(Class<T> t) mehod",
//                element.getQualifiedName().toString(), element.getQualifiedName().toString() + "<T>");
    }

    @Throws(ProcessingException::class)
    private fun checkConverterValidClass(element: VariableElement) {
        if (!element.modifiers.contains(Modifier.PUBLIC)) {
            throw ProcessingException(element,
                "The variable %s is not public",
                element.simpleName)
        }
        if (!element.modifiers.contains(Modifier.STATIC)) {
            throw ProcessingException(element,
                "The variable %s is not static",
                element.simpleName.toString())
        }
        if ("rxhttp.wrapper.callback.IConverter" != element.asType().toString()) {
            throw ProcessingException(element,
                "The variable %s is not a IConverter",
                element.simpleName.toString())
        }
    }

    @Throws(ProcessingException::class)
    private fun checkVariableValidClass(element: VariableElement) {
        if (!element.modifiers.contains(Modifier.PUBLIC)) {
            throw ProcessingException(element,
                "The variable %s is not public",
                element.simpleName)
        }
        if (!element.modifiers.contains(Modifier.STATIC)) {
            throw ProcessingException(element,
                "The variable %s is not static",
                element.simpleName.toString())
        }
    }

    private fun error(e: Element, msg: String?, vararg args: Any) {
        messager!!.printMessage(Diagnostic.Kind.ERROR, String.format(msg!!, *args), e)
    }
}