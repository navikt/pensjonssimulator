package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.enum.YrkeEnum
import no.nav.pensjon.simulator.core.domain.regler.kode.YrkeCti
import java.util.*

// Checked 2025-02-28
class Uforegrunnlag {
    /**
     * Uføregrad, 0-100
     */
    var ufg = 0

    /**
     * Dato for uføretidspunktet.
     */
    var uft: Date? = null

    /**
     * Virkningstidspunkt for hendelsen Uforegrunnlaget representerer.
     */
    var uftVirk: Date? = null

    /**
     * Inntekt før uførhet.Normalinntekten personen ville hatt på
     * uføretidspunktet dersom personen ikke hadde blitt syk. Settes av saksbehandler.
     * ifu relaterer seg til første uføretidspunkt. Det er uforegrunnlag.uft ved første gangs UP.
     * Ellers hentes den fra historikken.
     */
    var ifu = 0

    /**
     * Fødselsår for yngste barn. Når denne variabelen er satt er det underforstått at bruker også har omsorg for dette barnet ved UFT (barnet må altså være under 7 år). Dette har
     * konsekvens for for godskriving av FPP (garanti grense på 3.00 i perioden tom barnet fyller 6 år).
     */
    var fodselsArYngsteBarn = 0

    /**
     * Dato for rett til friinntekt.
     */
    var friinntektsDato: Date? = null

    /**
     * Om tilfellet er en reaktivisering- f.eks har forsøkt å jobbe, men forsøket feilet.
     */
    var reaktivisering = false

    /**
     * Opplysning om at uførepensjonen skal utbetales til arbeidsgiver.
     */
    var lonnstilskudd = false

    /**
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
     * Kode for yrke til den uføre.
     * Denne koden brukes til statstikk og for å beregne lovlig inntekt § 12-12
     * for kombinert yrkesaktiv/husmor, kode = 11,12,13 og 18.
     */
    var yrke: YrkeCti? = null
    var yrkeEnum: YrkeEnum? = null

    /**
     * Garantert tilleggspensjon til ung ufør født før 1940.
     */
    var garantertTPUngUfor = false

    /**
     * Alternativt uføretidspunkt ung ufør ved krav før 36 år.
     */
    var altUftUngUfor: Date? = null

    /**
     * Hele eller deler av uførheten skyldes yrkesskade.
     */
    var uforhetSkyldesYrkesskade = false

    /**
     * Angir om uføregrunnlaget brukes som grunnlag på kravet.
     */
    var bruk: Boolean = true

    constructor()

    constructor(source: Uforegrunnlag) : this() {
        ufg = source.ufg
        uft = source.uft?.clone() as? Date
        uftVirk = source.uftVirk?.clone() as? Date
        ifu = source.ifu
        fodselsArYngsteBarn = source.fodselsArYngsteBarn
        friinntektsDato = source.friinntektsDato?.clone() as? Date
        reaktivisering = source.reaktivisering
        lonnstilskudd = source.lonnstilskudd
        forhoyelseUtenNyttUft = source.forhoyelseUtenNyttUft
        ungUfor = source.ungUfor
        arligInntektMinst1g = source.arligInntektMinst1g
        unntakVentetid12_12 = source.unntakVentetid12_12
        yrke = source.yrke?.let(::YrkeCti)
        yrkeEnum = source.yrkeEnum
        garantertTPUngUfor = source.garantertTPUngUfor
        altUftUngUfor = source.altUftUngUfor?.clone() as? Date
        uforhetSkyldesYrkesskade = source.uforhetSkyldesYrkesskade
        bruk = source.bruk
    }

    override fun toString(): String {
        val TAB = "    "

        val retValue = StringBuilder()

        retValue.append("Uforegrunnlag ( ").append(super.toString()).append(TAB).append("ufg = ").append(ufg)
            .append(TAB).append("uft = ").append(uft).append(TAB).append("ifu = ")
            .append(ifu).append(TAB).append("fodselsArYngsteBarn = ").append(fodselsArYngsteBarn).append(TAB)
            .append("friinntektsDato = ").append(friinntektsDato).append(TAB)
            .append("reaktivisering = ").append(reaktivisering).append(TAB).append("lonnstilskudd = ")
            .append(lonnstilskudd).append(TAB).append("forhoyelseUtenNyttUft = ")
            .append(forhoyelseUtenNyttUft).append(TAB).append("ungUfor = ").append(ungUfor).append(TAB)
            .append("arligInntektMinst1g = ").append(arligInntektMinst1g)
            .append(TAB).append("unntakVentetid12_12 = ").append(unntakVentetid12_12).append(TAB).append("yrke = ")
            .append(yrke).append(TAB).append("garantertTPUngUfor = ")
            .append(garantertTPUngUfor).append(TAB).append("altUftUngUfor = ").append(altUftUngUfor).append(TAB)
            .append("uforhetSkyldesYrkesskade = ")
            .append(uforhetSkyldesYrkesskade).append(TAB).append("bruk = ").append(bruk).append(TAB)

            .append(" )")

        return retValue.toString()
    }
}
