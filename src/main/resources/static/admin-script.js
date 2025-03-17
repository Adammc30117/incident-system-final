const adminApiUrl = "http://localhost:8080/api/incidents"; // Admin API for fetching all incidents

// Fetch and display all incidents for the admin
function getAllIncidentsForAdmin() {
    fetch(adminApiUrl, {
        method: "GET",
        credentials: "same-origin" // Ensures cookies are sent for authentication
    })
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            console.log("Fetched incidents:", data);

            const tableBody = document.querySelector("#admin-incidents-table");
            if (!tableBody) {
                console.error("Error: Table body not found!");
                return;
            }

            tableBody.innerHTML = ""; // Clear existing rows

            data.forEach(incident => {
                const row = document.createElement("tr");
                row.innerHTML = `
                    <td>${incident.incidentNumber}</td>
                    <td>${incident.title}</td>
                    <td>${incident.description}</td>
                    <td>
                        <select class="form-select" onchange="updateSeverity(${incident.id}, this.value)">
                            <option value="Low" ${incident.severityLevel === "Low" ? "selected" : ""}>Low</option>
                            <option value="Medium" ${incident.severityLevel === "Medium" ? "selected" : ""}>Medium</option>
                            <option value="High" ${incident.severityLevel === "High" ? "selected" : ""}>High</option>
                        </select>
                    </td>
                    <td>
                        <select class="form-select" onchange="updateStatus(${incident.id}, this.value)">
                            <option value="Open" ${incident.status === "Open" ? "selected" : ""}>Open</option>
                            <option value="Ongoing" ${incident.status === "Ongoing" ? "selected" : ""}>Ongoing</option>
                            <option value="Closed" ${incident.status === "Closed" ? "selected" : ""}>Closed</option>
                        </select>
                    </td>
                    <td>
                        <select class="form-select" onchange="updateAssignedTeam(${incident.id}, this.value)">
                            <option value="1" ${incident.assignedTeam && incident.assignedTeam.id === 1 ? "selected" : ""}>IT</option>
                            <option value="2" ${incident.assignedTeam && incident.assignedTeam.id === 2 ? "selected" : ""}>HR</option>
                            <option value="3" ${incident.assignedTeam && incident.assignedTeam.id === 3 ? "selected" : ""}>Support</option>
                        </select>
                    </td>
                    <td>
                        <select class="form-select" onchange="updateAssignedAdmin(${incident.id}, this.value)">
                            <option value="1" ${incident.assignedAdmin && incident.assignedAdmin.id === 1 ? "selected" : ""}>john.doe</option>
                            <option value="2" ${incident.assignedAdmin && incident.assignedAdmin.id === 2 ? "selected" : ""}>jane.smith</option>
                            <option value="3" ${incident.assignedAdmin && incident.assignedAdmin.id === 3 ? "selected" : ""}>michael.johnson</option>
                        </select>
                    </td>
                    <td>
                        <button class="btn btn-danger" onclick="deleteIncident(${incident.id})">Delete</button>
                    </td>
                `;
                tableBody.appendChild(row);
            });
        })
        .catch(error => console.error("Error fetching admin incidents:", error));
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

// Function to update status
function updateStatus(id, status) {
    fetch(`http://localhost:8080/api/incidents/${id}/status`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ status })
    })
        .then(response => response.ok ? console.log("Status updated!") : console.error("Error updating status"))
        .catch(error => console.error(error));
}

// Function to update assigned team
function updateAssignedTeam(id, assignedTeamValue) {
    // Send team ID to backend
    fetch(`http://localhost:8080/api/incidents/${id}/assign`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ assignedTeam: assignedTeamValue }) // Pass team ID
    })
        .then(response => {
            if (!response.ok) throw new Error("Error updating assigned team");
            return response.text();
        })
        .then(message => console.log(message))
        .catch(error => console.error(error));
}

// Function to update assigned admin
function updateAssignedAdmin(id, assignedAdminValue, assignedTeamValue) {
    fetch(`http://localhost:8080/api/incidents/${id}/assign`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
            assignedTeam: assignedTeamValue,  // Pass the correct team ID
            assignedAdmin: assignedAdminValue  // Pass the correct admin ID
        })
    })
        .then(response => {
            if (!response.ok) throw new Error("Error updating assigned admin");
            return response.text();
        })
        .then(message => console.log(message))
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
            data.forEach(incident => {
                const row = document.createElement("tr");
                row.innerHTML = `
                    <td>${incident.incidentNumber}</td>
                    <td>${incident.title}</td>
                    <td>${incident.description}</td>
                    <td>${incident.severityLevel}</td>
                    <td>${incident.status}</td>
                    <td>${incident.assignedTeam ? incident.assignedTeam.name : "Not assigned"}</td>
                    <td>${incident.assignedAdmin ? incident.assignedAdmin.username : "Not assigned"}</td>
                    <td>
                        <button class="btn btn-danger" onclick="deleteIncident(${incident.id})">Delete</button>
                    </td>
                `;
                tableBody.appendChild(row);
            });
        })
        .catch(error => console.error("Error fetching incidents:", error));
}

// Load incidents when the page loads
document.addEventListener("DOMContentLoaded", () => {
    getAllIncidentsForAdmin();
});
