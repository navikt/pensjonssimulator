package no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.acl

import java.time.LocalDate

data class HentPrognoseRequestDto(
    var fnr: String,
    var fodselsdato: LocalDate,
    var sisteTpnr: String,
    var sprak: String? = null,
    var simulertAFPOffentlig: SimulertAFPOffentligDto? = null,
    var simulertAFPPrivat: SimulertAFPPrivatDto? = null,
    var sivilstandkode: SivilstandCodeEnumDto,
    var inntektListe: List<InntektDto>,
    var pensjonsbeholdningsperiodeListe: List<PensjonsbeholdningsperiodeDto> = emptyList(),
    var simuleringsperiodeListe: List<SimuleringsperiodeDto>,
    var simuleringsdataListe: List<SimuleringsdataDto>,
    var tpForholdListe: List<TpForholdDto> = emptyList()
)

data class SimulertAFPOffentligDto(
    val simulertAFPOffentligBrutto: Int,
    val tpi: Int
)

data class SimulertAFPPrivatDto(
    val afpOpptjeningTotalbelop: Int,
    val kompensasjonstillegg: Double
)

enum class SivilstandCodeEnumDto {
    ENKE,
    GIFT,
    REPA,
    SKIL,
    UGIF
}

data class InntektDto(
    val datoFom: LocalDate,
    val inntekt: Double
)

data class PensjonsbeholdningsperiodeDto(
    val datoFom: LocalDate,
    val pensjonsbeholdning: Int,
    val garantipensjonsbeholdning: Int = 0,
    val garantitilleggsbeholdning: Int = 0
)

data class SimuleringsperiodeDto(
    val datoFom: LocalDate,
    val folketrygdUttaksgrad: Int,
    val stillingsprosentOffentlig: Int,
    val simulerAFPOffentligEtterfulgtAvAlder: Boolean
)

data class SimuleringsdataDto(
    val datoFom: LocalDate,
    val andvendtTrygdetid: Int,
    val poengArTom1991: Int?,
    val poengArFom1992: Int?,
    val uforegradVedOmregning: Int?,
    val basisgp: Double? ,
    val basispt: Double?,
    val basistp: Double?,
    val delingstallUttak: Double?,
    val forholdstallUttak: Double,
    val sluttpoengtall: Double?
)

data class TpForholdDto(
    val tpnr: String,
    val opptjeningsperiodeListe: List<OpptjeningsperiodeDto>
)

data class OpptjeningsperiodeDto(
    val datoFom: LocalDate,
    val datoTom: LocalDate?,
    val stillingsprosent: Double,
    val aldersgrense: Int?,
    val faktiskHovedlonn: Int?,
    val stillingsuavhengigTilleggslonn: Int?
)