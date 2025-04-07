package no.nav.pensjon.simulator.core.beholdning

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.virkning.FoersteVirkningDato
import java.time.LocalDate

class BeholdningUpdaterUtilTest : FunSpec({

    val foersteVirkningDatoListe = listOf(
        FoersteVirkningDato(null, null, LocalDate.of(2027, 1, 1), null),
        FoersteVirkningDato(null, null, LocalDate.of(2025, 2, 15), null), // earliest
        FoersteVirkningDato(null, null, LocalDate.of(2026, 1, 1), null)
    )

    test("isRevurderingBackToFirstUttaksdatoOrForstegangsbehandling => true if matching earliest 'første virkningsdato'") {
        BeholdningUpdaterUtil.isRevurderingBackToFirstUttaksdatoOrForstegangsbehandling(Kravhode().apply {
            sakForsteVirkningsdatoListe = foersteVirkningDatoListe
            onsketVirkningsdato = LocalDate.of(2025, 2, 15)
        }).shouldBeTrue()
    }

    test("isRevurderingBackToFirstUttaksdatoOrForstegangsbehandling => true if no 'første virkningsdato'") {
        BeholdningUpdaterUtil.isRevurderingBackToFirstUttaksdatoOrForstegangsbehandling(Kravhode().apply {
            sakForsteVirkningsdatoListe = emptyList()
            onsketVirkningsdato = LocalDate.of(2025, 2, 15)
        }).shouldBeTrue()
    }

    test("isRevurderingBackToFirstUttaksdatoOrForstegangsbehandling => false if not matching earliest 'første virkningsdato'") {
        BeholdningUpdaterUtil.isRevurderingBackToFirstUttaksdatoOrForstegangsbehandling(Kravhode().apply {
            sakForsteVirkningsdatoListe = foersteVirkningDatoListe
            onsketVirkningsdato = LocalDate.of(2027, 1, 1)
        }).shouldBeFalse()
    }
})
