package no.nav.pensjon.simulator.core.domain.regler.enum

// Corresponds to kjerne.kodetabeller.KravlinjeTypeCti with 'hovedKravLinje' value defined.
enum class KravlinjeTypeEnum(
    val description: String,
    val erHovedkravlinje: Boolean
) {
    // NB: Defined by pensjon-regler, but not PEN: EG, TB, UT_ET
    AFP(description = "AFP", erHovedkravlinje = true),
    AFP_PRIVAT(description = "AFP Privat", erHovedkravlinje = true),
    ANKE(description = "Anke", erHovedkravlinje = true),
    AP(description = "Alderspensjon", erHovedkravlinje = true),
    BP(description = "Barnepensjon", erHovedkravlinje = true),
    BT(description = "Barnetillegg", erHovedkravlinje = false),
    EO(description = "AFP-etteroppgjør", erHovedkravlinje = true),
    ERSTATNING(description = "Erstatning", erHovedkravlinje = true),
    ET(description = "Ektefelletillegg", erHovedkravlinje = false),
    ETTERGIV_GJELD(description = "Ettergivelse av gjeld", erHovedkravlinje = true),
    FAST_UTG_INST(description = "Dekning faste utgifter inst.opphold", erHovedkravlinje = false),
    FP(description = "Familiepleierytelse", erHovedkravlinje = true),
    GJP(description = "Gjenlevendeytelse", erHovedkravlinje = true),
    GJR(description = "Gjenlevenderettighet", erHovedkravlinje = false),
    GY(description = "Gammel yrkesskade", erHovedkravlinje = true),
    HJBIDRAG(description = "Hjelpeløshetsbidrag", erHovedkravlinje = false),
    HJELP_HUS(description = "Tillegg til hjelp i huset", erHovedkravlinje = false),
    IK(description = "Inntektskontroll", erHovedkravlinje = true),
    KLAGE(description = "Klage", erHovedkravlinje = true),
    KP(description = "Krigspensjon", erHovedkravlinje = true),
    MTK(description = "Merskatt tilbakekreving", erHovedkravlinje = true),
    OG(description = "Godskriving omsorgspoeng", erHovedkravlinje = true),
    OO(description = "Overføring omsorgspoeng", erHovedkravlinje = true),
    OTK(description = "Omgjøring av tilbakekreving", erHovedkravlinje = true),
    SAK_OMKOST(description = "Saksomkostninger", erHovedkravlinje = true),
    TK(description = "Tilbakekreving", erHovedkravlinje = true),
    UP(description = "Uførepensjon", erHovedkravlinje = true),
    UT(description = "Uføretrygd", erHovedkravlinje = true),
    UT_GJT(description = "Gjenlevendetillegg til uføretrygd", erHovedkravlinje = false);
}
