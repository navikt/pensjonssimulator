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

// 2025-03-18
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
    var delberegning2011Liste: List<BeregningRelasjon> = mutableListOf()
    var merknadListe: List<Merknad> = mutableListOf()

    val delberegningsListe: List<BeregningRelasjon>
        get() = delberegning2011Liste

    /**
     * Feltet støtter navngivning av beregningen i beregningstreet.
     * Ifbm. Gjenlevendetillegg settes koden avhengig av hvilke grunnlagsroller som beregningen angår.
     */
    var beregningGjelderTypeEnum: BeregningGjelderTypeEnum? = null
}
