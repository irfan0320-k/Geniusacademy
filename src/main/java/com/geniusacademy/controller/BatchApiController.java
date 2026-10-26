
package com.geniusacademy.controller;

import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.sql.Date;

@RestController
@RequestMapping("/api/batches")
public class BatchApiController {

    private final DataSource dataSource;

    public BatchApiController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // GET /api/batches/options
    // Load teachers and students who are not assigned to a batch.
    @GetMapping("/options")
    public Map<String, Object> getOptions() {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> teachers = new ArrayList<>();
        List<Map<String, Object>> students = new ArrayList<>();

        String teacherSql =
            "SELECT t.teacher_id, t.teacher_name, t.subject " +
            "FROM teachers t " +
            "LEFT JOIN batches b ON b.teacher_id = t.teacher_id " +
            "WHERE b.batch_id IS NULL " +
            "ORDER BY t.teacher_name";

        String studentSql =
            "SELECT s.student_id, s.student_name, s.standard, " +
            "s.phone, s.division " +
            "FROM students s " +
            "LEFT JOIN batch_students bs ON bs.student_id = s.student_id " +
            "WHERE bs.student_id IS NULL " +
            "AND (s.status IS NULL OR s.status = 'Active') " +
            "ORDER BY s.student_name";

        try (Connection con = dataSource.getConnection()) {

            try (PreparedStatement ps = con.prepareStatement(teacherSql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> teacher = new HashMap<>();
                    teacher.put("id", rs.getInt("teacher_id"));
                    teacher.put("name", rs.getString("teacher_name"));
                    teacher.put("subject", rs.getString("subject"));
                    teachers.add(teacher);
                }
            }

            try (PreparedStatement ps = con.prepareStatement(studentSql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> student = new HashMap<>();
                    student.put("id", rs.getInt("student_id"));
                    student.put("name", rs.getString("student_name"));
                    student.put("standard", rs.getString("standard"));
                    student.put("phone", rs.getString("phone"));
                    student.put("division", rs.getString("division"));
                    students.add(student);
                }
            }

            result.put("success", true);
            result.put("teachers", teachers);
            result.put("students", students);

        } catch (SQLException e) {
            result.put("success", false);
            result.put("message", "Could not load options: " + e.getMessage());
        }

        return result;
    }

    // GET /api/batches
    @GetMapping
    public List<Map<String, Object>> getBatches() {
        List<Map<String, Object>> batches = new ArrayList<>();

        String sql =
            "SELECT b.batch_id, b.batch_name, b.standard, b.subject, " +
            "b.teacher_id, b.batch_timing, b.created_at, t.teacher_name, " +
            "(SELECT COUNT(*) FROM batch_students bs " +
            " WHERE bs.batch_id = b.batch_id) AS student_count " +
            "FROM batches b JOIN teachers t ON t.teacher_id = b.teacher_id " +
            "ORDER BY b.batch_id DESC";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> batch = new HashMap<>();
                batch.put("id", rs.getInt("batch_id"));
                batch.put("name", rs.getString("batch_name"));
                batch.put("standard", rs.getString("standard"));
                batch.put("subject", rs.getString("subject"));
                batch.put("teacherId", rs.getInt("teacher_id"));
                batch.put("teacherName", rs.getString("teacher_name"));
                batch.put("timing", rs.getString("batch_timing"));
                batch.put("studentCount", rs.getInt("student_count"));
                batch.put("createdAt", rs.getTimestamp("created_at"));
                batches.add(batch);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Could not load batches", e);
        }

