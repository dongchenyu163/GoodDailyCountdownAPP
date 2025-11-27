package com.dlx.smartalarm.demo

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class FilterMenuStateTest {

    @Test
    fun `click favorites keeps menu expanded and sets filter`() {
        val initial = FilterMenuState(expanded = true, filterFavorites = false)
        val next = applyFilterSelection(initial, selectFavorites = true)
        assertTrue(next.expanded)
        assertTrue(next.filterFavorites)
    }

    @Test
    fun `click all keeps menu expanded and clears favorites`() {
        val initial = FilterMenuState(expanded = true, filterFavorites = true)
        val next = applyFilterSelection(initial, selectFavorites = false)
        assertTrue(next.expanded)
        assertFalse(next.filterFavorites)
    }

    @Test
    fun `rapid consecutive clicks toggle filter without collapsing`() {
        var state = FilterMenuState(expanded = true, filterFavorites = false)
        state = applyFilterSelection(state, selectFavorites = true)
        assertTrue(state.expanded)
        assertTrue(state.filterFavorites)
        state = applyFilterSelection(state, selectFavorites = false)
        assertTrue(state.expanded)
        assertFalse(state.filterFavorites)
    }
}

