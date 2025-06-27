package no.nav.pensjon.simulator.core.domain.reglerextend.grunnlag

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import no.nav.pensjon.simulator.core.domain.regler.enum.*
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.*
import no.nav.pensjon.simulator.testutil.TestDateUtil.dateAtNoon
import java.util.*

class GrunnlagExtensionTest : FunSpec({

    test("copy AfpHistorikk") {
        AfpHistorikk().apply {
            afpFpp = 1.2
            virkFom = dateAtNoon(2021, Calendar.JANUARY, 1)
            virkTom = dateAtNoon(2022, Calendar.FEBRUARY, 2)
            afpPensjonsgrad = 1
            afpOrdningEnum = AFPtypeEnum.AFPKOM
        }.copy() shouldBeEqualToComparingFields AfpHistorikk().apply {
            afpFpp = 1.2
            virkFom = dateAtNoon(2021, Calendar.JANUARY, 1)
            virkTom = dateAtNoon(2022, Calendar.FEBRUARY, 2)
            afpPensjonsgrad = 1
            afpOrdningEnum = AFPtypeEnum.AFPKOM
        }
    }

    test("copy InngangOgEksportGrunnlag") {
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
        }.copy() shouldBeEqualToComparingFields InngangOgEksportGrunnlag().apply {
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
    }
})
