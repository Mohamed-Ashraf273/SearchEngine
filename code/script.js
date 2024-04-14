document.addEventListener("DOMContentLoaded", function() {
    const searchInput = document.getElementById('searchInput');
    const searchButton = document.getElementById('searchButton');
    
    // Function to perform search
    function performSearch() {
        const query = searchInput.value.trim();
        if (query !== '') {
            // Redirect to index2.html with query as parameter
            window.location.href = `index2.html?query=${encodeURIComponent(query)}`;
        }
    }
    
    // Search when button is clicked
    searchButton.addEventListener('click', performSearch);
    
    // Search when Enter key is pressed
    searchInput.addEventListener('keypress', function(event) {
        if (event.key === 'Enter') {
            performSearch();
        }
    });
});
