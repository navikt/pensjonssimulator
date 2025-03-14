package no.nav.pensjon.simulator.testutil

import no.nav.pensjon.simulator.core.domain.Avdoed
import no.nav.pensjon.simulator.core.domain.SimuleringType
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.core.krav.FremtidigInntekt
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.trygd.UtlandPeriode
import no.nav.pensjon.simulator.generelt.organisasjon.Organisasjonsnummer
import no.nav.pensjon.simulator.person.Pid
import org.springframework.security.oauth2.jwt.Jwt
import java.time.LocalDate

object TestObjects {
    val jwt = Jwt("j.w.t", null, null, mapOf("k" to "v"), mapOf("k" to "v"))

    val organisasjonsnummer = Organisasjonsnummer("123456789")

    val pid = Pid("12345678910")

    val simuleringSpec = simuleringSpec()

    fun simuleringSpec(
        type: SimuleringType = SimuleringType.ALDER_M_AFP_PRIVAT,
        sivilstatus: SivilstatusType = SivilstatusType.UGIF,
        epsHarPensjon: Boolean = false,
        foersteUttakDato: LocalDate? = LocalDate.of(2029, 1, 1),
        inntektSpecListe: List<FremtidigInntekt> = emptyList()
    ) = SimuleringSpec(
        type,
        sivilstatus,
        epsHarPensjon,
        foersteUttakDato,
        heltUttakDato = LocalDate.of(2032, 6, 1),
        pid = pid,
        foedselDato = null,
        avdoed = Avdoed(
            pid = Pid("04925398980"),
            antallAarUtenlands = 1,
            inntektFoerDoed = 50000,
            doedDato = LocalDate.of(2020, 11, 11),
            erMedlemAvFolketrygden = true,
            harInntektOver1G = false,
        ),
        isTpOrigSimulering = false,
        simulerForTp = false,
        uttakGrad = UttakGradKode.P_50,
        forventetInntektBeloep = 250000,
        inntektUnderGradertUttakBeloep = 125000,
        inntektEtterHeltUttakBeloep = 67500,
        inntektEtterHeltUttakAntallAar = 5,
        foedselAar = 1963,
        utlandAntallAar = 3,
        utlandPeriodeListe = mutableListOf(
            UtlandPeriode(
                fom = LocalDate.of(2010, 1, 1),
                tom = LocalDate.of(2010, 12, 31),
                land = LandkodeEnum.ALB,
                arbeidet = false
            ),
            UtlandPeriode(
                fom = LocalDate.of(2011, 1, 1),
                tom = LocalDate.of(2020, 5, 31),
                land = LandkodeEnum.BDI,
                arbeidet = true
            )
        ),
        fremtidigInntektListe = inntektSpecListe.toMutableList(),
        brukFremtidigInntekt = true,
        inntektOver1GAntallAar = 0,
        flyktning = false,
        epsHarInntektOver2G = true,
        rettTilOffentligAfpFom = null,
        afpOrdning = null,
        afpInntektMaanedFoerUttak = null,
        erAnonym = false,
        ignoreAvslag = false,
        isHentPensjonsbeholdninger = true,
        isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = false,
        onlyVilkaarsproeving = false
    )
}