        return batches;
    }

    // POST /api/batches
    //
    // Example request:
    // {
    //   "name": "Class 8 Maths",
    //   "standard": "8",
    //   "subject": "Mathematics",
    //   "teacherId": 1,
    //   "timing": "5 PM - 6 PM",
    //   "studentIds": [1, 2],
    //   "newStudents": [
    //     {"name":"New Student","standard":"8","phone":"9876543210"}
    //   ]
    // }
    @PostMapping
    public Map<String, Object> createBatch(
            @RequestBody Map<String, Object> data) {

        Map<String, Object> response = new HashMap<>();

        String name = value(data.get("name"));
        String standard = value(data.get("standard"));
        String subject = value(data.get("subject"));
        String timing = value(data.get("timing"));

        int teacherId = number(data.get("teacherId"));

        if (name == null || standard == null ||
            subject == null || teacherId <= 0) {
            response.put("success", false);
            response.put("message",
                "Batch name, class, subject and teacher are required");
            return response;
        }

        List<?> studentIds =
            data.get("studentIds") instanceof List<?>
                ? (List<?>) data.get("studentIds")
                : Collections.emptyList();

        List<?> newStudents =
            data.get("newStudents") instanceof List<?>
                ? (List<?>) data.get("newStudents")
                : Collections.emptyList();

        try (Connection con = dataSource.getConnection()) {
            con.setAutoCommit(false);

            try {
                // Check teacher exists and has no batch.
                String teacherSql =
                    "SELECT teacher_id FROM teachers WHERE teacher_id = ?";

                try (PreparedStatement ps =
                         con.prepareStatement(teacherSql)) {
                    ps.setInt(1, teacherId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException(
                                "Selected teacher does not exist");
                        }
                    }
                }

                String insertBatch =
                    "INSERT INTO batches " +
                    "(batch_name, standard, subject, teacher_id, batch_timing) " +
                    "VALUES (?, ?, ?, ?, ?)";

                int batchId;

                try (PreparedStatement ps = con.prepareStatement(
                        insertBatch, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, name);
                    ps.setString(2, standard);
                    ps.setString(3, subject);
                    ps.setInt(4, teacherId);
                    ps.setString(5, timing);
                    ps.executeUpdate();

                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException(
                                "Could not get new batch ID");
                        }
                        batchId = keys.getInt(1);
                    }
                }

                // Assign existing students.
                String assignSql =
                    "INSERT INTO batch_students (batch_id, student_id) " +
                    "VALUES (?, ?)";

                try (PreparedStatement ps =
                         con.prepareStatement(assignSql)) {
                    for (Object item : studentIds) {
                        int studentId = number(item);
                        if (studentId <= 0) {
                            throw new IllegalArgumentException(
                                "Invalid existing student ID");
                        }

                        ps.setInt(1, batchId);
                        ps.setInt(2, studentId);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                // Register new students and assign them to this batch.
                String newStudentSql =
                    "INSERT INTO students " +
                    "(student_name, standard, phone, parent_name, " +
                    "admission_date, status) " +
                    "VALUES (?, ?, ?, ?, ?, 'Active')";

                for (Object item : newStudents) {
                    if (!(item instanceof Map<?, ?>)) {
                        throw new IllegalArgumentException(
                            "Invalid new student details");
                    }

                    Map<?, ?> student = (Map<?, ?>) item;
                    String studentName = value(student.get("name"));

                    if (studentName == null) {
                        throw new IllegalArgumentException(
                            "Every new student must have a name");
                    }

                    int studentId;

                    try (PreparedStatement ps = con.prepareStatement(
                            newStudentSql, Statement.RETURN_GENERATED_KEYS)) {
                        ps.setString(1, studentName);
                        ps.setString(2,
                            value(student.get("standard")) != null
                                ? value(student.get("standard")) : standard);
                        ps.setString(3, value(student.get("phone")));
                        ps.setString(4, value(student.get("parentName")));

                        String admissionDate =
                            value(student.get("admissionDate"));

                        if (admissionDate == null) {
                            ps.setNull(5, Types.DATE);
                        } else {
                            ps.setDate(5, Date.valueOf(admissionDate));
                        }

                        ps.executeUpdate();

                        try (ResultSet keys = ps.getGeneratedKeys()) {
                            if (!keys.next()) {
                                throw new SQLException(
                                    "Could not get new student ID");
                            }
                            studentId = keys.getInt(1);
                        }
                    }

                    try (PreparedStatement ps =
                             con.prepareStatement(assignSql)) {
                        ps.setInt(1, batchId);
                        ps.setInt(2, studentId);
                        ps.executeUpdate();
                    }
                }

                con.commit();

                response.put("success", true);
                response.put("batchId", batchId);
                response.put("message", "Batch created successfully");

            } catch (Exception e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }

        } catch (Exception e) {
            response.put("success", false);

            if (e instanceof IllegalArgumentException) {
                response.put("message", e.getMessage());
            } else if (e instanceof SQLException &&
                       ((SQLException) e).getErrorCode() == 1062) {
                response.put("message",
                    "This teacher already has a batch, or a student is already assigned to a batch.");
            } else {
                response.put("message", "Batch creation failed: " +
                    e.getMessage());
            }
        }

        return response;
    }

    // GET /api/batches/{id}/students
    @GetMapping("/{id}/students")
    public List<Map<String, Object>> getBatchStudents(
            @PathVariable int id) {

        List<Map<String, Object>> students = new ArrayList<>();

        String sql =
            "SELECT s.student_id, s.student_name, s.standard, s.division, " +
            "s.phone, s.parent_name, s.parent_phone " +
            "FROM batch_students bs " +
            "JOIN students s ON s.student_id = bs.student_id " +
            "WHERE bs.batch_id = ? ORDER BY s.student_name";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> student = new HashMap<>();
                    student.put("id", rs.getInt("student_id"));
                    student.put("name", rs.getString("student_name"));
                    student.put("standard", rs.getString("standard"));
                    student.put("division", rs.getString("division"));
                    student.put("phone", rs.getString("phone"));
                    student.put("parentName", rs.getString("parent_name"));
                    student.put("parentPhone", rs.getString("parent_phone"));
                    students.add(student);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Could not load batch students", e);
        }

        return students;
    }

    private String value(Object value) {
        if (value == null) {
            return null;
        }

        String text = value.toString().trim();
        return text.isEmpty() ? null : text;
    }

    private int number(Object value) {
        if (value == null) {
            return 0;
        }

        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
