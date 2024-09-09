package no.nav.pensjon.simulator.core

import no.nav.pensjon.simulator.core.domain.Avdoed
import no.nav.pensjon.simulator.core.domain.SimuleringType
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.afp.AfpOrdningType
import no.nav.pensjon.simulator.core.krav.FremtidigInntekt
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.trygd.UtlandPeriode
import no.nav.pensjon.simulator.person.Pid
import java.time.LocalDate

// no.nav.domain.pensjon.kjerne.simulering.SimuleringEtter2011
data class SimuleringSpec(
    val type: SimuleringType,
    val sivilstatus: SivilstatusType,
    var epsHarPensjon: Boolean,
    val foersteUttakDato: LocalDate?,
    var heltUttakDato: LocalDate?, // NB var
    val pid: Pid?, // null for forenklet simulering
    val avdoed: Avdoed?,
    val isTpOrigSimulering: Boolean,
    var simulerForTp: Boolean,
    val uttakGrad: UttakGradKode,
    val forventetInntektBeloep: Int,
    val inntektUnderGradertUttakBeloep: Int,
    val inntektEtterHeltUttakBeloep: Int,
    val inntektEtterHeltUttakAntallAar: Int?,
    val foedselAar: Int,
    val boddUtenlands: Boolean,
    val utlandAntallAar: Int,
    val utlandPeriodeListe: MutableList<UtlandPeriode>,
    val fremtidigInntektListe: MutableList<FremtidigInntekt>,
    val inntektOver1GAntallAar: Int,
    val flyktning: Boolean?,
    val epsHarInntektOver2G: Boolean,
    val rettTilOffentligAfpFom: LocalDate?,
    val afpOrdning: AfpOrdningType? = null, // Hvilken AFP-ordning bruker er tilknyttet (kun for simulering av pre-2025 offentlig AFP)
    val afpInntektMaanedFoerUttak: Int? = null, // Brukers inntekt måneden før uttak av AFP (kun for simulering av pre-2025 offentlig AFP)
    var erAnonym: Boolean
) {

    fun withUttak(
        foersteUttakDato: LocalDate?,
        uttakGrad: UttakGradKode,
        heltUttakDato: LocalDate?,
        inntektEtterHeltUttakAntallAar: Int?
    ) =
        SimuleringSpec(
            type,
            sivilstatus,
            epsHarPensjon,
            foersteUttakDato = foersteUttakDato,
            heltUttakDato = heltUttakDato,
            pid,
            avdoed,
            isTpOrigSimulering,
            simulerForTp,
            uttakGrad = uttakGrad,
            forventetInntektBeloep,
            inntektUnderGradertUttakBeloep,
            inntektEtterHeltUttakBeloep,
            inntektEtterHeltUttakAntallAar = inntektEtterHeltUttakAntallAar,
            foedselAar,
            boddUtenlands,
            utlandAntallAar,
            utlandPeriodeListe,
            fremtidigInntektListe,
            inntektOver1GAntallAar,
            flyktning,
            epsHarInntektOver2G,
            rettTilOffentligAfpFom,
            afpOrdning,
            afpInntektMaanedFoerUttak,
            erAnonym,
        )

    fun gjelderPre2025OffentligAfp() =
        type == SimuleringType.AFP_ETTERF_ALDER

    fun gjelderPrivatAfpFoersteUttak() =
        type == SimuleringType.ALDER_M_AFP_PRIVAT

    fun gjelderEndring() =
        type == SimuleringType.ENDR_ALDER ||
                type == SimuleringType.ENDR_AP_M_AFP_PRIVAT ||
                type == SimuleringType.ENDR_ALDER_M_GJEN
}
