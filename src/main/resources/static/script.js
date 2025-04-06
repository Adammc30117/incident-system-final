//  API Endpoints
const apiUrl = "http://localhost:8080/api/incidents";
const userIncidentsUrl = `${apiUrl}/user`;
const adminIncidentsUrl = `${apiUrl}`;
const searchIncidentsUrl = `${apiUrl}/search`;

// Pagination config for user incidents
let userCurrentPage = 1;
const userIncidentsPerPage = 10;
let userAllIncidents = [];

// Helper function to format dates like "April 6th, 13:25"
function formatCustomDate(dateString) {
    const date = new Date(dateString);
    const options = { month: "long", day: "numeric" };
    const formattedDate = date.toLocaleDateString("en-US", options);
    const day = date.getDate();
    const suffix = (day % 10 === 1 && day !== 11) ? "st" :
        (day % 10 === 2 && day !== 12) ? "nd" :
            (day % 10 === 3 && day !== 13) ? "rd" : "th";
    const time = date.toLocaleTimeString("en-GB", { hour: "2-digit", minute: "2-digit" });
    return `${formattedDate.replace(/\d+/, day + suffix)}, ${time}`;
}

function getUserIncidents() {
    fetch("http://localhost:8080/api/incidents/my-incidents", {
        method: "GET",
        credentials: "same-origin"
    })
        .then(response => response.json())
        .then(data => {
            data.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
            userAllIncidents = data;
            userCurrentPage = 1;
            displayUserPaginatedIncidents();
            updateUserPagination();
        })
        .catch(error => console.error("Error fetching user incidents:", error));
}

function displayUserPaginatedIncidents() {
    const tableBody = document.querySelector("#incidents-table");
    tableBody.innerHTML = "";

    const start = (userCurrentPage - 1) * userIncidentsPerPage;
    const end = start + userIncidentsPerPage;
    const paginatedIncidents = userAllIncidents.slice(start, end);

    paginatedIncidents.forEach((incident, index) => {
        const globalIndex = start + index;
        const createdAtFormatted = incident.createdAt ? formatCustomDate(incident.createdAt) : "Unknown";
        const partialDescription = incident.description ? incident.description.split(" ").slice(0, 10).join(" ") + "..." : "No description";

        const mainRow = document.createElement("tr");
        mainRow.innerHTML = `
            <td>${incident.incidentNumber}</td>
            <td>${incident.title}</td>
            <td>${partialDescription}</td>
            <td>${incident.status}</td>
            <td>${createdAtFormatted}</td>
            <td class="text-end">
                <button class="btn btn-primary btn-sm" onclick="toggleUserIncidentDetails(${globalIndex})">▼</button>
            </td>`;
        tableBody.appendChild(mainRow);

        const detailRow = document.createElement("tr");
        detailRow.id = `userIncidentDetails-${globalIndex}`;
        detailRow.style.display = "none";
        const assignedAdmin = incident.assignedAdmin?.username || "Unassigned";
        const assignedTeam = incident.assignedTeam?.name || "Unassigned";
        detailRow.innerHTML = `
            <td colspan="6">
                <div style="background: #f9f9f9; padding: 10px; border-radius: 5px; text-align: left;">
                    <p><strong>Full Description:</strong> ${incident.description}</p>
                    <p><strong>Assigned Admin:</strong> ${assignedAdmin}</p>
                    <p><strong>Assigned Team:</strong> ${assignedTeam}</p>
                    <p><strong>Submitted On:</strong> ${createdAtFormatted}</p>
                </div>
            </td>`;
        tableBody.appendChild(detailRow);
    });
}

function updateUserPagination() {
    const paginationContainer = document.getElementById("user-pagination-controls");
    if (!paginationContainer) return;
    paginationContainer.innerHTML = "";

    const totalPages = Math.ceil(userAllIncidents.length / userIncidentsPerPage);

    const prevBtn = document.createElement("button");
    prevBtn.innerText = "« Prev";
    prevBtn.className = "pagination-btn me-2";
    prevBtn.disabled = userCurrentPage === 1;
    prevBtn.onclick = () => changeUserPage(-1);
    paginationContainer.appendChild(prevBtn);

    for (let i = 1; i <= totalPages; i++) {
        const btn = document.createElement("button");
        btn.innerText = i;
        btn.className = `pagination-btn ${i === userCurrentPage ? "active" : ""}`;
        btn.onclick = () => {
            userCurrentPage = i;
            displayUserPaginatedIncidents();
            updateUserPagination();
        };
        paginationContainer.appendChild(btn);
    }

    const nextBtn = document.createElement("button");
    nextBtn.innerText = "Next »";
    nextBtn.className = "pagination-btn ms-2";
    nextBtn.disabled = userCurrentPage === totalPages;
    nextBtn.onclick = () => changeUserPage(1);
    paginationContainer.appendChild(nextBtn);
}

function changeUserPage(direction) {
    const totalPages = Math.ceil(userAllIncidents.length / userIncidentsPerPage);
    userCurrentPage = Math.min(Math.max(1, userCurrentPage + direction), totalPages);
    displayUserPaginatedIncidents();
    updateUserPagination();
}

