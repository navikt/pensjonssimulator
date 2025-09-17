package no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v1

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDate

@JsonIgnoreProperties(ignoreUnknown = true)
data class SimulerOffentligTjenestepensjonSpecV1(
    val fnr: String,
    val fodselsdato: LocalDate,
    val sisteTpnr: String?,
    val sprak: String?,
    val simulertAFPOffentlig: SimulertAFPOffentligV1?,
    val simulertAFPPrivat: SimulertAFPPrivatV1?,
    val sivilstandkode: SivilstandCodeEnumV1,
    val inntektListe: List<InntektV1> = emptyList(),
    val pensjonsbeholdningsperiodeListe: List<PensjonsbeholdningsperiodeV1> = emptyList(),
    val simuleringsperiodeListe: List<SimuleringsperiodeV1> = emptyList(),
    val simuleringsdataListe: List<SimuleringsdataV1> = emptyList(),
    val tpForholdListe: List<TpForholdV1>? = emptyList(),
){

    override fun toString(): String {
        return "SimulerOffentligTjenestepensjonRequest(fodselsdato='$fodselsdato', sisteTpnr=$sisteTpnr, sprak=$sprak, simulertAFPOffentlig=$simulertAFPOffentlig, simulertAFPPrivat=$simulertAFPPrivat, sivilstandkode=$sivilstandkode, inntektListe=$inntektListe, pensjonsbeholdningsperiodeListe=$pensjonsbeholdningsperiodeListe, simuleringsperiodeListe=$simuleringsperiodeListe, simuleringsdataListe=$simuleringsdataListe, tpForholdListe=$tpForholdListe)"
    }
}

data class SimulertAFPOffentligV1 (
    val simulertAFPOffentligBrutto: Int,
    val tpi: Int = 0,
)

data class SimulertAFPPrivatV1 (
    val afpOpptjeningTotalbelop: Int,
    val kompensasjonstillegg: Double,
)

enum class SivilstandCodeEnumV1 {
    ENKE,
    GIFT,
    /**
     * Registrert partner
     */
    REPA,
    SKIL,
    UGIF
}

data class InntektV1(
    val datoFom: LocalDate,
    val inntekt: Double,
)


data class PensjonsbeholdningsperiodeV1 (
    val datoFom: LocalDate,
    val pensjonsbeholdning: Double,
    val garantipensjonsbeholdning: Double,
    val garantitilleggsbeholdning: Double,
)

data class SimuleringsperiodeV1 (
    val datoFom: LocalDate,
    val folketrygdUttaksgrad: Int,
    val stillingsprosentOffentlig: Int,
    val simulerAFPOffentligEtterfulgtAvAlder: Boolean,
)

data class SimuleringsdataV1 (
    val datoFom: LocalDate,
    val andvendtTrygdetid: Int,
    val poengArTom1991: Int,
    val poengArFom1992: Int,
    val uforegradVedOmregning: Int,
    val basisgp: Double,
    val basispt: Double,
    val basistp: Double,
    val delingstallUttak: Double,
    val forholdstallUttak: Double,
    val sluttpoengtall: Double,
)

data class TpForholdV1 (
    val tpnr: String,
    val opptjeningsperiodeListe: List<OpptjeningsperiodeV1> = emptyList(),
)

data class OpptjeningsperiodeV1(
    val stillingsprosent: Int,
    val datoFom: LocalDate,
    val datoTom: LocalDate,
    val faktiskHovedlonn: Int,
    val stillingsuavhengigTilleggslonn: Int,
    val aldersgrense: Int,
)
