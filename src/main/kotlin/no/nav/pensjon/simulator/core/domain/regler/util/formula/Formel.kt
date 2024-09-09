package no.nav.pensjon.simulator.core.domain.regler.util.formula

import com.fasterxml.jackson.annotation.JsonIgnore
import no.nav.pensjon.simulator.core.domain.regler.error.InvalidFormulaException
import no.nav.pensjon.simulator.core.domain.regler.util.formula.Builder.Companion.kmath
import no.nav.pensjon.simulator.core.domain.regler.util.formula.Operator.*
import org.apache.commons.lang3.StringUtils
import java.io.Serializable
import kotlin.reflect.KFunction1
import kotlin.reflect.KMutableProperty0

class Formel(
    var emne: String = "", //SIMDOM-MOD to avoid error "no Creators, like default constructor, exist"
    var prefix: String = "",
    var notasjon: String = "",
    var innhold: String = "",
    var expectDoubleResult: Boolean = false
) : Serializable {
    /**
     * Copy constructor
     */
    constructor(formel: Formel) : this(
        emne = formel.emne,
        prefix = formel.prefix,
        notasjon = formel.notasjon,
        innhold = formel.innhold,
        expectDoubleResult = formel.expectDoubleResult
    ) {
        this.locked = formel.locked
        formel.subFormelList.forEach {
            this.subFormelList.add(Formel(it))
        }
        this.expectDoubleResult = formel.expectDoubleResult
        this.targetProperty = null
        this.namedVarMap.putAll(formel.namedVarMap)
    }

    constructor(emne: String, value: Number) : this(
        emne = emne,
        prefix = "",
        notasjon = emne,
        innhold = value.toString(),
        expectDoubleResult = value is Double
    ) {
        namedVarMap[emne] = value
    }

    constructor(value: Number) : this(
        emne = value.toString(),
        prefix = "",
        notasjon = value.toString(),
        innhold = value.toString(),
        expectDoubleResult = value is Double
    )

    val subFormelList = linkedSetOf<Formel>()

    @JsonIgnore
    var locked = false

    @JsonIgnore
    val namedVarMap = mutableMapOf<String, Number>()

    @JsonIgnore
    var targetProperty: KMutableProperty0<out Number>? = null

    fun navn(): String = createNavn()

    private fun createNavn(): String {
        val navn = listOfNotNull(
            prefix,
            emne,
            targetProperty?.name
        ).filter { it.isNotEmpty() }.joinToString(separator = "_")
        return navn.ifBlank { "anonymous#${this.hashCode()}" }
    }
/* SIMDOM-MOD
    val result: Number
        get() {
            val expression = compileExpression(innhold, evalEnv)

            return if (expectDoubleResult)
                expression.evaluate().also { updateTargetProperty(it) }
            else
                expression.evaluate().toInt().also { updateTargetProperty(it) }
        }*/
    val result: Number = 0

    private fun updateTargetProperty(d: Double) = targetProperty?.let { (it as KMutableProperty0<Double>).set(d) }
    private fun updateTargetProperty(i: Int) = targetProperty?.let { (it as KMutableProperty0<Int>).set(i) }

    internal fun expand(operator: Operator, right: Formel): Formel {
        val left = this
        addParanthesisIfNeeded(left, operator, right)

        return Formel(
            emne = left.emne,
            prefix = left.prefix,
            notasjon = "${left.finalNotasjon()}${operator.syntax}${right.finalNotasjon()}",
            innhold = "${left.finalInnhold()}${operator.syntax}${right.finalInnhold()}",
            expectDoubleResult = left.expectDoubleResult || right.expectDoubleResult || operator == DIVIDE
        ).apply {
            if (left.locked) {
                subFormelList.add(left)
            } else {
                subFormelList.addAll(left.subFormelList)
            }
            if (right.locked) {
                subFormelList.add(right)
            } else {
                subFormelList.addAll(right.subFormelList)
            }
            this.emne = "anonymous#${this.hashCode()}"
            this.verifyAndUpdateVarMap(left, right)
        }
    }

    private fun verifyAndUpdateVarMap(left: Formel, right: Formel) {
        if (left.emne == right.emne && left.result != right.result) {
            throw InvalidFormulaException("Formula conflict: '${left.emne}' with value ${left.result} would be reassigned to value ${right.result}")
        }
        if (!left.locked && !right.locked) {
            left.namedVarMap.forEach { (leftName, leftValue) ->
                if (right.namedVarMap.containsKey(leftName) && right.namedVarMap[leftName] != leftValue) {
                    throw InvalidFormulaException("Variable conflict: '$leftName' with value $leftValue would be reassigned to value ${right.namedVarMap[leftName]}")
                }
            }
        }
        /**
         * If a formula ([left] or [right]) is locked, its variables will not be used in resulting formula, only its name will be present.
         */
        if (!left.locked) namedVarMap += left.namedVarMap
        if (!right.locked) namedVarMap += right.namedVarMap
    }

    fun copy(): Formel = Formel(this)

    private fun addParanthesisIfNeeded(left: Formel, operator: Operator, right: Formel) {
        if (operator == TIMES || operator == DIVIDE) {
            checkAndAddParanthesis(left)
            checkAndAddParanthesis(right)
        } else if (operator == MINUS) {
            checkAndAddParanthesis(right)
        }
    }

    private fun checkAndAddParanthesis(expr: Formel) {
        var level = 0
        val plus = PLUS.syntax.trim().first()
        val minus = MINUS.syntax.trim().first()
        var needsPara = false
        expr.notasjon.toCharArray().forEach {
            if (level == 0 && (it == plus || it == minus)) {
                needsPara = true
            } else if (it == '(') {
                level++
            } else if (it == ')')
                level--
        }
        if (needsPara) {
            expr.notasjon = "(${expr.notasjon})"
            expr.innhold = "(${expr.innhold})"
        }
    }

    fun toBuilder(): Builder = kmath().formel(Formel(this)).emne(this.emne)

    fun emne(newEmne: String): Formel = this.apply { emne = newEmne }

    /**
     * TODO Kanskje denne implementasjonen skal erstatte [notasjon] feltet. Det er forvirrende at det finnes to måter å hente ut notasjonen på.
     * Gjelder også [innhold]
     */
    private fun finalNotasjon(): String = if (locked) navn() else notasjon
    private fun finalInnhold(): String = if (locked) result.toString() else innhold

    override fun toString(): String = toTreeString(0, Int.MAX_VALUE)

    fun toString(maxLevel: Int): String = toTreeString(0, maxLevel)

    private fun toTreeString(level: Int, maxLevel: Int): String {
        val s = StringBuilder()
        s.append(StringUtils.repeat(' ', level * 2)).append("Formelnavn: ").append(emne)
            .append("  level: ").append(level)
            .append("  resultat: ").append(result)
            .append("  locked: ").append(locked)
            .append("  ant.subFormler: ").append(subFormelList.size)
            .append("  hash: ").append(this.hashCode()).append("\n")
        s.append(StringUtils.repeat(' ', level * 2)).append("    notasjon:\t\t").append(notasjon).append("\n")
        s.append(StringUtils.repeat(' ', level * 2)).append("    innhold: \t\t").append(innhold)
            .append(" = $result").append("\n")
        s.append(StringUtils.repeat(' ', level * 2)).append("    namedVarMap:  \t").append(namedVarMap.toString())
            .append("\n")
        s.append(StringUtils.repeat(' ', level * 2)).append("    subFormelList:\t")
            .append(subFormelList.map { it.emne }).append("\n")
        if (level < maxLevel) {
            subFormelList.forEach {
                s.append(it.toTreeString(level + 1, maxLevel))
            }
        }
        return s.toString()
    }

    fun toHTML(): String = toTreeHTML(0, Int.MAX_VALUE)
    fun toHTML(maxLevel: Int): String = toTreeHTML(0, maxLevel)

    /**
     *
     */
    fun logUsing(kFunction1: KFunction1<String?, Unit>): Formel {
        this.toHTML(0).split("\n").forEach { kFunction1.invoke(it) }
        return this
    }

    private fun toTreeHTML(level: Int, maxLevel: Int): String {
        val sb = StringBuilder()

        sb.append(StringUtils.repeat(' ', level * 2))
            .append("<formel navn='").append(navn()).append("'")
        targetProperty?.let { sb.append(" felt='").append(it.name).append("'") }
        sb.append(" level='").append(level).append("'")
            .append(" resultat='").append(result).append("'")
            .append(" locked='").append(locked).append("'")
            .append(" antSubFormler='").append(subFormelList.size).append("'>\n")
        sb.append(StringUtils.repeat(' ', level * 2 + 2)).append("<fl>").append(emne).append(" = ").append(notasjon)
            .append("</fl>\n")
        sb.append(StringUtils.repeat(' ', level * 2 + 2)).append("<fl>").append(emne).append(" = ").append(innhold)
            .append("</fl>\n")
        sb.append(StringUtils.repeat(' ', level * 2 + 2)).append("<fl>").append(emne).append(" = ").append(result)
            .append("</fl>\n")

        if (level < maxLevel) {
            subFormelList.forEach {
                sb.append(it.toTreeHTML(level + 1, maxLevel))
            }
        }
        sb.append(StringUtils.repeat(' ', level * 2)).append("</formel>\n")
        return sb.toString()
    }

}