function toggleUserIncidentDetails(index) {
    const detailRow = document.getElementById(`userIncidentDetails-${index}`);
    if (detailRow) {
        detailRow.style.display = detailRow.style.display === "none" ? "table-row" : "none";
    }
}

// Fetch and display incidents for the admin dashboard
function checkAdminDashboard() {
    const tableBody = document.querySelector("#admin-incidents-table");

    if (!tableBody) return; //  Don't execute if not on the admin page

    fetch(adminIncidentsUrl, { credentials: "include" })
        .then(response => response.json())
        .then(data => {
            console.log("Fetched Admin Incidents:", data); //  Debugging output
            tableBody.innerHTML = ""; //  Clear previous incidents

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

// Function to show success modal
function showSuccessModal() {
    const successModal = new bootstrap.Modal(document.getElementById('successModal'));
    successModal.show();
}

//  Submit a new incident with inline validation
document.getElementById("incident-form").addEventListener("submit", function (event) {
    event.preventDefault(); // Prevent immediate form submission

    const title = document.getElementById("title");
    const description = document.getElementById("description");
    const severityLevel = document.getElementById("status");

    let valid = true;

    //  Validate Title (Min 20 Characters)
    if (title.value.trim().length < 20) {
        title.classList.add("is-invalid");
        valid = false;
    } else {
        title.classList.remove("is-invalid");
        title.classList.add("is-valid");
    }

    //  Validate Description (Min 50 Characters, No Keyboard Mashing)
    if (description.value.trim().length < 50 || isKeyboardMashing(description.value.trim())) {
        description.classList.add("is-invalid");
        valid = false;
    } else {
        description.classList.remove("is-invalid");
        description.classList.add("is-valid");
    }

    //  Validate Severity Level Selection
    if (!severityLevel.value) {
        severityLevel.classList.add("is-invalid");
        valid = false;
    } else {
        severityLevel.classList.remove("is-invalid");
        severityLevel.classList.add("is-valid");
    }

    // If validation fails, stop submission
    if (!valid) return;

    //  Show confirmation modal before final submission
    const confirmModal = new bootstrap.Modal(document.getElementById('confirmSubmitModal'));
    confirmModal.show();

    document.getElementById("confirmSubmitBtn").onclick = function () {
        confirmModal.hide(); // Close modal

        fetch("http://localhost:8080/api/users/role", { credentials: "include" })
            .then(response => response.json())
            .then(userData => {
                const createdBy = userData.username;

                const newIncident = {
                    title: title.value.trim(),
                    description: description.value.trim(),
                    severityLevel: severityLevel.value,
                    createdBy: createdBy
                };

                fetch(apiUrl, {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(newIncident),
                })
                    .then(response => {
                        if (response.ok) {
                            showSuccessModal(); //  Show success modal
                            getUserIncidents(); //  Refresh incidents list
                            checkAdminDashboard(); //  Refresh admin dashboard
                            document.getElementById("incident-form").reset(); //  Clear form

                            // Reset validation styles
                            title.classList.remove("is-valid");
                            description.classList.remove("is-valid");
                            severityLevel.classList.remove("is-valid");
                        } else {
                            response.text().then(text => alert("Error creating incident: " + text));
                        }
                    })
                    .catch(error => console.error("Error creating incident:", error));
            });
    };
});

//  Function to detect keyboard mashing
function isKeyboardMashing(text) {
    const lowerText = text.toLowerCase();

    //  Check for repetitive characters (e.g., "aaaaaa", "qqqqqqq")
    if (/(.)\1{6,}/.test(lowerText)) return true;

    //  Check if there are more than 50% non-alphabetic characters
    const nonAlphaCount = (lowerText.match(/[^a-z\s]/g) || []).length;
    if (nonAlphaCount > text.length * 0.5) return true;

    //  Check for repeated patterns (e.g., "qwertyqwerty")
    const patterns = ["asdf", "qwert", "1234", "0000", "1111", "9999", "abcdef"];
    for (const pattern of patterns) {
        const regex = new RegExp(`(${pattern}){2,}`, "i");
        if (regex.test(lowerText)) return true;
    }

    return false;
}

//  Similarity Search
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

//  Delete an incident (Admin Only)
function deleteIncident(id) {
    fetch(`${apiUrl}/${id}`, {
        method: "DELETE",
        credentials: "same-origin"
    })
        .then(response => {
            if (response.ok) {
                alert("Incident deleted successfully!");
                checkAdminDashboard(); //  Refresh admin dashboard
            } else {
                alert("Error deleting incident!");
            }
        })
        .catch(error => console.error("Error deleting incident:", error));
}

//  Update Incident Severity (Admin Only)
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

//  Update Incident Status (Admin Only)
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

//  Validate Admin Access
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

//  Logout Functionality
document.getElementById("logoutButton").addEventListener("click", function () {
    fetch("/perform_logout", { method: "POST", credentials: "same-origin" })
        .then(() => { window.location.href = "/login"; })
        .catch(error => console.error("Error logging out:", error));
});

document.addEventListener("DOMContentLoaded", function () {
    getUserIncidents(); //  Load user-specific incidents after login
    checkAdminDashboard(); //  Load admin dashboard if applicable
});