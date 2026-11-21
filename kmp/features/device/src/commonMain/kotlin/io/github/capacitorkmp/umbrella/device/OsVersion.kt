package io.github.capacitorkmp.umbrella.device

/**
 * A comparable `major.minor.patch` version, parsed leniently from strings such as `"16"`,
 * `"17.4.1"`, `"10_15_7"` or `"14 (API 34)"`.
 */
data class OsVersion(val major: Int, val minor: Int = 0, val patch: Int = 0) : Comparable<OsVersion> {

    override fun compareTo(other: OsVersion): Int =
        compareValuesBy(this, other, OsVersion::major, OsVersion::minor, OsVersion::patch)

    override fun toString(): String = when {
        patch != 0 -> "$major.$minor.$patch"
        else -> "$major.$minor"
    }

    companion object {
        private val VERSION = Regex("""(\d+)(?:[._](\d+))?(?:[._](\d+))?""")

        /** Parses the first version number found in [text], or returns `null`. */
        fun parse(text: String?): OsVersion? {
            val match = text?.let { VERSION.find(it) } ?: return null
            val (major, minor, patch) = match.destructured
            return OsVersion(major.toInt(), minor.toIntOrNull() ?: 0, patch.toIntOrNull() ?: 0)
        }
    }
}
