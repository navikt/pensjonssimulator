package no.nav.pensjon.simulator.alderspensjon.api.nav.direct.acl.v3.result

import no.nav.pensjon.simulator.alderspensjon.alternativ.*
import no.nav.pensjon.simulator.core.beholdning.OpptjeningGrunnlag

/**
 * Maps between data transfer objects (DTOs) and domain objects related to simulering.
 * The DTOs are specified by version 3 of the API offered to clients.
 */
object NavSimuleringResultMapperV3 {

    fun mapNavSimuleringResultV3(source: SimulertPensjonEllerAlternativ?) =
        NavSimuleringResultV3(
            alderspensjon = source?.pensjon?.alderspensjon.orEmpty().map(::alderspensjon),
            afpPrivat = source?.pensjon?.privatAfp.orEmpty().map(::privatAfp),
            afpOffentliglivsvarig = source?.pensjon?.livsvarigOffentligAfp.orEmpty().map(::livsvarigOffentligAfp),
            vilkaarsproeving = vilkaarsproevingResultat(source?.alternativ),
            harNokTrygdetidForGarantipensjon = source?.pensjon?.harNokTrygdetidForGarantipensjon,
            trygdetid = source?.pensjon?.trygdetid ?: 0,
            opptjeningGrunnlagListe = source?.pensjon?.opptjeningGrunnlagListe.orEmpty().map(::opptjeningGrunnlag)
        )

    private fun alderspensjon(source: SimulertAarligAlderspensjon) =
        SimulertAlderspensjonV3(
            alder = source.alderAar,
            beloep = source.beloep,
            inntektspensjon = source.inntektspensjon,
            garantipensjon = source.garantipensjon,
            delingstall = source.delingstall,
            pensjonBeholdningFoerUttak = source.pensjonBeholdningFoerUttak
        )

    private fun privatAfp(source: SimulertPrivatAfp) =
        SimulertPrivatAfpV3(
            alder = source.alderAar,
            beloep = source.beloep
        )

    private fun livsvarigOffentligAfp(source: SimulertLivsvarigOffentligAfp) =
        SimulertLivsvarigOffentligAfpV3(
            alder = source.alderAar,
            beloep = source.beloep
        )

    private fun vilkaarsproevingResultat(source: SimulertAlternativ?) =
        VilkaarsproevingResultatV3(
            vilkaarErOppfylt = source == null,
            alternativ = source?.let(::alternativ)
        )

    private fun opptjeningGrunnlag(source: OpptjeningGrunnlag) =
        SimulatorOpptjeningGrunnlagV3(
            aar = source.aar,
            pensjonsgivendeInntekt = source.pensjonsgivendeInntekt
        )

    private fun alternativ(source: SimulertAlternativ) =
        AlternativtResultatV3(
            gradertUttaksalder = source.gradertUttakAlder?.let(::alder),
            uttaksgrad = source.uttakGrad.value.toInt(),
            heltUttaksalder = alder(source.heltUttakAlder)
        )

    private fun alder(source: SimulertUttakAlder) =
        AlderV3(
            aar = source.alder.aar,
            maaneder = source.alder.maaneder
        )
}
