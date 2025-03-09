package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.enum.BarnepensjonEosKapEnum
import no.nav.pensjon.simulator.core.domain.regler.kode.BarnepensjonEOSKapittelCti

// Checked 2025-02-28
class Trygdeavtaledetaljer {
    /**
     * Angir om arbeidsinntekten er på minst 1G på Uføre-/dødstidspunktet.
     */
    var arbeidsinntektMinst1G: Boolean? = null

    /**
     * Liste med poengår i avtaleland av typen PoengarManuell
     */
    var poengarListe: List<PoengarManuell> = mutableListOf()

    /**
     * Faktisk trygdetid i andre EØS-land for alternativ pro rata beregning
     */
    var ftt_andreEOSLand: AntallArOgMnd? = null

    /**
     * Faktisk trygdetid garantitillegg
     */
    var ftt_garanti: AntallArOgMnd? = null

    /**
     * Faktisk trygdetid annet nordisk land
     */
    var ftt_annetNordiskLand: AntallArOgMnd? = null

    /**
     * Sum pensjon i andre avtaleland
     */
    var sumPensjonAndreAvtaleland = 0

    /**
     * Inntektsprøvet pensjon fra annet avtaleland
     */
    var inntektsprovetPensjonAvtaleland: Boolean? = null

    /**
     * Art.10 anvendes på grunnpensjon
     */
    var erArt10BruktGP: Boolean? = null

    /**
     * Art.10 anvendes på tilleggspensjon
     */
    var erArt10BruktTP: Boolean? = null

    /**
     * Antall faktiske poengår i annet nordisk land
     */
    var fpa_nordisk = 0

    /**
     * Angir hvilket kapittel (3 eller 8) i forordning 1408/71 barnepensjon skal beregnes etter ved EØS-saker.
     */
    var barnepensjonForordning1408_71: BarnepensjonEOSKapittelCti? = null
    var barnepensjonForordning1408_71Enum: BarnepensjonEosKapEnum? = null

    constructor()

    constructor(source: Trygdeavtaledetaljer) : this() {
        arbeidsinntektMinst1G = source.arbeidsinntektMinst1G
        poengarListe = source.poengarListe.map(::PoengarManuell)
        ftt_andreEOSLand = source.ftt_andreEOSLand?.let(::AntallArOgMnd)
        ftt_garanti = source.ftt_garanti?.let(::AntallArOgMnd)
        ftt_annetNordiskLand = source.ftt_annetNordiskLand?.let(::AntallArOgMnd)
        sumPensjonAndreAvtaleland = source.sumPensjonAndreAvtaleland
        inntektsprovetPensjonAvtaleland = source.inntektsprovetPensjonAvtaleland
        erArt10BruktGP = source.erArt10BruktGP
        erArt10BruktTP = source.erArt10BruktTP
        fpa_nordisk = source.fpa_nordisk
        barnepensjonForordning1408_71 = source.barnepensjonForordning1408_71?.let(::BarnepensjonEOSKapittelCti)
        barnepensjonForordning1408_71Enum = source.barnepensjonForordning1408_71Enum
    }
}
