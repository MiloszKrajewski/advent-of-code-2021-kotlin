import java.io.File
import java.math.BigInteger
import java.security.MessageDigest

fun sgn0(v: Int): Int = if (v == 0) 0 else if (v < 0) -1 else 1

fun <T> T?.failIfNull(): T = this ?: throw NullPointerException()

fun loadLines(filename: String): List<String> =
    File("./src/test/kotlin", filename).readLines()

fun loadText(filename: String): String =
    File("./src/test/kotlin", filename).readText()

fun String.md5(): String =
    BigInteger(1, MessageDigest.getInstance("MD5").digest(toByteArray())).toString(16)

fun String.insertAt(i: Int, c: Char): String =
    StringBuilder(this).apply { insert(i, c) }.toString()

fun Int.round(step: Int) = this / step * step