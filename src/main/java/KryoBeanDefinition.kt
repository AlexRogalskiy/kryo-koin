import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.Serializer
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import org.koin.core.context.GlobalContext
import org.koin.core.definition.BeanDefinition
import org.koin.core.definition.Definition
import org.koin.core.definition.Kind
import org.koin.core.parameter.ParametersHolder
import org.koin.core.qualifier.Qualifier
import org.koin.core.qualifier.StringQualifier
import org.koin.core.qualifier.TypeQualifier
import org.koin.core.scope.Scope
import org.koin.core.scope.ScopeID
import java.awt.Dimension
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.reflect.KClass

fun main() {
    val saveFile = File("file.bin")
    val kryo = buildKryo()

    val smthg: Definition<Dimension> = { it -> Dimension() }

    val beanDef = BeanDefinition(
        StringQualifier("foo"),
        Dimension::class,
        definition = smthg,
        kind = Kind.Factory
    )

    val output = Output(FileOutputStream(saveFile))
    kryo.writeClassAndObject(output, beanDef)
    output.close()

    // the downstream bits here do not matter, because it fails already while saving

    println("restoring...")
    val input = Input(FileInputStream(saveFile));
    val restored = kryo.readClassAndObject(input) as BeanDefinition<Dimension>

    // invoke the lambda in the factory to build the objet
    val koin = GlobalContext.startKoin {}.koin
    val scope = Scope(StringQualifier("foo"), ScopeID(), false, koin)

    val result = restored.definition(scope, ParametersHolder())
    println("result is ${result}")
}


fun buildKryo(): Kryo {
    val kryo = Kryo()

    kryo.setOptimizedGenerics(false);
    kryo.setReferences(true)
    kryo.isRegistrationRequired = false

//    kryo.register(InstanceRegistry::class.java, object : FieldSerializer<Any?>(kryo, BeanDefinition::class.java) {
//        override fun create(kryo: Kryo, input: Input, type: Class<*>?): BeanDefinition {
//            return BeanDefinition()
//        }
//    })
    kryo.register(BeanDefinition::class.java, object : Serializer<BeanDefinition<*>>() {

        override fun write(kryo: Kryo?, output: Output?, beanDef: BeanDefinition<*>) {
            kryo!!.writeObject(output, beanDef.scopeQualifier)
            kryo.writeClassAndObject(output, beanDef.primaryType)
            kryo.writeObjectOrNull(output, beanDef.qualifier, Qualifier::class.java)
            kryo.writeClassAndObject(output, beanDef.definition)
            kryo.writeObject(output, beanDef.kind)
            kryo.writeClassAndObject(output, beanDef.secondaryTypes)

            println(beanDef)
        }

        override fun read(
            kryo: Kryo?,
            input: Input?,
            type: Class<out BeanDefinition<*>>?
        ): BeanDefinition<*> {

            val scopeQualifier = kryo!!.readObject(input, StringQualifier::class.java)
            val primaryType = kryo.readClassAndObject(input) as KClass<*>
            val qualifier = kryo.readObjectOrNull(input, Qualifier::class.java)
            val definition = kryo.readClassAndObject(input) as Definition<*>
            val kind = kryo.readObject(input, Kind::class.java)
            val secondaryTypes = kryo.readClassAndObject(input) as List<KClass<*>>

            return BeanDefinition(scopeQualifier, primaryType, qualifier, definition, kind, secondaryTypes)
        }
    })


    kryo.register(TypeQualifier::class.java, object : Serializer<TypeQualifier>() {

        override fun write(kryo: Kryo?, output: Output?, qualifier: TypeQualifier) {
            kryo!!.writeObjectOrNull(output, qualifier.type, TypeQualifier::class.java)
        }

        override fun read(
            kryo: Kryo?,
            input: Input?,
            type: Class<out TypeQualifier>?
        ): TypeQualifier {

            val type = kryo!!.readObjectOrNull(input, KClass::class.java)
            return TypeQualifier(type)
        }
    })

    kryo.register(StringQualifier::class.java, object : Serializer<StringQualifier>() {

        override fun write(kryo: Kryo?, output: Output?, qualifier: StringQualifier) {
            kryo!!.writeObject(output, qualifier.value)
        }

        override fun read(
            kryo: Kryo?,
            input: Input?,
            type: Class<out StringQualifier>?
        ): StringQualifier {

            val value = kryo!!.readObjectOrNull(input, String::class.java)
            return StringQualifier(value)
        }
    })

    kryo.register(kotlin.jvm.internal.ClassReference::class.java, object : Serializer<kotlin.jvm.internal.ClassReference>() {

        override fun write(kryo: Kryo?, output: Output?, qualifier: kotlin.jvm.internal.ClassReference) {
            kryo!!.writeClassAndObject(output, qualifier.jClass)
        }

        override fun read(
            kryo: Kryo?,
            input: Input?,
            type: Class<out kotlin.jvm.internal.ClassReference>?
        ): kotlin.jvm.internal.ClassReference {

            val value = kryo!!.readClassAndObject(input) as java.lang.Class<*>
            return kotlin.jvm.internal.ClassReference(value)
        }
    })

    return kryo
}