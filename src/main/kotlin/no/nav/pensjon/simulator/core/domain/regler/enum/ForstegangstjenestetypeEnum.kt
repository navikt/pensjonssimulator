package no.nav.pensjon.simulator.core.domain.regler.enum

enum class ForstegangstjenestetypeEnum {
    TEKN,
    NORMAL,
    BEFAL,
    UKJENT;

    //--- Extra:
    companion object {
        private val valuesByName = entries.associateBy { it.name }

        fun fromValue(value: String?): ForstegangstjenestetypeEnum =
            valuesByName[value] ?: UKJENT
    }
}