package no.nav.pensjon.simulator.uttak.api.acl

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.SimuleringType
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.krav.FremtidigInntekt
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.testutil.TestObjects.pid
import java.time.LocalDate

class UttakSpecMapperV1Test : FunSpec({

    test("fromSpecV1 maps DTO to domain object representing simulering specification") {
        UttakSpecMapperV1.fromSpecV1(
            source = TidligstMuligUttakSpecV1(
                personId = pid.value,
                fodselsdato = LocalDate.of(1964, 5, 6),
                uttaksgrad = 20,
                heltUttakFraOgMedDato = LocalDate.of(2030, 1, 1),
                rettTilAfpOffentligDato = LocalDate.of(2027, 8, 1),
                fremtidigInntektListe = listOf(
                    UttakInntektSpecV1(
                        arligInntekt = 123000,
                        fraOgMedDato = LocalDate.of(2029, 2, 1)
                    )
                ),
            ),
            foedselsdato = LocalDate.of(1964, 5, 6)
        ) shouldBe SimuleringSpec(
            type = SimuleringType.ALDER_MED_AFP_OFFENTLIG_LIVSVARIG,
            sivilstatus = SivilstatusType.UGIF,
            epsHarPensjon = false,
            foersteUttakDato = null,
            heltUttakDato = LocalDate.of(2030, 1, 1),
            pid = pid,
            foedselDato = LocalDate.of(1964, 5, 6),
            avdoed = null,
            isTpOrigSimulering = true,
            simulerForTp = false,
            uttakGrad = UttakGradKode.P_20,
            forventetInntektBeloep = 0,
            inntektUnderGradertUttakBeloep = 0,
            inntektEtterHeltUttakBeloep = 0,
            inntektEtterHeltUttakAntallAar = null,
            foedselAar = 1964,
            utlandAntallAar = 0,
            utlandPeriodeListe = mutableListOf(),
            fremtidigInntektListe = mutableListOf(
                FremtidigInntekt(
                    aarligInntektBeloep = 123000,
                    fom = LocalDate.of(2029, 2, 1)
                )
            ),
            brukFremtidigInntekt = true,
            inntektOver1GAntallAar = 0,
            flyktning = false,
            epsHarInntektOver2G = false,
            rettTilOffentligAfpFom = LocalDate.of(2027, 8, 1),
            afpOrdning = null,
            afpInntektMaanedFoerUttak = null,
            erAnonym = false,
            ignoreAvslag = false,
            isHentPensjonsbeholdninger = true,
            isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = true,
            onlyVilkaarsproeving = true, // true for 'tidligst mulig uttak'
            epsKanOverskrives = false
        )
    }
})
