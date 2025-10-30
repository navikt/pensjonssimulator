package no.nav.pensjon.simulator.trygdetid

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.core.SimulatorContext
import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.TTPeriode
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.to.TrygdetidRequest
import no.nav.pensjon.simulator.core.exception.BadSpecException
import no.nav.pensjon.simulator.core.exception.RegelmotorValideringException
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import java.time.LocalDate

class TrygdetidBeregnerProxyTest : ShouldSpec({

    should("sette 'siste gyldige opptjeningsår' i spec'en til to 2 år før virkFom") {
        val spec = TrygdetidRequest().apply {
            virkFom = LocalDate.of(2025, 1, 1).toNorwegianDateAtNoon()
            persongrunnlag = Persongrunnlag()
        }

        TrygdetidBeregnerProxy(context = mockk(relaxed = true)).fastsettTrygdetidForPeriode(
            spec,
            rolle = GrunnlagsrolleEnum.SOKER,
            kravIsUforetrygd = false,
            sakId = null
        )

        spec.persongrunnlag?.sisteGyldigeOpptjeningsAr shouldBe 2023
    }

    should("gi klar feilmelding når kapittel 20-trygdetid ikke starter før uttak") {
        val spec = TrygdetidRequest().apply {
            val uttakFom = LocalDate.of(2025, 1, 1).toNorwegianDateAtNoon()
            virkFom = uttakFom
            persongrunnlag = Persongrunnlag().apply {
                trygdetidPerioderKapittel20 = mutableListOf(TTPeriode().apply { fom = uttakFom })
            }
        }
        val context = mockk<SimulatorContext> {
            every {
                refreshFastsettTrygdetid(any(), any(), any())
            } throws RegelmotorValideringException(
                message = "",
                merknadListe = listOf(Merknad().apply { kode = "InputdataKontrollTTPeriodeRS.FomIkkeMindreEnnVirk" })
            )
        }

        shouldThrow<BadSpecException> {
            TrygdetidBeregnerProxy(context).fastsettTrygdetidForPeriode(
                spec,
                rolle = GrunnlagsrolleEnum.SOKER,
                kravIsUforetrygd = false,
                sakId = null
            )
        }.message shouldBe "Trygdetiden må starte før uttak (oppnås ved å utsette uttaket eller redusere antall år i utlandet)"
    }
})
