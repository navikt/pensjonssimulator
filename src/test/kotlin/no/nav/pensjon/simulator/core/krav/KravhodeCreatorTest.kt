package no.nav.pensjon.simulator.core.krav

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.RegelverkTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.SakTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.PersonDetalj
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.person.PersongrunnlagService
import no.nav.pensjon.simulator.testutil.TestDateUtil.dateAtNoon
import no.nav.pensjon.simulator.testutil.TestObjects.simuleringSpec
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
            beholdningUpdater = mockk(relaxed = true),
            epsService = mockk(relaxed = true),
            persongrunnlagService = arrangePersongrunnlag(),
            opptjeningUpdater = mockk(relaxed = true),
            generelleDataHolder = mockk(),
            kravService = mockk(),
            ufoereService = mockk(relaxed = true),
            endringPersongrunnlag = mockk(),
            endringUttaksgrad = mockk(),
            pre2025OffentligAfpPersongrunnlag = mockk(),
            pre2025OffentligAfpUttaksgrad = mockk(),
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
            sakType shouldBe SakTypeEnum.ALDER
            regelverkTypeEnum shouldBe RegelverkTypeEnum.N_REG_N_OPPTJ
        }
    }
})

private fun arrangePersongrunnlag(): PersongrunnlagService =
    mockk<PersongrunnlagService>().apply {
        every { getPersongrunnlagForSoeker(spec = any(), kravhode = any(), person = any()) } returns persongrunnlag()
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
