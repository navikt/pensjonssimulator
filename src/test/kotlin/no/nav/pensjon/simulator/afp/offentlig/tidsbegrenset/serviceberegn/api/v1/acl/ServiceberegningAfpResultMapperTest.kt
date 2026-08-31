package no.nav.pensjon.simulator.afp.offentlig.tidsbegrenset.serviceberegn.api.v1.acl

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.afp.offentlig.tidsbegrenset.FolketrygdberegnetAfp
import no.nav.pensjon.simulator.afp.offentlig.tidsbegrenset.serviceberegn.ServiceberegningAfpResult
import java.time.LocalDate

class ServiceberegningAfpResultMapperTest : ShouldSpec({

    should("mappe beregnet AFP til overførbart format (DTO)") {
        ServiceberegningAfpResultMapper.transferable(
            source = ServiceberegningAfpResult(
                beregnetAfp = FolketrygdberegnetAfp(
                    totalbelopAfp = 1,
                    virkFom = LocalDate.of(2024, 1, 1),
                    tidligereArbeidsinntekt = 2,
                    grunnbelop = 3,
                    sluttpoengtall = 4.1,
                    trygdetid = 5,
                    poengar = 6,
                    poeangar_f92 = 7,
                    poeangar_e91 = 8,
                    grunnpensjon = 9,
                    tilleggspensjon = 10,
                    afpTillegg = 11,
                    fpp = 12.2,
                    sertillegg = 13,
                    grad = 14,
                    erAvkortet = true
                ),
                opptjeningListe = emptyList(),
                problem = null
            )
        ).beregnetAfp shouldBe ServiceberegningFolketrygdberegnetAfpDto(
            afpTotalbeloep = 1,
            virkningFom = LocalDate.of(2024, 1, 1),
            tidligereArbeidsinntekt = 2,
            grunnbeloep = 3,
            sluttpoengtall = 4.1,
            trygdetid = 5,
            poengaar = 6,
            poengaarFoer1992 = 7,
            poengaarEtter1991 = 8,
            grunnpensjon = 9,
            tilleggspensjon = 10,
            afpTillegg = 11,
            fpp = 12.2,
            saertillegg = 13,
            grad = 14,
            erAvkortet = true
        )
    }
})