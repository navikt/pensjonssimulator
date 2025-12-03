package no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v2

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.swagger.v3.oas.annotations.media.Schema
import no.nav.pensjon.simulator.person.Pid
import java.time.LocalDate

@JsonIgnoreProperties(ignoreUnknown = true)
data class SimuleringEtter2011SpecV2(
    @param:Schema(description = "Angir hva som er simulert i den lagrede simuleringen.")
    var simuleringType: SimuleringTypeSpecV2,

    @param:Schema(description = "Brukeroppgitt navn ved lagring. Benyttes ikke ved simulering, kun ved lagring/henting.")
    var simuleringNavn: String? = null,

    @param:Schema(description = "Tidspunktet simuleringen ble lagret på. Benyttes ikke ved simulering, kun ved lagring/henting.")
    var lagringstidspunkt: LocalDate? = null,

    @param:Schema(description = "Fødselsnummer for personen som har denne simuleringen")
    var fnr: Fnr,

    @param:Schema(description = "Avdødes fødselsnummer. Benyttes ved simulering av gjenlevenderett.")
    var fnrAvdod: Fnr? = null,

    @param:Schema(description = "Brukers fødselsår. Benyttes dersom bruker ikke er innlogget.")
    var fodselsar: Int? = null,

    @param:Schema(description = "Brukeroppgitt rett til AFP i offentlig sektor. Benyttes ikke ved simulering, kun ved lagring/henting.")
    var offentligAfpRett: Boolean? = null,

    @param:Schema(description = "Brukeroppgitt rett til AFP i privat sektor. Benyttes ikke ved simulering, kun ved lagring/henting.")
    var privatAfpRett: Boolean? = null,

    @param:Schema(description = "Brukeroppgitt valg av simuleringstype for bruker med rett til AFP i offentlig sektor. Benyttes ikke ved simulering, kun ved lagring/henting.")
    var simuleringsvalgOffentligAfp: Boolean? = null,

    @param:Schema(description = "Samtykke til å hente inn informasjon fra TP-ordninger Nav har grensesnitt mot.")
    var samtykke: Boolean? = null,

    @param:Schema(description = "Forventet/gj.snittlig årlig inntekt før første uttak av pensjon.")
    var forventetInntekt: Int? = null,

    @param:Schema(description = "Antall år med inntekt over 1G før første uttak (kun ikke-innlogget).")
    var antArInntektOverG: Int? = null,

    @param:Schema(description = "Dato for første uttak av pensjon.")
    var forsteUttakDato: LocalDate? = null,

    @param:Schema(description = "Uttaksgrad fra første uttak.")
    var utg: UttaksgradSpecV2? = null,

    @param:Schema(description = "Årlig inntekt fra gradert uttak til helt uttak.")
    var inntektUnderGradertUttak: Int? = null,

    @param:Schema(description = "Dato for helt uttak (100 %).")
    var heltUttakDato: LocalDate? = null,

    @param:Schema(description = "Årlig inntekt etter helt uttak.")
    var inntektEtterHeltUttak: Int = 0,

    @param:Schema(description = "Antall år med inntekt etter helt uttak.")
    var antallArInntektEtterHeltUttak: Int = 0,

    @param:Schema(description = "Antall år i utlandet etter fylte 16 år.")
    var utenlandsopphold: Int? = null,

    @param:Schema(description = "Har bruker flyktningstatus.")
    var flyktning: Boolean? = null,

    @param:Schema(description = "Sivilstatus under uttak.")
    var sivilstatus: SivilstatusSpecV2? = null,

    @param:Schema(description = "Om EPS mottar pensjon/AFP.")
    var epsPensjon: Boolean? = null,

    @param:Schema(description = "Om EPS har inntekt over 2G.")
    var eps2G: Boolean? = null,

    @param:Schema(description = "Brukers AFP-ordning (offentlig).")
    var afpOrdning: AfpOrdningTypeSpecV2? = null,

    @param:Schema(description = "Inntekt måneden før AFP-uttak (offentlig AFP).")
    var afpInntektMndForUttak: Boolean? = null,

    @param:Schema(description = "Ektefelles dødsdato.")
    var dodsdato: LocalDate? = null,

    @param:Schema(description = "Avdøds antall år i utlandet.")
    var avdodAntallArIUtlandet: Int? = null,

    @param:Schema(description = "Avdøds inntekt året før død.")
    var avdodInntektForDod: Int? = null,

    @param:Schema(description = "Om avdød hadde inntekt over 1G.")
    var inntektAvdodOver1G: Boolean? = null,

    @param:Schema(description = "Om avdød var medlem av folketrygden.")
    var avdodMedlemAvFolketrygden: Boolean? = null,

    @param:Schema(description = "Om avdød var flyktning.")
    var avdodFlyktning: Boolean? = null,

    @param:Schema(description = "Kalles dette for simulering mot TP? (2025-regelverk).")
    var simulerForTp: Boolean? = null,

    @param:Schema(description = "Utenlandsperioder brukt i simulering.")
    var utenlandsperiodeForSimuleringList: List<UtenlandsperiodeForSimuleringV2> = listOf(),

    // Not used in PSELV but included to avoid failing on unknown properties:
    @param:Schema(description = "Fremtidige inntekter brukt i simulering.")
    var fremtidigInntektList: List<FremtidigInntektV2> = listOf(),

    @param:Schema(description = "Egenregistrerte TP-er (lagring/visning).")
    var brukerRegTPListe: List<BrukerRegTjenestepensjonV2>? = listOf(),

    @param:Schema(description = "Brukeroppgitt ansettelsessektor. Benyttes ikke ved simulering, kun ved lagring/henting. Eksempel: ANNET, OFFENTLIG, PRIVAT")
    var ansettelsessektor: AnsattTypeCodeV2? = null,

    @param:Schema(description = "Offentlig stillingsprosent ved helt uttak.")
    var stillingsprosentOffHeltUttak: StillingsprOffCodeV2? = null,

    @param:Schema(description = "Offentlig stillingsprosent ved gradert uttak.")
    var stillingsprosentOffGradertUttak: StillingsprOffCodeV2? = null,
)

data class Fnr(val pid: String) {
    override fun toString(): String {
        return Pid.redact(pid)
    }
}