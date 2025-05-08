package no.nav.pensjon.simulator.core.afp.offentlig.pre2025

import no.nav.pensjon.simulator.core.SimulatorContext
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AbstraktBeregningsResultat
import no.nav.pensjon.simulator.core.domain.regler.enum.BeholdningtypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Beholdning
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Pensjonsbeholdning
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isSameDay
import no.nav.pensjon.simulator.core.util.toNorwegianLocalDate
import no.nav.pensjon.simulator.normalder.NormertPensjonsalderService
import no.nav.pensjon.simulator.tech.time.DateUtil.foersteDag
import org.springframework.stereotype.Component

// no.nav.service.pensjon.simulering.support.command.SimulerAFPogAPCommand (beholdning part)
@Component
class Pre2025OffentligAfpBeholdning(
    val context: SimulatorContext,
    val normalderService: NormertPensjonsalderService
) {
    // SimulerAFPogAPCommand.settPensjonsbeholdning
    fun setPensjonsbeholdning(
        persongrunnlag: Persongrunnlag,
        forrigeAlderspensjonBeregningResult: AbstraktBeregningsResultat?
    ): Persongrunnlag {
        if (forrigeAlderspensjonBeregningResult != null) return persongrunnlag

       val normalderOppnaasAar = normalderService.normalderOppnaasDato(persongrunnlag.fodselsdato!!.toNorwegianLocalDate()).year

        val beholdningListe: List<Beholdning> =
            context.beregnOpptjening(foersteDag(normalderOppnaasAar), persongrunnlag)

        addMissingBeholdningerToPersongrunnlag(beholdningListe, persongrunnlag)
        return persongrunnlag
    }

    private companion object {

        // SimulerAFPogAPCommandHelper.addNewBeholdingIfDontExistsInPg
        private fun addMissingBeholdningerToPersongrunnlag(
            beholdningListe: List<Beholdning>,
            persongrunnlag: Persongrunnlag
        ) {
            val missingBeholdningListe: MutableList<Beholdning> = mutableListOf()

            beholdningListe
                .filter(Companion::isPensjonsbeholdning)
                .forEach {
                    var match = false
                    // Check if this beholding already exists in persongrunnlag.
                    for (persongrunnlagBeholdning in persongrunnlag.beholdninger) {
                        val isSameDay = isSameDay(persongrunnlagBeholdning.fom, (it as Pensjonsbeholdning).fom)

                        if (isSameDay && persongrunnlagBeholdning.beholdningsTypeEnum == it.beholdningsTypeEnum) {
                            match = true
                        }
                    }

                    if (!match) {
                        missingBeholdningListe.add(it)
                    }
                }

            missingBeholdningListe.forEach {
                persongrunnlag.addBeholdning(it as Pensjonsbeholdning)
            }
        }

        private fun isPensjonsbeholdning(beholdning: Beholdning) =
            BeholdningtypeEnum.PEN_B == beholdning.beholdningsTypeEnum
    }
}
