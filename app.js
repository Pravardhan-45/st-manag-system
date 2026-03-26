document.addEventListener('DOMContentLoaded', () => {
    fetchStudents();
});

const studentTableBody = document.getElementById('student-table-body');
const totalStudentsEl = document.getElementById('total-students');
const avgMarksEl = document.getElementById('avg-marks');
const studentModal = document.getElementById('student-modal');
const studentForm = document.getElementById('student-form');

async function fetchStudents() {
    try {
        const res = await fetch('/api/students');
        const students = await res.json();
        renderTable(students);
        updateStats(students);
    } catch (err) {
        console.error('Failed to fetch students:', err);
    }
}

function renderTable(students) {
    studentTableBody.innerHTML = '';
    students.forEach(s => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>#${s.id}</td>
            <td>${s.name}</td>
            <td>${s.age}</td>
            <td>${s.course}</td>
            <td><span class="badge">${s.marks}%</span></td>
            <td>
                <button class="btn-icon" onclick="editStudent(${s.id}, '${s.name}', ${s.age}, '${s.course}', ${s.marks})">✏️</button>
                <button class="btn-icon" onclick="deleteStudent(${s.id})">🗑️</button>
            </td>
        `;
        studentTableBody.appendChild(tr);
    });
}

function updateStats(students) {
    totalStudentsEl.innerText = students.length;
    if (students.length > 0) {
        const avg = students.reduce((acc, s) => acc + s.marks, 0) / students.length;
        avgMarksEl.innerText = Math.round(avg) + '%';
    } else {
        avgMarksEl.innerText = '0%';
    }
}

function openModal(edit = false) {
    document.getElementById('modal-title').innerText = edit ? 'Edit Student' : 'Add Student';
    if (!edit) {
        studentForm.reset();
        document.getElementById('student-id').value = '';
    }
    studentModal.style.display = 'flex';
}

function closeModal() {
    studentModal.style.display = 'none';
}

function editStudent(id, name, age, course, marks) {
    document.getElementById('student-id').value = id;
    document.getElementById('name').value = name;
    document.getElementById('age').value = age;
    document.getElementById('course').value = course;
    document.getElementById('marks').value = marks;
    openModal(true);
}

studentForm.onsubmit = async (e) => {
    e.preventDefault();
    const id = document.getElementById('student-id').value;
    const data = {
        name: document.getElementById('name').value,
        age: document.getElementById('age').value,
        course: document.getElementById('course').value,
        marks: document.getElementById('marks').value
    };

    const method = id ? 'PUT' : 'POST';
    if (id) data.id = id;

    try {
        const res = await fetch('/api/students', {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        if (res.ok) {
            closeModal();
            fetchStudents();
        }
    } catch (err) {
        alert('Action failed!');
    }
};

async function deleteStudent(id) {
    if (!confirm('Are you sure you want to delete this student?')) return;
    try {
        const res = await fetch(`/api/students?id=${id}`, { method: 'DELETE' });
        if (res.ok) fetchStudents();
    } catch (err) {
        alert('Delete failed!');
    }
}
