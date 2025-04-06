// === Base API URLs ===
const adminApiUrl = "http://localhost:8080/api/incidents";
const adminByTeamApiUrl = "http://localhost:8080/api/assignments/admins/by-team";

// === Pagination Config ===
let currentPage = 1;
const incidentsPerPage = 10;
let allIncidents = [];

/**
 * Fetches all incidents for the admin and initializes pagination.
 */
function getAllIncidentsForAdmin() {
    fetch(adminApiUrl, { method: "GET", credentials: "same-origin" })
        .then(response => response.json())
        .then(data => {
            allIncidents = data;
            currentPage = 1;
            isFiltered = false;
            displayIncidents();    // Display incidents on the current page
            updatePagination();    // Generate pagination controls
        })
        .catch(error => {
            console.error("Error fetching admin incidents:", error);
            alert("Error loading incidents. Please try again.");
        });
}

/**
 * Displays paginated incidents in the table along with expandable details.
 */
function displayIncidents() {
    const tableBody = document.querySelector("#admin-incidents-table");
    tableBody.innerHTML = "";

    const start = (currentPage - 1) * incidentsPerPage;
    const end = start + incidentsPerPage;
    const incidentsToDisplay = allIncidents.slice(start, end);

    // Iterate through paginated incidents and build table rows
    incidentsToDisplay.forEach((incident, index) => {
        const isResolved = incident.status === "Resolved";
        const disabledAttribute = isResolved ? "disabled" : "";
        const rowIndex = start + index + 1;

        // Format date for display (e.g., March 21st, 14:30)
        const createdAtFormatted = incident.createdAt
            ? formatCustomDate(incident.createdAt)
            : "Unknown";

        // === Summary Row (collapsed view) ===
        const row = document.createElement("tr");
        row.innerHTML = `
            <td>${incident.incidentNumber}</td>
            <td>${incident.title}</td>
            <td>${incident.description.split(" ").slice(0, 10).join(" ")}...</td>
            <td><strong>${incident.status}</strong></td>
            <td>${createdAtFormatted}</td> <!-- ✅ New Column -->
            <td class="text-end">
                <button class="btn btn-secondary btn-sm incident-expand-btn" onclick="toggleDetails(${rowIndex})">▼</button>
            </td>
        `;

        tableBody.appendChild(row);

        // === Detail Row (expandable view) ===
        const detailRow = document.createElement("tr");
        detailRow.id = `details-${rowIndex}`;
        detailRow.style.display = "none";
        detailRow.innerHTML = `
        <td colspan="5">
            <div class="incident-details">
                <!-- Basic Metadata -->
                <p><strong>Description:</strong> ${incident.description}</p>
                <p><strong>Submitted:</strong> ${createdAtFormatted}</p>
                <p><strong>Submitted By:</strong> ${incident.createdBy}</p>

                <!-- Severity Dropdown -->
                <label><strong>Severity:</strong></label>
                <select class="form-select w-50 d-inline-block" ${disabledAttribute} 
                        onchange="updateSeverity(${incident.id}, this.value)">
                    <option value="Low" ${incident.severityLevel === "Low" ? "selected" : ""}>Low</option>
                    <option value="Medium" ${incident.severityLevel === "Medium" ? "selected" : ""}>Medium</option>
                    <option value="High" ${incident.severityLevel === "High" ? "selected" : ""}>High</option>
                </select>

                <!-- Status Dropdown -->
                <label><strong>Status:</strong></label>
                <select class="form-select w-50 d-inline-block" ${disabledAttribute} 
                        onchange="updateStatus(${incident.id}, this.value)">
                    <option value="Open" ${incident.status === "Open" ? "selected" : ""}>Open</option>
                    <option value="Ongoing" ${incident.status === "Ongoing" ? "selected" : ""}>Ongoing</option>
                </select>

                <!-- Assigned Team -->
                <label><strong>Assigned Team:</strong></label>
                <select class="form-select w-50 d-inline-block" ${disabledAttribute} 
                        onchange="updateAssignedTeam(${incident.id}, this.value)">
                    <option value="Unassigned">Unassigned</option>
                    <option value="1" ${incident.assignedTeam?.id === 1 ? "selected" : ""}>IT</option>
                    <option value="3" ${incident.assignedTeam?.id === 3 ? "selected" : ""}>HR</option>
                    <option value="4" ${incident.assignedTeam?.id === 4 ? "selected" : ""}>Support</option>
                </select>

                <!-- Assigned Admin -->
                <label><strong>Assigned Admin:</strong></label>
                <select id="admin-select-${incident.id}" class="form-select w-50 d-inline-block" ${disabledAttribute} 
                        onchange="updateAssignedAdmin(${incident.id}, this.value)">
                    <option value="Unassigned">Unassigned</option>
                </select>

                <!-- Admin Comments Section -->
                <div class="comment-section mt-3">
                    <h6>Admin Notes</h6>
                    <div id="comments-container-${incident.id}" class="bg-light p-2 mb-2 rounded" style="max-height: 200px; overflow-y: auto;"></div>
                    <div class="input-group mb-3">
                        <input type="text" id="comment-input-${incident.id}" class="form-control" placeholder="Add a comment..." ${disabledAttribute}>
                        <button class="btn btn-outline-primary" type="button" onclick="submitComment(${incident.id})" ${disabledAttribute}>Post</button>
                    </div>
                </div>

                <br><br>

                <!-- Resolution Button or Details -->
                ${
            incident.status === "Resolved" && incident.resolution
                ? `<button class="btn btn-info btn-sm w-50 mt-2"
                               onclick="openResolutionDetailsModal('${incident.resolution.replace(/'/g, "\\'")}')">
                               See Resolution Details
                           </button>`
                : `<button class="btn btn-success btn-sm w-50 mt-2"
                               onclick="openResolveModal(${incident.id}); 
                                        const modal = new bootstrap.Modal(document.getElementById('resolveModal'));
                                        modal.show();">
                               Resolve
                           </button>`
        }

                <!-- Delete Button -->
                <button class="btn btn-danger btn-sm w-50 mt-2" ${isResolved ? "disabled" : ""}
                        onclick="confirmDeleteIncident(${incident.id})">
                    Delete
                </button>

                <!-- Collapse Button -->
                <button class="btn btn-secondary btn-sm w-50 mt-2" onclick="toggleDetails(${rowIndex})">▲ Close</button>
            </div>
        </td>
        `;
        tableBody.appendChild(detailRow);

        // Load comments for this incident
        fetchComments(incident.id);

        // Load available admins if team is already assigned
        if (incident.assignedTeam) {
            loadAdminsForTeam(incident.id, incident.assignedTeam.id, isResolved, incident.assignedAdmin?.id);
        }
    });
}
/**
 * Formats a date string into "Month Day<ordinal>, HH:MM" format.
 * Example: "March 21st, 14:30"
 */
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

