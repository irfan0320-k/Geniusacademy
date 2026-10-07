const STORAGE_KEY = "geniusAcademyData";

function defaultData() {
    return {
        students: [
            {
                id: 1,
                name: "Aarav Sharma",
                email: "aarav@email.com",
                phone: "9876543210",
                className: "Class 10",
                parentName: "Rakesh Sharma",
                joinDate: "2026-04-12"
            },
            {
                id: 2,
                name: "Diya Patel",
                email: "diya@email.com",
                phone: "9876501234",
                className: "Class 9",
                parentName: "Meera Patel",
                joinDate: "2026-05-03"
            },
            {
                id: 3,
                name: "Rohan Khan",
                email: "rohan@email.com",
                phone: "9123456780",
                className: "Class 12",
                parentName: "Imran Khan",
                joinDate: "2026-06-18"
            }
        ],

        teachers: [
            {
                id: 1,
                name: "Priya Nair",
                email: "priya@geniusacademy.com",
                phone: "9000011111",
                subject: "Mathematics",
                joinDate: "2024-06-01"
            },
            {
                id: 2,
                name: "Arjun Mehta",
                email: "arjun@geniusacademy.com",
                phone: "9000022222",
                subject: "Physics",
                joinDate: "2025-01-10"
            }
        ],

        attendance: [
            {
                id: 1,
                studentId: 1,
                date: today(),
                status: "Present"
            },
            {
                id: 2,
                studentId: 2,
                date: today(),
                status: "Present"
            },
            {
                id: 3,
                studentId: 3,
                date: today(),
                status: "Absent"
            }
        ],

        fees: [
            {
                id: 1,
                studentId: 1,
                paidAmount: 4000,
                pendingAmount: 1000,
                paymentDate: "2026-09-01"
            },
            {
                id: 2,
                studentId: 2,
                paidAmount: 5000,
                pendingAmount: 0,
                paymentDate: "2026-09-10"
            },
            {
                id: 3,
                studentId: 3,
                paidAmount: 2000,
                pendingAmount: 3000,
                paymentDate: "2026-09-15"
            }
        ],

        performance: [
            {
                id: 1,
                studentId: 1,
                subject: "Mathematics",
                examName: "Unit Test 1",
                marks: 88
            },
            {
                id: 2,
                studentId: 2,
                subject: "Science",
                examName: "Mid Term",
                marks: 91
            },
            {
                id: 3,
                studentId: 3,
                subject: "English",
                examName: "Unit Test 1",
                marks: 76
            }
        ]
    };
}


/* =========================
   DATE FUNCTION
========================= */

function today() {
    return new Date().toISOString().slice(0, 10);
}


/* =========================
   LOAD DATA
========================= */

function loadData() {
    const raw = localStorage.getItem(STORAGE_KEY);

    if (!raw) {
        const data = defaultData();
        saveData(data);
        return data;
    }

    return JSON.parse(raw);
}


/* =========================
   SAVE DATA
========================= */

function saveData(data) {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(data));
}


/* =========================
   NEXT ID
========================= */

function nextId(list) {
    return list.length
        ? Math.max(...list.map((item) => item.id)) + 1
        : 1;
}


/* =========================
   GET STUDENT NAME
========================= */

function getStudentName(studentId) {
    const student = loadData()
        .students
        .find((item) => item.id === Number(studentId));

    return student ? student.name : "Unknown";
}


/* =========================
   LOGIN CHECK
========================= */

function requireLogin() {
    if (localStorage.getItem("geniusAcademyLoggedIn") !== "true") {
        window.location.href = "login.html";
    }
}


/* =========================
   LOGOUT
========================= */

function logout() {
    localStorage.removeItem("geniusAcademyLoggedIn");
    localStorage.removeItem("geniusAcademyUser");
    window.location.href = "login.html";
}
function logout() {

    localStorage.removeItem("geniusAcademyLoggedIn");

    localStorage.removeItem("geniusAcademyUser");

    window.location.href = "login.html";

}
function toggleAdminProfile() {

    const menu =
        document.getElementById("adminProfileMenu");

    if (!menu) {
        return;
    }

    menu.classList.toggle("show");

}


/* =========================
   QUERY PARAMETER
========================= */

function queryParam(name) {
    return new URLSearchParams(window.location.search).get(name);
}


/* =========================
   MAIN LAYOUT
========================= */

