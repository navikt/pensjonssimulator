package no.nav.pensjon.simulator.core.domain.regler

import no.nav.pensjon.simulator.core.domain.regler.grunnlag.AntallArMndDag
import no.nav.pensjon.simulator.core.domain.regler.kode.RegelverkTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.TrygdetidGarantiTypeCti
import java.io.Serializable
import java.util.*

class Trygdetid(
    /**
     * Unik id for objektet, brukes ikke av PREG,og blir med ut uforandret.
     */
    var trygdetidId: Long = 0,

    /**
     * Bestemmer hvilket regelverk objektet gjelder for
     * G_REG - gammelt regelverk
     * N_REG_G_OPPTJ - nytt regelverk, gammel opptjeningsmodell
     * N_REG_G_N_OPPTJ - nytt regelverk, gammel og ny opptjeningsmodell
     * N_REG_N_OPPTJ - nytt regelverk, ny opptjeningsmodell
     */
    var regelverkType: RegelverkTypeCti? = null,

    /**
     * Samlet trygdetid i antall år. Vanligvis lik tt_anv i Beregning. Unntaket
     * er når antall poengår er større. I simulering er dette den eneste
     * trygdetiden som trengs.
     */
    var tt: Int = 0,

    /**
     * Framtidig trygdetid i antall måneder.
     */
    var ftt: Int = 0,

    /**
     * Om framtidig trygdetid er redusert i henhold til 4/5-dels regelen (Ø3-6
     * tredje ledd).
     */
    var ftt_redusert: Boolean = false,

    /**
     * Dato fremtidig trygdetid regnes fra.
     */
    var ftt_fom: Date? = null,

    /**
     * Faktiske trygdetidsmåneder. Brukes etter EØS og land med bilaterale
     * avtaler. Utgjør summen av all faktisk trygdetid i Norge og andre EØS-land
     * eller alternativt det landet vi har bilateral avtale med. Måneder.
     */
    var tt_fa_mnd: Int = 0,

    /**
     * Trygdetid i antall år på grunnlag av poengår i det 67., 68. og 69.
     * leveåret. Godskrives ved fylte 70 år.
     */
    var tt_67_70: Int = 0,

    /**
     * Trygdetid beregnet for poengår opptjent fra og med kalenderåret bruker fylte 6 år
     * til og med kalenderåret bruker fylte 75 år.
     */
    var tt_67_75: Int = 0,

    /**
     * Summen av norsk faktisk trygdetid og eventuelle poengår opptjent fra året fyller 67.
     * Slike poengår vil kun legges til summen dersom vilkår for at de skal kunne telle
     * med er oppfylt. I antall måneder.
     */
    var tt_faktisk: Int = 0,

    /**
     * Trygdetid etter 1966 i antall år.
     */
    var tt_E66: Int = 0,

    /**
     * Trygdetid før 1967 i antall år.
     */
    var tt_F67: Int = 0,

    /**
     * Faktisk trygdetid i antall år, måneder og dager før 2021.
     * Innført ifbm overgangsregler for flyktninger.
     */
    var tt_fa_F2021: AntallArMndDag? = null,

    /**
     * Opptjeningstiden er tidsrommet i antall måneder fra og med måneden etter
     * fylte 16 år til og med måneden før stønadstilfellet inntrødte. Brukes til
     * å bestemme 4/5-dels krav til faktisk trygdetid (Ø3-6 tredje ledd).
     */
    var opptjeningsperiode: Int = 0,

    /**
     * Trygdetid i EØS land unntatt Norge.
     */
    var ttUtlandEos: TTUtlandEOS? = null,

    /**
     * Trygdetid i land tilhørende Nordisk konvensjon (artikkel 10) unntatt
     * Norge.
     */
    var ttUtlandKonvensjon: TTUtlandKonvensjon? = null,

    /**
     * Trygdetid i land med bilaterale avtaler.
     */
    var ttUtlandTrygdeavtaler: MutableList<TTUtlandTrygdeavtale> = mutableListOf(),
    var merknadListe: MutableList<Merknad> = mutableListOf(),
    var garantiType: TrygdetidGarantiTypeCti? = null,

    /**
     * Felt som blir brukt ved proratisering av pensjonsnivå ved
     * vilkårsprøving av tidliguttak av AP
     */
    var prorataTellerVKAP: Int = 0,
    var prorataNevnerVKAP: Int = 0,

    /**
     * Felt som blir brukt for å holde orden på nøyaktig antall år, måneder og dager trygdetid
     * for å unngå avrundingsfeil på grunn av dobbel avrunding.
     */
    var tt_fa: AntallArMndDag? = null,

    /**
     * Trygdetidens virkningsdato fom. Brukes ved fastsettelse av periodisert trygdetid for AP2011/AP2016 og AP2025
     */
    var virkFom: Date? = null,

    /**
     * Trygdetidens virkningsdato tom. Brukes ved fastsettelse av periodisert trygdetid for AP2011/AP2016 og AP2025
     */
    var virkTom: Date? = null,

    ) : Serializable {

    //@JsonGetter fun anvendtFlyktning(): UtfallTypeCti = anvendtFlyktningFaktum.value

    constructor(trygdetid: Trygdetid) : this() {
        this.trygdetidId = trygdetid.trygdetidId
        this.tt = trygdetid.tt
        this.ftt = trygdetid.ftt
        this.ftt_redusert = trygdetid.ftt_redusert
        this.tt_fa_mnd = trygdetid.tt_fa_mnd
        this.tt_67_70 = trygdetid.tt_67_70
        this.tt_67_75 = trygdetid.tt_67_75
        this.tt_E66 = trygdetid.tt_E66
        this.tt_F67 = trygdetid.tt_F67
        this.tt_faktisk = trygdetid.tt_faktisk

        if (trygdetid.tt_fa_F2021 != null) {
            tt_fa_F2021 = AntallArMndDag(trygdetid.tt_fa_F2021!!)
        }
        if (trygdetid.ftt_fom != null) {
            this.ftt_fom = Date(trygdetid.ftt_fom!!.time)
        }
        this.opptjeningsperiode = trygdetid.opptjeningsperiode
        if (trygdetid.regelverkType != null) {
            this.regelverkType = RegelverkTypeCti(trygdetid.regelverkType)
        }
        if (trygdetid.garantiType != null) {
            this.garantiType = TrygdetidGarantiTypeCti(trygdetid.garantiType)
        }
        if (trygdetid.ttUtlandEos != null) {
            this.ttUtlandEos = TTUtlandEOS(trygdetid.ttUtlandEos!!)
        }
        if (trygdetid.ttUtlandKonvensjon != null) {
            this.ttUtlandKonvensjon = TTUtlandKonvensjon(trygdetid.ttUtlandKonvensjon!!)
        }
        this.ttUtlandTrygdeavtaler = mutableListOf()
        for (ta in trygdetid.ttUtlandTrygdeavtaler) {
            this.ttUtlandTrygdeavtaler.add(TTUtlandTrygdeavtale(ta))
        }
        this.merknadListe = mutableListOf()
        for (merknad in trygdetid.merknadListe) {
            this.merknadListe.add(Merknad(merknad))
        }
        this.prorataNevnerVKAP = trygdetid.prorataNevnerVKAP
        this.prorataTellerVKAP = trygdetid.prorataTellerVKAP
        if (trygdetid.virkFom != null) {
            this.virkFom = Date(trygdetid.virkFom!!.time)
        }
        if (trygdetid.virkTom != null) {
            this.virkTom = Date(trygdetid.virkTom!!.time)
        }
        if (trygdetid.tt_fa != null) {
            this.tt_fa = AntallArMndDag(trygdetid.tt_fa!!)
        }
        // SIMDOM-MOD
        //anvendtFlyktningFaktum = Faktum(
        //    trygdetid.anvendtFlyktningFaktum.name,
        //    trygdetid.anvendtFlyktningFaktum.value
        //).apply { children.addAll(trygdetid.anvendtFlyktningFaktum.children) }
    }

    fun tTUtlandTrygdeavtaleListe(): MutableList<TTUtlandTrygdeavtale> {
        return ttUtlandTrygdeavtaler
    }
}
