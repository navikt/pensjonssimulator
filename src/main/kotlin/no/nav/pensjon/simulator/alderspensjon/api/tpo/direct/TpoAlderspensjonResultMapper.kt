package no.nav.pensjon.simulator.alderspensjon.api.tpo.direct

import no.nav.pensjon.simulator.alderspensjon.alternativ.SimulertAlderspensjonFraFolketrygden
import no.nav.pensjon.simulator.alderspensjon.alternativ.SimulertAlternativ
import no.nav.pensjon.simulator.alderspensjon.alternativ.SimulertDelytelse
import no.nav.pensjon.simulator.alderspensjon.alternativ.SimulertPensjon
import no.nav.pensjon.simulator.alderspensjon.alternativ.SimulertPensjonEllerAlternativ
import no.nav.pensjon.simulator.alderspensjon.AlderspensjonFraFolketrygden
import no.nav.pensjon.simulator.alderspensjon.AlderspensjonResult
import no.nav.pensjon.simulator.alderspensjon.ForslagVedForLavOpptjening
import no.nav.pensjon.simulator.alderspensjon.GradertUttak
import no.nav.pensjon.simulator.alderspensjon.PensjonDelytelse
import no.nav.pensjon.simulator.alderspensjon.PensjonSimuleringStatus
import no.nav.pensjon.simulator.alderspensjon.PensjonSimuleringStatusKode
import no.nav.pensjon.simulator.alderspensjon.Uttaksgrad
import java.time.LocalDate

/**
 * Maps simuleringsresultat from core domain to TPO result domain.
 * TPO = tjenestepensjonsordning
 */
object TpoAlderspensjonResultMapper {

    fun mapPensjonEllerAlternativ(
        source: SimulertPensjonEllerAlternativ?,
        angittFoersteUttakFom: LocalDate,
        angittAndreUttakFom: LocalDate? // null if not gradert uttak
    ): AlderspensjonResult {
        val pensjon: SimulertPensjon? = source?.pensjon
        val harUttak = pensjon?.harUttak == true
        val alderspensjonFraFolketrygdenListe = pensjon?.alderspensjonFraFolketrygden.orEmpty()

        if (source?.alternativ == null) {
            // Angitt uttak ble innvilget i vilkårsprøvingen
            val alderspensjonListe =
                pickEntriesForFomDatoer(
                    pensjonListe = alderspensjonFraFolketrygdenListe,
                    foersteUttakFom = angittFoersteUttakFom,
                    andreUttakFom = angittAndreUttakFom
                )

            return AlderspensjonResult(
                simuleringSuksess = alderspensjonListe.isNotEmpty(),
                aarsakListeIkkeSuksess = emptyList(), // TODO - will be supported later
                alderspensjon = alderspensjonListe.map(::alderspensjon),
                forslagVedForLavOpptjening = null,
                harUttak = harUttak
            )
        }

        // Non-null alternativ => angitt uttak ble avslått i vilkårsprøvingen
        with(source.alternativ) {
            val gradertUttakFom: LocalDate? = gradertUttakAlder?.uttakDato
            val heltUttakFom: LocalDate = heltUttakAlder.uttakDato
            val foersteUttakFom: LocalDate = gradertUttakFom ?: heltUttakFom
            val andreUttakFom: LocalDate? = if (gradertUttakFom == null) null else heltUttakFom

            val pensjonListe =
                pickEntriesForFomDatoer(
                    pensjonListe = alderspensjonFraFolketrygdenListe,
                    foersteUttakFom = foersteUttakFom,
                    andreUttakFom = andreUttakFom
                )

            return AlderspensjonResult(
                simuleringSuksess = false,
                aarsakListeIkkeSuksess = listOf(utilstrekkeligOpptjening()),
                alderspensjon = pensjonListe.map(::alderspensjon),
                forslagVedForLavOpptjening = forslagVedForLavOpptjening(this),
                harUttak = harUttak
            )
        }
    }

    /**
     * Velger ut elementene i listen som har datoer som matcher foersteUttakFom/andreUttakFom.
     * Det returneres en tom liste hvis uttakFom-datoene ikke finnes i listen.
     * NB: Dette er forskjellig fra den opprinnelige logikken i PEN:
     * I SimulerAlderspensjonResponseV3Converter.getSimulertBeregningsinformasjonForDatoFom
     * inkluderes et tomt object i listen hvis uttakFom-datoene ikke finnes i listen:
     * ".orElse(new SimulertBeregningsinformasjon())"
     */
    private fun pickEntriesForFomDatoer(
        pensjonListe: List<SimulertAlderspensjonFraFolketrygden>,
        foersteUttakFom: LocalDate,
        andreUttakFom: LocalDate?
    ): List<SimulertAlderspensjonFraFolketrygden> {
        val utvalgListe = mutableListOf<SimulertAlderspensjonFraFolketrygden>()
        val datoListe = andreUttakFom?.let { listOf(foersteUttakFom, it) } ?: listOf(foersteUttakFom)

        datoListe.forEach {
            pickForDatoFom(liste = pensjonListe, fom = it)?.let(utvalgListe::add)
        }

        return utvalgListe
    }

    private fun pickForDatoFom(
        liste: List<SimulertAlderspensjonFraFolketrygden>,
        fom: LocalDate
    ): SimulertAlderspensjonFraFolketrygden? =
        liste.firstOrNull { it.datoFom == fom }

    @OptIn(ExperimentalStdlibApi::class)
    private fun alderspensjon(source: SimulertAlderspensjonFraFolketrygden) =
        AlderspensjonFraFolketrygden(
            fom = source.datoFom,
            delytelseListe = source.delytelseListe.map(::delytelse),
            uttaksgrad = Uttaksgrad.entries.firstOrNull { it.prosentsats == source.uttakGrad }
                ?: Uttaksgrad.HUNDRE_PROSENT
        )

    @OptIn(ExperimentalStdlibApi::class)
    private fun forslagVedForLavOpptjening(source: SimulertAlternativ) =
        ForslagVedForLavOpptjening(
            gradertUttak = source.gradertUttakAlder?.let {
                GradertUttak(
                    fom = it.uttakDato,
                    uttaksgrad = Uttaksgrad.entries.firstOrNull { it.prosentsats == source.uttakGrad.value.toInt() }
                        ?: Uttaksgrad.HUNDRE_PROSENT
                )
            },
            heltUttakFom = source.heltUttakAlder.uttakDato
        )

    private fun delytelse(source: SimulertDelytelse) =
        PensjonDelytelse(
            pensjonType = source.type.pensjonType,
            beloep = source.beloep
        )

    private fun utilstrekkeligOpptjening() =
        PensjonSimuleringStatus(
            statusKode = PensjonSimuleringStatusKode.AVSLAG_FOR_LAV_OPPTJENING,
            statusBeskrivelse = "For lav pensjonsopptjening til å kunne starte uttak med angitt dato og grad"
        )
}
