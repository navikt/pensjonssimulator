package no.nav.pensjon.simulator.uttak.client.pen.acl

import no.nav.pensjon.simulator.person.Sivilstand

/**
 * The 'externalValue' is sivilstand values used by PEN (pensjonsfaglig kjerne).
 * PEN supports the concept of 'samboer' (unlike Folkeregisteret/PDL).
 * The source of PEN's sivilstand values is:
 * https://github.com/navikt/pesys/blob/main/pen/domain/nav-domain-pensjon-pen-api/src/main/java/no/nav/domain/pensjon/kjerne/kodetabeller/SivilstandTypeCode.java
 */
enum class PenSivilstand(val externalValue: String, val internalValue: Sivilstand) {

    ENKE_ELLER_ENKEMANN("ENKE", Sivilstand.ENKE_ELLER_ENKEMANN),
    GIFT("GIFT", Sivilstand.GIFT),
    GIFT_LEVER_ADSKILT("GLAD", Sivilstand.GIFT),
    GJENLEVENDE_PARTNER("GJPA", Sivilstand.GJENLEVENDE_PARTNER),
    REGISTRERT_PARTNER("REPA", Sivilstand.REGISTRERT_PARTNER),
    REGISTRERT_PARTNER_LEVER_ADSKILT("PLAD", Sivilstand.REGISTRERT_PARTNER),
    SAMBOER("SAMB", Sivilstand.SAMBOER),
    SEPARERT("SEPR", Sivilstand.SEPARERT),
    SEPARERT_PARTNER("SEPA", Sivilstand.SEPARERT_PARTNER),
    SKILT("SKIL", Sivilstand.SKILT),
    SKILT_PARTNER("SKPA", Sivilstand.SKILT_PARTNER),
    UGIFT("UGIF", Sivilstand.UGIFT),
    GJENLEVENDE_ETTER_SAMLIVSBRUDD("GJES", Sivilstand.UOPPGITT),
    GJENLEVENDE_SAMBOER("GJSA", Sivilstand.UOPPGITT),
    UDEFINERT("NULL", Sivilstand.UOPPGITT);


    companion object {
        fun fromInternalValue(sivilstand: Sivilstand?): PenSivilstand =
            when (sivilstand) {
                Sivilstand.UOPPGITT -> UDEFINERT // ambiguous UDEFINERT/GJENLEVENDE_*
                Sivilstand.GIFT -> GIFT // ambiguous GIFT/-_LEVER_ADSKILT
                Sivilstand.REGISTRERT_PARTNER -> REGISTRERT_PARTNER // ambiguous REGISTRERT_PARTNER/-_LEVER_ADSKILT
                else -> entries.firstOrNull { it.internalValue == sivilstand } ?: UDEFINERT
            }
    }
}
