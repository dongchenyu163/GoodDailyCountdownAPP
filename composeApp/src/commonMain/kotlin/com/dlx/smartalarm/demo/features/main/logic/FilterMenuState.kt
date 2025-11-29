package com.dlx.smartalarm.demo.features.main.logic

data class FilterMenuState(
    val expanded: Boolean,
    val filterFavorites: Boolean
)

fun applyFilterSelection(state: FilterMenuState, selectFavorites: Boolean): FilterMenuState {
    return state.copy(filterFavorites = selectFavorites)
}
