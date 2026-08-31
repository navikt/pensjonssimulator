package no.nav.pensjon.simulator.afp.offentlig.tidsbegrenset.serviceberegn.api.v1.acl

import no.nav.pensjon.simulator.afp.offentlig.tidsbegrenset.FolketrygdberegnetAfp
import no.nav.pensjon.simulator.afp.offentlig.tidsbegrenset.serviceberegn.ServiceberegningAfpResult
import no.nav.pensjon.simulator.opptjening.OpptjeningGrunnlag
import no.nav.pensjon.simulator.validity.Problem

object ServiceberegningAfpResultMapper {

    fun transferable(source: ServiceberegningAfpResult) =
        ServiceberegningAfpResultDto(
            beregnetAfp = source.beregnetAfp?.let(::afp),
            opptjeningListe = source.opptjeningListe.map(::opptjening),
            problem = source.problem?.let(::problem)
        )

    private fun afp(source: FolketrygdberegnetAfp) =
        ServiceberegningFolketrygdberegnetAfpDto(
            afpTotalbeloep = source.totalbelopAfp ?: 0,
            virkningFom = source.virkFom,
            tidligereArbeidsinntekt = source.tidligereArbeidsinntekt,
            grunnbeloep = source.grunnbelop,
            sluttpoengtall = source.sluttpoengtall,
            trygdetid = source.trygdetid,
            poengaar = source.poengar,
            poengaarFoer1992 = source.poeangar_f92,
            poengaarEtter1991 = source.poeangar_e91,
            grunnpensjon = source.grunnpensjon,
            tilleggspensjon = source.tilleggspensjon,
            afpTillegg = source.afpTillegg,
            fpp = source.fpp,
            saertillegg = source.sertillegg,
            grad = source.grad,
            erAvkortet = source.erAvkortet
        )

    private fun opptjening(source: OpptjeningGrunnlag) =
        ServiceberegningOpptjeningDto(
            aarstall = source.aar,
            pensjonsgivendeInntekt = source.pensjonsgivendeInntekt,
            pensjonspoeng = source.pensjonspoeng ?: 0.0
        )

    private fun problem(source: Problem) =
        ServiceberegningProblemDto(
            type = ServiceberegningProblemtypeDto.entries.firstOrNull { it.internalValue == source.type }
                ?: ServiceberegningProblemtypeDto.ANNEN_SERVERFEIL,
            beskrivelse = source.beskrivelse
        )
}