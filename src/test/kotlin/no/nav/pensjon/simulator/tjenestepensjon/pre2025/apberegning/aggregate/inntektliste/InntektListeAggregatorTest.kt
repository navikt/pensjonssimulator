package no.nav.pensjon.simulator.tjenestepensjon.pre2025.apberegning.aggregate.inntektliste

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.Inntekt
import java.time.LocalDate

class InntektListeAggregatorTest : StringSpec({

    "skal opprette inntekter for alle perioder"{
    val actualTime = LocalDate.now()
    val sisteGyldigeOpptjeningsaar = actualTime.minusYears(2).year
    val inntektListeSpec = InntektListeSpec(
            foedselsdato = actualTime.minusYears(65),
            inntektFoerFoersteUttak = 1,
            gradertUttak = true,
            simuleringTypeErAfpEtterfAlder = true,
            inntektUnderGradertUttakBeloep = 2,
            inntektEtterHeltUttakBeloep = 3,
            inntektEtterHeltUttakAntallAar = 4,
            foersteUttakDato = actualTime.minusYears(1),
            heltUttakDato = actualTime //blir overskrevet til ALDER_AAR_VED_OVERGANG_FRA_AFP_TIL_AP år i aggregatormetoden
        )

        val result: List<Inntekt> = InntektListeAggregator.aggregate(inntektListeSpec, actualTime, sisteGyldigeOpptjeningsaar)
        result.size shouldBe 4
        with(result[0]) {
            fom shouldBe actualTime.withYear(sisteGyldigeOpptjeningsaar + 1).withDayOfYear(1)
            beloep shouldBe 1.0
        }
        with(result[1]) {
            fom shouldBe inntektListeSpec.foersteUttakDato
            beloep shouldBe 2.0
        }
        with(result[2]) {
            fom shouldBe inntektListeSpec.foedselsdato.plusYears(ALDER_AAR_VED_OVERGANG_FRA_AFP_TIL_AP).plusMonths(1).withDayOfMonth(1)
            beloep shouldBe 3.0
        }
        with(result[3]) {
            fom shouldBe inntektListeSpec.foedselsdato.plusYears(ALDER_AAR_VED_OVERGANG_FRA_AFP_TIL_AP).plusMonths(1).withDayOfMonth(1)
                .plusYears(inntektListeSpec.inntektEtterHeltUttakAntallAar!!.toLong())
            beloep shouldBe 0.0
        }
    }

    "skal opprette inntekter frem til helt uttak"{
        val actualTime = LocalDate.now()
        val sisteGyldigeOpptjeningsaar = actualTime.minusYears(2).year
        val inntektListeSpec = InntektListeSpec(
            foedselsdato = actualTime.minusYears(65),
            inntektFoerFoersteUttak = 1,
            gradertUttak = true,
            simuleringTypeErAfpEtterfAlder = true,
            inntektUnderGradertUttakBeloep = 2,
            inntektEtterHeltUttakBeloep = 0,
            inntektEtterHeltUttakAntallAar = 0,
            foersteUttakDato = actualTime.minusYears(1),
            heltUttakDato = actualTime //blir overskrevet til ALDER_AAR_VED_OVERGANG_FRA_AFP_TIL_AP år i aggregatormetoden
        )

        val result: List<Inntekt> = InntektListeAggregator.aggregate(inntektListeSpec, actualTime, sisteGyldigeOpptjeningsaar)
        result.size shouldBe 3
        with(result[0]) {
            fom shouldBe actualTime.withYear(sisteGyldigeOpptjeningsaar + 1).withDayOfYear(1)
            beloep shouldBe 1.0
        }
        with(result[1]) {
            fom shouldBe inntektListeSpec.foersteUttakDato
            beloep shouldBe 2.0
        }
        with(result[2]) {
            fom shouldBe inntektListeSpec.foedselsdato.plusYears(ALDER_AAR_VED_OVERGANG_FRA_AFP_TIL_AP).plusMonths(1).withDayOfMonth(1)
            beloep shouldBe 0.0
        }
    }

    "skal opprette inntekter frem til første uttak"{
        val actualTime = LocalDate.now()
        val sisteGyldigeOpptjeningsaar = actualTime.minusYears(2).year
        val inntektListeSpec = InntektListeSpec(
            foedselsdato = actualTime.minusYears(65),
            inntektFoerFoersteUttak = 1,
            gradertUttak = true,
            simuleringTypeErAfpEtterfAlder = true,
            inntektUnderGradertUttakBeloep = 0,
            inntektEtterHeltUttakBeloep = 0,
            inntektEtterHeltUttakAntallAar = 0,
            foersteUttakDato = actualTime.minusYears(1),
            heltUttakDato = actualTime //blir overskrevet til ALDER_AAR_VED_OVERGANG_FRA_AFP_TIL_AP år i aggregatormetoden
        )

        val result: List<Inntekt> = InntektListeAggregator.aggregate(inntektListeSpec, actualTime, sisteGyldigeOpptjeningsaar)
        result.size shouldBe 2
        with(result[0]) {
            fom shouldBe actualTime.withYear(sisteGyldigeOpptjeningsaar + 1).withDayOfYear(1)
            beloep shouldBe 1.0
        }
        with(result[1]) {
            fom shouldBe inntektListeSpec.foersteUttakDato
            beloep shouldBe 0.0
        }
    }

    "skal opprette 0-inntekt hvis ingen inntekter speisifisert"{
        val actualTime = LocalDate.now()
        val sisteGyldigeOpptjeningsaar = actualTime.minusYears(2).year
        val inntektListeSpec = InntektListeSpec(
            foedselsdato = actualTime.minusYears(65),
            inntektFoerFoersteUttak = 0,
            gradertUttak = true,
            simuleringTypeErAfpEtterfAlder = true,
            inntektUnderGradertUttakBeloep = 0,
            inntektEtterHeltUttakBeloep = 0,
            inntektEtterHeltUttakAntallAar = 0,
            foersteUttakDato = actualTime.minusYears(1),
            heltUttakDato = actualTime //blir overskrevet til ALDER_AAR_VED_OVERGANG_FRA_AFP_TIL_AP år i aggregatormetoden
        )

        val result: List<Inntekt> = InntektListeAggregator.aggregate(inntektListeSpec, actualTime, sisteGyldigeOpptjeningsaar)
        result.size shouldBe 1
        with(result[0]) {
            fom shouldBe actualTime.withYear(sisteGyldigeOpptjeningsaar + 1).withDayOfYear(1)
            beloep shouldBe 0.0
        }
    }

    "skal opprette inntekter frem til 75 år selv om brukeren har oppgitt flere år"{
        val actualTime = LocalDate.now()
        val sisteGyldigeOpptjeningsaar = actualTime.minusYears(2).year
        val inntektListeSpec = InntektListeSpec(
            foedselsdato = actualTime.minusYears(65),
            inntektFoerFoersteUttak = 1,
            gradertUttak = true,
            simuleringTypeErAfpEtterfAlder = true,
            inntektUnderGradertUttakBeloep = 2,
            inntektEtterHeltUttakBeloep = 3,
            inntektEtterHeltUttakAntallAar = 20,
            foersteUttakDato = actualTime.minusYears(1),
            heltUttakDato = actualTime //blir overskrevet til ALDER_AAR_VED_OVERGANG_FRA_AFP_TIL_AP år i aggregatormetoden
        )

        val result: List<Inntekt> = InntektListeAggregator.aggregate(inntektListeSpec, actualTime, sisteGyldigeOpptjeningsaar)
        result.size shouldBe 4
        with(result[0]) {
            fom shouldBe actualTime.withYear(sisteGyldigeOpptjeningsaar + 1).withDayOfYear(1)
            beloep shouldBe 1.0
        }
        with(result[1]) {
            fom shouldBe inntektListeSpec.foersteUttakDato
            beloep shouldBe 2.0
        }
        with(result[2]) {
            fom shouldBe inntektListeSpec.foedselsdato.plusYears(ALDER_AAR_VED_OVERGANG_FRA_AFP_TIL_AP).plusMonths(1).withDayOfMonth(1)
            beloep shouldBe 3.0
        }
        with(result[3]) {
            fom shouldBe inntektListeSpec.foedselsdato.plusYears(OEVRE_ALDERSGRENSE_FOR_OPPTJENING).plusMonths(1).withDayOfMonth(1)
            beloep shouldBe 0.0
        }
    }

    "heltuttaksdato skal ikke overskrives ved andre simuleringstyper enn AFP_ETTERF_ALDER"{
        val actualTime = LocalDate.now()
        val sisteGyldigeOpptjeningsaar = actualTime.minusYears(2).year
        val inntektListeSpec = InntektListeSpec(
            foedselsdato = actualTime.minusYears(65),
            inntektFoerFoersteUttak = 1,
            gradertUttak = true,
            simuleringTypeErAfpEtterfAlder = false,
            inntektUnderGradertUttakBeloep = 2,
            inntektEtterHeltUttakBeloep = 3,
            inntektEtterHeltUttakAntallAar = 10,
            foersteUttakDato = actualTime.minusYears(1),
            heltUttakDato = actualTime.plusMonths(1).withDayOfMonth(1)
        )

        val result: List<Inntekt> = InntektListeAggregator.aggregate(inntektListeSpec, actualTime, sisteGyldigeOpptjeningsaar)
        result.size shouldBe 4
        with(result[2]) {
            fom shouldBe inntektListeSpec.heltUttakDato
            beloep shouldBe 3.0
        }
    }

    "skal bruke inntektEtterHeltUttakBeloep etter uttak ved annen simuleringstype enn AFP_ETTERF_ALDER og ikke gradertUttak"{
        val actualTime = LocalDate.now()
        val sisteGyldigeOpptjeningsaar = actualTime.minusYears(2).year
        val inntektListeSpec = InntektListeSpec(
            foedselsdato = actualTime.minusYears(65),
            inntektFoerFoersteUttak = 1,
            gradertUttak = false,
            simuleringTypeErAfpEtterfAlder = false,
            inntektUnderGradertUttakBeloep = 2,
            inntektEtterHeltUttakBeloep = 3,
            inntektEtterHeltUttakAntallAar = 10,
            foersteUttakDato = actualTime.minusYears(1),
            heltUttakDato = actualTime.plusMonths(1).withDayOfMonth(1)
        )

        val result: List<Inntekt> = InntektListeAggregator.aggregate(inntektListeSpec, actualTime, sisteGyldigeOpptjeningsaar)
        result.size shouldBe 3
        with(result[1]) {
            fom shouldBe inntektListeSpec.foersteUttakDato
            beloep shouldBe 3.0
        }
    }

    "skal IKKE legge til like beløp i inntektliste i sammenhengende periode"{
        val actualTime = LocalDate.now()
        val sisteGyldigeOpptjeningsaar = actualTime.minusYears(2).year
        val inntektListeSpec = InntektListeSpec(
            foedselsdato = actualTime.minusYears(65),
            inntektFoerFoersteUttak = 1,
            gradertUttak = true,
            simuleringTypeErAfpEtterfAlder = true,
            inntektUnderGradertUttakBeloep = 1,
            inntektEtterHeltUttakBeloep = 1,
            inntektEtterHeltUttakAntallAar = 3,
            foersteUttakDato = actualTime.minusYears(1),
            heltUttakDato = actualTime.plusMonths(1).withDayOfMonth(1)
        )

        val result: List<Inntekt> = InntektListeAggregator.aggregate(inntektListeSpec, actualTime, sisteGyldigeOpptjeningsaar)
        result.size shouldBe 2
        with(result[0]) {
            fom shouldBe actualTime.withYear(sisteGyldigeOpptjeningsaar + 1).withDayOfYear(1)
            beloep shouldBe 1.0
        }
        with(result[1]) {
            fom shouldBe inntektListeSpec.foedselsdato.plusYears(ALDER_AAR_VED_OVERGANG_FRA_AFP_TIL_AP + inntektListeSpec.inntektEtterHeltUttakAntallAar!!).plusMonths(1).withDayOfMonth(1)
            beloep shouldBe 0.0
        }
    }

    "inntekt skal stoppes ved 75 år ved manglende heltuttaksdato og annen simuleringstype enn AFP_ETTERF_ALDER"{
        val actualTime = LocalDate.now()
        val sisteGyldigeOpptjeningsaar = actualTime.minusYears(2).year
        val inntektListeSpec = InntektListeSpec(
            foedselsdato = actualTime.minusYears(65),
            inntektFoerFoersteUttak = 1,
            gradertUttak = true,
            simuleringTypeErAfpEtterfAlder = false,
            inntektUnderGradertUttakBeloep = 1,
            inntektEtterHeltUttakBeloep = 0,
            inntektEtterHeltUttakAntallAar = null,
            foersteUttakDato = actualTime.minusYears(1),
            heltUttakDato = null
        )

        val result: List<Inntekt> = InntektListeAggregator.aggregate(inntektListeSpec, actualTime, sisteGyldigeOpptjeningsaar)
        result.size shouldBe 2
        with(result[0]) {
            fom shouldBe actualTime.withYear(sisteGyldigeOpptjeningsaar + 1).withDayOfYear(1)
            beloep shouldBe 1.0
        }
        with(result[1]) {
            fom shouldBe inntektListeSpec.foedselsdato.plusYears(OEVRE_ALDERSGRENSE_FOR_OPPTJENING).plusMonths(1).withDayOfMonth(1)
            beloep shouldBe 0.0
        }
    }

}){
    companion object{
        const val ALDER_AAR_VED_OVERGANG_FRA_AFP_TIL_AP: Long = 67
        const val OEVRE_ALDERSGRENSE_FOR_OPPTJENING: Long = 75
    }
}
