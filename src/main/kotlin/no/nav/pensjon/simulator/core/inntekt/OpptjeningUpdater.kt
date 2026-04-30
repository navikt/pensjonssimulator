package no.nav.pensjon.simulator.core.inntekt

import no.nav.pensjon.simulator.core.SimulatorContext
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagkildeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.OpptjeningtypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Opptjeningsgrunnlag
import no.nav.pensjon.simulator.inntekt.AarligInntekt
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
        inntektListe: List<AarligInntekt>,
        foedselsdato: LocalDate?
    ): MutableList<Opptjeningsgrunnlag> {
        val resultGrunnlagListe = mutableListOf<Opptjeningsgrunnlag>().apply {
            addAll(originalGrunnlagListe)
        }

        var inntektbasertGrunnlagListe: MutableList<Opptjeningsgrunnlag> = mutableListOf()

        inntektListe
            .filter { it.beloep > 0L }
            .forEach { inntektbasertGrunnlagListe.add(opptjeningsgrunnlag(inntekt = it)) }

        inntektbasertGrunnlagListe = context.beregnPoengtallBatch(
            opptjeningGrunnlagListe = inntektbasertGrunnlagListe,
            foedselsdato
        )

        inntektbasertGrunnlagListe.forEach {
            it.bruk = true
            it.grunnlagKildeEnum = GrunnlagkildeEnum.BRUKER
            resultGrunnlagListe.add(it)
        }

        return resultGrunnlagListe
    }

    private companion object {
        private fun opptjeningsgrunnlag(inntekt: AarligInntekt) =
            Opptjeningsgrunnlag().apply {
                ar = inntekt.inntektAar
                pi = inntekt.beloep
                opptjeningTypeEnum = OpptjeningtypeEnum.PPI
                bruk = true
            }
    }
}
