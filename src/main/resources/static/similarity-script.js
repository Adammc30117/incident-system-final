function searchSimilarIncidents() {
    const incidentNumber = document.getElementById("incidentNumber").value.trim();
    const resultsContainer = document.getElementById("searchResults");

    if (!incidentNumber) {
        resultsContainer.innerHTML = "<div class='alert alert-warning'>Please enter an incident number!</div>";
        return;
    }

    fetch(`http://localhost:8080/api/incidents/search?incidentNumber=${incidentNumber}`)
        .then(response => response.json())
        .then(data => {
            resultsContainer.innerHTML = "<h5>Similar Incidents</h5><ul class='list-group'>";

            if (!Array.isArray(data) || data.length === 0) {
                resultsContainer.innerHTML += "<li class='list-group-item'>No similar incidents found.</li>";
            } else {
                data.forEach(result => {
                    resultsContainer.innerHTML += `<li class='list-group-item'><strong>${result}</strong></li>`;
                });
            }
            resultsContainer.innerHTML += "</ul>";
        })
        .catch(error => {
            console.error("Error fetching similar incidents:", error);
            resultsContainer.innerHTML = "<div class='alert alert-danger'>Error fetching results. Check console for details.</div>";
        });
}
