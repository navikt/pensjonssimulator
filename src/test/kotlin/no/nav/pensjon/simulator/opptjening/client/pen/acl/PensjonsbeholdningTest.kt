package no.nav.pensjon.simulator.opptjening.client.pen.acl

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.Opptjening
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.LonnsvekstInformasjon
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.ReguleringsInformasjon
import no.nav.pensjon.simulator.core.domain.regler.enum.FormelKodeEnum
import java.time.LocalDate
import no.nav.pensjon.simulator.opptjening.Pensjonsbeholdning as DomainPensjonsbeholdning

class PensjonsbeholdningTest : ShouldSpec({

    context("toInternalValue") {
        should("convert from external representation (data transfer object) to internal domain representation") {
            val opptjening = Opptjening().apply { ar = 2000 }
            val loennsvekstinformasjon = LonnsvekstInformasjon().apply { lonnsvekst = 3.2 }
            val reguleringsinformasjon = ReguleringsInformasjon().apply { reguleringsfaktor = 4.3 }
            val merknader = listOf(Merknad().apply { kode = "K" })

            Pensjonsbeholdning().apply {
                ar = 1999
                fomLd = LocalDate.of(2020, 1, 1)
                tomLd = LocalDate.of(2021, 12, 31)
                totalbelop = 2.1
                this.opptjening = opptjening
                lonnsvekstInformasjon = loennsvekstinformasjon
                reguleringsInformasjon = reguleringsinformasjon
                formelKodeEnum = FormelKodeEnum.AP2016_1
                merknadListe = merknader.toMutableList()
            }.toInternalValue() shouldBe DomainPensjonsbeholdning(
                aar = 1999,
                fom = LocalDate.of(2020, 1, 1),
                tom = LocalDate.of(2021, 12, 31),
                totalbeloep = 2.1,
                opptjening = opptjening, // NB: same instance
                loennsvekstinformasjon = loennsvekstinformasjon, // ditto
                reguleringsinformasjon = reguleringsinformasjon, // ditto
                formelkode = FormelKodeEnum.AP2016_1,
                merknadListe = merknader
            )
        }
    }
})