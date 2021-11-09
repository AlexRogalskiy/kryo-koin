import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import org.koin.core.Koin
import org.koin.core.context.GlobalContext
import org.koin.dsl.module
import java.awt.Dimension
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

fun main() {
    val saveFile = File("file.bin")
    val kryo = buildKryo()

    val koin = GlobalContext.startKoin {}.koin

    koin. loadModules(listOf(
        module(createdAtStart = true) {
            single { Dimension(23,3) }
        }
    ))

    val output = Output(FileOutputStream(saveFile))
    kryo.writeClassAndObject(output, koin)
    output.close()

    // the downstream bits here do not matter, because it fails already while saving

    println("restoring...")
    val input = Input(FileInputStream(saveFile));
    val restored = kryo.readClassAndObject(input) as Koin

    val result = restored.get<Dimension>()
    println("result is ${result}")
}


