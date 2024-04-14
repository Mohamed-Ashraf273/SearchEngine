document.addEventListener("DOMContentLoaded", function() {
    const searchInput = document.getElementById('searchInput');
    const searchButton = document.getElementById('searchButton');
    const searchResults = document.getElementById('searchResults');
    
    // Function to perform search
    function performSearch() {
        const query = searchInput.value.trim();
        if (query !== '') {
            // Display search query
            searchResults.innerHTML = `<p>Search Query: ${query}</p>`;
            
            // For demonstration, let's just log the query to the console
            console.log('Search query:', query);
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
