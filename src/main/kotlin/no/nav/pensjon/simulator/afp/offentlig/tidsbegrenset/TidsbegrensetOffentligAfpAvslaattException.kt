package no.nav.pensjon.simulator.afp.offentlig.tidsbegrenset

class TidsbegrensetOffentligAfpAvslaattException(
    message: String,
    val aarsak: TidsbegrensetOffentligAfpAvslagAarsak? = null
) : RuntimeException(message)