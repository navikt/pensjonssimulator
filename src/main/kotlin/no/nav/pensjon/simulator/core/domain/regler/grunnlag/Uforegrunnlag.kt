package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.enum.YrkeEnum
import java.time.LocalDate

// 2026-04-23
class Uforegrunnlag {
    /**
     * Uføregrad, 0-100
     */
    var ufg = 0

    /**
     * Dato for uføretidspunktet.
     */
    var uftLd: LocalDate? = null

    /**
     * Virkningstidspunkt for hendelsen Uforegrunnlaget representerer.
     */
    var uftVirkLd: LocalDate? = null

    /**
     * Inntekt før uførhet.Normalinntekten personen ville hatt på
     * uføretidspunktet dersom personen ikke hadde blitt syk. Settes av saksbehandler.
     * ifu relaterer seg til Første uføretidspunkt. Det er uforegrunnlag.uft ved Første gangs UP.
     * Ellers hentes den fra historikken.
     */
    var ifu = 0

    /**
     *
     *
     * Fødselsår for yngste barn. når denne variabelen er satt er det underforstått at bruker også har omsorg for dette barnet ved UFT (barnet må altså være under 7 år). Dette har
     * konsekvens for for godskriving av FPP (garanti grense på 3.00 i perioden tom barnet fyller 6 år).
     *
     */
    var fodselsArYngsteBarn = 0

    /**
     * Dato for rett til friinntekt.
     */
    var friinntektsDatoLd: LocalDate? = null

    /**
     * Om tilfellet er en reaktivisering- f.eks har forsøkt å jobbe, men forsøket feilet.
     */
    var reaktivisering = false

    /**
     * Dato når reaktivering startet
     */

    /**
     * Opplysning om at uførepensjonen skal utbetales til arbeidsgiver.
     */
    var lonnstilskudd = false

    /*
       * Forhøyelse av Uføregrad uten nytt uføretidspunkt.
       * Brukes i skjermbildet for validering og mulighet til å gi feilmelding
       * dersom saksbehandler ikke har satt nytt uføretidspunkt.
       */
    var forhoyelseUtenNyttUft = false

    /**
     * Angir om personen er "ung ufør".Settes av saksbehandler.
     * Brukes ikke i pensjon-regler. Det er garantertTPUngUfor som brukes istedet.
     */
    var ungUfor = false

    /**
     * Angir on årlig inntekt er større enn G på uføretidspunktet.
     */
    var arligInntektMinst1g = false

    /**
     * Unntak for ventetid etter §12-12.
     */
    var unntakVentetid12_12 = false

    /**
     * Kode for yrke til den Uføre.
     * Denne koden brukes til statstikk og for å beregne lovlig inntekt å 12-12
     * for kombinert yrkesaktiv/husmor, kode = 11,12,13 og 18.
     */
    var yrkeEnum: YrkeEnum? = null

    /**
     * Garantert tilleggspensjon til ung ufør fådt før 1940.
     */
    var garantertTPUngUfor = false

    /*
       * Alternativt uføretidspunkt ung ufør ved krav før 36 år.
       */
    var altUftUngUforLd: LocalDate? = null

    /**
     * Hele eller deler av uførheten skyldes yrkesskade.
     */
    var uforhetSkyldesYrkesskade = false

    /**
     * Angir om Uføregrunnlaget brukes som grunnlag på kravet.
     */
    var bruk: Boolean = true

    constructor()

    constructor(source: Uforegrunnlag) : this() {
        ufg = source.ufg
        uftLd = source.uftLd
        uftVirkLd = source.uftVirkLd
        ifu = source.ifu
        fodselsArYngsteBarn = source.fodselsArYngsteBarn
        friinntektsDatoLd = source.friinntektsDatoLd
        reaktivisering = source.reaktivisering
        lonnstilskudd = source.lonnstilskudd
        forhoyelseUtenNyttUft = source.forhoyelseUtenNyttUft
        ungUfor = source.ungUfor
        arligInntektMinst1g = source.arligInntektMinst1g
        unntakVentetid12_12 = source.unntakVentetid12_12
        yrkeEnum = source.yrkeEnum
        garantertTPUngUfor = source.garantertTPUngUfor
        altUftUngUforLd = source.altUftUngUforLd
        uforhetSkyldesYrkesskade = source.uforhetSkyldesYrkesskade
        bruk = source.bruk
    }
}
