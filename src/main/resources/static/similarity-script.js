function searchSimilarIncidents() {
    const incidentNumber = document.getElementById("incidentNumber").value.trim();
    const resultsContainer = document.getElementById("searchResultsContainer");
    const tableBody = document.getElementById("similarIncidentsTable");

    // Clear any old results
    tableBody.innerHTML = "";

    if (!incidentNumber) {
        resultsContainer.style.display = "none";
        alert("Please enter an incident number!");
        return;
    }

    fetch(`http://localhost:8080/api/incidents/search?incidentNumber=${incidentNumber}`)
        .then(response => {
            if (!response.ok) {
                throw new Error("Incident not found or error fetching data");
            }
            return response.json();
        })
        .then(data => {
            // If 'data' is a string (like an error message), handle that
            if (!Array.isArray(data) || data.length === 0) {
                resultsContainer.style.display = "none";
                alert("No similar incidents found.");
                return;
            }

            // Show the container/table if we have results
            resultsContainer.style.display = "block";

            data.forEach((result, index) => {
                // Create main row
                const row = document.createElement("tr");
                row.innerHTML = `
                    <td>${result.incidentNumber}</td>
                    <td>${result.title}</td>
                    <td>${result.status}</td>
                    <td>${result.matchPercentage.toFixed(2)}%</td>
                    <td class="text-end">
                        <button class="btn btn-primary btn-sm" onclick="toggleDetails(${index})">▼</button>
                    </td>
                `;
                tableBody.appendChild(row);

                // Create details row
                const detailRow = document.createElement("tr");
                detailRow.id = `details-${index}`;
                detailRow.style.display = "none"; // Hidden by default
                detailRow.innerHTML = `
                    <td colspan="5">
                        <div class="incident-details" style="background: #f9f9f9; padding: 10px; border-radius: 5px;">
                            <p><strong>Description:</strong> ${result.description || "No description"}</p>
                            <p><strong>Severity:</strong> ${result.severityLevel || "N/A"}</p>
                            <p><strong>Assigned Team:</strong> ${result.assignedTeamName || "Unassigned"}</p>
                            <p><strong>Assigned Admin:</strong> ${result.assignedAdminUsername || "Unassigned"}</p>
                        </div>
                    </td>
                `;
                tableBody.appendChild(detailRow);
            });
        })
        .catch(error => {
            console.error("Error:", error);
            resultsContainer.style.display = "none";
            alert("Error fetching similar incidents. Check console for details.");
        });
}

// Toggle details function
function toggleDetails(index) {
    const detailRow = document.getElementById(`details-${index}`);
    detailRow.style.display = detailRow.style.display === "none" ? "table-row" : "none";
}
