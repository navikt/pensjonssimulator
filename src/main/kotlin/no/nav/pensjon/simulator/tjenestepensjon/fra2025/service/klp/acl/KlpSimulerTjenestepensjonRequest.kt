package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.klp.acl

import no.nav.pensjon.simulator.person.FoedselsnummerUtil
import java.time.LocalDate

data class KlpSimulerTjenestepensjonRequest(
    val personId: String,
    val uttaksListe: List<Uttak>,
    val fremtidigInntektsListe: List<KlpFremtidigInntekt>,
    val arIUtlandetEtter16: Int,
    val epsPensjon: Boolean,
    val eps2G: Boolean
){
    override fun toString(): String {
        return FoedselsnummerUtil.redact("(personId=$personId, uttaksListe=$uttaksListe, " +
                "fremtidigInntektsListe=$fremtidigInntektsListe, arIUtlandetEtter16=$arIUtlandetEtter16, " +
                "epsPensjon=$epsPensjon, eps2G=$eps2G)")!!
    }

    fun toAuditString(): String {
        return "KlpSimulerTjenestepensjonRequest(personId=$personId, uttaksListe=$uttaksListe, " +
                "fremtidigInntektsListe=$fremtidigInntektsListe, arIUtlandetEtter16=$arIUtlandetEtter16, " +
                "epsPensjon=$epsPensjon, eps2G=$eps2G)"
    }
}

data class Uttak(
    val ytelseType: String,
    val fraOgMedDato: LocalDate,
    val uttaksgrad: Int
)

data class KlpFremtidigInntekt(
    val fraOgMedDato: LocalDate,
    val arligInntekt: Int
)
