package no.nav.pensjon.simulator.core.krav

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.afp.offentlig.pre2025.Pre2025OffentligAfpPersongrunnlag
import no.nav.pensjon.simulator.core.afp.offentlig.pre2025.Pre2025OffentligAfpUttaksgrad
import no.nav.pensjon.simulator.core.beholdning.BeholdningUpdater
import no.nav.pensjon.simulator.core.domain.SakType
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.RegelverkTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.PersonDetalj
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.endring.EndringPersongrunnlag
import no.nav.pensjon.simulator.core.endring.EndringUttakGrad
import no.nav.pensjon.simulator.core.inntekt.OpptjeningUpdater
import no.nav.pensjon.simulator.core.person.PersongrunnlagService
import no.nav.pensjon.simulator.core.person.eps.EpsService
import no.nav.pensjon.simulator.generelt.GenerelleDataHolder
import no.nav.pensjon.simulator.krav.KravService
import no.nav.pensjon.simulator.testutil.TestDateUtil.dateAtNoon
import no.nav.pensjon.simulator.testutil.TestObjects.simuleringSpec
import no.nav.pensjon.simulator.ufoere.UfoeretrygdUtbetalingService
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import java.time.LocalDate
import java.util.*

class KravhodeCreatorTest : FunSpec({

    /**
     * Opprettet kravhode skal ha:
     * - kravFremsattDato = dagens dato
     * - onsketVirkningsdato = heltUttakDato, hvis denne er definert
     * - gjelder = null
     * - sakId = null
     * - sakType = ALDER
     * - regelverkTypeEnum = verdi basert på årskull, f.eks. N_REG_N_OPPTJ for årskull 1963
     */
    test("opprettKravhode should opprette kravhode") {
        val kravhode = KravhodeCreator(
            beholdningUpdater = mock(BeholdningUpdater::class.java),
            epsService = mock(EpsService::class.java),
            persongrunnlagService = arrangePersongrunnlag(),
            opptjeningUpdater = mock(OpptjeningUpdater::class.java),
            generelleDataHolder = mock(GenerelleDataHolder::class.java),
            kravService = mock(KravService::class.java),
            ufoereService = mock(UfoeretrygdUtbetalingService::class.java),
            endringPersongrunnlag = mock(EndringPersongrunnlag::class.java),
            endringUttakGrad = mock(EndringUttakGrad::class.java),
            pre2025OffentligAfpPersongrunnlag = mock(Pre2025OffentligAfpPersongrunnlag::class.java),
            pre2025OffentligAfpUttaksgrad = mock(Pre2025OffentligAfpUttaksgrad::class.java),
            time = { LocalDate.of(2025, 1, 1) } // "dagens dato"
        ).opprettKravhode(
            kravhodeSpec = KravhodeSpec(
                simulatorInput = simuleringSpec(), // heltUttakDato 2032-06-01
                forrigeAlderspensjonBeregningResult = null,
                grunnbeloep = 123000
            ),
            person = PenPerson().apply { foedselsdato = LocalDate.of(1963, 1, 1) }, // => regelverktype N_REG_N_OPPTJ
            virkningDatoGrunnlagListe = emptyList()
        )

        with(kravhode) {
            kravFremsattDato shouldBe dateAtNoon(2025, Calendar.JANUARY, 1) // = "dagens dato"
            onsketVirkningsdato shouldBe LocalDate.of(2032, 6, 1) // = heltUttakDato
            gjelder shouldBe null
            sakId shouldBe null
            sakType shouldBe SakType.ALDER
            regelverkTypeEnum shouldBe RegelverkTypeEnum.N_REG_N_OPPTJ
        }
    }
})

private fun arrangePersongrunnlag(): PersongrunnlagService =
    mock(PersongrunnlagService::class.java).also {
        `when`(it.getPersongrunnlagForSoeker(spec = any(), kravhode = any(), person = any()))
            .thenReturn(persongrunnlag())
    }

private fun persongrunnlag() =
    Persongrunnlag().apply {
        penPerson = PenPerson()
        fodselsdato = dateAtNoon(1963, Calendar.JANUARY, 1)
        personDetaljListe = mutableListOf(
            PersonDetalj().apply {
                bruk = true
                grunnlagsrolleEnum = GrunnlagsrolleEnum.SOKER
                penRolleTom = dateAtNoon(2026, Calendar.JANUARY, 1)
            }
        )
    }
