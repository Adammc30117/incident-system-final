function searchSimilarIncidents() {
    const incidentNumber = document.getElementById("incidentNumber").value.trim();
    const resultsContainer = document.getElementById("searchResultsContainer");
    const tableBody = document.getElementById("similarIncidentsTable");
    const searchedIncidentContainer = document.getElementById("searchedIncidentContainer");
    const searchedIncidentTable = document.getElementById("searchedIncidentTable");

    // Clear old results
    tableBody.innerHTML = "";
    searchedIncidentTable.innerHTML = "";

    if (!incidentNumber) {
        resultsContainer.style.display = "none";
        alert("Please enter an incident number!");
        return;
    }

    //  Fetch the searched-for incident
    fetch(`http://localhost:8080/api/incidents/${encodeURIComponent(incidentNumber)}`)
        .then(response => {
            if (!response.ok) {
                throw new Error("Unable to fetch the searched incident.");
            }
            return response.json();
        })
        .then(searchedIncident => {
            //  Display the searched-for incident above the similarity table
            searchedIncidentContainer.style.display = "block";

            const teamDisplay = searchedIncident.assignedTeamName ||
                (searchedIncident.assignedTeam ? searchedIncident.assignedTeam.name : "Unassigned");

            const adminDisplay = searchedIncident.assignedAdminUsername ||
                (searchedIncident.assignedAdmin ? searchedIncident.assignedAdmin.username : "Unassigned");

            const description = searchedIncident.description || "No description available";
            const severity = searchedIncident.severityLevel || "N/A";

            let resolutionButtonHtml = "";
            if (searchedIncident.status === "Resolved" && searchedIncident.resolution && searchedIncident.resolution.trim() !== "") {
                const escapedResolution = searchedIncident.resolution.replace(/'/g, "\\'");
                resolutionButtonHtml = `
                    <button class="btn btn-info btn-sm w-50 mt-2"
                            onclick="openResolutionDetailsModal('${escapedResolution}')">
                        See Resolution Details
                    </button>
                `;
            }

            // Create searched-for incident row
            const searchedRow = document.createElement("tr");
            searchedRow.innerHTML = `
                <td>${searchedIncident.incidentNumber}</td>
                <td>${searchedIncident.title}</td>
                <td>${searchedIncident.status}</td>
                <td class="text-end">
                    <button class="btn btn-primary btn-sm" onclick="toggleDetails('searched')">▼</button>
                </td>
            `;
            searchedIncidentTable.appendChild(searchedRow);

            // Create detail row
            const detailRow = document.createElement("tr");
            detailRow.id = `details-searched`;
            detailRow.style.display = "none";
            detailRow.innerHTML = `
                <td colspan="4">
                    <div class="incident-details"
                         style="background: #f9f9f9; padding: 10px; border-radius: 5px;">
                        <p><strong>Description:</strong> ${description}</p>
                        <p><strong>Severity:</strong> ${severity}</p>
                        <p><strong>Assigned Team:</strong> ${teamDisplay}</p>
                        <p><strong>Assigned Admin:</strong> ${adminDisplay}</p>
                        ${resolutionButtonHtml}
                    </div>
                </td>
            `;
            searchedIncidentTable.appendChild(detailRow);

            // Fetch Similar Incidents
            return fetch(`http://localhost:8080/api/incidents/search?incidentNumber=${encodeURIComponent(incidentNumber)}`);
        })
        .then(response => {
            if (!response.ok) {
                throw new Error("No similar incidents found.");
            }
            return response.json();
        })
        .then(data => {
            if (!Array.isArray(data) || data.length === 0) {
                resultsContainer.style.display = "none";
                alert("No similar incidents found.");
                return;
            }

            // Automatically sort results from highest to lowest match percentage
            data.sort((a, b) => b.matchPercentage - a.matchPercentage);

            resultsContainer.style.display = "block";

            data.forEach((incident, index) => {
                const teamDisplay = incident.assignedTeamName || "Unassigned";
                const adminDisplay = incident.assignedAdminUsername || "Unassigned";
                const description = incident.description || "No description available";
                const severity = incident.severityLevel || "N/A";

                let resolutionButtonHtml = "";
                if (incident.status === "Resolved" && incident.resolution && incident.resolution.trim() !== "") {
                    const escapedResolution = incident.resolution.replace(/'/g, "\\'");
                    resolutionButtonHtml = `
                <button class="btn btn-info btn-sm w-50 mt-2"
                        onclick="openResolutionDetailsModal('${escapedResolution}')">
                    See Resolution Details
                </button>
            `;
                }

                const row = document.createElement("tr");
                row.innerHTML = `
            <td>${incident.incidentNumber}</td>
            <td>${incident.title}</td>
            <td>${incident.status}</td>
            <td>${incident.matchPercentage.toFixed(2)}%</td>
            <td class="text-end">
                <button class="btn btn-primary btn-sm" onclick="toggleDetails(${index})">▼</button>
            </td>
        `;
                tableBody.appendChild(row);

                const detailRow = document.createElement("tr");
                detailRow.id = `details-${index}`;
                detailRow.style.display = "none";
                detailRow.innerHTML = `
            <td colspan="5">
                <div class="incident-details"
                     style="background: #f9f9f9; padding: 10px; border-radius: 5px;">
                    <p><strong>Description:</strong> ${description}</p>
                    <p><strong>Severity:</strong> ${severity}</p>
                    <p><strong>Assigned Team:</strong> ${teamDisplay}</p>
                    <p><strong>Assigned Admin:</strong> ${adminDisplay}</p>
                    ${resolutionButtonHtml}
                </div>
            </td>
        `;
                tableBody.appendChild(detailRow);
            });
        })

        .catch(error => {
            console.error("Error fetching incidents:", error);
            alert(error.message);
        });
}

// Toggle detail rows open/closed
function toggleDetails(index) {
    const detailRow = document.getElementById(`details-${index}`);
    detailRow.style.display = (detailRow.style.display === "none" ? "table-row" : "none");
}

// Show resolution text in a modal
function openResolutionDetailsModal(resolutionText) {
    document.getElementById("resolutionDetailsText").textContent = resolutionText;
    const resolutionModal = new bootstrap.Modal(document.getElementById('resolutionDetailsModal'));
    resolutionModal.show();
}
//  Logout Functionality
document.getElementById("logoutButton").addEventListener("click", function () {
    fetch("/perform_logout", { method: "POST", credentials: "same-origin" })
        .then(() => { window.location.href = "/login"; })
        .catch(error => console.error("Error logging out:", error));
});

