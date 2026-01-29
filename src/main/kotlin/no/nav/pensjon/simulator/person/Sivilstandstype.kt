package no.nav.pensjon.simulator.person

//TODO Merge with Sivilstand and possibly SivilstandEnum
// PEN: no.nav.domain.pensjon.kjerne.sivilstand.Sivilstandstype
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