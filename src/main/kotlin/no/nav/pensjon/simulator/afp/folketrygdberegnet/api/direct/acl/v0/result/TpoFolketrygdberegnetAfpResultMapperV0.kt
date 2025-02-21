package no.nav.pensjon.simulator.afp.folketrygdberegnet.api.direct.acl.v0.result

import no.nav.pensjon.simulator.core.domain.regler.beregning.Beregning
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.core.util.toNorwegianNoon

/**
 * Maps from to domain object to DTO for result of 'simulering av folketrygdberegnet AFP'.
 * V0 = Versipn 0 of the API (application programming interface) and DTO (data transfer object)
 * AFP = Avtalefestet pensjon
 */
object TpoFolketrygdberegnetAfpResultMapperV0 {

    fun toResultV0(source: SimulatorOutput): TpoFolketrygdberegnetAfpResultV0? =
        source.pre2025OffentligAfp?.beregning?.let(::beregnetAfp)

    private fun beregnetAfp(source: Beregning): TpoFolketrygdberegnetAfpResultV0 {
        val sluttpoengtall = source.tp?.spt
        val poengrekke = sluttpoengtall?.poengrekke

        return TpoFolketrygdberegnetAfpResultV0(
            totalbelopAfp = source.netto,
            virkFom = source.virkFom?.toNorwegianNoon(),
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
