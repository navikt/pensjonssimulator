package no.nav.pensjon.simulator.tjenestepensjon.pre2025.api

import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v1.SivilstandCodeEnumV1
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.acl.SivilstandCodeEnumDto
import java.time.LocalDate

data class TjenestepensjonSimuleringPre2025Spec(
    val pid: Pid,
    val foedselsdato: LocalDate,
    val sisteTpOrdningsTpNummer: String,
    val simulertOffentligAfp: SimulertOffentligAfp?,
    val simulertPrivatAfp: SimulertPrivatAfp?,
    val sivilstand: SivilstandKode,
    val inntekter: List<Inntekt>,
    val pensjonsbeholdningsperioder: List<Pensjonsbeholdningsperiode>,
    val simuleringsperioder: List<Simuleringsperiode>,
    val simuleringsdata: List<Simuleringsdata>,
    val tpForhold: List<TpForhold>,
    )

data class SimulertOffentligAfp(
    val brutto: Int,
    val tidligerePensjonsgivendeInntekt: Int
)

data class SimulertPrivatAfp(
    val totalAfpBeholdning: Int,
    val kompensasjonstillegg: Double
)

enum class SivilstandKode(val dtoSivilstand: SivilstandCodeEnumV1, val remoteDtoSivilstand: SivilstandCodeEnumDto) {
    ENKE(SivilstandCodeEnumV1.ENKE, SivilstandCodeEnumDto.ENKE),
    GIFT(SivilstandCodeEnumV1.GIFT, SivilstandCodeEnumDto.GIFT),
    REGISTRERT_PARTNER(SivilstandCodeEnumV1.REPA, SivilstandCodeEnumDto.REPA),
    SKILT(SivilstandCodeEnumV1.SKIL, SivilstandCodeEnumDto.SKIL),
    UGIFT(SivilstandCodeEnumV1.UGIF, SivilstandCodeEnumDto.UGIF);

    companion object {
        fun fromDto(dto: SivilstandCodeEnumV1) = entries.firstOrNull { it.dtoSivilstand == dto } ?: throw IllegalArgumentException("Unknown dtoSivilstand: $dto")
    }
}

data class Inntekt(
    val fom: LocalDate,
    val beloep: Double
)

data class Pensjonsbeholdningsperiode(
    val fom: LocalDate,
    val pensjonsbeholdning: Double,
    val garantipensjonsbeholdning: Double,
    val garantitilleggsbeholdning: Double
)

data class Simuleringsperiode(
    val fom: LocalDate,
    val folketrygdUttaksgrad: Int,
    val stillingsprosentOffentlig: Int,
    val simulerAFPOffentligEtterfulgtAvAlder: Boolean
)

data class Simuleringsdata(
    val fom: LocalDate,
    val andvendtTrygdetid: Int,
    val poengAarTom1991: Int,
    val poengAarFom1992: Int,
    val ufoeregradVedOmregning: Int?,
    val basisGrunnpensjon: Double? ,
    val basisPensjonstillegg: Double?,
    val basisTilleggspensjon: Double?,
    val delingstallUttak: Double,
    val forholdstallUttak: Double,
    val sluttpoengtall: Double?
)

data class TpForhold(
    val tpNr: String,
    val opptjeningsperioder: List<Opptjeningsperiode>
)

data class Opptjeningsperiode(
    val fom: LocalDate,
    val tom: LocalDate?,
    val stillingsprosent: Int,
    val aldersgrense: Int?,
    val faktiskHovedloenn: Int?,
    val stillingsuavhengigTilleggsloenn: Int?
)

