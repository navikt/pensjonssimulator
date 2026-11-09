package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service.storebrand.acl

import no.nav.pensjon.simulator.person.FoedselsnummerUtil
import java.time.LocalDate

data class StorebrandSimulerTjenestepensjonRequest(
    val personId: String,
    val uttaksListe: List<Uttak>,
    val fremtidigInntektListe: List<StorebrandFremtidigInntekt>,
    val aarIUtlandetEtter16: Int,
    val epsPensjon: Boolean,
    val eps2G: Boolean
){
    override fun toString(): String {
        return FoedselsnummerUtil.redact("(personId=$personId, uttaksListe=$uttaksListe, " +
                "fremtidigInntektListe=$fremtidigInntektListe, aarIUtlandetEtter16=$aarIUtlandetEtter16, " +
                "epsPensjon=$epsPensjon, eps2G=$eps2G)")!!
    }

    fun toAuditString(): String {
        return "StorebrandSimulerTjenestepensjonRequest(personId=$personId, uttaksListe=$uttaksListe, " +
                "fremtidigInntektListe=$fremtidigInntektListe, aarIUtlandetEtter16=$aarIUtlandetEtter16, " +
                "epsPensjon=$epsPensjon, eps2G=$eps2G)"
    }
}

data class Uttak(
    val ytelseType: String,
    val fraOgMedDato: LocalDate,
    val uttaksgrad: Int?
)

data class StorebrandFremtidigInntekt(
    val aarligInntekt: Int,
    val fraOgMedDato: LocalDate,
)
