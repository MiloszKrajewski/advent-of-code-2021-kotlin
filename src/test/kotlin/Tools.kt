import java.io.File
import java.math.BigInteger
import java.security.MessageDigest

fun loadLines(filename: String): List<String> =
    File("./src/test/kotlin", filename).readLines()

fun loadString(filename: String): String =
    File("./src/test/kotlin", filename).readText()

fun String.md5(): String =
    BigInteger(1, MessageDigest.getInstance("MD5").digest(toByteArray())).toString(16)

fun Int.round(step: Int) = this / step * step