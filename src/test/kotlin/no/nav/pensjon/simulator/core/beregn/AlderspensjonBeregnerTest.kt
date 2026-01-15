package no.nav.pensjon.simulator.core.beregn

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.core.SimulatorContext
import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.SisteAldersberegning2011
import no.nav.pensjon.simulator.core.domain.regler.enum.KravlinjeTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.RegelverkTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravlinje
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import no.nav.pensjon.simulator.core.exception.RegelmotorValideringException
import no.nav.pensjon.simulator.testutil.TestObjects.simuleringSpec
import no.nav.pensjon.simulator.validity.BadSpecException
import java.time.LocalDate

class AlderspensjonBeregnerTest : ShouldSpec({

    should("gi klar feilmelding når gjenlevenderett ikke støttes") {
        val context = mockk<SimulatorContext> {
            every {
                revurderAlderspensjon2016(any(), any())
            } throws RegelmotorValideringException(
                message = "",
                merknadListe = listOf(
                    logiskSammenhengMerknad(aarsak = "VilkarsVedtakKravlinjeMangler"),
                    logiskSammenhengMerknad(aarsak = "VilkarsVedtakRelatertPersonFinnesIkke"),
                )
            )
        }

        shouldThrow<BadSpecException> {
            AlderspensjonBeregner(context).beregnAlderspensjon(
                kravhode = alderspensjon2016Kravhode(),
                vedtakListe = mutableListOf(gjenlevenderettVedtak()),
                virkningDato = LocalDate.of(2025, 3, 1),
                sisteAldersberegning2011 = SisteAldersberegning2011(),
                privatAfp = null,
                livsvarigOffentligAfpGrunnlag = null,
                simuleringSpec = simuleringSpec, // type ALDER_M_AFP_PRIVAT
                sakId = null,
                isFoersteUttak = false, // dvs. revurdering
                ignoreAvslag = false
            )
        }.message shouldBe "Pensjonen involverer gjenlevenderett," +
                " noe som ikke støttes for simuleringstype ALDER_M_AFP_PRIVAT"
    }
})

private fun alderspensjon2016Kravhode() =
    Kravhode().apply {
        regelverkTypeEnum = RegelverkTypeEnum.N_REG_G_N_OPPTJ
    }

private fun gjenlevenderettVedtak() =
    VilkarsVedtak().apply {
        kravlinje = Kravlinje().apply { kravlinjeTypeEnum = KravlinjeTypeEnum.GJR }
    }

private fun logiskSammenhengMerknad(aarsak: String) =
    Merknad().apply {
        kode = "VILKARSVEDTAK_KontrollerVilkarsVedtakLogiskSammenhengRS.$aarsak"
    }
