package no.nav.pensjon.simulator.tech.json

object Stringifier{

    fun <T> listAsString(list: List<T>?) =
        list?.let { "[${it.joinToString { it.toString() }}]" } ?: "null"

    fun <T> textAsString(text: T?) =
        text?.let { "\"$it\"" } ?: "null"
}