function renderLayout(activePage, pageTitle) {

    requireLogin();

    document.body.innerHTML = `
    
    <div class="app-shell">

        <!-- ================= SIDEBAR ================= -->

        <aside class="sidebar" id="sidebar">

            <!-- BRAND -->

            <div class="brand-box d-flex align-items-center gap-3">

                <div class="brand-mark">
    <img src="images/logo.png" alt="Genius Academy Logo">
</div>
                <div>

                    <p class="brand-title">
                        GENIUS ACADEMY
                    </p>

                    <div class="brand-sub">
                        Tuition Management
                    </div>

                </div>

            </div>


            <!-- ================= NAVIGATION ================= -->

            <nav class="mt-3">

                <!-- DASHBOARD -->

                <a
                    class="nav-link ${activePage === "dashboard" ? "active" : ""}"
                    href="dashboard.html"
                >
                    <i class="bi bi-grid-1x2"></i>
                    Dashboard
                </a>


                <!-- STUDENTS -->

                <a
                    class="nav-link ${activePage === "students" ? "active" : ""}"
                    href="students.html"
                >
                    <i class="bi bi-people"></i>
                    Students
                </a>


                <!-- TEACHERS -->

                <a
                    class="nav-link ${activePage === "teachers" ? "active" : ""}"
                    href="teachers.html"
                >
                    <i class="bi bi-person-badge"></i>
                    Teachers
                </a>


                <!-- ATTENDANCE -->

                <a
                    class="nav-link ${activePage === "attendance" ? "active" : ""}"
                    href="attendance.html"
                >
                    <i class="bi bi-calendar-check"></i>
                    Attendance
                </a>


                <!-- FEES -->

                <a
                    class="nav-link ${activePage === "fees" ? "active" : ""}"
                    href="fees.html"
                >
                    <i class="bi bi-cash-stack"></i>
                    Fees
                </a>


                <!-- PERFORMANCE -->

                <a
                    class="nav-link ${activePage === "performance" ? "active" : ""}"
                    href="performance.html"
                >
                    <i class="bi bi-graph-up"></i>
                    Performance
                </a>


                <!-- REPORTS -->

                <a
                    class="nav-link ${activePage === "reports" ? "active" : ""}"
                    href="reports.html"
                >
                    <i class="bi bi-file-earmark-bar-graph"></i>
                    Reports
                </a>

            </nav>

        </aside>


        <!-- ================= MAIN AREA ================= -->

        <div class="main-wrap">


            <!-- ================= TOP BAR ================= -->

            <header class="topbar d-flex align-items-center justify-content-between">

    <!-- LEFT SIDE -->

    <div class="d-flex align-items-center gap-3">

        <!-- MOBILE MENU -->

        <button
            class="btn btn-outline-secondary d-lg-none"
            id="menuBtn"
            type="button"
        >
            <i class="bi bi-list"></i>
        </button>


        <!-- PAGE TITLE -->

        <h1 class="page-title">
            ${pageTitle}
        </h1>

    </div>


    <!-- ================= ADMIN PROFILE ================= -->

    <div class="admin-profile-wrapper">

        <button
            type="button"
            class="admin-profile-btn"
            onclick="toggleAdminProfile()"
        >

            <span class="admin-avatar">
                AD
            </span>

            <span class="admin-profile-text">

                <strong>
                    ${localStorage.getItem("geniusAcademyUser") || "Admin"}
                </strong>

                <small>
                    Administrator
                </small>

            </span>

            <i class="bi bi-chevron-down admin-arrow"></i>

        </button>


        <!-- PROFILE DROPDOWN -->

        <div
            id="adminProfileMenu"
            class="admin-profile-menu"
        >

            <div class="profile-menu-header">

                <div class="profile-large-avatar">
                    AD
                </div>

                <div>

                    <strong>
                        ${localStorage.getItem("geniusAcademyUser") || "Admin"}
                    </strong>

                    <small>
                        Administrator
                    </small>

                </div>

            </div>


            <div class="profile-divider"></div>


            <div class="profile-info">

                <div class="profile-info-row">

                    <i class="bi bi-person"></i>

                    <div>

                        <small>Username</small>

                        <span>
                            ${localStorage.getItem("geniusAcademyUser") || "Admin"}
                        </span>

                    </div>

                </div>


                <div class="profile-info-row">

                    <i class="bi bi-shield-check"></i>

                    <div>

                        <small>Role</small>

                        <span>
                            Administrator
                        </span>

                    </div>

                </div>


                <div class="profile-info-row">

                    <i class="bi bi-circle-fill text-success"></i>

                    <div>

                        <small>Status</small>

                        <span>
                            Active
                        </span>

                    </div>

                </div>

            </div>


            <div class="profile-divider"></div>


            <!-- LOGOUT -->

            <button
                type="button"
                class="profile-logout-btn"
                onclick="logout()"
            >

                <i class="bi bi-box-arrow-right"></i>

                Logout

            </button>

        </div>

    </div>

</header>


            <!-- ================= PAGE CONTENT ================= -->

             <main 
    class="content" 
    id="pageContent"
></main>

<footer class="ga-footer">
    <div>
        Genius Academy • Tuition Management System
    </div>

    <div class="ga-developer">
        Developed by <strong>Irfan Hussain</strong>
    </div>
</footer>


        </div>

    </div>
    `;


    /* =========================
       MOBILE MENU BUTTON
    ========================= */

    document
        .getElementById("menuBtn")
        ?.addEventListener("click", () => {

            document
                .getElementById("sidebar")
                .classList.toggle("show");

        });
}


/* =========================
   STUDENT DROPDOWN OPTIONS
========================= */

function studentOptions(selectedId) {

    return loadData()
        .students
        .map((student) => {

            return `
                <option
                    value="${student.id}"
                    ${Number(selectedId) === student.id ? "selected" : ""}
                >
                    ${student.name}
                </option>
            `;

        })
        .join("");
}