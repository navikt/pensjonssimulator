package no.nav.pensjon.simulator.trygdetid

enum class AvtaleDatoKode(val avtaledato: String) {
    AUT1986(avtaledato = "01.06.1986"),
    AUT1994(avtaledato = "01.01.1994"), // EØS
    AUT1998(avtaledato = "01.06.1998"),
    BIH1992(avtaledato = "01.03.1992"),
    CAN1987(avtaledato = "01.01.1987"),
    CAN2014(avtaledato = "01.01.2014"),
    CHE1980(avtaledato = "01.11.1980"),
    CHE1994(avtaledato = "01.01.1994"),
    CHE2002(avtaledato = "01.06.2002"),
    EOS1994(avtaledato = "01.01.1994"),
    EOS2004(avtaledato = "01.05.2004"),
    EOS2007(avtaledato = "01.01.2007"),
    EOS2010(avtaledato = "01.06.2012"),
    EOS2014(avtaledato = "12.04.2014"),
    FRA1956(avtaledato = "01.07.1956"),
    FRA1994(avtaledato = "01.01.1994"), // EØS
    GBR1958(avtaledato = "01.04.1958"),
    GBR1991(avtaledato = "01.04.1991"),
    GBR1994(avtaledato = "01.01.1994"), // EØS
    HRV1991(avtaledato = "08.10.1991"),
    HRV2014(avtaledato = "12.04.2014"), // EØS
    IND2015(avtaledato = "01.01.2015"),
    ITA1962(avtaledato = "01.02.1962"),
    ITA1994(avtaledato = "01.01.1994"), // EØS
    LUX1992(avtaledato = "01.03.1992"),
    LUX1994(avtaledato = "01.01.1994"), // EØS
    LUX1998(avtaledato = "01.09.1998"),
    MNE2006(avtaledato = "03.06.2006"),
    NLD1990(avtaledato = "01.10.1990"),
    NLD1994(avtaledato = "01.01.1994"), // EØS
    NLD1997(avtaledato = "01.04.1997"),
    NOR1956(avtaledato = "01.11.1956"),
    NOR1982(avtaledato = "01.01.1982"),
    NOR1994(avtaledato = "01.01.1994"), // EØS
    NOR2004(avtaledato = "01.09.2004"),
    PRT1981(avtaledato = "01.09.1981"),
    PRT1994(avtaledato = "01.01.1994"), // EØS
    SRB2003(avtaledato = "29.05.2003"),
    SVN1991(avtaledato = "25.06.1991"),
    TUR1981(avtaledato = "01.06.1981"),

    // Noteveksling https://lovdata.no/dokument/TRAKTAT/traktat/1968-06-26-1
    USA1968(avtaledato = "26.06.1968"),

    USA1984(avtaledato = "01.07.1984"),
    USA2003(avtaledato = "01.09.2003"),
    YUG1976(avtaledato = "01.08.1976")
}
