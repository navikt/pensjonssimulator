package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.kode.BarnepensjonEOSKapittelCti
import java.io.Serializable

class Trygdeavtaledetaljer(
    /**
     * Angir om arbeidsinntekten er på minst 1G på uføre-/dødstidspunktet.
     */
    var arbeidsinntektMinst1G: Boolean = false,
    /**
     * Liste med poengår i avtaleland av typen PoengarManuell
     */
    var poengarListe: MutableList<PoengarManuell> = mutableListOf(),
    /**
     * Faktisk trygdetid i andre EØS-land for alternativ pro rata beregning
     */
    var ftt_andreEOSLand: AntallArOgMnd? = null,
    /**
     * Faktisk trygdetid garantitillegg
     */
    var ftt_garanti: AntallArOgMnd? = null,
    /**
     * Faktisk trygdetid annet nordisk land
     */
    var ftt_annetNordiskLand: AntallArOgMnd? = null,
    /**
     * Sum pensjon i andre avtaleland
     */
    var sumPensjonAndreAvtaleland: Int = 0,
    /**
     * Inntektsprøvet pensjon fra annet avtaleland
     */
    var inntektsprovetPensjonAvtaleland: Boolean = false,
    /**
     * Art.10 anvendes på grunnpensjon
     */
    var erArt10BruktGP: Boolean = false,
    /**
     * Art.10 anvendes på tilleggspensjon
     */
    var erArt10BruktTP: Boolean? = null,
    /**
     * Antall faktiske poengår i annet nordisk land
     */
    var fpa_nordisk: Int = 0,
    /**
     * Angir hvilket kapittel (3 eller 8) i forordning 1408/71 barnepensjon skal beregnes etter ved EØS-saker.
     */
    var barnepensjonForordning1408_71: BarnepensjonEOSKapittelCti? = null
) : Serializable {

    constructor(trygdeavtaledetaljer: Trygdeavtaledetaljer) : this() {
        this.arbeidsinntektMinst1G = trygdeavtaledetaljer.arbeidsinntektMinst1G
        for (poengarManuell in trygdeavtaledetaljer.poengarListe) {
            this.poengarListe.add(PoengarManuell(poengarManuell))
        }
        if (trygdeavtaledetaljer.ftt_andreEOSLand != null) {
            this.ftt_andreEOSLand = AntallArOgMnd(trygdeavtaledetaljer.ftt_andreEOSLand!!)
        }
        if (trygdeavtaledetaljer.ftt_garanti != null) {
            this.ftt_garanti = AntallArOgMnd(trygdeavtaledetaljer.ftt_garanti!!)
        }
        if (trygdeavtaledetaljer.ftt_annetNordiskLand != null) {
            this.ftt_annetNordiskLand = AntallArOgMnd(trygdeavtaledetaljer.ftt_annetNordiskLand!!)
        }
        this.sumPensjonAndreAvtaleland = trygdeavtaledetaljer.sumPensjonAndreAvtaleland
        this.inntektsprovetPensjonAvtaleland = trygdeavtaledetaljer.inntektsprovetPensjonAvtaleland
        this.erArt10BruktGP = trygdeavtaledetaljer.erArt10BruktGP
        if (trygdeavtaledetaljer.erArt10BruktTP != null) {
            this.erArt10BruktTP = trygdeavtaledetaljer.erArt10BruktTP
        }
        this.fpa_nordisk = trygdeavtaledetaljer.fpa_nordisk
        if (trygdeavtaledetaljer.barnepensjonForordning1408_71 != null) {
            this.barnepensjonForordning1408_71 =
                BarnepensjonEOSKapittelCti(trygdeavtaledetaljer.barnepensjonForordning1408_71)
        }
    }
}