/**
 * Updates pagination controls: Prev, page numbers, and Next
 */
function updatePagination() {
    const paginationContainer = document.getElementById("pagination-controls");
    paginationContainer.innerHTML = "";

    const totalPages = Math.ceil(allIncidents.length / incidentsPerPage);

    // Prev Button
    let prevButton = document.createElement("button");
    prevButton.className = "pagination-btn";
    prevButton.innerText = "« Prev";
    prevButton.disabled = currentPage === 1;
    prevButton.onclick = () => changePage(-1);
    paginationContainer.appendChild(prevButton);

    // Page Number Buttons
    for (let i = 1; i <= totalPages; i++) {
        const pageButton = document.createElement("button");
        pageButton.className = `pagination-btn ${i === currentPage ? "active" : ""}`;
        pageButton.innerText = i;
        pageButton.onclick = () => {
            currentPage = i;
            displayIncidents();
            updatePagination();
        };
        paginationContainer.appendChild(pageButton);
    }

    // Next Button
    let nextButton = document.createElement("button");
    nextButton.className = "pagination-btn";
    nextButton.innerText = "Next »";
    nextButton.disabled = currentPage === totalPages;
    nextButton.onclick = () => changePage(1);
    paginationContainer.appendChild(nextButton);
}

/**
 * Moves between pages based on direction (-1 for prev, +1 for next)
 */
function changePage(direction) {
    const totalPages = Math.ceil(allIncidents.length / incidentsPerPage);
    currentPage = Math.min(Math.max(1, currentPage + direction), totalPages);
    displayIncidents();
    updatePagination();
}

/**
 * Displays the resolution text in a modal dialog
 */
function openResolutionDetailsModal(resolutionText) {
    document.getElementById("resolutionDetailsText").textContent = resolutionText;
    const resolutionModal = new bootstrap.Modal(document.getElementById('resolutionDetailsModal'));
    resolutionModal.show();
}

/**
 * Toggles the visibility of a row with incident details
 */
function toggleDetails(index) {
    const detailRow = document.getElementById(`details-${index}`);
    detailRow.style.display = detailRow.style.display === "none" ? "table-row" : "none";
}

/**
 * Prepares the modal for resolving an incident by injecting the ID
 */
