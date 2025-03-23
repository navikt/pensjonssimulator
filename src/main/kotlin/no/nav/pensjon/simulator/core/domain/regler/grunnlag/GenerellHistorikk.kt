package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.beregning.Sertillegg
import no.nav.pensjon.simulator.core.domain.regler.enum.Fravik_19_3_Enum
import no.nav.pensjon.simulator.core.domain.regler.enum.PoengtilleggEnum
import no.nav.pensjon.simulator.core.domain.reglerextend.beregning.copy
import no.nav.pensjon.simulator.core.domain.reglerextend.grunnlag.copy

/**
 * Generell historisk info for en bruker
 */
// Checked 2025-02-28
class GenerellHistorikk {

    /**
     * Identifikasjon av den generelle historikken
     */
    var generellHistorikkId: Long? = null

    /**
     * Koder som sier hvilken trygdetid som skal brukes i beregning når Første virk er før 1991.
     * Brukes i utenlandssaker der pensjonisten kan få godskrevet trygdetid opptjent i utlandet før 1967.
     */
    var fravik_19_3Enum: Fravik_19_3_Enum? = null

    /**
     * Gjennomsnittlig Uføregrad - EØS.
     */
    var fpp_eos: Double? = null

    /**
     * Ventetilleggsgrunnlag
     */
    var ventetilleggsgrunnlag: Ventetilleggsgrunnlag? = null

    var poengtilleggEnum: PoengtilleggEnum? = null

    /**
     * Inneholder informasjon ang tidligere EØS beregninger. Brukes ved konvertering til AP.
     */
    var eosEkstra: EosEkstra? = null

    /**
     * Innholder trygdetidsgarantien for ektefeller som går under gammel lov før 1.1.1991
     */
    var garantiTrygdetid: GarantiTrygdetid? = null

    /**
     * 1943 konvertert AP1967 til AP2011.
     * Særtillegget brukeren hadde på AP1967-ytelsen (settes lik null om han ikke hadde særtillegg).
     */
    var sertillegg1943kull: Sertillegg? = null

    /**
     * Gift eller tilsvarende med samme person siden 31.12.2010.
     */
    var giftFor2011 = false

    constructor()

    constructor(source: GenerellHistorikk) : this() {
        generellHistorikkId = source.generellHistorikkId
        fravik_19_3Enum = source.fravik_19_3Enum
        fpp_eos = source.fpp_eos
        ventetilleggsgrunnlag = source.ventetilleggsgrunnlag?.let(::Ventetilleggsgrunnlag)
        poengtilleggEnum = source.poengtilleggEnum
        eosEkstra = source.eosEkstra?.copy()
        garantiTrygdetid = source.garantiTrygdetid?.let(::GarantiTrygdetid)
        sertillegg1943kull = source.sertillegg1943kull?.copy()
        giftFor2011 = source.giftFor2011
    }
}
