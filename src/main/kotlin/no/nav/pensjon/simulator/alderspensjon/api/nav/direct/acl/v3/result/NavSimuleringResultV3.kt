package no.nav.pensjon.simulator.alderspensjon.api.nav.direct.acl.v3.result

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL

// Corresponds to SimulatorPersonligSimuleringResult in pensjonskalkulator-backend
@JsonInclude(NON_NULL)
data class NavSimuleringResultV3(
    val alderspensjonListe: List<NavAlderspensjonV3>,
    val alderspensjonMaanedsbeloep: NavMaanedsbeloepV3?,
    val pre2025OffentligAfp: NavPre2025OffentligAfp?,
    val privatAfpListe: List<NavPrivatAfpV3>,
    val livsvarigOffentligAfpListe: List<NavLivsvarigOffentligAfpV3>,
    val vilkaarsproeving: NavVilkaarsproevingResultatV3,

    /**
     * Kapittel 19: Angir om nok trygdetid for alderspensjon
     * Kapittel 20: Angir om nok trygdetid for garantipensjon
     */
    val tilstrekkeligTrygdetidForGarantipensjon: Boolean?, //TODO rename to harTilstrekkeligTrygdetid

    val trygdetid: Int,
    val opptjeningGrunnlagListe: List<NavOpptjeningGrunnlagV3>,
    val error: NavSimuleringErrorV3? = null
)

@JsonInclude(NON_NULL)
data class NavAlderspensjonV3(
    val alderAar: Int,
    val beloep: Int,
    val inntektspensjon: Int?,
    val garantipensjon: Int?,
    val delingstall: Double?,
    val pensjonBeholdningFoerUttak: Int?,
    val andelsbroekKap19: Double?,
    val andelsbroekKap20: Double?,
    val sluttpoengtall: Double?,
    val trygdetidKap19: Int?,
    val trygdetidKap20: Int?,
    val poengaarFoer92: Int?,
    val poengaarEtter91: Int?,
    val forholdstall: Double?,
    val grunnpensjon: Int?,
    val tilleggspensjon: Int?,
    val pensjonstillegg: Int?,
    val skjermingstillegg: Int?,
    val kapittel19Gjenlevendetillegg: Int?
)

@JsonInclude(NON_NULL)
data class NavMaanedsbeloepV3(
    val gradertUttakBeloep: Int?,
    val heltUttakBeloep: Int
)

data class NavPrivatAfpV3(
    val alderAar: Int,
    val beloep: Int,
    val kompensasjonstillegg: Int,
    val kronetillegg: Int,
    val livsvarig: Int,
    val maanedligBeloep: Int
)

data class NavLivsvarigOffentligAfpV3(
    val alderAar: Int,
    val beloep: Int,
    val maanedligBeloep: Int
)

data class NavPre2025OffentligAfp(
    val alderAar: Int,
    val totaltAfpBeloep: Int,
    val tidligereArbeidsinntekt: Int,
    val grunnbeloep: Int,
    val sluttpoengtall: Double,
    val trygdetid: Int,
    val poengaarTom1991: Int,
    val poengaarFom1992: Int,
    val grunnpensjon: Int,
    val tilleggspensjon: Int,
    val afpTillegg: Int,
    val saertillegg: Int,
    val afpGrad: Int,
    val afpAvkortetTil70Prosent: Boolean
)

@JsonInclude(NON_NULL)
data class NavVilkaarsproevingResultatV3(
    val vilkaarErOppfylt: Boolean,
    val alternativ: NavAlternativtResultatV3?
)

data class NavOpptjeningGrunnlagV3(
    val aar: Int,
    val pensjonsgivendeInntektBeloep: Int
)

@JsonInclude(NON_NULL)
data class NavSimuleringErrorV3(
    val exception: String?,
    val message: String
)

@JsonInclude(NON_NULL)
data class NavAlternativtResultatV3(
    val gradertUttakAlder: NavAlderV3?,
    val uttaksgrad: Int,
    val heltUttakAlder: NavAlderV3
)

data class NavAlderV3(
    val aar: Int,
    val maaneder: Int
)
