package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.beregning.Sertillegg
import no.nav.pensjon.simulator.core.domain.regler.kode.Fravik_19_3Cti
import no.nav.pensjon.simulator.core.domain.regler.kode.PoengtilleggCti
import java.io.Serializable

/**
 * Generell historisk info for en bruker
 */
class GenerellHistorikk(

    var generellHistorikkId: Long = 0,
    /**
     * Koder som sier hvilken trygdetid som skal brukes i beregning når første virk er før 1991.
     * Brukes i utenlandssaker der pensjonisten kan få godskrevet trygdetid opptjent i utlandet før 1967.
     */
    var fravik_19_3: Fravik_19_3Cti? = null,
    /**
     * Gjennomsnittlig uføregrad - EØS.
     */
    var fpp_eos: Double = 0.0,

    var ventetilleggsgrunnlag: Ventetilleggsgrunnlag? = null,

    var poengtillegg: PoengtilleggCti? = null,

    /**
     * Inneholder informasjon ang tidligere EØS beregninger. Brukes ved konvertering til AP.
     */
    var eosEkstra: EosEkstra? = null,

    /**
     * Innholder trygdetidsgarantien for ektefeller som går under gammel lov før 1.1.1991
     */
    var garantiTrygdetid: GarantiTrygdetid? = null,

    /** CR175446 - 1943 konvertert AP1967 til AP2011 */
    /** Særtillegget brukeren hadde på AP1967-ytelsen (settes lik null om han ikke hadde særtillegg) */
    var sertillegg1943kull: Sertillegg? = null,

    /** CR175446 - Gift eller tilsvarende med samme person siden 31.12.2010 */
    var giftFor2011: Boolean = false
) : Serializable {

    constructor(generellHistorikk: GenerellHistorikk) : this() {
        this.generellHistorikkId = generellHistorikk.generellHistorikkId
        if (generellHistorikk.fravik_19_3 != null) {
            this.fravik_19_3 = Fravik_19_3Cti(generellHistorikk.fravik_19_3)
        }
        this.fpp_eos = generellHistorikk.fpp_eos
        if (generellHistorikk.ventetilleggsgrunnlag != null) {
            this.ventetilleggsgrunnlag = Ventetilleggsgrunnlag(generellHistorikk.ventetilleggsgrunnlag!!)
        }
        if (generellHistorikk.poengtillegg != null) {
            this.poengtillegg = PoengtilleggCti(generellHistorikk.poengtillegg)
        }
        if (generellHistorikk.eosEkstra != null) {
            this.eosEkstra = EosEkstra(generellHistorikk.eosEkstra!!)
        }
        if (generellHistorikk.garantiTrygdetid != null) {
            this.garantiTrygdetid = GarantiTrygdetid(generellHistorikk.garantiTrygdetid!!)
        }
        if (generellHistorikk.sertillegg1943kull != null) {
            this.sertillegg1943kull = Sertillegg(generellHistorikk.sertillegg1943kull!!)
        }
        if (generellHistorikk.giftFor2011) {
            this.giftFor2011 = true
        }
    }

    constructor(
        generellHistorikkId: Long,
        fravik_19_3: Fravik_19_3Cti,
        fpp_eos: Double,
        ventetilleggsgrunnlag: Ventetilleggsgrunnlag,
        poengtillegg: PoengtilleggCti,
        eosEkstra: EosEkstra
    ) : this() {
        this.generellHistorikkId = generellHistorikkId
        this.fravik_19_3 = fravik_19_3
        this.fpp_eos = fpp_eos
        this.ventetilleggsgrunnlag = ventetilleggsgrunnlag
        this.poengtillegg = poengtillegg
        this.eosEkstra = eosEkstra
    }
}
