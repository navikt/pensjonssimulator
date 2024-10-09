package no.nav.pensjon.simulator.core.domain.regler.beregning

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.Beregning2011
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.Uforetrygdberegning
import java.lang.reflect.InvocationTargetException

class BeregningRelasjon {

    /**
     * 1967 beregningen som det relateres til
     */
    var beregning: Beregning? = null

    /**
     * Beregning 2011 som det relateres til
     */
    var beregning2011: Beregning2011? = null

    /**
     * Angir om beregningen er brukt (helt eller delvis) i beregningen den tilhører.
     */
    var bruk = false

    constructor()

    constructor(beregningRelasjon: BeregningRelasjon) {
        if (beregningRelasjon.beregning != null) {
            beregning = Beregning(beregningRelasjon.beregning)
            //beregning!!.beregningsrelasjon = this
        }

        beregning2011 = createBeregning2011UsingCopyConstructor(beregningRelasjon.beregning2011)

        //if (beregning2011 != null) {
        //    beregning2011!!.beregningsrelasjon = this
        //}

        bruk = beregningRelasjon.bruk
    }

    private fun createBeregning2011UsingCopyConstructor(beregning2011: Beregning2011?): Beregning2011? {
        if (beregning2011 == null) return null

        val clazz = beregning2011.javaClass

        return try {
            if (beregning2011 is Uforetrygdberegning) {
                val constructor = clazz.getConstructor(clazz, Boolean::class.javaPrimitiveType)
                constructor.newInstance(beregning2011, true)
            } else {
                val constructor = clazz.getConstructor(clazz)
                constructor.newInstance(beregning2011)
            }
        } catch (e: NoSuchMethodException) {
            throw RuntimeException(e)
        } catch (e: IllegalArgumentException) {
            throw RuntimeException(e)
        } catch (e: InstantiationException) {
            throw RuntimeException(e)
        } catch (e: IllegalAccessException) {
            throw RuntimeException(e)
        } catch (e: InvocationTargetException) {
            throw RuntimeException(e)
        }
    }
}
