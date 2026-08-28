package no.nav.pensjon.simulator.afp.offentlig.tidsbegrenset.serviceberegn.api.v1.acl

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.domain.regler.enum.AFPtypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.SivilstandEnum
import java.time.LocalDate

data class ServiceberegningAfpSpecDto(
    @field:Schema(description = "startdato for uttak av pensjon (fra og med)")
    @field:NotNull
    val uttakFom: LocalDate,

    @field:Schema(description = "personopplysninger")
    @field:NotNull
    val personopplysninger: PersonopplysningerDto,

    @field:Schema(description = "opptjening i folketrygden")
    @field:NotNull
    var opptjeningListe: List<OpptjeningFolketrygdenDataDto>
)

data class PersonopplysningerDto(
    val pid: String? = null,
    val foedselsdato: LocalDate? = null,
    val angittAfpOrdning: AfpTypeDto? = null,
    val flyktning: Boolean? = null,
    val antallAarUtenlands: Int? = null,
    val utenlandsoppholdListe: List<UtlandSpecDto>? = null,
    val forventetArbeidsinntekt: Int? = null,
    val inntektMaanedenFoerAfp: Int? = null,
    val eps: EpsDataDto? = null
)

data class EpsDataDto(
    @field:Schema(description = "relasjonen til ektefelle/partner/samboer")
    @field:NotNull
    val relasjon: RelasjonDto,

    val angittSivilstatus: SivilstatusDto? = null,
    val registrertSivilstand: SivilstandDto? = null,
    val mottarPensjon: Boolean? = null,
    val harInntektOver1G: Boolean? = null,
    val harInntektOver2G: Boolean? = null,
    val tidligereGiftEllerBarnMedSamboer: Boolean? = null
)

data class OpptjeningFolketrygdenDataDto(
    val aar: Int? = null,
    val pensjonsgivendeInntekt: Int? = null,
    val omsorgspoeng: Double? = null,
    val registrertePensjonspoeng: Double? = null,
    val maxUfoeregrad: Int? = null
)

data class StatsborgerDto(
    val pid: String? = null,
    val statsborgerskap: LandkodeEnum? = null
)

data class RelasjonDto(
    val fom: LocalDate? = null,
    val person: StatsborgerDto? = null
)

data class UtlandSpecDto(
    @field:Schema(description = "startdato for utenlandsopphold (fra og med)")
    @field:NotNull
    val fom: LocalDate,

    val tom: LocalDate?,

    @field:Schema(description = "land")
    @field:NotNull
    val land: LandkodeEnum,

    @field:Schema(description = "hvorvidt personen arbeidet i løpet av utenlandsoppholdet")
    @field:NotNull
    val arbeidetUtenlands: Boolean
)

enum class AfpTypeDto(val internalValue: AFPtypeEnum) {
    // LO/NHO
    LONHO(internalValue = AFPtypeEnum.LONHO),

    // Spekter
    NAVO(internalValue = AFPtypeEnum.NAVO),

    // Finansnæringen
    FINANS(internalValue = AFPtypeEnum.FINANS),

    // AFP Stat
    AFPSTAT(internalValue = AFPtypeEnum.AFPSTAT),

    // AFP i kommunal sektor
    AFPKOM(internalValue = AFPtypeEnum.AFPKOM),

    // Konvertert offentlig
    KONV_O(internalValue = AFPtypeEnum.KONV_O),

    // Konvertert privat
    KONV_K(internalValue = AFPtypeEnum.KONV_K)
}

enum class SivilstandDto(val internalValue: SivilstandEnum) {
    /**
     * Enke/-mann
     */
    ENKE(internalValue = SivilstandEnum.ENKE),

    /**
     * Gift
     */
    GIFT(internalValue = SivilstandEnum.GIFT),

    /**
     * Gjenlevende partner
     */
    GJPA(internalValue = SivilstandEnum.GJPA),

    /**
     * Uoppgitt
     */
    NULL(internalValue = SivilstandEnum.NULL),

    /**
     * Registrert partner
     */
    REPA(internalValue = SivilstandEnum.REPA),

    /**
     * Separert partner
     */
    SEPA(internalValue = SivilstandEnum.SEPA),

    /**
     * Separert
     */
    SEPR(internalValue = SivilstandEnum.SEPR),

    /**
     * Skilt
     */
    SKIL(internalValue = SivilstandEnum.SKIL),

    /**
     * Skilt partner
     */
    SKPA(internalValue = SivilstandEnum.SKPA),

    /**
     * Ugift
     */
    UGIF(internalValue = SivilstandEnum.UGIF)
}

enum class SivilstatusDto(val internalValue: SivilstatusType) {
    ENKE(internalValue = SivilstatusType.ENKE),
    GIFT(internalValue = SivilstatusType.GIFT),
    GLAD(internalValue = SivilstatusType.GLAD),
    GJES(internalValue = SivilstatusType.GJES),
    GJPA(internalValue = SivilstatusType.GJPA),
    GJSA(internalValue = SivilstatusType.GJSA),
    REPA(internalValue = SivilstatusType.REPA),
    PLAD(internalValue = SivilstatusType.PLAD),
    SAMB(internalValue = SivilstatusType.SAMB),
    SEPR(internalValue = SivilstatusType.SEPR),
    SEPA(internalValue = SivilstatusType.SEPA),
    SKIL(internalValue = SivilstatusType.SKIL),
    SKPA(internalValue = SivilstatusType.SKPA),
    UGIF(internalValue = SivilstatusType.UGIF),
    NULL(internalValue = SivilstatusType.NULL)
}