class Builder {
    companion object {
        fun kmath() = Builder()
    }

    private var bEmne: String = ""
    private var bProvider: FormelProvider? = null
    private var bLock: Boolean = true
    private var bKFormel: Formel? = null
    private var bProperty: KMutableProperty0<out Number>? = null

    fun formel(kFormel: Formel) = apply { this.bKFormel = kFormel }
    fun emne(emne: String) = apply { this.bEmne = emne }
    fun provider(provider: FormelProvider) = apply { this.bProvider = provider }
    fun unlock() = apply { this.bLock = false }
    fun felt(property: KMutableProperty0<out Number>) = apply { this.bProperty = property }
    fun build(): Formel {
        val prettyEmne = bEmne.trim().replace(" ", "_")
        val formel = bKFormel ?: defaultbKFormel()
        validateState(prettyEmne, formel)
        return formel.apply {
            emne = prettyEmne
            bProvider?.let {
                prefix = it.formelPrefix()
                this.targetProperty = bProperty
                this.expectDoubleResult = bProperty?.returnType?.classifier == Double::class
                it.add(this)
            }
            locked = bLock
            this.result
        }
    }

    private fun defaultbKFormel(): Formel {
        return if (bProperty?.returnType?.classifier == Double::class) {
            Formel(emne = "null", prefix = "", notasjon = "0.0", innhold = "0.0", true)
        } else {
            Formel(emne = "null", prefix = "", notasjon = "0", innhold = "0", false)
        }
    }

    private fun validateState(prettyName: String, formel: Formel) {
        if (bProperty != null && bProvider == null) {
            throw InvalidFormulaException("Illegal state. IKMathProvider must be set when using targetProperty with .felt(..).")
        }
        if (formel.namedVarMap.containsKey(prettyName)) {
            throw InvalidFormulaException("Illegal circular reference. Formula name $prettyName cannot contain named variables of same name.")
        }
        if (formel.expectDoubleResult && bProperty?.returnType?.classifier == Int::class) {
            throw InvalidFormulaException("Illegal type. The floating-point literal does not conform to the expected type Int.")
        }
        if (!formel.expectDoubleResult && bProperty?.returnType?.classifier == Double::class) {
            throw InvalidFormulaException("Illegal type. The integer literal does not conform to the expected type Double.")
        }
    }
}