function openResolveModal(incidentId) {
    document.getElementById("resolveIncidentId").value = incidentId;
    document.getElementById("resolutionText").value = "";
}

/**
 * Sends the resolution text to the backend and marks the incident as resolved
 */
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
            if (!response.ok) throw new Error("Failed to resolve incident");
            return response.text();
        })
        .then(message => {
            alert(message);
            const modal = bootstrap.Modal.getInstance(document.getElementById('resolveModal'));
            modal.hide();
            getAllIncidentsForAdmin(); // Refresh incident list
        })
        .catch(error => {
            console.error(error);
            alert("Error resolving incident. Check console for details.");
        });
}

/**
 * Updates the assigned team of an incident.
 * Also resets the admin selection since teams and admins are linked.
 */
function updateAssignedTeam(incidentId, teamId) {
    const assignedAdminDropdown = document.getElementById(`admin-select-${incidentId}`);

    fetch(`${adminApiUrl}/${incidentId}/assign`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
            assignedTeam: teamId !== "Unassigned" ? teamId : null,
            assignedAdmin: null
        })
    })
        .then(() => {
            // Clear and disable admin dropdown
            assignedAdminDropdown.innerHTML = '<option value="Unassigned">Unassigned</option>';
            assignedAdminDropdown.disabled = true;

            // If a valid team is selected, reload its admins
            if (teamId !== "Unassigned") {
                loadAdminsForTeam(incidentId, teamId);
            }
        })
        .catch(error => {
            console.error("Error updating assigned team:", error);
            alert("Error updating team. Please try again.");
        });
}

/**
 * Loads admins for a given team and populates the dropdown.
 * Optionally preselects an already assigned admin.
 */
function loadAdminsForTeam(incidentId, teamId, isResolved, assignedAdminId) {
    const adminDropdown = document.getElementById(`admin-select-${incidentId}`);
    adminDropdown.innerHTML = '<option value="Unassigned">Unassigned</option>';

    fetch(`${adminByTeamApiUrl}/${teamId}`)
        .then(response => response.json())
        .then(admins => {
            console.log("Admins fetched for team", teamId, ":", admins);
            admins.forEach(admin => {
                const option = document.createElement("option");
                option.value = admin.id;
                option.textContent = admin.username;
                if (admin.id === assignedAdminId) option.selected = true;
                adminDropdown.appendChild(option);
            });

            adminDropdown.disabled = isResolved;
        })
        .catch(error => {
            console.error("Error loading admins:", error);
            adminDropdown.disabled = true;
        });
}

/**
 * Updates the assigned admin of an incident
 */
function updateAssignedAdmin(incidentId, adminId) {
    fetch(`${adminApiUrl}/${incidentId}/assign`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
            assignedAdmin: adminId !== "Unassigned" ? adminId : null
        })
    })
        .catch(error => {
            console.error("Error updating assigned admin:", error);
            alert("Error updating admin. Please try again.");
        });
}

// Update the severity level of an incident
function updateSeverity(id, severityLevel) {
    fetch(`http://localhost:8080/api/incidents/${id}/severity`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ severityLevel })
    })
        .then(response => response.ok ? console.log("Severity updated!") : console.error("Error updating severity"))
        .catch(error => console.error(error));
}

// Update the status of an incident (does not allow resolving/closing here)
function updateStatus(id, status) {
    fetch(`http://localhost:8080/api/incidents/${id}/status`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ status })
    })
        .then(response => response.ok ? console.log("Status updated!") : console.error("Error updating status"))
        .catch(error => console.error(error));
}

// Function to confirm before deleting an incident
function confirmDeleteIncident(id) {
    // Set the ID in the modal's hidden input
    document.getElementById("confirmDeleteIncidentId").value = id;

    // Show the modal
    const deleteModal = new bootstrap.Modal(document.getElementById("deleteConfirmModal"));
    deleteModal.show();
}

