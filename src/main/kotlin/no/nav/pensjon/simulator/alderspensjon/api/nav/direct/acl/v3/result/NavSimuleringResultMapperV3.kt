package no.nav.pensjon.simulator.alderspensjon.api.nav.direct.acl.v3.result

import no.nav.pensjon.simulator.alderspensjon.Uttaksgrad
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
            pre2025OffentligAfp = source?.pensjon?.pre2025OffentligAfp?.let(::pre2025OffentligAfp),
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
            pensjonBeholdningFoerUttak = source.pensjonBeholdningFoerUttak,
            andelsbroekKap19 = source.andelsbroekKap19,
            andelsbroekKap20 = source.andelsbroekKap20,
            sluttpoengtall = source.sluttpoengtall,
            trygdetidKap19 = source.trygdetidKap19,
            trygdetidKap20 = source.trygdetidKap20,
            poengaarFoer92 = source.poengaarFoer92,
            poengaarEtter91 = source.poengaarEtter91,
            forholdstall = source.forholdstall,
            grunnpensjon = source.grunnpensjon,
            tilleggspensjon = source.tilleggspensjon,
            pensjonstillegg = source.pensjonstillegg,
            skjermingstillegg = source.skjermingstillegg,
            kapittel19Gjenlevendetillegg = source.kapittel19Gjenlevendetillegg
        )

    private fun maanedsbeloep(source: List<SimulertAlderspensjonFraFolketrygden>) =
        NavMaanedsbeloepV3(
            gradertUttakBeloep = source.firstOrNull(::erGradert)?.maanedligBeloep,
            heltUttakBeloep = source.firstOrNull(::erHel)?.maanedligBeloep ?: 0
        )

    private fun pre2025OffentligAfp(source: SimulertPre2025OffentligAfp) =
        NavPre2025OffentligAfp(
            alderAar = source.alderAar,
            totaltAfpBeloep = source.totaltAfpBeloep,
            tidligereArbeidsinntekt = source.tidligereArbeidsinntekt,
            grunnbeloep = source.grunnbeloep,
            sluttpoengtall = source.sluttpoengtall,
            trygdetid = source.trygdetid,
            poengaarTom1991 = source.poengaarTom1991,
            poengaarFom1992 = source.poengaarFom1992,
            grunnpensjon = source.grunnpensjon,
            tilleggspensjon = source.tilleggspensjon,
            afpTillegg = source.afpTillegg,
            saertillegg = source.saertillegg,
            afpGrad = source.afpGrad,
            afpAvkortetTil70Prosent = source.afpAvkortetTil70Prosent
        )

    private fun privatAfp(source: SimulertPrivatAfp) =
        NavPrivatAfpV3(
            alderAar = source.alderAar,
            beloep = source.beloep,
            kompensasjonstillegg = source.kompensasjonstillegg,
            kronetillegg = source.kronetillegg,
            livsvarig = source.livsvarig,
            maanedligBeloep = source.maanedligBeloep
        )

    private fun livsvarigOffentligAfp(source: SimulertLivsvarigOffentligAfp) =
        NavLivsvarigOffentligAfpV3(
            alderAar = source.alderAar,
            beloep = source.beloep,
            maanedligBeloep = source.maanedligBeloep
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
        if (source.resultStatus == SimulatorResultStatus.NONE)
            null
        else
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

    private fun erGradert(pensjon: SimulertAlderspensjonFraFolketrygden): Boolean =
        erHel(pensjon).not()

    private fun erHel(pensjon: SimulertAlderspensjonFraFolketrygden): Boolean =
        pensjon.uttakGrad == Uttaksgrad.HUNDRE_PROSENT.prosentsats
}
