package no.nav.pensjon.simulator.person

//TODO Merge with SivilstandEnum?
// PEN: no.nav.domain.pensjon.kjerne.sivilstand.Sivilstandstype
/**
 * Ref. lovdata.no/forskrift/2017-07-14-1201/§3-1-1
 */
enum class Sivilstandstype {
    UOPPGITT,
    UGIFT,
    GIFT,
    ENKE_ELLER_ENKEMANN,
    SKILT,
    SEPARERT,
    REGISTRERT_PARTNER,
    SEPARERT_PARTNER,
    SKILT_PARTNER,
    GJENLEVENDE_PARTNER
}
