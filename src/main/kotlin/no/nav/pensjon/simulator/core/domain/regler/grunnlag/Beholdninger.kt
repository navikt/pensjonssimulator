package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import java.beans.PropertyChangeListener
import java.beans.PropertyChangeSupport
import java.io.ObjectStreamException
import java.io.Serializable
import java.lang.reflect.InvocationTargetException
import java.util.*

class Beholdninger() : Serializable {
    /**
     * property change variabler
     */
    @Transient
    private var pcs = PropertyChangeSupport(this)

    var beholdninger: MutableList<Beholdning> = mutableListOf()
        set(beholdninger) {
            field = beholdninger
        }

    var pensjonsbeholdning: Pensjonsbeholdning?
        get() = hentBeholdning(Pensjonsbeholdning::class.java)
        set(pensjonsbeholdning) = fjernEllerLeggTilBeholdning(Pensjonsbeholdning::class.java, pensjonsbeholdning)

    var garantipensjonsbeholdning: Garantipensjonsbeholdning?
        get() = hentBeholdning(Garantipensjonsbeholdning::class.java)
        set(garantipensjonsbeholdning) = fjernEllerLeggTilBeholdning(
            Garantipensjonsbeholdning::class.java,
            garantipensjonsbeholdning
        )

    var garantitilleggsbeholdning: Garantitilleggsbeholdning?
        get() = hentBeholdning(Garantitilleggsbeholdning::class.java)
        set(garantitilleggsbeholdning) = fjernEllerLeggTilBeholdning(
            Garantitilleggsbeholdning::class.java,
            garantitilleggsbeholdning
        )

    init {
        pcs = PropertyChangeSupport(this)
    }

    constructor(br: Beholdninger) : this() {
        for (b in br.beholdninger) {
            val clazz = b.javaClass
            try {
                val constructor = clazz.getConstructor(clazz)
                beholdninger.add(constructor.newInstance(b))
            } catch (e: InvocationTargetException) {
                //Vil kastes hvis copy constructor f.eks. ledet til nullpointerexception.
                val cause = e.cause
                if (cause is RuntimeException) {
                    throw cause
                } else {
                    throw RuntimeException(e)
                }
            } catch (e: Exception) {
                //Vil kastes hvis f.eks. copy constructor ikke finnes for beholdningen.
                throw RuntimeException(e)
            }

        }
    }

    /**
     * Metode som benyttes av XStream og Java-serialisering for å
     * initialisere felt vi hopper over p.g.a sykliske avhengigheter.
     */
    @Throws(ObjectStreamException::class)
    private fun readResolve(): Any {
        pcs = PropertyChangeSupport(this)
        return this
    }

    /**
     * Legger en listener til registeret av lyttere
     */
    @Synchronized
    fun addPropertyChangeListener(listener: PropertyChangeListener) {
        pcs.addPropertyChangeListener(listener)
    }

    /**
     * Fjerner en listener i registeret for lyttere
     */
    @Synchronized
    fun removePropertyChangeListener(listener: PropertyChangeListener) {
        pcs.removePropertyChangeListener(listener)
    }

    fun leggTilBeholdning(b: Beholdning) {
        val lagret = hentBeholdning(b.javaClass)
        if (lagret != null) {
            beholdninger.remove(lagret)
        }
        beholdninger.add(b)

        if (properties.containsKey(b.javaClass)) {
            pcs.firePropertyChange(properties[b.javaClass], lagret, b)
            pcs.firePropertyChange("beholdninger", null, null)
        }
    }

    fun fjernBeholdning(b: Beholdning) {
        fjernBeholdning(b.javaClass)
    }

    /**
     *  <T>
     *  classOfBeholdning, angir klassen til beholdningen man ønsker å hente
     * return Beholdning av ønsket type
    </T> */
    @Suppress("UNCHECKED_CAST")
    fun <T : Beholdning> hentBeholdning(classOfBeholdning: Class<T>): T? {
        for (b in beholdninger) {
            if (classOfBeholdning.isInstance(b)) {
                return b as T
            }
        }
        return null
    }

    /**
     * Fjerner en beholdning, null safe
     */
    private fun fjernEllerLeggTilBeholdning(clazz: Class<out Beholdning>, b: Beholdning?) {
        if (b == null) {
            fjernBeholdning(clazz)
        } else {
            leggTilBeholdning(b)
        }
    }

    /**
     * Fjerner en beholdning og oppdaterer properties ved å fyre av en property changed event
     */
    private fun fjernBeholdning(clazz: Class<out Beholdning>) {
        var funnet = false
        var tmpBeholdning: Beholdning? = null
        for (beholdning in beholdninger) {
            if (beholdning.javaClass == clazz) {
                funnet = true
                tmpBeholdning = beholdning
            }
        }
        if (funnet) {
            beholdninger.remove(tmpBeholdning)
            if (properties.containsKey(clazz)) {
                pcs.firePropertyChange(properties[clazz], tmpBeholdning, null)
                //Fungerte ikke
                pcs.firePropertyChange("beholdninger", null, null)
            }
        }
    }

    companion object {
        private val properties = finnProperties()

        /**
         * Metode som endrer forste bokstav til lowecase
         */
        private fun forbokstavTilLowercase(input: String): String {
            val forsteBokstav = input.substring(0, 1)
            val resten = input.substring(1)

            return forsteBokstav.lowercase(Locale.getDefault()) + resten
        }

        /**
         * Metode som finner properties for beholdninger
         * og legger dem i hashmap
         */
        protected fun finnProperties(): HashMap<Class<*>, String> {
            val properties = HashMap<Class<*>, String>()
            val methods = Beholdninger::class.java.declaredMethods
            for (method in methods) {
                val clazz = method.returnType
                var superclass: Class<*>? = null
                if (clazz != null) {
                    superclass = clazz.superclass
                }
                while (superclass != null) {
                    if (superclass == Beholdning::class.java) {
                        val propertyName = method.name.substring(3)
                        if (propertyName.equals(clazz.simpleName, ignoreCase = true)) {
                            properties[clazz] = forbokstavTilLowercase(propertyName)
                        }
                    }
                    superclass = superclass.superclass
                }
            }

            return properties
        }
    }
}
