package no.nav.pensjon.simulator.alderspensjon.api.nav.viapen.acl.v2.spec

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING
import no.nav.pensjon.simulator.core.afp.AfpOrdningType
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.person.Pid.Companion.redact
import no.nav.pensjon.simulator.tech.json.Stringifier.listAsString
import no.nav.pensjon.simulator.tech.json.Stringifier.textAsString
import java.time.LocalDate
import java.util.*

/**
 * Maps 1-to-1 with no.nav.pensjon.pen.domain.api.kalkulator.SimuleringEtter2011
 *              and no.nav.pensjon.pen.domain.api.kalkulator.SimuleringEtter2011Dto in PEN
 * (same as no.nav.pensjon.pen.domain.api.kalkulator.SimuleringEtter2011 in PSELV)
 */
data class NavSimuleringSpecV2(
    val simuleringId: Long? = null,
    val simuleringType: NavSimuleringTypeSpecV2? = null,
    val simuleringNavn: String? = null,
    @JsonFormat(shape = STRING, pattern = "yyyy-MM-dd") val lagringstidspunkt: LocalDate? = null,
    val fnr: String? = null,
    val fnrAvdod: String? = null,
    val fodselsar: Int? = null,
    val offentligAfpRett: Boolean? = null,
    val privatAfpRett: Boolean? = null,
    val simuleringsvalgOffentligAfp: Boolean? = null,
    val samtykke: Boolean? = null,
    val forventetInntekt: Int? = null,
    val antArInntektOverG: Int? = null,
    val forsteUttakDato: Date? = null, // epoch value in JSON
    val utg: UttakGradKode? = null,
    val inntektUnderGradertUttak: Int? = null,
    var heltUttakDato: Date? = null, // epoch value in JSON; mutable
    val inntektEtterHeltUttak: Int? = null,
    val antallArInntektEtterHeltUttak: Int? = null,
    val utenlandsopphold: Int? = null,
    val flyktning: Boolean? = null,
    val sivilstatus: NavSivilstandSpecV2? = null,
    var epsPensjon: Boolean? = null, // mutable (in AlderspensjonVilkaarsproeverOgBeregner.vilkaarsproevOgBeregnAlder)
    val eps2G: Boolean? = null,
    val afpOrdning: AfpOrdningType? = null,
    val afpInntektMndForUttak: Int? = null,
    val dodsdato: Date? = null, // epoch value in JSON
    val avdodAntallArIUtlandet: Int? = null,
    val avdodInntektForDod: Int? = null,
    val inntektAvdodOver1G: Boolean? = null,
    val avdodMedlemAvFolketrygden: Boolean? = null,
    val avdodFlyktning: Boolean? = null,
    val simulerForTp: Boolean? = null,
    val tpOrigSimulering: Boolean = false,
    val utenlandsperiodeForSimuleringList: List<NavSimuleringUtlandPeriodeV2> = listOf(),
    // Not used in PSELV, but included to avoid failing on unknown properties:
    val ansettelsessektor: String? = null,
    val brukerRegTPListe: List<NavSimuleringBrukerRegTjenestepensjonSpecDummyV2> = emptyList(),
    val stillingsprosentOffHeltUttak: String? = null,
    val stillingsprosentOffGradertUttak: String? = null,
    val fremtidigInntektList: List<NavSimuleringFremtidigInntektSpecDummyV2> = emptyList(),
    val changeStamp: NavSimuleringChangeStampSpecDummyV2? = null
) {
    /**
     * toString with redacted person ID
     */
    override fun toString() =
        "{ \"fnr\": ${textAsString(redact(fnr))}, " +
                "\"fnrAvdod\": ${textAsString(redact(fnrAvdod))}, " +
                "\"simuleringType\": ${textAsString(simuleringType)}, " +
                "\"sivilstatus\": ${textAsString(sivilstatus)}, " +
                "\"forventetInntekt\": $forventetInntekt, " +
                "\"inntektUnderGradertUttak\": $inntektUnderGradertUttak, " +
                "\"inntektEtterHeltUttak\": $inntektEtterHeltUttak, " +
                "\"antallArInntektEtterHeltUttak\": $antallArInntektEtterHeltUttak, " +
                "\"epsPensjon\": $epsPensjon, " +
                "\"eps2G\": $eps2G, " +
                "\"utenlandsopphold\": $utenlandsopphold, " +
                "\"simuleringType\": ${textAsString(simuleringType)}, " +
                "\"fremtidigInntektList\": ${listAsString(fremtidigInntektList)}, " +
                "\"utenlandsperiodeForSimuleringList\": ${listAsString(utenlandsperiodeForSimuleringList)}, " +
                "\"forsteUttakDato\": ${textAsString(forsteUttakDato)}, " +
                "\"utg\": ${textAsString(utg)}, " +
                "\"heltUttakDato\": ${textAsString(heltUttakDato)} }"
}

// Maps 1-to-1 with no.nav.pensjon.pen.domain.api.kalkulator.UtenlandsperiodeForSimulering in PEN
// (which is the same as no.nav.pensjon.pen.domain.api.kalkulator.UtenlandsperiodeForSimulering in PSELV)
data class NavSimuleringUtlandPeriodeV2(
    val land: LandkodeEnum,
    val arbeidetIUtland: Boolean = false,
    val periodeFom: Date,
    val periodeTom: Date?
)

/**
 * Dummy class required to avoid failing on unknown properties.
 */
data class NavSimuleringBrukerRegTjenestepensjonSpecDummyV2(
    val navnOrdningTP: String?,
    val utgTP: String?,
    val utbetStartTP: String?,
    val utbetSluttTP: String?,
    val belopTP: Int?
)

/**
 * Dummy class required to avoid failing on unknown properties.
 */
data class NavSimuleringFremtidigInntektSpecDummyV2(
    val datoFom: Date,
    val arliginntekt: Int?
)

/**
 * Dummy class required to avoid failing on unknown properties.
 */
data class NavSimuleringChangeStampSpecDummyV2(
    val createdDate: Date,
    val createdBy: String,
    val updatedDate: Date,
    val updatedBy: String
)
