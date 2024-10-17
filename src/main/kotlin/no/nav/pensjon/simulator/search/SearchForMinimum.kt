package no.nav.pensjon.simulator.search

/**
 * Searches for the lowest integer that satisfies a given 'discriminator' function.
 * If the function returns 'true', it means the current value is satisfactory, but there may be other satisfactory lower values, so the search tries lower values.
 * If the function returns 'false', it means the current value is not satisfactory, so the search tries higher values.
 * The candidate values are in the range [0, max].
 * The search is binary and recursive.
 */
class SearchForMinimum(private val discriminator: IntegerDiscriminator) {

    fun search(max: Int): Int {
        return nextState(
            SearchState(
                min = 0,
                current = max / 2,
                max = max,
                lastBadValue = -9, // some negative value (outside the search range)
                lastGoodValue = max + 9 // some positive value outside the search range
            )
        ).lastGoodValue
    }

    private fun nextState(state: SearchState): SearchState {
        if (state.lastGoodValue - state.lastBadValue < 2) {
            // Search range is at its smallest size, and the satisfactory value can be picked out
            return SearchState(lastGoodValue = state.lastGoodValue)
        }

        if (state.lastGoodValue == state.min) {
            // Search has reached lower limit, and that value is satisfactory
            return SearchState(lastGoodValue = state.lastGoodValue)
        }

        if (state.lastBadValue == state.max) {
            // Search has reached upper limit without finding a satisfactory value
            return SearchState(lastGoodValue = NO_MATCH_RESULT)
        }

        val tryLower = discriminator.valueIsGood(state.current) // current value is satisfactory, but we want the lowest satisfactory value

        return if (tryLower) {
            // Search the lower half search range:
            nextState(
                SearchState(
                    min = state.min,
                    current = if (state.current - state.min < 2) state.min else (state.min + state.current) / 2,
                    max = state.current,
                    lastBadValue = state.lastBadValue,
                    lastGoodValue = state.current
                )
            )
        } else {
            // Search the upper half search range:
            nextState(
                SearchState(
                    min = state.current,
                    current = if (state.max - state.current < 2) state.max else (state.current + state.max) / 2,
                    max = state.max,
                    lastBadValue = state.current,
                    lastGoodValue = state.lastGoodValue
                )
            )
        }
    }

    private data class SearchState(
        val min: Int = 0,
        val current: Int = 0,
        val max: Int = 0,
        val lastBadValue: Int = 0,
        val lastGoodValue: Int)

    private companion object {
        private const val NO_MATCH_RESULT = -1
    }
}
