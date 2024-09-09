package no.nav.pensjon.simulator.core.krav

// Corresponds to kjerne.kodetabeller.KravlinjeTypeCti with 'hovedKravLinje' value defined.
enum class KravlinjeTypePlus(
    val description: String,
    val erHovedkravlinje: Boolean
) {

    AFP("AFP", true),
    AFP_PRIVAT("AFP Privat", true),
    ANKE("Anke", true),
    AP("Alderspensjon", true),
    BP("Barnepensjon", true),
    BT("Barnetillegg", false),
    EO("AFP-etteroppgjør", true),
    ERSTATNING("Erstatning", true),
    ET("Ektefelletillegg", false),
    ETTERGIV_GJELD("Ettergivelse av gjeld", true),
    FAST_UTG_INST("Dekning faste utgifter inst.opphold", false),
    FP("Familiepleierytelse", true),
    GJP("Gjenlevendeytelse", true),
    GJR("Gjenlevenderettighet", false),
    GY("Gammel yrkesskade", true),
    HJBIDRAG("Hjelpeløshetsbidrag", false),
    HJELP_HUS("Tillegg til hjelp i huset", false),
    IK("Inntektskontroll", true),
    KLAGE("Klage", true),
    KP("Krigspensjon", true),
    MTK("Merskatt tilbakekreving", true),
    OG("Godskriving omsorgspoeng", true),
    OO("Overføring omsorgspoeng", true),
    OTK("Omgjøring av tilbakekreving", true),
    SAK_OMKOST("Saksomkostninger", true),
    TK("Tilbakekreving", true),
    UP("Uførepensjon", true),
    UT("Uføretrygd", true),
    UT_GJT("Gjenlevendetillegg til uføretrygd", false);
}
