const adminApiUrl = "http://localhost:8080/api/incidents";
const adminByTeamApiUrl = "http://localhost:8080/api/admins/by-team"; // Fetch admins by team

// Fetch and display all incidents
function getAllIncidentsForAdmin() {
    fetch(adminApiUrl, { method: "GET", credentials: "same-origin" })
        .then(response => response.json())
        .then(data => {
            const tableBody = document.querySelector("#admin-incidents-table");
            tableBody.innerHTML = "";

            data.forEach((incident, index) => {
                const teamDisplay = incident.assignedTeam ? incident.assignedTeam.name : "Unassigned";
                const adminDisplay = incident.assignedAdmin ? incident.assignedAdmin.username : "Unassigned";

                // Main row
                const row = document.createElement("tr");
                row.innerHTML = `
                    <td>${incident.incidentNumber}</td>
                    <td>${incident.title}</td>
                    <td>${incident.description.split(" ").slice(0, 10).join(" ")}...</td>
                    <td><strong>${incident.status}</strong></td>
                    <td class="text-end">
                        <button class="btn btn-primary btn-sm" onclick="toggleDetails(${index})">▼</button>
                    </td>
                `;
                tableBody.appendChild(row);

                // Build the detail row
                const detailRow = document.createElement("tr");
                detailRow.id = `details-${index}`;
                detailRow.style.display = "none";

                // Condition for showing "Resolve" or "See Resolution" button
                let resolveButtonHtml = "";
                let showResolutionButtonHtml = "";

                // Show "Resolve" button if not resolved
                if (incident.status !== "Resolved") {
                    resolveButtonHtml = `
                        <button class="btn btn-warning btn-sm w-50 mt-2"
                                data-bs-toggle="modal"
                                data-bs-target="#resolveModal"
                                onclick="openResolveModal(${incident.id})">
                            Resolve
                        </button>`;
                }

                // Show "See Resolution Details" if resolved AND resolution is present
                if (incident.status === "Resolved" && incident.resolution) {
                    showResolutionButtonHtml = `
                        <button class="btn btn-info btn-sm w-50 mt-2"
                                onclick="openResolutionDetailsModal('${incident.resolution.replace(/'/g, "\\'")}')">
                            See Resolution Details
                        </button>`;
                }

                detailRow.innerHTML = `
                    <td colspan="5" class="text-end">
                        <div class="incident-details">
                            <p><strong>Description:</strong> ${incident.description}</p>

                            <label><strong>Severity:</strong></label>
                            <select class="form-select w-50 d-inline-block" onchange="updateSeverity(${incident.id}, this.value)">
                                <option value="Low" ${incident.severityLevel === "Low" ? "selected" : ""}>Low</option>
                                <option value="Medium" ${incident.severityLevel === "Medium" ? "selected" : ""}>Medium</option>
                                <option value="High" ${incident.severityLevel === "High" ? "selected" : ""}>High</option>
                            </select>

                            <label><strong>Status:</strong></label>
                            <select class="form-select w-50 d-inline-block" onchange="updateStatus(${incident.id}, this.value)">
                                <option value="Open" ${incident.status === "Open" ? "selected" : ""}>Open</option>
                                <option value="Ongoing" ${incident.status === "Ongoing" ? "selected" : ""}>Ongoing</option>
                                <!-- "Closed" removed; "Resolved" also removed from direct dropdown -->
                            </select>

                            <label><strong>Assigned Team:</strong></label>
                            <select class="form-select w-50 d-inline-block" onchange="updateAssignedTeam(${incident.id}, this.value)">
                                <option value="Unassigned">Unassigned</option>
                                <option value="1" ${incident.assignedTeam && incident.assignedTeam.id === 1 ? "selected" : ""}>IT</option>
                                <option value="2" ${incident.assignedTeam && incident.assignedTeam.id === 2 ? "selected" : ""}>HR</option>
                                <option value="3" ${incident.assignedTeam && incident.assignedTeam.id === 3 ? "selected" : ""}>Support</option>
                            </select>

                            <label><strong>Assigned Admin:</strong></label>
                            <select id="admin-select-${incident.id}" class="form-select w-50 d-inline-block" 
                                    ${incident.assignedTeam ? "" : "disabled"} 
                                    onchange="updateAssignedAdmin(${incident.id}, this.value)">
                                <option value="Unassigned">Unassigned</option>
                            </select>

                            <br><br>
                            ${resolveButtonHtml}
                            ${showResolutionButtonHtml}
                            <button class="btn btn-danger btn-sm w-50 mt-2" onclick="deleteIncident(${incident.id})">Delete</button>
                            <button class="btn btn-secondary btn-sm w-50 mt-2" onclick="toggleDetails(${index})">▲ Close</button>
                        </div>
                    </td>
                `;

                tableBody.appendChild(detailRow);

                // Load admins if assignedTeam is set
                if (incident.assignedTeam) {
                    loadAdminsForTeam(incident.id, incident.assignedTeam.id);
                }
            });
        })
        .catch(error => {
            console.error("Error fetching admin incidents:", error);
            alert("Error loading incidents. Please try again.");
        });
}

// This function shows the resolution text in a separate modal
function openResolutionDetailsModal(resolutionText) {
    // Set the text in the modal
    document.getElementById("resolutionDetailsText").textContent = resolutionText;
    // Show the modal
    const resolutionModal = new bootstrap.Modal(document.getElementById('resolutionDetailsModal'));
    resolutionModal.show();
}

