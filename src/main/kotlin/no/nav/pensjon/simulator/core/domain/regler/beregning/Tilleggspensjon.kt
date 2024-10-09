package no.nav.pensjon.simulator.core.domain.regler.beregning

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BasisTilleggspensjon
import no.nav.pensjon.simulator.core.domain.regler.enum.FormelKodeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.util.formula.Formel
import no.nav.pensjon.simulator.core.domain.regler.util.formula.IFormelProvider

@JsonSubTypes(
    JsonSubTypes.Type(value = BasisTilleggspensjon::class)
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
open class Tilleggspensjon : Ytelseskomponent, IFormelProvider {

    /**
     * Det ordinåre sluttpoengtallet.
     */
    var spt: Sluttpoengtall? = null

    /**
     * Sluttpoengtallet for yrkesskaden.Denne blir utfylt dersom det foreligger
     * yrkesskadegrunnlag i persongrunnlaget. ypt.pt er beregnet på grunnlag av
     * a) tilhørende poengtall (ypt.poengrekke.poengtallListe ) eller b) paa (
     * poeng etter antatt årlig inntekt ). Poengtall-listen er da tom. I alle
     * tilfeller er YPT.pt >= SPT.pt. Det vanligste tilfellet hvor YPT.pt >
     * SPT.pt skyldes yrkessadegrunnlag.antattArligInntekt.
     */
    var ypt: Sluttpoengtall? = null

    /**
     * Sluttpoengtallet for overkompensasjon.
     */
    var opt: Sluttpoengtall? = null

    /**
     * Den skiltes del av avdødes tilleggspensjon. Angis i prosent.
     */
    var skiltesDelAvAdodesTP = 0

    /**
     * Map av formler brukt i beregning av Tilleggspensjon.
     */
    override var formelMap: HashMap<String, Formel> = HashMap()

    override var ytelsekomponentTypeEnum: YtelseskomponentTypeEnum = YtelseskomponentTypeEnum.TP

    constructor() {
        formelKodeEnum = FormelKodeEnum.TPx
    }

    constructor(source: Tilleggspensjon) : super(source) {
        if (source.spt != null) {
            spt = Sluttpoengtall(source.spt!!)
        }

        if (source.ypt != null) {
            ypt = Sluttpoengtall(source.ypt!!)
        }

        if (source.opt != null) {
            opt = Sluttpoengtall(source.opt!!)
        }

        skiltesDelAvAdodesTP = source.skiltesDelAvAdodesTP

        if (source.formelMap.isNotEmpty()) {
            source.formelMap.forEach { (key, value) ->
                formelMap[key] = Formel(value)
            }
        }
    }
}
