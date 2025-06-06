package no.nav.pensjon.simulator.core.domain.reglerextend

import no.nav.pensjon.simulator.core.domain.regler.*
import no.nav.pensjon.simulator.core.domain.reglerextend.grunnlag.copy
import java.util.*

// Checked 2025-03-06

fun Merknad.copy() =
    Merknad().also {
        it.kode = this.kode
        it.argumentListe = ArrayList(this.argumentListe)
    }

fun Trygdetid.copy() =
    Trygdetid().also {
        it.trygdetidId = this.trygdetidId
        it.regelverkTypeEnum = this.regelverkTypeEnum
        it.tt = this.tt
        it.ftt = this.ftt
        it.ftt_redusert = this.ftt_redusert
        it.ftt_fom = this.ftt_fom?.clone() as? Date
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
        it.ttUtlandTrygdeavtaler = this.ttUtlandTrygdeavtaler.map { it.copy() }
        it.merknadListe = this.merknadListe.map { it.copy() }
        it.garantiTypeEnum = this.garantiTypeEnum
        it.prorataNevnerVKAP = this.prorataNevnerVKAP
        it.prorataTellerVKAP = this.prorataTellerVKAP
        it.tt_fa = this.tt_fa?.copy()
        it.virkFom = this.virkFom?.clone() as? Date
        it.virkTom = this.virkTom?.clone() as? Date
        it.anvendtFlyktningEnum = this.anvendtFlyktningEnum
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
        it.merknadListe = this.merknadListe.map { it.copy() }
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
        it.merknadListe = this.merknadListe.map { it.copy() }
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
        it.merknadListe = this.merknadListe.map { it.copy() }
    }