// Toggle details function
function toggleDetails(index) {
    const detailRow = document.getElementById(`details-${index}`);
    detailRow.style.display = detailRow.style.display === "none" ? "table-row" : "none";
}

// Open the resolve modal for a specific incident
function openResolveModal(incidentId) {
    document.getElementById("resolveIncidentId").value = incidentId;
    document.getElementById("resolutionText").value = "";
}

// Submit the resolution details
function submitResolution() {
    const incidentId = document.getElementById("resolveIncidentId").value;
    const resolutionText = document.getElementById("resolutionText").value.trim();

    if (!resolutionText) {
        alert("Please provide resolution details!");
        return;
    }

    fetch(`http://localhost:8080/api/incidents/${incidentId}/resolve`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ resolution: resolutionText })
    })
        .then(response => {
            if (!response.ok) {
                throw new Error("Failed to resolve incident");
            }
            return response.text();
        })
        .then(message => {
            alert(message);
            // Hide the modal using Bootstrap
            const modal = bootstrap.Modal.getInstance(document.getElementById('resolveModal'));
            modal.hide();
            // Refresh the incidents
            getAllIncidentsForAdmin();
        })
        .catch(error => {
            console.error(error);
            alert("Error resolving incident. Check console for details.");
        });
}

// Function to update assigned team and reset admin to "Unassigned"
function updateAssignedTeam(incidentId, teamId) {
    const assignedAdminDropdown = document.getElementById(`admin-select-${incidentId}`);

    fetch(`${adminApiUrl}/${incidentId}/assign`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
            assignedTeam: teamId !== "Unassigned" ? teamId : null,
            assignedAdmin: null // Reset admin selection
        })
    })
        .then(() => {
            assignedAdminDropdown.innerHTML = '<option value="Unassigned">Unassigned</option>';
            assignedAdminDropdown.disabled = true;
            if (teamId !== "Unassigned") {
                loadAdminsForTeam(incidentId, teamId);
            }
        })
        .catch(error => {
            console.error("Error updating assigned team:", error);
            alert("Error updating team. Please try again.");
        });
}

// Function to load admins dynamically based on selected team
function loadAdminsForTeam(incidentId, teamId) {
    const adminDropdown = document.getElementById(`admin-select-${incidentId}`);
    adminDropdown.innerHTML = '<option value="Unassigned">Unassigned</option>';

    if (teamId === "Unassigned") {
        adminDropdown.disabled = true;
        return;
    }

    fetch(`${adminByTeamApiUrl}/${teamId}`)
        .then(response => response.json())
        .then(admins => {
            if (admins.length > 0) {
                admins.forEach(admin => {
                    const option = document.createElement("option");
                    option.value = admin.id;
                    option.textContent = admin.username;
                    adminDropdown.appendChild(option);
                });
                // Auto-select first admin
                adminDropdown.value = admins[0].id;
                updateAssignedAdmin(incidentId, admins[0].id);
            }
            adminDropdown.disabled = false;
        })
        .catch(error => console.error("Error loading admins:", error));
}

// Function to update assigned admin
function updateAssignedAdmin(incidentId, adminId) {
    fetch(`${adminApiUrl}/${incidentId}/assign`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ assignedAdmin: adminId !== "Unassigned" ? adminId : null })
    })
        .catch(error => {
            console.error("Error updating assigned admin:", error);
            alert("Error updating admin. Please try again.");
        });
}

// Function to update severity level
function updateSeverity(id, severityLevel) {
    fetch(`http://localhost:8080/api/incidents/${id}/severity`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ severityLevel })
    })
        .then(response => response.ok ? console.log("Severity updated!") : console.error("Error updating severity"))
        .catch(error => console.error(error));
}

// Function to update status (Removed "Closed" from the dropdown, so this won't set to closed or resolved)
function updateStatus(id, status) {
    fetch(`http://localhost:8080/api/incidents/${id}/status`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ status })
    })
        .then(response => response.ok ? console.log("Status updated!") : console.error("Error updating status"))
        .catch(error => console.error(error));
}

// Function to delete an incident
function deleteIncident(id) {
    fetch(`${adminApiUrl}/${id}`, {
        method: "DELETE",
        credentials: "same-origin"
    })
        .then(response => {
            if (response.ok) {
                alert("Incident deleted successfully!");
                getAllIncidentsForAdmin();
            } else {
                alert("Error deleting incident!");
            }
        })
        .catch(error => console.error("Error deleting incident:", error));
}

// Function to filter incidents based on status, team, and admin
function filterIncidents() {
    const selectedStatus = document.getElementById("filterStatus").value;
    const selectedTeam = document.getElementById("filterTeam").value;
    const selectedAdmin = document.getElementById("filterAdmin").value;

    let url = `http://localhost:8080/api/incidents?status=${selectedStatus}`;

    if (selectedTeam !== "All") {
        url += `&teamId=${selectedTeam}`;
    }

    if (selectedAdmin !== "All") {
        url += `&adminId=${selectedAdmin}`;
    }

    fetch(url)
        .then(response => response.json())
        .then(data => {
            const tableBody = document.querySelector("#admin-incidents-table");
            tableBody.innerHTML = ""; // Clear the table before rendering new rows
            data.forEach((incident, index) => {
                // same logic to build the main row + detail row
                // or you can re-call getAllIncidentsForAdmin() if you want consistent rendering
            });
        })
        .catch(error => console.error("Error fetching incidents:", error));
}

// On page load
document.addEventListener("DOMContentLoaded", () => {
    getAllIncidentsForAdmin();
});
