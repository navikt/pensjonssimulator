package no.nav.pensjon.simulator.core.result

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.beregning.Poengtall
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag

class SimulatorOutputMapperTest : FunSpec({

    test("mapToSimulertOpptjening maps poengtall.pp to pensjonsgivendeInntektPensjonspoeng") {
        val actual = SimulatorOutputMapper.mapToSimulertOpptjening(
            kalenderAar = 2024,
            resultatListe = emptyList(),
            soekerGrunnlag = Persongrunnlag(),
            poengtallListe = listOf(Poengtall().apply {
                ar = 2024
                pp = 1.23
            })
        )

        actual.pensjonsgivendeInntektPensjonspoeng shouldBe 1.23
    }

    test("mapToSimulertOpptjening returns undefined pensjonsgivendeInntektPensjonspoeng when no poengtall for angitt aar") {
        val actual = SimulatorOutputMapper.mapToSimulertOpptjening(
            kalenderAar = 2023, // not present in poengtallListe
            resultatListe = emptyList(),
            soekerGrunnlag = Persongrunnlag(),
            poengtallListe = listOf(Poengtall().apply {
                ar = 2024
                pp = 1.23
            })
        )

        actual.pensjonsgivendeInntektPensjonspoeng shouldBe null
    }
})
