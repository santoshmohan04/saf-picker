package com.mycompany.plugins.safpicker

import java.util.Locale

enum class SafPickerFilter {
    ALL,
    FILES,
    FOLDERS
}

enum class SafPickerSortBy {
    NAME,
    SIZE,
    LAST_MODIFIED
}

enum class SafPickerSortOrder {
    ASC,
    DESC
}

data class SafPickerListing<T>(
    val payload: T,
    val name: String?,
    val isDirectory: Boolean,
    val size: Long,
    val lastModified: Long
)

data class SafPickerPage<T>(
    val items: List<SafPickerListing<T>>,
    val totalCount: Int,
    val nextOffset: Int?
)

object SafPickerListingProcessor {
    fun <T> process(
        items: List<SafPickerListing<T>>,
        filter: SafPickerFilter,
        sortBy: SafPickerSortBy,
        sortOrder: SafPickerSortOrder,
        offset: Int,
        maxItems: Int?
    ): SafPickerPage<T> {
        val filtered = when (filter) {
            SafPickerFilter.ALL -> items
            SafPickerFilter.FILES -> items.filter { !it.isDirectory }
            SafPickerFilter.FOLDERS -> items.filter { it.isDirectory }
        }

        val nameKey: (SafPickerListing<T>) -> String = { it.name?.lowercase(Locale.US) ?: "" }

        val comparator = when (sortBy) {
            SafPickerSortBy.NAME -> compareBy(nameKey)
            SafPickerSortBy.SIZE -> compareBy<SafPickerListing<T>> { it.size }
                .thenBy(nameKey)
            SafPickerSortBy.LAST_MODIFIED -> compareBy<SafPickerListing<T>> { it.lastModified }
                .thenBy(nameKey)
        }

        val sorted = if (sortOrder == SafPickerSortOrder.DESC) {
            filtered.sortedWith(comparator.reversed())
        } else {
            filtered.sortedWith(comparator)
        }

        val safeOffset = offset.coerceAtLeast(0)
        val safeMaxItems = maxItems?.takeIf { it > 0 }
        val totalCount = sorted.size

        if (safeOffset >= totalCount) {
            return SafPickerPage(emptyList(), totalCount, null)
        }

        val end = if (safeMaxItems != null) {
            minOf(safeOffset + safeMaxItems, totalCount)
        } else {
            totalCount
        }

        val pageItems = sorted.subList(safeOffset, end)
        val nextOffset = if (end < totalCount) end else null

        return SafPickerPage(pageItems, totalCount, nextOffset)
    }
}
