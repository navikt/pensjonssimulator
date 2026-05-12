package no.nav.pensjon.simulator.afp.offentlig.pre2025

import no.nav.pensjon.simulator.validity.ProblemType

/**
 * Ref. github.com/navikt/pensjon-regler/blob/master/repository/nav-repository-pensjon/src/main/kotlin/no/nav/domain/pensjon/regler/repository/komponent/vilkarafp/regler/Vilk%C3%A5rspr%C3%B8vAFPRS.kt
 */
enum class TidsbegrensetOffentligAfpAvslagAarsak(
    val externalValue: String,
    val problemType: ProblemType
) {
    FOR_TIDLIG_VIRKNING(
        externalValue = "VilkarsprovAFPRS.VIRKtidligereEnn01082000",
        problemType = ProblemType.UGYLDIG_UTTAKSDATO
    ),
    FOR_LAV_ALDER(
        externalValue = "VilkarsprovAFPRS.AlderForLav",
        problemType = ProblemType.PERSON_FOR_LAV_ALDER
    ),
    FOR_HOEY_ALDER(
        externalValue = "VilkarsprovAFPRS.AlderForHoy",
        problemType = ProblemType.PERSON_FOR_HOEY_ALDER
    ),
    UTILSTREKKELIG_INNTEKT_SISTE_MAANED(
        externalValue = "VilkarsprovAFPRS.InntektSisteMndOverG",
        problemType = ProblemType.UTILSTREKKELIG_INNTEKT
    ),
    UTILSTREKKELIG_AARLIG_INNTEKT_VED_UTTAK(
        externalValue = "VilkarsprovAFPRS.InntektMinstGVArForUttak",
        problemType = ProblemType.UTILSTREKKELIG_INNTEKT
    ),
    UTILSTREKKELIG_ANTALL_POENGAAR(
        externalValue = "VilkarsprovAFPRS.AntallPoengarEtterFylte50Minst10",
        problemType = ProblemType.UTILSTREKKELIG_OPPTJENING
    ),
    UTILSTREKKELIG_AARLIG_GJENNOMSNITTSINNTEKT(
        externalValue = "VilkarsprovAFPRS.ErSnittetAvDe10HoyesteInntekteneSiden1967over2G",
        problemType = ProblemType.UTILSTREKKELIG_INNTEKT
    ),
    DEFAULT(
        externalValue = "(ukjent feilkode)",
        problemType = ProblemType.ANNEN_KLIENTFEIL
    );

    companion object {
        fun fromExternalValue(value: String?): TidsbegrensetOffentligAfpAvslagAarsak =
            entries.singleOrNull { it.externalValue.equals(value, true) } ?: DEFAULT
    }
}