package no.nav.pensjon.simulator.core.inntekt

import no.nav.pensjon.simulator.core.SimulatorContext
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagkildeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.OpptjeningtypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Opptjeningsgrunnlag
import no.nav.pensjon.simulator.core.krav.Inntekt
import org.springframework.stereotype.Component
import java.time.LocalDate

/**
 * Corresponds to extracts from PEN class
 * no.nav.service.pensjon.simulering.support.command.abstractsimulerapfra2011.OpprettKravhodeHelper
 */
@Component
class OpptjeningUpdater(private val context: SimulatorContext) {

    // PEN: AbstraktSimulerAPFra2011Command.oppdaterOpptjeningsgrunnlagFraInntektListe
    // -> OpprettKravHodeHelper.oppdaterOpptjeningsgrunnlagFraInntektListe
    fun oppdaterOpptjeningsgrunnlagFraInntekter(
        originalGrunnlagListe: List<Opptjeningsgrunnlag>,
        inntektListe: List<Inntekt>,
        foedselsdato: LocalDate?
    ): MutableList<Opptjeningsgrunnlag> {
        val resultGrunnlagListe = mutableListOf<Opptjeningsgrunnlag>().apply {
            addAll(originalGrunnlagListe)
        }

        var inntektbasertGrunnlagListe: MutableList<Opptjeningsgrunnlag> = mutableListOf()

        inntektListe
            .filter { it.beloep > 0L }
            .forEach { inntektbasertGrunnlagListe.add(opptjeningGrunnlag(it)) }

        inntektbasertGrunnlagListe = context.beregnPoengtallBatch(inntektbasertGrunnlagListe, foedselsdato)

        inntektbasertGrunnlagListe.forEach {
            it.bruk = true
            it.grunnlagKildeEnum = GrunnlagkildeEnum.BRUKER
            resultGrunnlagListe.add(it)
        }

        return resultGrunnlagListe
    }

    private companion object {
        private fun opptjeningGrunnlag(inntekt: Inntekt) =
            Opptjeningsgrunnlag().apply {
                ar = inntekt.inntektAar
                pi = inntekt.beloep.toInt()
                opptjeningTypeEnum = OpptjeningtypeEnum.PPI
                bruk = true
            }
    }
}
