package no.nav.pensjon.simulator.afp.folketrygdberegnet.api.viapen.acl.v1.result

import no.nav.pensjon.simulator.core.domain.regler.beregning.Beregning
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.core.util.toLocalDate

/**
 * Maps from to domain object to DTO for result of 'simulering av folketrygdberegnet AFP'.
 * V1 = Versipn 1 of the API (application programming interface) and DTO (data transfer object)
 * AFP = Avtalefestet pensjon
 */
object FolketrygdberegnetAfpResultMapperV1 {

    fun toResultV1(source: SimulatorOutput): FolketrygdberegnetAfpResultV1? =
        source.pre2025OffentligAfp?.beregning?.let(FolketrygdberegnetAfpResultMapperV1::beregnetAfp)

    // PEN: no.nav.domain.pensjon.kjerne.beregning.Beregning.hentFolketrygdberegnetAfp
    private fun beregnetAfp(source: Beregning): FolketrygdberegnetAfpResultV1 {
        val sluttpoengtall = source.tp?.spt
        val poengrekke = sluttpoengtall?.poengrekke

        return FolketrygdberegnetAfpResultV1(
            totalbelopAfp = source.netto,
            virkFom = source.virkFom.toLocalDate(),
            trygdetid = source.tt_anv,
            grunnbelop = source.g,
            tidligereArbeidsinntekt = poengrekke?.tpi,
            sluttpoengtall = sluttpoengtall?.pt,
            poengar = poengrekke?.pa,
            poeangar_f92 = poengrekke?.pa_f92,
            poeangar_e91 = poengrekke?.pa_e91,
            tilleggspensjon = source.tp?.netto,
            fpp = poengrekke?.fpp?.pt,
            grunnpensjon = source.gp?.netto,
            afpTillegg = source.afpTillegg?.netto,
            sertillegg = source.st?.netto
        )
    }
}
