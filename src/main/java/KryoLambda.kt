import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import java.awt.Dimension
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream


typealias Builder<T> = String.(Int) -> T


fun main() {
    val saveFile = File("file.bin")
    val kryo = buildKryo()

    val smthg: Builder<Dimension> = { it ->
        println(this)
        Dimension(it, it+10)
    }

    val instFac = smthg
    val output: Output = Output(FileOutputStream(saveFile))
    kryo.writeClassAndObject(output, smthg)
    output.close()

    println("restoring...")
    val input = Input(FileInputStream(saveFile));
    val restored = kryo.readClassAndObject(input) as Builder<Dimension>

    val result = "foo".restored(12)
    println("the result was ${result}")
}


private fun buildKryo(): Kryo {
    val kryo = Kryo()

    kryo.setReferences(true)

    // https://github.com/EsotericSoftware/kryo/issues/196
    kryo.isRegistrationRequired = false

    return kryo
}