package com.mycompany.plugins.safpicker

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class SafPickerListingProcessorTest {
    @Test
    fun processFiltersSortsAndPagesItems() {
        val items = listOf(
            SafPickerListing(payload = "alpha", name = "Alpha", isDirectory = false, size = 120, lastModified = 3),
            SafPickerListing(payload = "beta", name = "Beta", isDirectory = true, size = 0, lastModified = 2),
            SafPickerListing(payload = "gamma", name = "Gamma", isDirectory = false, size = 240, lastModified = 1)
        )

        val page = SafPickerListingProcessor.process(
            items = items,
            filter = SafPickerFilter.FILES,
            sortBy = SafPickerSortBy.NAME,
            sortOrder = SafPickerSortOrder.ASC,
            offset = 0,
            maxItems = 1
        )

        assertEquals(2, page.totalCount)
        assertEquals(1, page.items.size)
        assertEquals("Alpha", page.items[0].name)
        assertEquals(1, page.nextOffset)
        assertNotNull(page.items[0].payload)
    }

    @Test
    fun processHandlesOffsetsBeyondList() {
        val items = listOf(
            SafPickerListing(payload = "alpha", name = "Alpha", isDirectory = false, size = 120, lastModified = 3)
        )

        val page = SafPickerListingProcessor.process(
            items = items,
            filter = SafPickerFilter.ALL,
            sortBy = SafPickerSortBy.LAST_MODIFIED,
            sortOrder = SafPickerSortOrder.DESC,
            offset = 4,
            maxItems = 10
        )

        assertEquals(1, page.totalCount)
        assertEquals(0, page.items.size)
    }
}
