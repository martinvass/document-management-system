/**
 * Universal table/card filter utility
 * Filters rows or cards based on search input and data attributes
 */

/**
 * Initialize filter for a table or card list
 * @param {string} searchInputId - ID of the search input element
 * @param {string} containerSelector - CSS selector for rows/cards container
 * @param {string} itemSelector - CSS selector for individual items (tr or .doc-card)
 * @param {string[]} searchAttributes - Array of data attributes to search in (e.g., ['data-name', 'data-desc'])
 */
function initializeFilter(searchInputId, containerSelector, itemSelector, searchAttributes) {
    const searchInput = document.getElementById(searchInputId);

    if (!searchInput) {
        console.warn(`Search input with ID "${searchInputId}" not found`);
        return;
    }

    searchInput.addEventListener('input', function() {
        filterItems(this.value, containerSelector, itemSelector, searchAttributes);
    });
}

/**
 * Filter items based on search value
 * @param {string} searchValue - The search term
 * @param {string} containerSelector - CSS selector for container
 * @param {string} itemSelector - CSS selector for items
 * @param {string[]} searchAttributes - Array of data attributes to search in
 */
function filterItems(searchValue, containerSelector, itemSelector, searchAttributes) {
    const value = searchValue.toLowerCase();
    const items = document.querySelectorAll(`${containerSelector} ${itemSelector}`);

    items.forEach(item => {
        // Collect all searchable text from specified attributes
        const searchableText = searchAttributes
            .map(attr => item.getAttribute(attr) || '')
            .join(' ')
            .toLowerCase();

        // Show/hide based on match
        if (searchableText.includes(value)) {
            item.style.display = '';
        } else {
            item.style.display = 'none';
        }
    });
}

/**
 * Clear filter and show all items
 * @param {string} searchInputId - ID of the search input element
 * @param {string} containerSelector - CSS selector for container
 * @param {string} itemSelector - CSS selector for items
 */
function clearFilter(searchInputId, containerSelector, itemSelector) {
    const searchInput = document.getElementById(searchInputId);

    if (searchInput) {
        searchInput.value = '';
    }

    const items = document.querySelectorAll(`${containerSelector} ${itemSelector}`);
    items.forEach(item => {
        item.style.display = '';
    });
}