import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import org.koin.core.context.GlobalContext
import org.koin.core.definition.BeanDefinition
import org.koin.core.definition.Definition
import org.koin.core.definition.Kind
import org.koin.core.parameter.ParametersHolder
import org.koin.core.qualifier.StringQualifier
import org.koin.core.scope.Scope
import org.koin.core.scope.ScopeID
import java.awt.Dimension
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

fun main() {
    val saveFile = File("file.bin")
    val kryo = buildKryo()

    val smthg: Definition<Dimension> = { it -> Dimension() }

    val beanDef =         BeanDefinition<Dimension>(
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


private fun buildKryo(): Kryo {
    val kryo = Kryo()

    kryo.setReferences(true)
    kryo.isRegistrationRequired = false

    return kryo
}