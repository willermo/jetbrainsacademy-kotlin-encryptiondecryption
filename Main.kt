package encryptdecrypt
import encryptdecrypt.Operation.*
import encryptdecrypt.Parameter.*
import encryptdecrypt.Algorithm.*
import java.io.File
import kotlin.system.exitProcess

private val alphabet_minuscule = "abcdefghijklmnopqrstuvwxyz"
private val alphabet_maiuscule = alphabet_minuscule.uppercase()

enum class Operation(val op: String) {
    ENCODING("enc"),
    DECODING("dec"),
}

enum class Algorithm(val type: String) {
    ALPHABET("shift"),
    UNICODE("unicode")
}

enum class Parameter(val par:String) {
    MODE("-mode"),
    KEY("-key"),
    DATA("-data"),
    IN("-in"),
    OUT("-out"),
    ALGORITHM("-alg"),
}

fun alphabetEncrypt(letter: Char, shift: Int): Char {
    return when (letter){
        in alphabet_minuscule -> alphabet_minuscule[(alphabet_minuscule.indexOf(letter) + shift + alphabet_minuscule.length) % alphabet_minuscule.length]
        in alphabet_maiuscule -> alphabet_maiuscule[(alphabet_maiuscule.indexOf(letter) + shift + alphabet_minuscule.length) % alphabet_maiuscule.length]
        else -> letter
    }
}
fun alphabetDecrypt(letter: Char, shift: Int): Char{
    return when (letter){
        in alphabet_minuscule -> alphabet_minuscule[(alphabet_minuscule.indexOf(letter) - shift + alphabet_minuscule.length) % alphabet_minuscule.length]
        in alphabet_maiuscule -> alphabet_maiuscule[(alphabet_maiuscule.indexOf(letter) - shift + alphabet_minuscule.length) % alphabet_maiuscule.length]
        else -> letter
    }
}
fun unicodeEncrypt(letter: Char, shift: Int): Char = (letter + shift)
fun unicodeDecrypt(letter: Char, shift: Int): Char = (letter - shift)

fun transform(function: (Char, Int) -> Char): (Char, Int) -> Char { return { a, b -> function(a, b)} }

fun encrypt(letter: Char, shift: Int, alg: String): Char =
    if (alg == ALPHABET.type)  alphabetEncrypt(letter, shift) else unicodeEncrypt(letter, shift)

fun decrypt(letter: Char, shift: Int, alg: String): Char =
    if (alg == ALPHABET.type)  alphabetDecrypt(letter, shift) else unicodeDecrypt(letter, shift)

fun encryptDecrypt(letter: Char, operation: String, shift: Int, alg: String): Char =
    if (operation == ENCODING.op) encrypt(letter, shift, alg) else decrypt(letter, shift, alg)

fun paramListIsCorrect(pair: List<String>): Boolean {
    // check for every parameter to have a value
    if (pair.size != 2 || pair[1].startsWith("-")) {
        println("Error: Parameter ${pair[0]} has no specified value. Exiting now.")
        return false
    }
    // check for every parameter to be in list of acceptable parameters
    if (!(Parameter.values().map { it.par }.contains(pair[0]))) {
        println("Error: Parameter ${pair[0]} is not recognized. Exiting now.")
        return false
    }
    // check they key parameter is integer
    if (pair[0] == KEY.par && pair[1].toIntOrNull() == null) {
        println("Error: Parameter ${pair[0]} must be an integer number. Exiting now.")
        return false
    }
    // check that mode parameter is in list of acceptable operations
    if (pair[0] == MODE.par && !(Operation.values().map { it.op }.contains(pair[1]))) {
        println("Error: Parameter ${pair[0]} must be \"enc\" or \"dec\". Exiting now.")
        return false
    }
    // check that algorithm parameter is in list of acceptable algorithm
    if (pair[0] == ALGORITHM.par && !(Algorithm.values().map { it.type }.contains(pair[1]))) {
        println("Error: Parameter ${pair[0]} must be \"shift\" or \"unicode\". Exiting now.")
        return false
    }
    // check that "in" file exists
    if (pair[0] == IN.par && !(File(pair[1]).exists())) {
        println("Error: File \"${pair[1]}\" does not exist. Exiting now.")
        return false
    }
    return true
}

fun addDefaultParameters(map: MutableMap<String, String>){
    // add default operation
    if (map[MODE.par] == null) map[MODE.par] = "-enc"
    // add default key
    if (map[KEY.par] == null) map[KEY.par] = "0"
    // add default algorithm
    if (map[ALGORITHM.par] == null) map[ALGORITHM.par] = "shift"
    // sets up correct in data
    if (map[DATA.par] == null) {
        if (map[IN.par] == null) {
            map[DATA.par] = ""
        } else map[DATA.par] = File(map[IN.par]!!).readText()
    } else(map[DATA.par]!!.trim('"'))
}

fun getParameterMap(args: Array<String>): Map<String, String> {
    val parameterMap: MutableMap<String, String> = mutableMapOf()
    args.toList().chunked(2).forEach {
        if (!paramListIsCorrect(it))
            exitProcess(0)
        parameterMap[it[0]] = it[1]
    }
    addDefaultParameters(parameterMap)
    return parameterMap
}

fun main(args: Array<String>) {
    val parameterMap: Map<String, String> = getParameterMap(args)
    // translate message
    var translated = ""
    parameterMap[DATA.par]?.forEach { translated += encryptDecrypt(
        it,
        parameterMap[MODE.par]!!,
        parameterMap[KEY.par]!!.toInt(),
        parameterMap[ALGORITHM.par]!!
    )}
    if (parameterMap[OUT.par] == null) {
        println(translated)
    } else {
        try {
            File(parameterMap[OUT.par]!!).writeText(translated)
        } catch (e: Exception) {
            println ("Error: An error has occurred in the attempt to write result in file \"${parameterMap[OUT.par]}\". ")
        } finally {
            exitProcess(0)
        }
    }
}
