package no.nav.pensjon.simulator.core.domain.regler.beregning2011

class BeregnetInntekt {

    /**
     * Sum av ytelse og andre inntekter for relevant bruker.
     */
    var forventetInntekt: Int = 0

    /**
     * Faktisk utbetalt ytelse og fremskrevet ytelse fra folketrygden ved virk
     * som inngår i avkortning av barnetillegg.
     */
    var ytelseFolketrygd: Double = 0.0

    /**
     * Faktisk utbetalt ytelse og fremskrevet ytelse som ikke er en del av folketrygden
     * ved virk som inngår i avkortning av barnetillegg. Feks. AFP privat
     */
    var ytelseIkkeFolketrygd: Double = 0.0

    /**
     * Sum av relevante inntekter fra inntektskomponenten.
     */
    var inntektIK: Double = 0.0

    /**
     * Høyeste av akkumulert arbeidsinntekt hittil i år og forventet arbeidsinntekt.
     */
    var inntektArbeid: Double = 0.0

    /**
     * Høyeste av akkumulert andre ytelser hittil i år og forventet andre ytelser.
     */
    var andreYtelser: Double = 0.0

    constructor() {}

    constructor(beregnetInntekt: BeregnetInntekt) {
        forventetInntekt = beregnetInntekt.forventetInntekt
        ytelseFolketrygd = beregnetInntekt.ytelseFolketrygd
        ytelseIkkeFolketrygd = beregnetInntekt.ytelseIkkeFolketrygd
        inntektIK = beregnetInntekt.inntektIK
        inntektArbeid = beregnetInntekt.inntektArbeid
        andreYtelser = beregnetInntekt.andreYtelser
    }

    constructor(
            forventetInntekt: Int = 0,
            ytelseFolketrygd: Double = 0.0,
            ytelseIkkeFolketrygd: Double = 0.0,
            inntektIK: Double = 0.0,
            inntektArbeid: Double = 0.0,
            andreYtelser: Double = 0.0
    ) {
        this.forventetInntekt = forventetInntekt
        this.ytelseFolketrygd = ytelseFolketrygd
        this.ytelseIkkeFolketrygd = ytelseIkkeFolketrygd
        this.inntektIK = inntektIK
        this.inntektArbeid = inntektArbeid
        this.andreYtelser = andreYtelser
    }

}
