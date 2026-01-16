package no.nav.pensjon.simulator.core.domain.reglerextend

import no.nav.pensjon.simulator.core.domain.regler.*
import no.nav.pensjon.simulator.core.domain.reglerextend.grunnlag.copy
import no.nav.pensjon.simulator.core.util.copy

// Checked 2025-03-06

fun Merknad.copy() =
    Merknad().also {
        it.kode = this.kode
        it.argumentListe = ArrayList(this.argumentListe)
    }

fun Pakkseddel.copy() =
    Pakkseddel().also {
        it.kontrollTjenesteOk = this.kontrollTjenesteOk
        it.annenTjenesteOk = this.annenTjenesteOk
        it.merknadListe = this.merknadListe.map { o -> o.copy() }
        it.satstabell = this.satstabell
    }

fun Trygdetid.copy() =
    Trygdetid().also {
        it.trygdetidId = this.trygdetidId
        it.regelverkTypeEnum = this.regelverkTypeEnum
        it.tt = this.tt
        it.ftt = this.ftt
        it.ftt_redusert = this.ftt_redusert
        it.ftt_fom = this.ftt_fom?.copy()
        it.tt_fa_mnd = this.tt_fa_mnd
        it.tt_67_70 = this.tt_67_70
        it.tt_67_75 = this.tt_67_75
        it.tt_faktisk = this.tt_faktisk
        it.tt_E66 = this.tt_E66
        it.tt_F67 = this.tt_F67
        it.tt_fa_F2021 = this.tt_fa_F2021?.copy()
        it.opptjeningsperiode = this.opptjeningsperiode
        it.ttUtlandEos = this.ttUtlandEos?.copy()
        it.ttUtlandKonvensjon = this.ttUtlandKonvensjon?.copy()
        it.ttUtlandTrygdeavtaler = this.ttUtlandTrygdeavtaler.map { o -> o.copy() }
        it.merknadListe = this.merknadListe.map { o -> o.copy() }
        it.garantiTypeEnum = this.garantiTypeEnum
        it.prorataNevnerVKAP = this.prorataNevnerVKAP
        it.prorataTellerVKAP = this.prorataTellerVKAP
        it.tt_fa = this.tt_fa?.copy()
        it.virkFom = this.virkFom?.copy()
        it.virkTom = this.virkTom?.copy()
        it.anvendtFlyktningEnum = this.anvendtFlyktningEnum
    }

fun TTPeriode.copy() =
    TTPeriode().also {
        it.fom = this.fom?.copy()
        it.tom = this.tom?.copy()
        it.poengIInnAr = this.poengIInnAr
        it.poengIUtAr = this.poengIUtAr
        it.landEnum = this.landEnum
        it.ikkeProRata = this.ikkeProRata
        it.bruk = this.bruk
        it.grunnlagKildeEnum = this.grunnlagKildeEnum
    }

fun TTUtlandEOS.copy() =
    TTUtlandEOS().also {
        it.ftt_eos = this.ftt_eos
        it.ftt_eos_redusert = this.ftt_eos_redusert
        it.tt_eos_anv_mnd = this.tt_eos_anv_mnd
        it.tt_eos_anv_ar = this.tt_eos_anv_ar
        it.tt_eos_pro_rata_mnd = this.tt_eos_pro_rata_mnd
        it.tt_eos_teoretisk_mnd = this.tt_eos_teoretisk_mnd
        it.tt_eos_teller = this.tt_eos_teller
        it.tt_eos_nevner = this.tt_eos_nevner
        it.tt_lik_pa = this.tt_lik_pa
        it.tt_konvensjon_ar = this.tt_konvensjon_ar
        it.tt_fa = this.tt_fa
        it.merknadListe = this.merknadListe.map { o -> o.copy() }
    }

fun TTUtlandKonvensjon.copy() =
    TTUtlandKonvensjon().also {
        it.ftt_A10_brutto = this.ftt_A10_brutto
        it.ftt_A10_netto = this.ftt_A10_netto
        it.ftt_A10_redusert = this.ftt_A10_redusert
        it.ft_ar = this.ft_ar
        it.tt_A10_fa_mnd = this.tt_A10_fa_mnd
        it.tt_A10_anv_aar = this.tt_A10_anv_aar
        it.tt_A10_teller = this.tt_A10_teller
        it.tt_A10_nevner = this.tt_A10_nevner
        it.tt_konvensjon_ar = this.tt_konvensjon_ar
        it.tt_lik_pa = this.tt_lik_pa
        it.merknadListe = this.merknadListe.map { o -> o.copy() }
    }

fun TTUtlandTrygdeavtale.copy() =
    TTUtlandTrygdeavtale().also {
        it.ftt = this.ftt
        it.ftt_redusert = this.ftt_redusert
        it.tt_fa_mnd = this.tt_fa_mnd
        it.tt_anv_ar = this.tt_anv_ar
        it.tt_anv_mnd = this.tt_anv_mnd
        it.pro_rata_teller = this.pro_rata_teller
        it.pro_rata_nevner = this.pro_rata_nevner
        it.avtalelandEnum = this.avtalelandEnum
        it.tt_fa = this.tt_fa
        it.merknadListe = this.merknadListe.map { o -> o.copy() }
    }

fun Uforeopptjening.copy() =
    Uforeopptjening().also {
        it.belop = this.belop
        it.proRataBeregnetUP = this.proRataBeregnetUP
        it.poengtall = this.poengtall
        it.ufg = this.ufg
        it.antattInntekt = this.antattInntekt
        it.antattInntekt_proRata = this.antattInntekt_proRata
        it.andel_proRata = this.andel_proRata
        it.poengarTeller_proRata = this.poengarTeller_proRata
        it.poengarNevner_proRata = this.poengarNevner_proRata
        it.antFremtidigeAr_proRata = this.antFremtidigeAr_proRata
        it.yrkesskadeopptjening = this.yrkesskadeopptjening?.copy()
        it.uforetrygd = this.uforetrygd
        it.konvertertUFT = this.konvertertUFT
        it.uforear = this.uforear
    }

fun Yrkesskadeopptjening.copy() =
    Yrkesskadeopptjening().also {
        it.paa = this.paa
        it.yug = this.yug
        it.antattInntektYrke = this.antattInntektYrke
    }
