package no.nav.pensjon.simulator.alderspensjon.api.nav.viapen.acl.v2.spec

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.Avdoed
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.trygd.UtlandPeriode
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.testutil.Arrange
import no.nav.pensjon.simulator.testutil.TestDateUtil.dateAtMidnight
import no.nav.pensjon.simulator.testutil.TestObjects.pid
import java.time.LocalDate
import java.util.*

class NavSimuleringSpecMapperV2Test : FunSpec({

    test("fromSimuleringSpecV2 maps from domain to DTO version 2") {
        val personService = Arrange.foedselsdato(1963, 4, 5)

        NavSimuleringSpecMapperV2(personService).fromSimuleringSpecV2(
            source = NavSimuleringSpecV2(
                simuleringId = 123L,
                simuleringType = NavSimuleringTypeSpecV2.ALDER,
                simuleringNavn = "x",
                lagringstidspunkt = LocalDate.of(2012, 3, 4),
                fnr = pid.value,
                fnrAvdod = "04925398980",
                fodselsar = 1963,
                forventetInntekt = 250000,
                antArInntektOverG = 0, // used for anonym only
                forsteUttakDato = LocalDate.of(2029, 1, 1).toNorwegianDateAtNoon(),
                utg = UttakGradKode.P_50,
                inntektUnderGradertUttak = 125000,
                heltUttakDato = LocalDate.of(2032, 6, 1).toNorwegianDateAtNoon(),
                inntektEtterHeltUttak = 67500,
                antallArInntektEtterHeltUttak = 5,
                utenlandsopphold = 3,
                flyktning = false,
                sivilstatus = NavSivilstandSpecV2.UGIF,
                epsPensjon = false,
                eps2G = true,
                dodsdato = LocalDate.of(2020, 11, 11).toNorwegianDateAtNoon(),
                avdodAntallArIUtlandet = 1,
                avdodInntektForDod = 50000,
                inntektAvdodOver1G = false,
                avdodMedlemAvFolketrygden = true,
                avdodFlyktning = false,
                simulerForTp = false,
                tpOrigSimulering = false,
                utenlandsperiodeForSimuleringList = listOf(
                    NavSimuleringUtlandPeriodeV2(
                        land = LandkodeEnum.ALB,
                        arbeidetIUtland = false,
                        periodeFom = dateAtMidnight(2010, Calendar.JANUARY, 1),
                        periodeTom = dateAtMidnight(2010, Calendar.DECEMBER, 31)
                    ),
                    NavSimuleringUtlandPeriodeV2(
                        land = LandkodeEnum.BDI,
                        arbeidetIUtland = true,
                        periodeFom = dateAtMidnight(2011, Calendar.JANUARY, 1),
                        periodeTom = dateAtMidnight(2020, Calendar.MAY, 31)
                    )
                )
            ),
            isHentPensjonsbeholdninger = true,
            isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = false
        ) shouldBe
                SimuleringSpec(
                    type = SimuleringTypeEnum.ALDER,
                    sivilstatus = SivilstatusType.UGIF,
                    epsHarPensjon = false,
                    foersteUttakDato = LocalDate.of(2029, 1, 1),
                    heltUttakDato = LocalDate.of(2032, 6, 1),
                    pid = pid,
                    foedselDato = LocalDate.of(1963, 4, 5),
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
                    fremtidigInntektListe = mutableListOf(), // ikke brukt i PSELV
                    brukFremtidigInntekt = false,
                    inntektOver1GAntallAar = 0, // kun for anonym simulering
                    flyktning = false,
                    epsHarInntektOver2G = true,
                    rettTilOffentligAfpFom = null,
                    pre2025OffentligAfp = null,
                    erAnonym = false,
                    ignoreAvslag = false,
                    isHentPensjonsbeholdninger = true,
                    isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = false,
                    onlyVilkaarsproeving = false,
                    epsKanOverskrives = false
                )
    }
})
