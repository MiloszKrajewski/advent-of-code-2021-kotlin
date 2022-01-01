import java.io.File
import java.math.BigInteger
import java.security.MessageDigest
import kotlin.test.fail

fun sgn0(v: Int): Int = if (v == 0) 0 else if (v < 0) -1 else 1

fun maxOrNull(a: Int?, b: Int?) = if (a == null || b == null) null else Integer.max(a, b)

fun <T> T?.failIfNull(): T = this ?: throw NullPointerException()

fun error(message: String): Nothing = fail(message)

fun loadLines(filename: String): List<String> =
    File("./src/test/kotlin", filename).readLines()

fun loadText(filename: String): String =
    File("./src/test/kotlin", filename).readText()

fun String.md5(): String =
    BigInteger(1, MessageDigest.getInstance("MD5").digest(toByteArray())).toString(16)

fun String.insertAt(i: Int, c: Char): String =
    StringBuilder(this).apply { insert(i, c) }.toString()

fun Int.round(step: Int) = this / step * step