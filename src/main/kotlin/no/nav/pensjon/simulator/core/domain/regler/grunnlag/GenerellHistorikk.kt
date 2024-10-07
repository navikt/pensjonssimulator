package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.beregning.Sertillegg
import no.nav.pensjon.simulator.core.domain.regler.enum.Fravik_19_3_Enum
import no.nav.pensjon.simulator.core.domain.regler.enum.PoengtilleggEnum

/**
 * Generell historisk info for en bruker
 */
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
        this.generellHistorikkId = source.generellHistorikkId

        if (source.fravik_19_3Enum != null) {
            this.fravik_19_3Enum = source.fravik_19_3Enum
        }

        this.fpp_eos = source.fpp_eos

        if (source.ventetilleggsgrunnlag != null) {
            this.ventetilleggsgrunnlag = Ventetilleggsgrunnlag(source.ventetilleggsgrunnlag!!)
        }

        if (source.poengtilleggEnum != null) {
            this.poengtilleggEnum = source.poengtilleggEnum
        }

        if (source.eosEkstra != null) {
            this.eosEkstra = EosEkstra(source.eosEkstra!!)
        }

        if (source.garantiTrygdetid != null) {
            this.garantiTrygdetid = GarantiTrygdetid(source.garantiTrygdetid!!)
        }

        if (source.sertillegg1943kull != null) {
            this.sertillegg1943kull = Sertillegg(source.sertillegg1943kull!!)
        }

        if (source.giftFor2011) {
            this.giftFor2011 = true
        }
    }
}
