package no.nav.pensjon.simulator.opptjening.client

import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Opptjeningsgrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Pensjonsbeholdning
import no.nav.pensjon.simulator.inntekt.LoependeInntekt
import no.nav.pensjon.simulator.opptjening.BeholdningSpec
import no.nav.pensjon.simulator.opptjening.OpptjeningsgrunnlagSamling
import no.nav.pensjon.simulator.opptjening.OpptjeningsgrunnlagSpec
import no.nav.pensjon.simulator.opptjening.PensjonspoengSpec
import no.nav.pensjon.simulator.person.Pid

interface OpptjeningClient {

    fun fetchSistLignedeInntekt(pid: Pid): LoependeInntekt?

    fun fetchBeholdninger(spec: BeholdningSpec): List<Pensjonsbeholdning>

    fun fetchOpptjeningsgrunnlag(spec: OpptjeningsgrunnlagSpec): OpptjeningsgrunnlagSamling?

    fun fetchPensjonspoeng(spec: PensjonspoengSpec): List<Opptjeningsgrunnlag>
}