package no.nav.pensjon.simulator.alderspensjon.api.nav.viapen.acl.v2.spec

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING
import no.nav.pensjon.simulator.core.afp.AfpOrdningType
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import java.time.LocalDate
import java.util.Date

/**
 * Maps 1-to-1 with no.nav.pensjon.pen.domain.api.kalkulator.SimuleringEtter2011
 *              and no.nav.pensjon.pen.domain.api.kalkulator.SimuleringEtter2011Dto in PEN
 * (same as no.nav.pensjon.pen.domain.api.kalkulator.SimuleringEtter2011 in PSELV)
 */
data class NavSimuleringSpecV2 (
    val simuleringId: Long? = null,
    val simuleringType: NavSimuleringTypeSpecV2? = null,
    val simuleringNavn: String? = null,
    @JsonFormat(shape = STRING, pattern = "yyyy-MM-dd", timezone = "CET") val lagringstidspunkt: LocalDate? = null,
    val fnr: NavSimuleringPersonIdComboSpecV2? = null,
    val fnrAvdod: NavSimuleringPersonIdComboSpecV2? = null,
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
    val heltUttakDato: Date? = null, // epoch value in JSON
    val inntektEtterHeltUttak: Int? = null,
    val antallArInntektEtterHeltUttak: Int? = null,
    val utenlandsopphold: Int? = null,
    val flyktning: Boolean? = null,
    val sivilstatus: NavSivilstandSpecV2? = null,
    val epsPensjon: Boolean? = null,
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
    val utenlandsperiodeForSimuleringList: List<NavSimuleringUtlandPeriodeV2> = listOf()
    // Not used in PSELV:
    // ansettelsessektor
    // brukerRegTPListe
    // stillingsprosentOffHeltUttak
    // stillingsprosentOffGradertUttak
    // fremtidigInntektList
    // changeStamp
)

data class NavSimuleringPersonIdComboSpecV2(
    val pid: String,
    val dnummer: Boolean? = false,
    val npid: Boolean? = false,
    val pidInvalidWithBostnummer: Boolean? = false
)

// Maps 1-to-1 with no.nav.pensjon.pen.domain.api.kalkulator.UtenlandsperiodeForSimulering in PEN
// (which is the same as no.nav.pensjon.pen.domain.api.kalkulator.UtenlandsperiodeForSimulering in PSELV)
data class NavSimuleringUtlandPeriodeV2(
    val land: LandkodeEnum,
    val arbeidetIUtland: Boolean = false,
    val periodeFom: LocalDate,
    val periodeTom: LocalDate?
)
