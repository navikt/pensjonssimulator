package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import java.io.Serializable
import java.util.*

class BarnDetalj(
    /**
     * Angir barnets andre forelder enn søker.
     */
    var annenForelder: PenPerson? = null,
    /**
     * Angir om barnet bor med en begge foreldre.
     * "true" betyr fellesbarn, "false" betyr særkullsbarn.
     */
    var borMedBeggeForeldre: Boolean = false,
    /**
     * Fra-og-med dato for når barnet bor sammen med begge foreldrene.
     */
    var borFomDato: Date? = null,
    /**
     * Til-og-med dato for når barnet bor sammen med begge foreldrene.
     */
    var borTomDato: Date? = null,

    /**
     * Angir om barnet har hatt inntekt over 1G.
     */
    var inntektOver1G: Boolean = false,
    /**
     * Angir hvorvidt barnet er under utdanning.
     */
    var underUtdanning: Boolean = false
) : Serializable {
    constructor(barnDetalj: BarnDetalj) : this() {
        if (barnDetalj.annenForelder != null) {
            this.annenForelder = PenPerson(barnDetalj.annenForelder!!)
        }
        this.borMedBeggeForeldre = barnDetalj.borMedBeggeForeldre
        if (barnDetalj.borFomDato != null) {
            this.borFomDato = barnDetalj.borFomDato!!.clone() as Date
        }
        if (barnDetalj.borTomDato != null) {
            this.borTomDato = barnDetalj.borTomDato!!.clone() as Date
        }
        this.inntektOver1G = barnDetalj.inntektOver1G
        this.underUtdanning = barnDetalj.underUtdanning
    }

    override fun toString(): String {
        val TAB = "    "

        val retValue = StringBuilder()

        retValue.append("BarnDetalj ( ").append(super.toString()).append(TAB).append("annenForelder = ")
            .append(annenForelder).append(TAB).append("borMedBeggeForeldre = ")
            .append(borMedBeggeForeldre).append(TAB).append("borFomDato = ").append(borFomDato).append(TAB)
            .append("borTomDato = ").append(borTomDato).append(TAB)
            .append("inntektOver1G = ").append(inntektOver1G).append(TAB).append("underUtdanning = ")
            .append(underUtdanning).append(TAB).append(" )")

        return retValue.toString()
    }
}
