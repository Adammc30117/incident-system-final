document.addEventListener("DOMContentLoaded", function () {
    getUserIncidents(); // ✅ Load user-specific incidents after login
    checkAdminDashboard(); // ✅ Load admin dashboard if applicable
});

// ✅ API Endpoints
const apiUrl = "http://localhost:8080/api/incidents";
const userIncidentsUrl = `${apiUrl}/user`;
const adminIncidentsUrl = `${apiUrl}`; // Admin fetches all incidents
const searchIncidentsUrl = `${apiUrl}/search`;

// ✅ Fetch and display incidents for the logged-in user
function getUserIncidents() {
    const tableBody = document.querySelector("#incidents-table");

    if (!tableBody) {
        console.error("Error: #incidents-table element not found!");
        return;
    }

    fetch(userIncidentsUrl, { credentials: "include" }) // ✅ Ensure cookies/session are included
        .then(response => response.json())
        .then(data => {
            console.log("Fetched User Incidents:", data); // ✅ Debugging output
            tableBody.innerHTML = ""; // ✅ Clear previous incidents

            if (data.length === 0) {
                tableBody.innerHTML = "<tr><td colspan='4' class='text-center'>No incidents found</td></tr>";
                return;
            }

            data.forEach(incident => {
                const row = document.createElement("tr");
                row.innerHTML = `
                    <td>${incident.incidentNumber}</td>
                    <td>${incident.title}</td>
                    <td>${incident.description}</td>
                    <td>${incident.status}</td>
                `;
                tableBody.appendChild(row);
            });
        })
        .catch(error => console.error("Error fetching user incidents:", error));
}

// ✅ Fetch and display incidents for the admin dashboard
function checkAdminDashboard() {
    const tableBody = document.querySelector("#admin-incidents-table");

    if (!tableBody) return; // ✅ Don't execute if not on the admin page

    fetch(adminIncidentsUrl, { credentials: "include" })
        .then(response => response.json())
        .then(data => {
            console.log("Fetched Admin Incidents:", data); // ✅ Debugging output
            tableBody.innerHTML = ""; // ✅ Clear previous incidents

            data.forEach(incident => {
                const row = document.createElement("tr");
                row.innerHTML = `
                    <td>${incident.incidentNumber}</td>
                    <td>${incident.title}</td>
                    <td>${incident.description}</td>
                    <td>${incident.severityLevel}</td>
                    <td>${incident.status}</td>
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
                        <button class="btn btn-danger" onclick="deleteIncident(${incident.id})">Delete</button>
                    </td>
                `;
                tableBody.appendChild(row);
            });
        })
        .catch(error => console.error("Error fetching admin incidents:", error));
}

// ✅ Submit a new incident (Includes createdBy)
document.getElementById("incident-form").addEventListener("submit", function (event) {
    event.preventDefault(); // ✅ Prevent page refresh

    fetch("http://localhost:8080/api/users/role", { credentials: "include" })
        .then(response => response.json())
        .then(userData => {
            const createdBy = userData.username; // ✅ Get logged-in user's username

            const newIncident = {
                title: document.getElementById("title").value,
                description: document.getElementById("description").value,
                severityLevel: document.getElementById("status").value,
                createdBy: createdBy // ✅ Include 'createdBy' in the request
            };

            fetch("http://localhost:8080/api/incidents", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(newIncident),
            })
                .then(response => {
                    if (response.ok) {
                        alert("Incident created successfully!");
                        getUserIncidents(); // ✅ Refresh the list on the index page
                        checkAdminDashboard(); // ✅ Refresh the admin dashboard
                        document.getElementById("incident-form").reset(); // ✅ Clear form
                    } else {
                        response.text().then(text => alert("Error creating incident: " + text));
                    }
                })
                .catch(error => console.error("Error creating incident:", error));
        });
});

// ✅ Similarity Search
function searchSimilarIncidents() {
    const incidentNumber = document.getElementById("searchQuery").value;

    fetch(`${searchIncidentsUrl}?incidentNumber=${incidentNumber}`)
        .then(response => response.json())
        .then(data => {
            const resultsContainer = document.getElementById("searchResults");
            resultsContainer.innerHTML = "<h5>Similar Incidents</h5><ul class='list-group'>";

            if (data.length === 0) {
                resultsContainer.innerHTML += "<li class='list-group-item'>No similar incidents found.</li>";
            } else {
                data.forEach(result => {
                    resultsContainer.innerHTML += `<li class='list-group-item'>${result}</li>`;
                });
            }
            resultsContainer.innerHTML += "</ul>";
        })
        .catch(error => console.error("Error fetching similar incidents:", error));
}

// ✅ Delete an incident (Admin Only)
function deleteIncident(id) {
    fetch(`${apiUrl}/${id}`, {
        method: "DELETE",
        credentials: "same-origin"
    })
        .then(response => {
            if (response.ok) {
                alert("Incident deleted successfully!");
                checkAdminDashboard(); // ✅ Refresh admin dashboard
            } else {
                alert("Error deleting incident!");
            }
        })
        .catch(error => console.error("Error deleting incident:", error));
}

// ✅ Update Incident Severity (Admin Only)
function updateSeverity(id, severityLevel) {
    fetch(`${apiUrl}/${id}/severity`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ severityLevel })
    })
        .then(response => response.text())
        .then(message => console.log(message))
        .catch(error => console.error(error));
}

// ✅ Update Incident Status (Admin Only)
function updateStatus(id, status) {
    fetch(`${apiUrl}/${id}/status`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ status })
    })
        .then(response => response.text())
        .then(message => console.log(message))
        .catch(error => console.error(error));
}

// ✅ Validate Admin Access
function validateAdminAccess() {
    fetch("http://localhost:8080/api/users/role", { credentials: "include" })
        .then(response => response.json())
        .then(data => {
            if (data.role === "ROLE_ADMIN") {
                window.location.href = "admin-dashboard.html";
            } else {
                alert("You must be an admin to access this page.");
            }
        })
        .catch(error => console.error("Error fetching user role:", error));
}

// ✅ Logout Functionality
document.getElementById("logoutButton").addEventListener("click", function () {
    fetch("/perform_logout", { method: "POST", credentials: "same-origin" })
        .then(() => { window.location.href = "/login"; })
        .catch(error => console.error("Error logging out:", error));
});
