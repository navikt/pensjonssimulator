package no.nav.pensjon.simulator.afp.offentlig.fra2025.beregning

import no.nav.pensjon.simulator.afp.offentlig.fra2025.beregning.domain.AfpBeregningsgrunnlag
import no.nav.pensjon.simulator.afp.offentlig.fra2025.beregning.domain.AlderForDelingstall
import no.nav.pensjon.simulator.alder.Alder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

class OffentligAFPYtelseBeregnerTest {

    @Test
    fun `beregn AFP Offentlig ytelse`() {
        assertEquals(48_787.97, LivsvarigOffentligAfpYtelseBeregner.beregn(4_000_000, 19.07), 0.1)
        assertEquals(49785.24, LivsvarigOffentligAfpYtelseBeregner.beregn(2_500_000, 11.68), 0.1)
        assertEquals(17677.019, LivsvarigOffentligAfpYtelseBeregner.beregn(2_150_000, 28.29), 0.1)
        assertEquals(26190.84, LivsvarigOffentligAfpYtelseBeregner.beregn(1_753_213, 15.57), 0.1)
    }


    @Test
    fun `beregn AFP offentlig livsvarig-ytelser for 62 aar gamle brukere ved uttak`() {
        val grunnlag = listOf(
            AfpBeregningsgrunnlag(3_000_000, AlderForDelingstall(Alder(62, 0), LocalDate.of(2029, 1, 1)), 20.75),
            AfpBeregningsgrunnlag(3_150_000, AlderForDelingstall(Alder(63, 0), LocalDate.of(2030, 1, 1)),19.93)
        )
        val resultat = LivsvarigOffentligAfpYtelseBeregner.beregnYtelser(grunnlag)
        assertEquals(2, resultat.size)
        assertEquals(33628.43, resultat[0].afpYtelsePerAar, 0.1)
        assertEquals(35379.03, resultat[1].afpYtelsePerAar, 0.1)
    }


    @Test
    fun `beregn AFP Offentlig livsvarig-ytelser for brukere eldre enn 62 aar`() {
        val grunnlag = listOf(
            AfpBeregningsgrunnlag(2_658_000, AlderForDelingstall(Alder(64, 2), LocalDate.of(2045, 6, 3)), 20.88),
        )
        val resultat = LivsvarigOffentligAfpYtelseBeregner.beregnYtelser(grunnlag)
        assertEquals(1, resultat.size)
        assertEquals(29609.29, resultat[0].afpYtelsePerAar, 0.1)
    }
}