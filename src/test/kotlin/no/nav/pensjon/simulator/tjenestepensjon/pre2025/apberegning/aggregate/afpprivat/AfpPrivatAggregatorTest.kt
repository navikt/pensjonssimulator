package no.nav.pensjon.simulator.tjenestepensjon.pre2025.apberegning.aggregate.afpprivat

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.afp.privat.PrivatAfpPeriode

class AfpPrivatAggregatorTest : StringSpec({

    "Afp etterfulgt av AP blir ikke brukt i aggregasjon" {
        val list = listOf<PrivatAfpPeriode>()
        AfpPrivatAggregator.aggregate(list, true) shouldBe null
    }

    "aggregator tåler tom liste når afp ikke er etterfulgt av alderpensjon" {
        val list = listOf<PrivatAfpPeriode>()
        AfpPrivatAggregator.aggregate(list, false) shouldBe null
    }

    "aggregator tåler en liste med en afp privat periode" {
        val list = listOf(PrivatAfpPeriode(
            afpOpptjening = 1,
            kompensasjonstillegg = 2,
            alderAar = 3,
        ))
        val result = AfpPrivatAggregator.aggregate(list, false)
        result?.totalAfpBeholdning shouldBe 1
        result?.kompensasjonstillegg shouldBe 2.0
    }

    "aggregator gir periode med minst alder" {
        val list = listOf(PrivatAfpPeriode(
            afpOpptjening = 1,
            kompensasjonstillegg = 2,
            alderAar = 3,
        ),
            PrivatAfpPeriode(
                afpOpptjening = 4,
                kompensasjonstillegg = 5,
                alderAar = 2,
            ),
            PrivatAfpPeriode(
                afpOpptjening = 6,
                kompensasjonstillegg = 7,
                alderAar = 4,
            )
            )
        val result = AfpPrivatAggregator.aggregate(list, false)
        result?.totalAfpBeholdning shouldBe 4
        result?.kompensasjonstillegg shouldBe 5.0
    }

    "aggregator gir periode med minst alder og tåler null i alderAar" {
        val list = listOf(PrivatAfpPeriode(
            afpOpptjening = 1,
            kompensasjonstillegg = 2,
            alderAar = 3,
        ),
            PrivatAfpPeriode(
                afpOpptjening = 4,
                kompensasjonstillegg = 5,
                alderAar = 2,
            ),
            PrivatAfpPeriode(
                afpOpptjening = 6,
                kompensasjonstillegg = 7,
                alderAar = 4,
            ),
            PrivatAfpPeriode(
                afpOpptjening = 8,
                kompensasjonstillegg = 9,
                alderAar = null,
            )
        )
        val result = AfpPrivatAggregator.aggregate(list, false)
        result?.totalAfpBeholdning shouldBe 4
        result?.kompensasjonstillegg shouldBe 5.0
    }

})
