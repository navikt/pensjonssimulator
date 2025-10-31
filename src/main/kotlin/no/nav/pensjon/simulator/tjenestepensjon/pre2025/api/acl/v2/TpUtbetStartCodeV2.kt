package no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v2

enum class TpUtbetStartCodeV2 {
    /**
     * 62
     */
    P_62("62"),

    /**
     * 63
     */
    P_63("63"),

    /**
     * 64
     */
    P_64("64"),

    /**
     * 65
     */
    P_65("65"),

    /**
     * 66
     */
    P_66("66"),

    /**
     * 67
     */
    P_67("67"),

    /**
     * 68
     */
    P_68("68"),

    /**
     * 69
     */
    P_69("69"),

    /**
     * 70
     */
    P_70("70"),

    /**
     * 71
     */
    P_71("71"),

    /**
     * 72
     */
    P_72("72"),

    /**
     * 73
     */
    P_73("73"),

    /**
     * 74
     */
    P_74("74"),

    /**
     * 75
     */
    P_75("75"),

    /**
     * Ved pensjonering
     */
    VED_PENSJONERING;

    private var value: String? = null

    /**
     * Value constructor for illegal enums.
     * 
     * @param value the illegal value
     */
    constructor(value: String?) {
        this.value = value
    }

    /**
     * Default constructor.
     */
    constructor()
}
