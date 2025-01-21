package no.nav.pensjon.simulator.alderspensjon.api.nav.direct.acl.v3.result

import no.nav.pensjon.simulator.alderspensjon.alternativ.*
import no.nav.pensjon.simulator.core.beholdning.OpptjeningGrunnlag

/**
 * Maps between data transfer objects (DTOs) and domain objects related to simulering.
 * The DTOs are specified by version 3 of the API offered to clients.
 */
object NavSimuleringResultMapperV3 {

    fun toDto(source: SimulertPensjonEllerAlternativ?) =
        NavSimuleringResultV3(
            alderspensjonListe = source?.pensjon?.alderspensjon.orEmpty().map(::alderspensjon),
            alderspensjonMaanedsbeloep = maanedsbeloep(source?.pensjon?.alderspensjonFraFolketrygden.orEmpty()),
            privatAfpListe = source?.pensjon?.privatAfp.orEmpty().map(::privatAfp),
            livsvarigOffentligAfpListe = source?.pensjon?.livsvarigOffentligAfp.orEmpty().map(::livsvarigOffentligAfp),
            vilkaarsproeving = vilkaarsproevingResultat(source?.alternativ),
            tilstrekkeligTrygdetidForGarantipensjon = source?.pensjon?.harNokTrygdetidForGarantipensjon,
            trygdetid = source?.pensjon?.trygdetid ?: 0,
            opptjeningGrunnlagListe = source?.pensjon?.opptjeningGrunnlagListe.orEmpty().map(::opptjeningGrunnlag)
        )

    private fun alderspensjon(source: SimulertAarligAlderspensjon) =
        NavAlderspensjonV3(
            alderAar = source.alderAar,
            beloep = source.beloep,
            inntektspensjon = source.inntektspensjon,
            garantipensjon = source.garantipensjon,
            delingstall = source.delingstall,
            pensjonBeholdningFoerUttak = source.pensjonBeholdningFoerUttak
        )

    private fun maanedsbeloep(source: List<SimulertAlderspensjonFraFolketrygden>) =
        NavMaanedsbeloepV3(
            gradertUttakBeloep = source.firstOrNull { it.uttakGrad != 100 }?.maanedligBeloep,
            heltUttakBeloep = source.firstOrNull { it.uttakGrad == 100 }?.maanedligBeloep ?: 0
        )

    private fun privatAfp(source: SimulertPrivatAfp) =
        NavPrivatAfpV3(
            alderAar = source.alderAar,
            beloep = source.beloep
        )

    private fun livsvarigOffentligAfp(source: SimulertLivsvarigOffentligAfp) =
        NavLivsvarigOffentligAfpV3(
            alderAar = source.alderAar,
            beloep = source.beloep
        )

    private fun vilkaarsproevingResultat(source: SimulertAlternativ?) =
        NavVilkaarsproevingResultatV3(
            vilkaarErOppfylt = source == null,
            alternativ = source?.let(::alternativ)
        )

    private fun opptjeningGrunnlag(source: OpptjeningGrunnlag) =
        NavOpptjeningGrunnlagV3(
            aar = source.aar,
            pensjonsgivendeInntektBeloep = source.pensjonsgivendeInntekt
        )

    private fun alternativ(source: SimulertAlternativ) =
        NavAlternativtResultatV3(
            gradertUttakAlder = source.gradertUttakAlder?.let(::alder),
            uttaksgrad = source.uttakGrad.value.toInt(),
            heltUttakAlder = alder(source.heltUttakAlder)
        )

    private fun alder(source: SimulertUttakAlder) =
        NavAlderV3(
            aar = source.alder.aar,
            maaneder = source.alder.maaneder
        )
}
