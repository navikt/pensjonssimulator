package no.nav.pensjon.simulator.search

/**
 * Searches for the smallest integer that satisfies a given 'discriminator' function.
 * The candidate values are in the range [0, max].
 * The search is binary and recursive.
 * Associated with each integer is an object of type T;
 * T is the result of the discriminator function for the corresponding integer value.
 */
class SmallestValueSearch<T : ValueAssessment>(
    private val discriminator: IntegerAttempt<T>,
    private val max: Int) {

    fun search(): T? =
        nextState(
            SearchState(
                min = 0,
                current = max / 2,
                max = max,
                lastBadValue = -9, // some negative value (outside the search range)
                lastGoodValue = max + 9 // some positive value outside the search range
            )
        ).lastGoodObject

    private fun nextState(state: SearchState<T>): SearchState<T> {
        if (state.lastGoodValue - state.lastBadValue < 2) {
            // Search range is at its smallest size, and the good value can be picked out
            return SearchState(lastGoodValue = state.lastGoodValue, lastGoodObject = state.lastGoodObject)
        }

        if (state.lastGoodValue == state.min) {
            // Search has reached lower limit, and that value is good
            return SearchState(lastGoodValue = state.lastGoodValue, lastGoodObject = state.lastGoodObject)
        }

        if (state.lastBadValue == state.max) {
            // Search has reached upper limit without finding a good value
            return SearchState(lastGoodValue = NO_MATCH_RESULT)
        }

        val currentAssessment = discriminator.tryValue(state.current)
        val trySmaller = currentAssessment.valueIsGood // current value is good, but we want the smallest good value

        return if (trySmaller) {
            // Search the lower half search range:
            nextState(
                SearchState(
                    min = state.min,
                    current = if (state.current - state.min < 2) state.min else (state.min + state.current) / 2,
                    max = state.current,
                    lastBadValue = state.lastBadValue,
                    lastGoodValue = state.current,
                    lastGoodObject = currentAssessment
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
                    lastGoodValue = state.lastGoodValue,
                    lastGoodObject = state.lastGoodObject
                )
            )
        }
    }

    private data class SearchState<T>(
        val min: Int = 0,
        val current: Int = 0,
        val max: Int = 0,
        val lastBadValue: Int = 0,
        val lastGoodValue: Int,
        val lastGoodObject: T? = null)

    private companion object {
        private const val NO_MATCH_RESULT = -1
    }
}
