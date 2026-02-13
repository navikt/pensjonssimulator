package no.nav.pensjon.simulator.api.nav.v1.acl.result

import no.nav.pensjon.simulator.alderspensjon.Uttaksgrad
import no.nav.pensjon.simulator.alderspensjon.alternativ.*
import no.nav.pensjon.simulator.api.nav.v1.acl.UttaksgradDto
import no.nav.pensjon.simulator.opptjening.OpptjeningGrunnlag
import no.nav.pensjon.simulator.trygdetid.Trygdetid
import no.nav.pensjon.simulator.validity.Problem

/**
 * Maps between data transfer objects (DTOs) and domain objects related to simulering.
 */
object SimuleringResultMapper {

    fun toDto(source: SimulertPensjonEllerAlternativ?): SimuleringResultDto {
        val pensjon: SimulertPensjon? = source?.pensjon

        return SimuleringResultDto(
            alderspensjonListe = pensjon?.alderspensjon.orEmpty().map(::alderspensjon),
            alderspensjonMaanedsbeloep = maanedsbeloep(pensjon?.alderspensjonFraFolketrygden.orEmpty()),
            livsvarigOffentligAfpListe = pensjon?.livsvarigOffentligAfp.orEmpty().map(::livsvarigOffentligAfp),
            tidsbegrensetOffentligAfp = pensjon?.pre2025OffentligAfp?.let(::tidsbegrensetOffentligAfp),
            privatAfpListe = pensjon?.privatAfp.orEmpty().map(::privatAfp),
            primaerTrygdetid = pensjon?.primaerTrygdetid?.let(::trygdetid),
            vilkaarsproevingsresultat = vilkaarsproevingsresultat(source?.alternativ),
            pensjonsgivendeInntektListe = pensjon?.opptjeningGrunnlagListe.orEmpty().map(::opptjeningGrunnlag),
            problem = source?.problem?.let(::problem)
        )
    }

    private fun alderspensjon(source: SimulertAarligAlderspensjon) =
        AlderspensjonDto(
            alderAar = source.alderAar,
            beloep = source.beloep,
            inntektspensjon = source.inntektspensjon,
            garantipensjon = source.garantipensjon,
            delingstall = source.delingstall,
            pensjonsbeholdningFoerUttak = source.pensjonBeholdningFoerUttak,
            sluttpoengtall = source.sluttpoengtall,
            poengaarFoer92 = source.poengaarFoer92,
            poengaarEtter91 = source.poengaarEtter91,
            forholdstall = source.forholdstall,
            grunnpensjon = source.grunnpensjon,
            tilleggspensjon = source.tilleggspensjon,
            pensjonstillegg = source.pensjonstillegg,
            skjermingstillegg = source.skjermingstillegg,
            kapittel19Pensjon = Kapittel19PensjonDto(
                andelsbroek = source.andelsbroekKap19,
                trygdetidAntallAar = source.trygdetidKap19 ?: 0,
                gjenlevendetillegg = source.kapittel19Gjenlevendetillegg
            ),
            kapittel20Pensjon = Kapittel20PensjonDto(
                andelsbroek = source.andelsbroekKap20,
                trygdetidAntallAar = source.trygdetidKap20 ?: 0
            )
        )

    private fun maanedsbeloep(source: List<SimulertAlderspensjonFraFolketrygden>) =
        UttaksbeloepDto(
            gradertUttakBeloep = source.firstOrNull(::erGradert)?.maanedligBeloep,
            heltUttakBeloep = source.firstOrNull(::erHel)?.maanedligBeloep ?: 0
        )

    private fun tidsbegrensetOffentligAfp(source: SimulertPre2025OffentligAfp) =
        TidsbegrensetOffentligAfpDto(
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
            erAvkortet = source.afpAvkortetTil70Prosent
        )

    private fun privatAfp(source: SimulertPrivatAfp) =
        PrivatAfpDto(
            alderAar = source.alderAar,
            beloep = source.beloep,
            kompensasjonstillegg = source.kompensasjonstillegg,
            kronetillegg = source.kronetillegg,
            livsvarig = source.livsvarig,
            maanedligBeloep = source.maanedligBeloep
        )

    private fun livsvarigOffentligAfp(source: SimulertLivsvarigOffentligAfp) =
        AldersbestemtUtbetalingDto(
            alderAar = source.alderAar,
            beloep = source.beloep,
            maanedligBeloep = source.maanedligBeloep
        )

    private fun trygdetid(source: Trygdetid) =
        TrygdetidDto(
            antallAar = source.kapittel19.coerceAtLeast(source.kapittel20),
            erUtilstrekkelig = source.erTilstrekkelig.not()
        )

    private fun vilkaarsproevingsresultat(source: SimulertAlternativ?) =
        VilkaarsproevingsresultatDto(
            erInnvilget = source == null,
            alternativ = source?.let(::alternativ)
        )

    private fun opptjeningGrunnlag(source: OpptjeningGrunnlag) =
        AarligBeloepDto(
            aarstall = source.aar,
            beloep = source.pensjonsgivendeInntekt
        )

    private fun alternativ(source: SimulertAlternativ) =
        if (source.resultStatus == SimulatorResultStatus.NONE)
            null
        else
            UttaksparametreDto(
                gradertUttakAlder = source.gradertUttakAlder?.let(::alder),
                uttaksgrad = UttaksgradDto.fromInternalValue(source.uttakGrad),
                heltUttakAlder = alder(source.heltUttakAlder)
            )

    private fun alder(source: SimulertUttakAlder) =
        AlderDto(
            aar = source.alder.aar,
            maaneder = source.alder.maaneder
        )

    private fun erGradert(pensjon: SimulertAlderspensjonFraFolketrygden): Boolean =
        erHel(pensjon).not()

    private fun erHel(pensjon: SimulertAlderspensjonFraFolketrygden): Boolean =
        pensjon.uttakGrad == Uttaksgrad.HUNDRE_PROSENT.prosentsats

    private fun problem(source: Problem) =
        ProblemDto(
            kode = ProblemTypeDto.entries.firstOrNull { it.internalValue == source.type } ?: ProblemTypeDto.SERVERFEIL,
            beskrivelse = source.beskrivelse
        )
}
