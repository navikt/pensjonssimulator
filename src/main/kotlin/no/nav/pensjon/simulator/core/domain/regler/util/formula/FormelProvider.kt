package no.nav.pensjon.simulator.core.domain.regler.util.formula

import no.nav.pensjon.simulator.core.domain.regler.error.InvalidFormulaException
import kotlin.reflect.KMutableProperty0

interface FormelProvider {
    fun formelPrefix(): String
    val formelMap: HashMap<String, Formel>

    fun findByEmne(emne: String): Formel? = formelMap.values.find { it.emne == emne }
    fun findByEmneOrDefault(targetEmne: String): Formel = formelMap.values.find { f -> f.emne == targetEmne } ?: Builder.kmath().emne(targetEmne).build()
    fun findByProperty(propertyToFind:KMutableProperty0<out Number>): Formel? = formelMap[propertyToFind.name]
    fun findByPropertyOrDefault(propertyToFind:KMutableProperty0<out Number>): Formel {
        return formelMap[propertyToFind.name]
            ?: Formel(emne = "", prefix = "", notasjon = propertyToFind.name, innhold = propertyToFind.get().toString())
                .apply {
                    targetProperty = propertyToFind
                    expectDoubleResult = propertyToFind.get() is Double
                }
    }

    fun reset() = formelMap.clear()

    fun add(formelToAdd: Formel) {
        if (formelToAdd.targetProperty == null) throw InvalidFormulaException("Formel '${formelToAdd.navn()}' must have targetProperty when added to provider.")
        formelMap[formelToAdd.targetProperty!!.name] = formelToAdd
    }

    fun debugFormel(): String {
        val sb = StringBuilder("FormelProvider  class: ${this::class.simpleName} prefix: ${formelPrefix()}\n")
        if (formelMap.isNotEmpty()) {
            formelMap.forEach { (k, v) ->
                sb.append("[$k]").append(v.toHTML(0))
                if (k != v.targetProperty?.name) sb.append("### ERROR ### $k != targetProperty.name")
            }
        } else {
            sb.append("\t").append("empty provider.")
        }
        return sb.toString()
    }
}
