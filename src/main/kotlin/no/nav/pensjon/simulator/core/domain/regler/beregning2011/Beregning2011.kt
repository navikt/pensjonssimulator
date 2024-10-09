package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.domain.pensjon.regler.repository.komponent.uforetrygd.koder.BeregningGjelderTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.beregning.BeregningRelasjon
import no.nav.pensjon.simulator.core.domain.regler.enum.BeregningsmetodeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.BeregningtypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.ResultattypeEnum

@JsonSubTypes(
    JsonSubTypes.Type(value = Uforetrygdberegning::class),
    JsonSubTypes.Type(value = AfpPrivatBeregning::class),
    JsonSubTypes.Type(value = AldersberegningKapittel20::class),
    JsonSubTypes.Type(value = AldersberegningKapittel19::class),
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
abstract class Beregning2011 {

    var gjelderPerson: PenPerson? = null
    open var grunnbelop = 0
    var tt_anv = 0
    var resultatTypeEnum: ResultattypeEnum? = null
    var beregningsMetodeEnum: BeregningsmetodeEnum? = null
    var beregningTypeEnum: BeregningtypeEnum? = null
    var delberegning2011Liste: MutableList<BeregningRelasjon> = mutableListOf() // SIMDOM-EDIT Mutable
    var merknadListe: MutableList<Merknad> = mutableListOf()

    val delberegningsListe: MutableList<BeregningRelasjon>
        get() = delberegning2011Liste

    /**
     * Feltet støtter navngivning av beregningen i beregningstreet.
     * Ifbm. Gjenlevendetillegg settes koden avhengig av hvilke grunnlagsroller som beregningen angår.
     */
    var beregningGjelderTypeEnum: BeregningGjelderTypeEnum? = null

    constructor()

    constructor(source: Beregning2011, kopierDelberegning2011Liste: Boolean = true) {
        this.grunnbelop = source.grunnbelop

        if (source.gjelderPerson != null) {
            gjelderPerson = PenPerson(source.gjelderPerson!!.penPersonId)
        }

        tt_anv = source.tt_anv

        if (source.resultatTypeEnum != null) {
            resultatTypeEnum = source.resultatTypeEnum
        }

        if (source.beregningsMetodeEnum != null) {
            beregningsMetodeEnum = source.beregningsMetodeEnum
        }

        if (source.beregningTypeEnum != null) {
            beregningTypeEnum = source.beregningTypeEnum
        }

        if (kopierDelberegning2011Liste) {
            for (r in source.delberegningsListe) {
                delberegningsListe.add(BeregningRelasjon(r))
            }
        }

        for (m in source.merknadListe) {
            merknadListe.add(Merknad(m))
        }

        if (source.beregningGjelderTypeEnum != null) {
            this.beregningGjelderTypeEnum = source.beregningGjelderTypeEnum
        }
    }
}
