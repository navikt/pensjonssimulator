package no.nav.pensjon.simulator.fpp.api.v2.acl

import no.nav.pensjon.simulator.afp.offentlig.pre2025.FolketrygdberegnetAfp
import no.nav.pensjon.simulator.fpp.FppSimuleringResult
import no.nav.pensjon.simulator.fpp.api.v1.acl.AfpTypeDto
import no.nav.pensjon.simulator.fpp.api.v1.acl.ProblemDto
import no.nav.pensjon.simulator.fpp.api.v1.acl.ProblemtypeDto
import no.nav.pensjon.simulator.validity.Problem

object FppSimuleringResultMapperV2 {

    fun toDto(source: FppSimuleringResult) =
        FppSimuleringResultDtoV2(
            afpOrdning = AfpTypeDto.entries.firstOrNull { it.internalValue == source.afpOrdning } ?: AfpTypeDto.AFPSTAT,
            beregnetAfp = source.beregnetAfp?.let(::folketrygdberegnetAfp),
            problem = source.problem?.let(::problem)
        )

    private fun folketrygdberegnetAfp(source: FolketrygdberegnetAfp) =
        FolketrygdberegnetAfpDtoV2(
            totalbelopAfp = source.totalbelopAfp,
            virkFom = source.virkFom,
            tidligereArbeidsinntekt = source.tidligereArbeidsinntekt,
            grunnbelop = source.grunnbelop,
            sluttpoengtall = source.sluttpoengtall,
            trygdetid = source.trygdetid,
            poengar = source.poengar,
            poeangar_f92 = source.poeangar_f92,
            poeangar_e91 = source.poeangar_e91,
            grunnpensjon = source.grunnpensjon,
            tilleggspensjon = source.tilleggspensjon,
            afpTillegg = source.afpTillegg,
            fpp = source.fpp,
            sertillegg = source.sertillegg,
            grad = source.grad,
            erAvkortet = source.erAvkortet
        )

    private fun problem(source: Problem) =
        ProblemDto(
            type = ProblemtypeDto.entries.firstOrNull { it.internalValue == source.type }
                ?: ProblemtypeDto.ANNEN_SERVERFEIL,
            beskrivelse = source.beskrivelse
        )
}
