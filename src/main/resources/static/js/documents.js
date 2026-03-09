var searchTimeout;

document.getElementById('userSearch')?.addEventListener('input', function(e) {
    clearTimeout(searchTimeout);
    const query = e.target.value.trim();

    if (query.length < 2) {
        document.getElementById('userAutocomplete').classList.remove('show');
        return;
    }

    searchTimeout = setTimeout(() => {
        fetch(`/api/corporation/users/search?query=${encodeURIComponent(query)}`)
            .then(r => r.json())
            .then(users => {
                const dropdown = document.getElementById('userAutocomplete');
                if (users.length === 0) {
                    dropdown.innerHTML = '<div class="autocomplete-item">No users found</div>';
                } else {
                    dropdown.innerHTML = users.map(u =>
                        `<div class="autocomplete-item" onclick="selectUser(${u.profileId}, '${u.firstName}', '${u.lastName}', '${u.email}')">
                                <strong>${u.firstName} ${u.lastName}</strong><br>
                                <small class="text-muted">${u.email}</small>
                            </div>`
                    ).join('');
                }
                dropdown.classList.add('show');
            });
    }, 300);
});

function selectUser(id, firstName, lastName, email) {
    document.getElementById('profileId').value = id;
    document.getElementById('selectedUserName').textContent = `${firstName} ${lastName} (${email})`;
    document.getElementById('selectedUser').classList.add('show');
    document.getElementById('userSearch').value = '';
    document.getElementById('userAutocomplete').classList.remove('show');
}

function clearUser() {
    document.getElementById('profileId').value = '';
    document.getElementById('selectedUser').classList.remove('show');
}

function scrollToPermissions() {
    document.getElementById('permissionsSection').scrollIntoView({ behavior: 'smooth', block: 'start' });
}

document.addEventListener('click', function(e) {
    if (!e.target.closest('#userSearch')) {
        document.getElementById('userAutocomplete')?.classList.remove('show');
    }
});

function enableEdit(elementId) {
    const element = document.getElementById(elementId);
    element.contentEditable = 'true';
    element.classList.add('editing');
    element.focus();
}

function saveDescription() {
    const element = document.getElementById('docDescription');
    element.contentEditable = 'false';
    element.classList.remove('editing');

    const docId = element.getAttribute('data-doc-id');
    const newDescription = element.textContent.trim();

    const csrfToken = document.querySelector('input[name="_csrf"]')?.value;

    if (!csrfToken) {
        console.error('CSRF token not found!');
        alert('Security token missing. Please refresh the page.');
        return;
    }

    fetch(`/documents/${docId}/update`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            'X-CSRF-TOKEN': csrfToken
        },
        body: `description=${encodeURIComponent(newDescription)}`
    })
        .then(response => {
            if (response.ok) {
                if (!newDescription) {
                    element.innerHTML = '<span class="description-placeholder">Click to add description...</span>';
                }
                console.log('Description updated successfully!');
            } else {
                console.error('Update failed:', response.status);
                alert('Failed to update description.');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('An error occurred.');
        });
}

function updateVersionFileName(input) {
    document.getElementById('versionFileName').textContent = input.files[0]?.name || 'Click to select a file';
}