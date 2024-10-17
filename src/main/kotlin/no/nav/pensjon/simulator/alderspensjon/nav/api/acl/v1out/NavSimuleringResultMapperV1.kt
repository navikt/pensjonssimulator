package no.nav.pensjon.simulator.alderspensjon.nav.api.acl.v1out

import no.nav.pensjon.simulator.alderspensjon.alternativ.*
import no.nav.pensjon.simulator.core.beholdning.OpptjeningGrunnlag

/**
 * Maps between data transfer objects (DTOs) and domain objects related to simulering.
 * The DTOs are specified by version 1 of the API offered to clients.
 */
object NavSimuleringResultMapperV1 {

    fun mapNavSimuleringResultV1(source: SimulertPensjonEllerAlternativ?) =
        NavSimuleringResultV1(
            alderspensjon = source?.pensjon?.alderspensjon.orEmpty().map(NavSimuleringResultMapperV1::alderspensjon),
            afpPrivat = source?.pensjon?.privatAfp.orEmpty().map(NavSimuleringResultMapperV1::privatAfp),
            afpOffentliglivsvarig = source?.pensjon?.livsvarigOffentligAfp.orEmpty().map(NavSimuleringResultMapperV1::livsvarigOffentligAfp),
            vilkaarsproeving = vilkaarsproevingResultat(source?.alternativ),
            harNokTrygdetidForGarantipensjon = source?.pensjon?.harNokTrygdetidForGarantipensjon,
            trygdetid = source?.pensjon?.trygdetid ?: 0,
            opptjeningGrunnlagListe = source?.pensjon?.opptjeningGrunnlagListe.orEmpty().map(NavSimuleringResultMapperV1::opptjeningGrunnlag)
        )

    private fun alderspensjon(source: SimulertAarligAlderspensjon) =
        SimulertAlderspensjonV1(
            alder = source.alderAar,
            beloep = source.beloep,
            inntektspensjon = source.inntektspensjon,
            garantipensjon = source.garantipensjon,
            delingstall = source.delingstall,
            pensjonBeholdningFoerUttak = source.pensjonBeholdningFoerUttak
        )

    private fun privatAfp(source: SimulertPrivatAfp) =
        SimulertPrivatAfpV1(
            alder = source.alderAar,
            beloep = source.beloep
        )

    private fun livsvarigOffentligAfp(source: SimulertLivsvarigOffentligAfp) =
        SimulertLivsvarigOffentligAfpV1(
            alder = source.alderAar,
            beloep = source.beloep
        )

    private fun vilkaarsproevingResultat(source: SimulertAlternativ?) =
        VilkaarsproevingResultatV1(
            vilkaarErOppfylt = source == null,
            alternativ = source?.let(NavSimuleringResultMapperV1::alternativ)
        )

    private fun opptjeningGrunnlag(source: OpptjeningGrunnlag) =
        SimulatorOpptjeningGrunnlagV1(
            aar = source.aar,
            pensjonsgivendeInntekt = source.pensjonsgivendeInntekt
        )

    private fun alternativ(source: SimulertAlternativ) =
        AlternativtResultatV1(
            gradertUttaksalder = source.gradertUttakAlder?.let(NavSimuleringResultMapperV1::alder),
            uttaksgrad = source.uttakGrad.value.toInt(),
            heltUttaksalder = alder(source.heltUttakAlder)
        )

    private fun alder(source: SimulertUttakAlder) =
        AlderV1(
            aar = source.alder.aar,
            maaneder = source.alder.maaneder
        )
}
