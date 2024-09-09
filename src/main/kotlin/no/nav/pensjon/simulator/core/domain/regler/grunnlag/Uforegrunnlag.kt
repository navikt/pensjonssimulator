package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import com.fasterxml.jackson.annotation.JsonIgnore
import no.nav.pensjon.simulator.core.domain.regler.kode.YrkeCti
import java.io.Serializable
import java.util.*

class Uforegrunnlag(

    /**
     * Uføregrad, 0-100
     */
    var ufg: Int = 0,

    /**
     * Dato for uføretidspunktet.
     */
    var uft: Date? = null,

    /**
     * Virkningstidspunkt for hendelsen Uforegrunnlaget representerer.
     */
    var uftVirk: Date? = null,

    /**
     * Inntekt før uførhet.Normalinntekten personen ville hatt på
     * uføretidspunktet dersom personen ikke hadde blitt syk. Settes av saksbehandler.
     * ifu relaterer seg til første uføretidspunkt. Det er uforegrunnlag.uft ved første gangs UP.
     * Ellers hentes den fra historikken.
     */
    var ifu: Int = 0,

    /**
     * Fødselsår for yngste barn. Når denne variabelen er satt er det underforstått at bruker også har omsorg for dette barnet ved UFT (barnet må altså være under 7 år). Dette har
     * konsekvens for for godskriving av FPP (garanti grense på 3.00 i perioden tom barnet fyller 6 år).
     */
    var fodselsArYngsteBarn: Int = 0,
    /**
     * Dato for rett til friinntekt.
     */
    var friinntektsDato: Date? = null,

    /**
     * Om tilfellet er en reaktivisering- f.eks har forsøkt å jobbe, men forsøket feilet.
     */
    var reaktivisering: Boolean = false,
    /**
     * Dato når reaktivering startet
     */
    @JsonIgnore var reaktiviseringFomDato: Date? = null,

    /**
     * Opplysning om at uførepensjonen skal utbetales til arbeidsgiver.
     */
    var lonnstilskudd: Boolean = false,

    /**
     * Forhøyelse av uføregrad uten nytt uføretidspunkt.
     * Brukes i skjermbildet for validering og mulighet til å gi feilmelding
     * dersom saksbehandler ikke har satt nytt uføretidspunkt.
     */
    var forhoyelseUtenNyttUft: Boolean = false,

    /**
     * Angir om personen er "ung ufør".Settes av saksbehandler.
     * Brukes ikke i PREG. Det er garantertTPUngUfor som brukes istedet.
     */
    var ungUfor: Boolean = false,

    /**
     * Angir on årlig inntekt er større enn G på uføretidspunktet.
     */
    var arligInntektMinst1g: Boolean = false,

    /**
     * Unntak for ventetid etter §12-12.
     */
    var unntakVentetid12_12: Boolean = false,
    /**
     * Kode for yrke til den uføre.
     * Denne koden brukes til statstikk og for å beregne lovlig inntekt § 12-12
     * for kombinert yrkesaktiv/husmor, kode = 11,12,13 og 18.
     */
    var yrke: YrkeCti? = null,
    /**
     * Garantert tilleggspensjon til ung ufør født før 1940.
     */
    var garantertTPUngUfor: Boolean = false,
    /**
     * Alternativt uføretidspunkt ung ufør ved krav før 36 år.
     */
    var altUftUngUfor: Date? = null,
    /**
     * Hele eller deler av uførheten skyldes yrkesskade.
     */
    var uforhetSkyldesYrkesskade: Boolean = false,
    /**
     * Angir om uføregrunnlaget brukes som grunnlag på kravet.
     */
    var bruk: Boolean = false,
    /**
     * Angir om uføregrunnlaget brukes som grunnlag på kravet.
     */
    @JsonIgnore var nedsattUfg: Boolean = false
) : Serializable {

    constructor(uforegrunnlag: Uforegrunnlag) : this() {
        this.ufg = uforegrunnlag.ufg
        if (uforegrunnlag.uft != null) {
            this.uft = uforegrunnlag.uft!!.clone() as Date
        }
        if (uforegrunnlag.uftVirk != null) {
            this.uftVirk = uforegrunnlag.uftVirk!!.clone() as Date
        }
        this.ifu = uforegrunnlag.ifu
        this.fodselsArYngsteBarn = uforegrunnlag.fodselsArYngsteBarn
        if (uforegrunnlag.friinntektsDato != null) {
            this.friinntektsDato = uforegrunnlag.friinntektsDato!!.clone() as Date
        }
        this.reaktivisering = uforegrunnlag.reaktivisering
        if (uforegrunnlag.reaktiviseringFomDato != null) {
            this.reaktiviseringFomDato = uforegrunnlag.reaktiviseringFomDato!!.clone() as Date
        }
        this.lonnstilskudd = uforegrunnlag.lonnstilskudd
        this.forhoyelseUtenNyttUft = uforegrunnlag.forhoyelseUtenNyttUft
        this.ungUfor = uforegrunnlag.ungUfor
        this.arligInntektMinst1g = uforegrunnlag.arligInntektMinst1g
        this.unntakVentetid12_12 = uforegrunnlag.unntakVentetid12_12
        if (uforegrunnlag.yrke != null) {
            this.yrke = YrkeCti(uforegrunnlag.yrke)
        }
        this.garantertTPUngUfor = uforegrunnlag.garantertTPUngUfor
        if (uforegrunnlag.altUftUngUfor != null) {
            this.altUftUngUfor = uforegrunnlag.altUftUngUfor!!.clone() as Date
        }
        this.uforhetSkyldesYrkesskade = uforegrunnlag.uforhetSkyldesYrkesskade
        this.bruk = uforegrunnlag.bruk
        this.nedsattUfg = uforegrunnlag.nedsattUfg
    }

    constructor(
        ufg: Int,
        uft: Date,
        uftVirk: Date,
        ifu: Int,
        fodselsArYngsteBarn: Int,
        friinntektsDato: Date,
        reaktivisering: Boolean,
        lonnstilskudd: Boolean,
        forhoyelseUtenNyttUft: Boolean,
        ungUfor: Boolean,
        arligInntektMinst1g: Boolean,
        unntakVentetid12_12: Boolean,
        yrke: YrkeCti,
        garantertTPUngUfor: Boolean,
        altUftUngUfor: Date,
        uforhetSkyldesYrkesskade: Boolean,
        bruk: Boolean = true
    ) : this() {
        this.ufg = ufg
        this.uft = uft
        this.uftVirk = uftVirk
        this.ifu = ifu
        this.fodselsArYngsteBarn = fodselsArYngsteBarn
        this.friinntektsDato = friinntektsDato
        this.reaktivisering = reaktivisering
        this.lonnstilskudd = lonnstilskudd
        this.forhoyelseUtenNyttUft = forhoyelseUtenNyttUft
        this.ungUfor = ungUfor
        this.arligInntektMinst1g = arligInntektMinst1g
        this.unntakVentetid12_12 = unntakVentetid12_12
        this.yrke = yrke
        this.garantertTPUngUfor = garantertTPUngUfor
        this.altUftUngUfor = altUftUngUfor
        this.uforhetSkyldesYrkesskade = uforhetSkyldesYrkesskade
        this.bruk = bruk
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