function deleteIncident() {
    const incidentId = document.getElementById("confirmDeleteIncidentId").value;

    fetch(`${adminApiUrl}/${incidentId}`, {
        method: "DELETE",
        credentials: "same-origin"
    })
        .then(response => {
            if (response.ok) {
                // Hide the confirmation modal
                const deleteConfirmModal = bootstrap.Modal.getInstance(document.getElementById("deleteConfirmModal"));
                if (deleteConfirmModal) {
                    deleteConfirmModal.hide();
                }

                // Show the success modal
                const successModal = new bootstrap.Modal(document.getElementById("deleteSuccessModal"));
                successModal.show();

                // Refresh the incidents
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

    let url = "http://localhost:8080/api/incidents?";

    if (selectedStatus && selectedStatus !== "All") {
        url += `status=${encodeURIComponent(selectedStatus)}&`;
    }
    if (selectedTeam && selectedTeam !== "All") {
        url += `teamId=${encodeURIComponent(selectedTeam)}&`;
    }

    url = url.replace(/&$/, "");

    fetch(url, { credentials: "same-origin" })
        .then(response => response.json())
        .then(data => {
            allIncidents = data;
            currentPage = 1;
            isFiltered = true;
            displayIncidents();
            updatePagination();
        })
        .catch(error => {
            console.error("Error fetching filtered incidents:", error);
            alert("Error loading filtered incidents. Please try again.");
        });
}

// Function to search an incident by its number or keyword
function searchIncidentByNumber() {
    // Get the search input value and trim any leading/trailing whitespace
    const searchInput = document.getElementById("incidentSearchInput").value.trim();
    if (!searchInput) {
        alert("Please enter an incident number or keyword!");
        return;
    }

    // Determine whether to search by incident number or keyword
    const queryParam = searchInput.toUpperCase().startsWith("INC")
        ? `incidentNumber=${encodeURIComponent(searchInput)}`
        : `keyword=${encodeURIComponent(searchInput)}`;

    // Construct the URL for the search API
    let url = `http://localhost:8080/api/incidents?${queryParam}`;

    // Fetch matching incidents from the backend
    fetch(url, { credentials: "same-origin" })
        .then(response => response.json())
        .then(data => {
            const tableBody = document.querySelector("#admin-incidents-table");
            tableBody.innerHTML = "";  // Clear the table before showing results

            if (data.length === 0) {
                alert("No incidents found!");
                return;
            }

            // Loop through each matching incident
            data.forEach((incident, index) => {
                const isResolved = incident.status === "Resolved";
                const disabledAttribute = isResolved ? "disabled" : "";

                // Format createdAt date
                const createdAtFormatted = incident.createdAt
                    ? new Date(incident.createdAt).toLocaleString("en-US", {
                        month: "long",
                        day: "numeric",
                        hour: "2-digit",
                        minute: "2-digit",
                        hour12: false
                    })
                    : "Unknown";

                // Summary row
                const row = document.createElement("tr");
                row.innerHTML = `
                    <td>${incident.incidentNumber}</td>
                    <td>${incident.title}</td>
                    <td>${incident.description.split(" ").slice(0, 10).join(" ")}...</td>
                    <td><strong>${incident.status}</strong></td>
                    <td>${createdAtFormatted}</td>
                    <td class="text-end">
                        <button class="btn btn-secondary btn-sm incident-expand-btn" onclick="toggleDetails(${index})">▼</button>
                    </td>
                `;
                tableBody.appendChild(row);

                // Detail row
                const detailRow = document.createElement("tr");
                detailRow.id = `details-${index}`;
                detailRow.style.display = "none";
                detailRow.innerHTML = `
                    <td colspan="6">
                        <div class="incident-details">
                            <p><strong>Description:</strong> ${incident.description}</p>
                            <p><strong>Submitted:</strong> ${createdAtFormatted}</p>
                            <p><strong>Submitted By:</strong> ${incident.createdBy}</p>

                            <label><strong>Severity:</strong></label>
                            <select class="form-select w-50 d-inline-block" ${disabledAttribute}
                                    onchange="updateSeverity(${incident.id}, this.value)">
                                <option value="Low" ${incident.severityLevel === "Low" ? "selected" : ""}>Low</option>
                                <option value="Medium" ${incident.severityLevel === "Medium" ? "selected" : ""}>Medium</option>
                                <option value="High" ${incident.severityLevel === "High" ? "selected" : ""}>High</option>
                            </select>

                            <label><strong>Status:</strong></label>
                            <select class="form-select w-50 d-inline-block" ${disabledAttribute}
                                    onchange="updateStatus(${incident.id}, this.value)">
                                <option value="Open" ${incident.status === "Open" ? "selected" : ""}>Open</option>
                                <option value="Ongoing" ${incident.status === "Ongoing" ? "selected" : ""}>Ongoing</option>
                            </select>

                            <label><strong>Assigned Team:</strong></label>
                            <select class="form-select w-50 d-inline-block" ${disabledAttribute}
                                    onchange="updateAssignedTeam(${incident.id}, this.value)">
                                <option value="Unassigned">Unassigned</option>
                                <option value="1" ${incident.assignedTeam?.id === 1 ? "selected" : ""}>IT</option>
                                <option value="3" ${incident.assignedTeam?.id === 3 ? "selected" : ""}>HR</option>
                                <option value="4" ${incident.assignedTeam?.id === 4 ? "selected" : ""}>Support</option>
                            </select>

                            <label><strong>Assigned Admin:</strong></label>
                            <select id="admin-select-${incident.id}" class="form-select w-50 d-inline-block" ${disabledAttribute}
                                    onchange="updateAssignedAdmin(${incident.id}, this.value)">
                                <option value="Unassigned">Unassigned</option>
                            </select>

                            <div class="comment-section mt-3">
                                <h6>Admin Notes</h6>
                                <div id="comments-container-${incident.id}" class="bg-light p-2 mb-2 rounded" style="max-height: 200px; overflow-y: auto;"></div>
                                <div class="input-group mb-3">
                                    <input type="text" id="comment-input-${incident.id}" class="form-control" placeholder="Add a comment..." ${disabledAttribute}>
                                    <button class="btn btn-outline-primary" type="button" onclick="submitComment(${incident.id})" ${disabledAttribute}>Post</button>
                                </div>
                            </div>

                            <br><br>

                            ${incident.status === "Resolved" && incident.resolution
                    ? `<button class="btn btn-info btn-sm w-50 mt-2"
                                           onclick="openResolutionDetailsModal('${incident.resolution.replace(/'/g, "\\'")}')">
                                           See Resolution Details
                                       </button>`
                    : `<button class="btn btn-success btn-sm w-50 mt-2"
                                           onclick="openResolveModal(${incident.id});
                                                    const modal = new bootstrap.Modal(document.getElementById('resolveModal'));
                                                    modal.show();">
                                           Resolve
                                       </button>`}

                            <button class="btn btn-danger btn-sm w-50 mt-2" ${isResolved ? "disabled" : ""}
                                    onclick="confirmDeleteIncident(${incident.id})">
                                Delete
                            </button>

                            <button class="btn btn-secondary btn-sm w-50 mt-2" onclick="toggleDetails(${index})">▲ Close</button>
                        </div>
                    </td>
                `;
                tableBody.appendChild(detailRow);

                // Load comments
                fetchComments(incident.id);

                // Load admin options if a team is already assigned
                if (incident.assignedTeam) {
                    loadAdminsForTeam(incident.id, incident.assignedTeam.id, isResolved, incident.assignedAdmin?.id);
                }
            });
        })
        .catch(error => {
            console.error("Error searching incident by number:", error);
            alert("Error searching incident. Please check console.");
        });
}


// Function to fetch and display all comments for a given incident
function fetchComments(incidentId) {
    // Send a GET request to retrieve comments for the specified incident
    fetch(`http://localhost:8080/api/incidents/${incidentId}/comments`)
        .then(res => res.json())
        .then(comments => {
            // Get the container where comments will be displayed
            const container = document.getElementById(`comments-container-${incidentId}`);

            // Populate the container with each comment using formatted HTML
            container.innerHTML = comments.map(c => `
                <div class="mb-2">
                    <strong>${c.createdBy}</strong> 
                    <small class="text-muted">[${formatCustomDate(c.createdAt)}]</small>
                    <p class="mb-1">${c.content}</p>
                    <hr />
                </div>
            `).join("");
        });
}

// Function to submit a new comment for a given incident
function submitComment(incidentId) {
    // Get the input field and extract the trimmed comment text
    const input = document.getElementById(`comment-input-${incidentId}`);
    const content = input.value.trim();

    // Validate input to ensure it's not empty
    if (!content) {
        alert("Please enter a comment!");
        return;
    }

    // Send a POST request to submit the comment to the backend
    fetch(`http://localhost:8080/api/incidents/${incidentId}/comments`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        credentials: "same-origin",  // Ensures cookies/session are sent with request
        body: JSON.stringify({ content })
    })
        .then(response => {
            if (!response.ok) throw new Error("Failed to post comment");

            // Clear the input field upon successful post
            input.value = "";

            // Refresh the comments section to include the new comment
            fetchComments(incidentId);
        })
        .catch(error => {
            console.error("Error posting comment:", error);
            alert("Failed to add comment.");
        });
}

// Logout Functionality
document.getElementById("logoutButton").addEventListener("click", function () {
    fetch("/perform_logout", { method: "POST", credentials: "same-origin" })
        .then(() => { window.location.href = "/login"; })
        .catch(error => console.error("Error logging out:", error));
});

// On page load
document.addEventListener("DOMContentLoaded", () => {
    getAllIncidentsForAdmin();
});
