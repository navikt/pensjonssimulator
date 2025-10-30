package no.nav.pensjon.simulator.ytelse

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAfpPrivat
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAlderspensjon2025
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.SisteAldersberegning2011
import no.nav.pensjon.simulator.core.ytelse.LoependeYtelser
import no.nav.pensjon.simulator.testutil.TestObjects
import no.nav.pensjon.simulator.testutil.TestObjects.pid
import no.nav.pensjon.simulator.ytelse.client.YtelseClient
import java.time.LocalDate

class YtelseServiceTest : ShouldSpec({

    should("inkludere privatAfpVirkningFom for simuleringstype 'alder med privat AFP'") {
        val spec = TestObjects.simuleringSpec // SimuleringType.ALDER_M_AFP_PRIVAT

        val client = mockk<YtelseClient>().apply {
            every {
                fetchLoependeYtelser(
                    LoependeYtelserSpec(
                        pid = pid,
                        foersteUttakDato = spec.foersteUttakDato!!,
                        avdoed = spec.avdoed,
                        alderspensjonFlags = AlderspensjonYtelserFlags(inkluderPrivatAfp = true),
                        endringAlderspensjonFlags = null,
                        pre2025OffentligAfpYtelserFlags = null
                    )
                )
            } returns
                    LoependeYtelserResult(
                        alderspensjon = AlderspensjonYtelser(
                            sokerVirkningFom = LocalDate.of(2019, 1, 1),
                            avdodVirkningFom = LocalDate.of(2020, 1, 1),
                            sisteBeregning = SisteAldersberegning2011(),
                            forrigeBeregningsresultat = BeregningsResultatAlderspensjon2025(),
                            forrigeVilkarsvedtakListe = emptyList(),
                            avdoed = AvdoedYtelser(
                                pid = pid,
                                doedsdato = LocalDate.of(2019, 3, 21),
                                foersteVirkningsdato = LocalDate.of(2020, 1, 1)
                            )
                        ),
                        afpPrivat = PrivatAfpYtelser(
                            virkningFom = LocalDate.of(2021, 1, 1),
                            forrigeBeregningsresultat = BeregningsResultatAfpPrivat()
                        )
                    )
        }

        val actual = YtelseService(client).getLoependeYtelser(spec)

        actual shouldBe LoependeYtelser(
            soekerVirkningFom = LocalDate.of(2019, 1, 1),
            privatAfpVirkningFom = LocalDate.of(2021, 1, 1),
            sisteBeregning = null, // not included for 'alder m/ privat AFP'
            forrigeAlderspensjonBeregningResultat = null, // ditto
            forrigePrivatAfpBeregningResultat = null, // ditto
            forrigeVedtakListe = mutableListOf(),
            avdoed = AvdoedYtelser(
                pid = pid,
                doedsdato = LocalDate.of(2019, 3, 21),
                foersteVirkningsdato = LocalDate.of(2020, 1, 1)
            )
        )
    }
})
