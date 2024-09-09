package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.beregning.Ytelseskomponent
import no.nav.pensjon.simulator.core.domain.regler.util.Copyable
import no.nav.pensjon.simulator.core.domain.regler.util.DateCompareUtil
import java.lang.reflect.InvocationTargetException
import java.util.*

class BeregnetUtbetalingsperiode : Comparable<BeregnetUtbetalingsperiode>, Copyable<BeregnetUtbetalingsperiode> {
    /**
     * Periodens startdato.
     */
    var fomDato: Date? = null

    /**
     * Periodens sluttdato.
     */
    var tomDato: Date? = null

    /**
     * Uføregrad for perioden
     */
    var uforegrad: Int = 0

    /**
     * Yrkesskadegrad for perioden
     */
    var yrkesskadegrad: Int = 0

    /**
     * Antall fellesbarn det er innvilget barnetillegg for i perioden.
     * Vil kun være angitt for fremtidige perioder i kontekst av etteroppgjør
     */
    var antallFellesbarn: Int = 0

    /**
     * Antall særkullsbarn det er innvilget barnetillegg for i perioden.
     * Vil kun være angitt for fremtidige perioder i kontekst av etteroppgjør
     */
    var antallSerkullsbarn: Int = 0

    var ytelseskomponenter: MutableMap<String, Ytelseskomponent> = HashMap()

    val ytelseskomponentListe: List<Ytelseskomponent>
        get() = ArrayList(ytelseskomponenter.values)

    constructor()

    constructor(bup: BeregnetUtbetalingsperiode) : super() {
        fomDato = bup.fomDato
        tomDato = bup.tomDato
        uforegrad = bup.uforegrad
        yrkesskadegrad = bup.yrkesskadegrad
        antallFellesbarn = bup.antallFellesbarn
        antallSerkullsbarn = bup.antallSerkullsbarn

        if (!bup.ytelseskomponenter.isEmpty()) {
            ytelseskomponenter = mutableMapOf()
            val it = bup.ytelseskomponenter.values.iterator()
            while (it.hasNext()) {
                val yk = it.next()
                val ykcopy: Ytelseskomponent
                try {
                    ykcopy = Class.forName(yk.javaClass.name).getConstructor(yk.javaClass)
                        .newInstance(yk) as Ytelseskomponent
                    ytelseskomponenter[ykcopy.ytelsekomponentType.kode] = ykcopy
                } catch (e: IllegalArgumentException) {
                } catch (e: SecurityException) {
                } catch (e: InstantiationException) {
                } catch (e: IllegalAccessException) {
                } catch (e: InvocationTargetException) {
                } catch (e: NoSuchMethodException) {
                } catch (e: ClassNotFoundException) {
                }
            }
        }
    }

    constructor(
        fomDato: Date? = null,
        tomDato: Date? = null,
        uforegrad: Int = 0,
        yrkesskadegrad: Int = 0,
        antallFellesbarn: Int = 0,
        antallSerkullsbarn: Int = 0,
        ytelseskomponenter: MutableMap<String, Ytelseskomponent> = HashMap()
    ) {
        this.fomDato = fomDato
        this.tomDato = tomDato
        this.uforegrad = uforegrad
        this.yrkesskadegrad = yrkesskadegrad
        this.antallFellesbarn = antallFellesbarn
        this.antallSerkullsbarn = antallSerkullsbarn
        this.ytelseskomponenter = ytelseskomponenter
    }

    fun addYtelseskomponent(yk: Ytelseskomponent) {
        ytelseskomponenter[yk.ytelsekomponentType.kode] = yk
    }

    override fun compareTo(other: BeregnetUtbetalingsperiode): Int {
        return DateCompareUtil.compareTo(fomDato, other.fomDato)
    }

    fun getYtelseskomponent(kode: String): Ytelseskomponent? {
        return if (ytelseskomponenter.containsKey(kode)) ytelseskomponenter[kode] as Ytelseskomponent else null
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Ytelseskomponent> getYtelseskomponentTypedOrNull(kode: String): T? {
        return if (ytelseskomponenter.containsKey(kode)) ytelseskomponenter[kode] as T else null
    }

    override fun deepCopy(): BeregnetUtbetalingsperiode {
        return BeregnetUtbetalingsperiode(this)
    }
}
