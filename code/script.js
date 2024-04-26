document.addEventListener("DOMContentLoaded", function() {
    const searchInput = document.getElementById('searchInput');
    const suggestionsDropdown = document.createElement('div');
    suggestionsDropdown.classList.add('options-dropdown');
    searchInput.parentNode.appendChild(suggestionsDropdown); // Append options dropdown to the parent of search input
    let suggestions = [];

    // Function to display suggestions in the dropdown
    function displaySuggestions() {
        const userInput = searchInput.value.trim();
        const filteredSuggestions = suggestions.filter(suggestion => suggestion.includes(userInput));
        suggestionsDropdown.innerHTML = ''; // Clear previous suggestions
        filteredSuggestions.forEach(suggestion => {
            const suggestionElement = document.createElement('div');
            suggestionElement.textContent = suggestion;
            suggestionElement.classList.add('option');
            // Event listener for selecting a suggestion
            suggestionElement.addEventListener('click', function() {
                // Fill the search input with the selected suggestion
                searchInput.value = suggestion;
                // Clear the dropdown
                suggestionsDropdown.innerHTML = '';
            });
            suggestionsDropdown.appendChild(suggestionElement);
        });
        // Show the dropdown if there are suggestions, otherwise hide it
        suggestionsDropdown.style.display = filteredSuggestions.length > 0 ? 'block' : 'none';
    }

    // Function to hide options dropdown
    function hideOptionsDropdown(event) {
        if (!searchInput.contains(event.target) && !suggestionsDropdown.contains(event.target)) {
            suggestionsDropdown.style.display = 'none';
        }
    }

    // Function to perform search
    function performSearch() {
        const query = searchInput.value.trim();
        if (query !== '') {
            // Redirect to index2.html with query as parameter
            window.location.href = `index2.html?query=${encodeURIComponent(query)}`;
        }
    }

    // Function to fetch suggestions from suggestion.txt
    function fetchSuggestion() {
        fetch(`http://localhost:8080/getSuggestions?${encodeURIComponent("suggestion.txt")}`)
            .then(response => response.text())
            .then(text => {
                // Split the file content by newline character to get individual lines
                suggestions = text.split('\n');
                // Remove any empty lines
                suggestions = suggestions.filter(line => line.trim() !== '');
                console.log('Suggestions:', suggestions);
            })
            .catch(error => {
                console.error('Error:', error);
            });
    }

    // Fetch suggestions when search input is clicked
    searchInput.addEventListener('click', fetchSuggestion);

    // Display suggestions when input value changes
    searchInput.addEventListener('input', displaySuggestions);

    // Hide options dropdown when search input loses focus
    document.addEventListener('click', hideOptionsDropdown);

    // Search when Enter key is pressed
    searchInput.addEventListener('keypress', function(event) {
        if (event.key === 'Enter') {
            performSearch();
        }
    });
});
