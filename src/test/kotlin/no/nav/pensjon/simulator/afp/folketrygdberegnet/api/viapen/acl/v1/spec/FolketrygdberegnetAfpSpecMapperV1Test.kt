package no.nav.pensjon.simulator.afp.folketrygdberegnet.api.viapen.acl.v1.spec

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.domain.regler.enum.AFPtypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.core.exception.BadSpecException
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.spec.Pre2025OffentligAfpSpec
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.testutil.Arrange
import no.nav.pensjon.simulator.testutil.TestDateUtil.dateAtNoon
import no.nav.pensjon.simulator.testutil.TestObjects.pid
import java.time.LocalDate
import java.util.*

class FolketrygdberegnetAfpSpecMapperV1Test : FunSpec({

    test("fromSimuleringSpecV1 should validate inntekt") {
        val personService = Arrange.foedselsdato(1963, 4, 5)

        shouldThrow<BadSpecException> {
            FolketrygdberegnetAfpSpecMapperV1(personService).fromSimuleringSpecV1(
                FolketrygdberegnetAfpSpecV1(
                    simuleringType = FolketrygdberegnetAfpSimuleringTypeSpecV1.AFP_FPP,
                    fnr = pid.value,
                    forventetInntekt = 2147483647,
                    forsteUttakDato = dateAtNoon(2025, Calendar.JANUARY, 1),
                    inntektUnderGradertUttak = 2147483647,
                    inntektEtterHeltUttak = 2147483647,
                    antallArInntektEtterHeltUttak = 456,
                    utenlandsopphold = 4,
                    sivilstatus = FolketrygdberegnetAfpSivilstandSpecV1.UGIF,
                    epsPensjon = true,
                    eps2G = false,
                    afpOrdning = "AFPSTAT",
                    afpInntektMndForUttak = 567
                )
            )
        }.message shouldBe "for høy inntekt: 2147483647 - max er 999999999"
    }

    test("fromSimuleringSpecV1 should validate antall år") {
        val personService = Arrange.foedselsdato(1963, 4, 5)

        shouldThrow<BadSpecException> {
            FolketrygdberegnetAfpSpecMapperV1(personService).fromSimuleringSpecV1(
                FolketrygdberegnetAfpSpecV1(
                    simuleringType = FolketrygdberegnetAfpSimuleringTypeSpecV1.AFP_FPP,
                    fnr = pid.value,
                    forventetInntekt = 1,
                    forsteUttakDato = dateAtNoon(2025, Calendar.JANUARY, 1),
                    inntektUnderGradertUttak = 1,
                    inntektEtterHeltUttak = 1,
                    antallArInntektEtterHeltUttak = 1000,
                    utenlandsopphold = 1000,
                    sivilstatus = FolketrygdberegnetAfpSivilstandSpecV1.UGIF,
                    epsPensjon = true,
                    eps2G = false,
                    afpOrdning = "AFPSTAT",
                    afpInntektMndForUttak = 567
                )
            )
        }.message shouldBe "for høyt antall år: 1000 - max er 999"
    }

    test("fromSimuleringSpecV1 should map values including pre2025OffentligAfp") {
        val personService = Arrange.foedselsdato(1963, 4, 5)

        FolketrygdberegnetAfpSpecMapperV1(personService).fromSimuleringSpecV1(
            FolketrygdberegnetAfpSpecV1(
                simuleringType = FolketrygdberegnetAfpSimuleringTypeSpecV1.AFP_FPP,
                fnr = pid.value,
                forventetInntekt = 123,
                forsteUttakDato = dateAtNoon(2025, Calendar.JANUARY, 1),
                inntektUnderGradertUttak = 234,
                inntektEtterHeltUttak = 345,
                antallArInntektEtterHeltUttak = 456,
                utenlandsopphold = 4,
                sivilstatus = FolketrygdberegnetAfpSivilstandSpecV1.UGIF,
                epsPensjon = true,
                eps2G = false,
                afpOrdning = "AFPSTAT",
                afpInntektMndForUttak = 567
            )
        ) shouldBe SimuleringSpec(
            SimuleringTypeEnum.AFP_FPP,
            SivilstatusType.UGIF,
            epsHarPensjon = true,
            foersteUttakDato = LocalDate.of(2025, 1, 1),
            heltUttakDato = null,
            pid = pid,
            foedselDato = LocalDate.of(1963, 4, 5),
            avdoed = null,
            isTpOrigSimulering = false,
            simulerForTp = false,
            uttakGrad = UttakGradKode.P_100,
            forventetInntektBeloep = 123,
            inntektUnderGradertUttakBeloep = 234,
            inntektEtterHeltUttakBeloep = 345,
            inntektEtterHeltUttakAntallAar = 456,
            foedselAar = 0,
            utlandAntallAar = 4,
            utlandPeriodeListe = mutableListOf(),
            fremtidigInntektListe = mutableListOf(),
            brukFremtidigInntekt = false,
            inntektOver1GAntallAar = 0,
            flyktning = null,
            epsHarInntektOver2G = false,
            livsvarigOffentligAfp = null,
            pre2025OffentligAfp = Pre2025OffentligAfpSpec(
                afpOrdning = AFPtypeEnum.AFPSTAT,
                inntektMaanedenFoerAfpUttakBeloep = 567,
                inntektUnderAfpUttakBeloep = 0 // zero for AFP_FPP
            ),
            erAnonym = false,
            ignoreAvslag = false,
            isHentPensjonsbeholdninger = false,
            isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = false,
            onlyVilkaarsproeving = false,
            epsKanOverskrives = false
        )
    }
})
