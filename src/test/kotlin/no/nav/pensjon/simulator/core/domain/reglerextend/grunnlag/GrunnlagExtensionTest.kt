package no.nav.pensjon.simulator.core.domain.reglerextend.grunnlag

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import no.nav.pensjon.simulator.core.domain.regler.enum.*
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.*
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import java.time.LocalDate

class GrunnlagExtensionTest : ShouldSpec({

    should("copy AFP-historikk") {
        afpHistorikk().let { it.copy() shouldBeEqualToComparingFields it }
    }

    should("copy AFP-relatert uførepensjonsgrunnlag hos tjenestepensjonsordning") {
        afpTjenestepensjonUfoerepensjonsgrunnlag().let { it.copy() shouldBeEqualToComparingFields it }
    }

    should("copy antall år, måneder, dager") {
        antallAarMaanederDager().let { it.copy() shouldBeEqualToComparingFields it }
    }

    should("copy inngang- og eksportgrunnlag") {
        inngangOgEksportGrunnlag().let { it.copy() shouldBeEqualToComparingFields it }
    }
})

private fun afpHistorikk() =
    AfpHistorikk().apply {
        afpFpp = 1.2
        virkFom = LocalDate.of(2021, 1, 1).toNorwegianDateAtNoon()
        virkTom = LocalDate.of(2022, 2, 2).toNorwegianDateAtNoon()
        afpPensjonsgrad = 1
        afpOrdningEnum = AFPtypeEnum.AFPKOM
    }

private fun afpTjenestepensjonUfoerepensjonsgrunnlag() =
    AfpTpoUpGrunnlag().apply {
        belop = 1
        virkFom = LocalDate.of(2021, 1, 1).toNorwegianDateAtNoon()
    }

private fun antallAarMaanederDager() =
    AntallArMndDag().apply {
        antallAr = 1
        antallMnd = 2
        antallDager = 3
    }

private fun inngangOgEksportGrunnlag() =
    InngangOgEksportGrunnlag().apply {
        treArTrygdetidNorge = true
        femArTrygdetidNorge = false
        unntakFraForutgaendeTT = Unntak().apply { unntak = true }
        fortsattMedlemFT = true
        minstTyveArBotidNorge = false
        opptjentRettTilTPEtterFT = true
        eksportforbud = Eksportforbud().apply { unntakTypeEnum = EksportUnntakEnum.MINDRE5AR_BARNEP }
        friEksportPgaYrkesskade = false
        eksportrettEtterEOSForordning = Eksportrett().apply { eksportrett = true }
        eksportrettEtterTrygdeavtalerEOS = Eksportrett().apply { bostedslandEnum = EksportlandEnum.AUS }
        eksportrettEtterAndreTrygdeavtaler = Eksportrett().apply { bostedslandEnum = EksportlandEnum.CZE }
        eksportrettGarantertTP = Unntak().apply { unntakTypeEnum = InngangUnntakEnum.FLYKT_BARNEP }
        minstTreArsFMNorge = true
        minstFemArsFMNorge = false
        minstTreArsFMNorgeVirkdato = true
        unntakFraForutgaendeMedlemskap = Unntak().apply { eksportUnntakEnum = EksportUnntakEnum.UFOR25_ALDER }
        oppfyltEtterGamleRegler = false
        oppfyltVedSammenlegging = OppfyltVedSammenlegging().apply { oppfylt = true }
        oppfyltVedSammenleggingFemAr = OppfyltVedSammenlegging().apply { avtalelandEnum = AvtaleLandEnum.AUT }
        oppfyltVedGjenlevendesMedlemskap = true
        gjenlevendeMedlemFT = false
        minstEttArFMNorge = true
        foreldreMinstTyveArBotidNorge = false
        friEksportDodsfall = true
        minstTyveArTrygdetidNorgeKap20 = false
        treArTrygdetidNorgeKap20 = true
        femArTrygdetidNorgeKap20 = false
        oppfyltVedSammenleggingKap20 = OppfyltVedSammenlegging().apply { avtalelandEnum = AvtaleLandEnum.BEL }
        oppfyltVedSammenleggingFemArKap20 = OppfyltVedSammenlegging().apply { avtalelandEnum = AvtaleLandEnum.DEU }
    }

