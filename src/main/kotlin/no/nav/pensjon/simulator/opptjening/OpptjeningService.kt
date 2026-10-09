package no.nav.pensjon.simulator.opptjening

import no.nav.pensjon.simulator.core.domain.regler.enum.BeholdningtypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.OpptjeningtypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Opptjeningsgrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Pensjonsbeholdning
import no.nav.pensjon.simulator.opptjening.client.OpptjeningClient
import no.nav.pensjon.simulator.person.PersonService
import no.nav.pensjon.simulator.person.Pid
import org.springframework.stereotype.Service

@Service
class OpptjeningService(
    private val client: OpptjeningClient,
    private val personService: PersonService
) {
    fun hentBeholdningListe(pid: Pid): List<Pensjonsbeholdning> =
        client.fetchBeholdninger(
            spec = BeholdningSpec(
                pid,
                beholdningType = BeholdningtypeEnum.PEN_B,
                serviceDirektiv = "INKL_OPPTJENINGBELOP"
            )
        )

    fun hentOpptjeningsgrunnlagSamling(
        pid: Pid,
        grunnlagstypeListe: List<OpptjeningsgrunnlagType>,
        fomAar: Int? = null,
        tomAar: Int? = null
    ): OpptjeningsgrunnlagSamling? =
        client.fetchOpptjeningsgrunnlag(
            spec = OpptjeningsgrunnlagSpec(pid, grunnlagstypeListe, fomAar, tomAar)
        )?.apply {
            omsorgListe.forEach {
                it.personOmsorgFor = it.pidOmsorgFor?.let(personService::person)
            }
        }

    fun hentPensjonspoeng(
        pid: Pid,
        pensjonspoengType: OpptjeningtypeEnum? = null,
        fomAar: Int? = null,
        tomAar: Int? = null
    ): List<Opptjeningsgrunnlag> =
        client.fetchPensjonspoeng(
            spec = PensjonspoengSpec(pid, pensjonspoengType, fomAar, tomAar)
        )
}