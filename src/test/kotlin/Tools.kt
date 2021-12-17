import java.io.File
import java.math.BigInteger
import java.security.MessageDigest

fun loadLines(filename: String): List<String> =
    File("./src/test/kotlin", filename).readLines()

fun loadText(filename: String): String =
    File("./src/test/kotlin", filename).readText()

fun String.md5(): String =
    BigInteger(1, MessageDigest.getInstance("MD5").digest(toByteArray())).toString(16)

fun String.insertAt(i: Int, c: Char): String =
    StringBuilder(this).apply { insert(i, c) }.toString()

fun Int.round(step: Int) = this / step * step