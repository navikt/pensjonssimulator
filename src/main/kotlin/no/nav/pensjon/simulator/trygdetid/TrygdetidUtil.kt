package no.nav.pensjon.simulator.trygdetid

import no.nav.pensjon.simulator.core.domain.regler.enum.KravlinjeTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.to.TrygdetidRequest
import java.time.LocalDate
import java.util.SortedSet

object TrygdetidUtil {

    const val MINIMUM_TRYGDETID_ANTALL_AAR: Int = 3
    const val FULL_TRYGDETID_ANTALL_AAR = 40

    fun antallAarMedOpptjening(
        registrerteAarMedOpptjening: SortedSet<Int>,
        aarSoekerFikkMinstealderForTrygdetid: Int,
        dagensDato: LocalDate
    ): Int {
        if (registrerteAarMedOpptjening.isEmpty()) return 0

        val forrigeAar = dagensDato.year - 1

        return if (aarSoekerFikkMinstealderForTrygdetid > forrigeAar)
            0
        else
            registrerteAarMedOpptjening.subSet(aarSoekerFikkMinstealderForTrygdetid, forrigeAar).size
    }

    fun trygdetidSpec(
        kravhode: Kravhode,
        persongrunnlag: Persongrunnlag,
        knekkpunktDato: LocalDate,
        soekerFoersteVirkningFom: LocalDate?, // nullable (e.g. for avdød)
        ytelseType: KravlinjeTypeEnum,
        boddEllerArbeidetUtenlands: Boolean
    ) =
        TrygdetidRequest().apply {
            this.virkFomLd = knekkpunktDato
            this.brukerForsteVirkLd = soekerFoersteVirkningFom
            this.hovedKravlinjeType = ytelseType
            this.persongrunnlag = persongrunnlag
            this.boddEllerArbeidetIUtlandet = boddEllerArbeidetUtenlands
            this.regelverkTypeEnum = kravhode.regelverkTypeEnum
            this.uttaksgradListe = kravhode.uttaksgradListe
        }
}