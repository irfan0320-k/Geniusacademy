
package com.geniusacademy.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class AttendanceApiController {

    private final DataSource dataSource;

    public AttendanceApiController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // ==========================================
    // GET ALL ATTENDANCE
    // ==========================================

    @GetMapping("/api/attendance")
    public List<Map<String, Object>> getAllAttendance() {

        List<Map<String, Object>> list = new ArrayList<>();

        String sql =
                "SELECT attendance_id, student_id, " +
                "attendance_date, status " +
                "FROM attendance " +
                "ORDER BY attendance_date DESC, attendance_id DESC";

        try (
                Connection con = dataSource.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();

                row.put("id", rs.getInt("attendance_id"));
                row.put("studentId", rs.getInt("student_id"));
                row.put("date", rs.getDate("attendance_date"));
                row.put("status", rs.getString("status"));

                list.add(row);
            }

            return list;

        } catch (SQLException e) {
            e.printStackTrace();

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to load attendance. Check database connection and attendance table.",
                    e
            );
        }
    }

    // ==========================================
    // GET ONE ATTENDANCE
    // ==========================================

    @GetMapping("/api/attendance/{id}")
    public Map<String, Object> getAttendance(@PathVariable int id) {

        String sql =
                "SELECT attendance_id, student_id, " +
                "attendance_date, status " +
                "FROM attendance " +
                "WHERE attendance_id = ?";

        try (
                Connection con = dataSource.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> row = new HashMap<>();

                    row.put("id", rs.getInt("attendance_id"));
                    row.put("studentId", rs.getInt("student_id"));
                    row.put("date", rs.getDate("attendance_date"));
                    row.put("status", rs.getString("status"));

                    return row;
                }
            }

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Attendance record not found: " + id
            );

        } catch (SQLException e) {
            e.printStackTrace();

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to load attendance record.",
                    e
            );
        }
    }

    // ==========================================
    // ADD ATTENDANCE
    // Expected JSON: studentId, date, status
    // ==========================================

    @PostMapping("/api/attendance")
    public Map<String, Object> addAttendance(
            @RequestBody Map<String, Object> data) {

        int studentId = readStudentId(data);
        java.sql.Date date = readDate(data);
        String status = readStatus(data);

        String sql =
                "INSERT INTO attendance " +
                "(student_id, attendance_date, status) " +
                "VALUES (?, ?, ?)";

        try (
                Connection con = dataSource.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setInt(1, studentId);
            ps.setDate(2, date);
            ps.setString(3, status);

            int rows = ps.executeUpdate();

            if (rows != 1) {
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Attendance was not inserted."
                );
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Attendance added successfully");

            return response;

        } catch (SQLException e) {
            e.printStackTrace();

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not save attendance. Check the database table and connection.",
                    e
            );
        }
    }

    // ==========================================
    // UPDATE ATTENDANCE
    // ==========================================

    @PutMapping("/api/attendance/{id}")
    public Map<String, Object> updateAttendance(
            @PathVariable int id,
            @RequestBody Map<String, Object> data) {

        int studentId = readStudentId(data);
        java.sql.Date date = readDate(data);
        String status = readStatus(data);

        String sql =
                "UPDATE attendance SET " +
                "student_id = ?, " +
                "attendance_date = ?, " +
                "status = ? " +
                "WHERE attendance_id = ?";

        try (
                Connection con = dataSource.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setInt(1, studentId);
            ps.setDate(2, date);
            ps.setString(3, status);
            ps.setInt(4, id);

            int rows = ps.executeUpdate();

            if (rows == 0) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Attendance record not found: " + id
                );
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Attendance updated successfully");

            return response;

        } catch (SQLException e) {
            e.printStackTrace();

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not update attendance.",
                    e
            );
        }
    }

    // ==========================================
    // DELETE ATTENDANCE
    // ==========================================

    @DeleteMapping("/api/attendance/{id}")
    public Map<String, Object> deleteAttendance(@PathVariable int id) {

        String sql =
                "DELETE FROM attendance WHERE attendance_id = ?";

        try (
                Connection con = dataSource.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setInt(1, id);

            int rows = ps.executeUpdate();

            if (rows == 0) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Attendance record not found: " + id
                );
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Attendance deleted successfully");

            return response;

        } catch (SQLException e) {
            e.printStackTrace();

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not delete attendance.",
                    e
            );
        }
    }

    // ==========================================
    // VALIDATE REQUEST DATA
    // ==========================================

    private int readStudentId(Map<String, Object> data) {

        Object value = data.get("studentId");

        if (value == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "studentId is required."
            );
        }

        try {
            int id = Integer.parseInt(value.toString());

            if (id <= 0) {
                throw new NumberFormatException();
            }

            return id;

        } catch (NumberFormatException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "studentId must be a positive integer."
            );
        }
    }

    private java.sql.Date readDate(Map<String, Object> data) {

        Object value = data.get("date");

        // Accept attendanceDate as an alternative field name too.
        if (value == null) {
            value = data.get("attendanceDate");
        }

        if (value == null || value.toString().trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "date is required in YYYY-MM-DD format."
            );
        }

        try {
            return java.sql.Date.valueOf(value.toString());

        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid date. Use YYYY-MM-DD format."
            );
        }
    }

    private String readStatus(Map<String, Object> data) {

        Object value = data.get("status");

        if (value == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "status is required."
            );
        }

        String status = value.toString().trim();

        if (!status.equalsIgnoreCase("Present")
                && !status.equalsIgnoreCase("Absent")
                && !status.equalsIgnoreCase("Late")) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Status must be Present, Absent, or Late."
            );
        }

        return status.substring(0, 1).toUpperCase()
                + status.substring(1).toLowerCase();
    }
    
    // ==========================================
    // BULK ATTENDANCE
    // POST /api/attendance/bulk
    // ==========================================

    @PostMapping("/api/attendance/bulk")
    public Map<String, Object> addBulkAttendance(
            @RequestBody Map<String, Object> data) {

        Object dateValue = data.get("date");
        Object attendanceValue = data.get("attendance");

        if (dateValue == null || attendanceValue == null
                || !(attendanceValue instanceof List<?>)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "date and attendance list are required."
            );
        }

        java.sql.Date attendanceDate;

        try {
            attendanceDate =
                    java.sql.Date.valueOf(dateValue.toString());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Date must be in YYYY-MM-DD format."
            );
        }

        List<?> records = (List<?>) attendanceValue;
        int inserted = 0;
        int skipped = 0;

        String checkSql =
                "SELECT attendance_id FROM attendance " +
                "WHERE student_id = ? AND attendance_date = ?";

        String insertSql =
                "INSERT INTO attendance " +
                "(student_id, attendance_date, status) VALUES (?, ?, ?)";

        try (Connection con = dataSource.getConnection()) {

            con.setAutoCommit(false);

            try (
                    PreparedStatement check =
                            con.prepareStatement(checkSql);
                    PreparedStatement insert =
                            con.prepareStatement(insertSql)
            ) {
                for (Object item : records) {

                    if (!(item instanceof Map<?, ?>)) {
                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Each attendance entry must be an object."
                        );
                    }

                    Map<?, ?> record = (Map<?, ?>) item;
                    Object studentValue = record.get("studentId");
                    Object statusValue = record.get("status");

                    if (studentValue == null || statusValue == null) {
                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Each entry needs studentId and status."
                        );
                    }

                    int studentId;
                    try {
                        studentId =
                                Integer.parseInt(studentValue.toString());
                    } catch (NumberFormatException e) {
                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Invalid studentId."
                        );
                    }

                    if (studentId <= 0) {
                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "studentId must be positive."
                        );
                    }

                    String status = statusValue.toString().trim();

                    if (!status.equalsIgnoreCase("Present")
                            && !status.equalsIgnoreCase("Absent")
                            && !status.equalsIgnoreCase("Late")) {
                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Status must be Present, Absent, or Late."
                        );
                    }

                    status = status.substring(0, 1).toUpperCase()
                            + status.substring(1).toLowerCase();

                    check.setInt(1, studentId);
                    check.setDate(2, attendanceDate);

                    try (ResultSet rs = check.executeQuery()) {
                        if (rs.next()) {
                            skipped++;
                            continue;
                        }
                    }

                    insert.setInt(1, studentId);
                    insert.setDate(2, attendanceDate);
                    insert.setString(3, status);
                    insert.executeUpdate();

                    inserted++;
                }

                con.commit();

            } catch (Exception e) {
                con.rollback();

                if (e instanceof ResponseStatusException) {
                    throw (ResponseStatusException) e;
                }

                throw e;
            } finally {
                con.setAutoCommit(true);
            }

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Bulk attendance could not be saved.",
                    e
            );
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("inserted", inserted);
        response.put("skipped", skipped);
        response.put("message",
                "Bulk attendance processed. Saved: " + inserted
                + ", already marked: " + skipped);

        return response;
    }
    
    // ==========================================
    // MONTHLY ATTENDANCE REPORT
    // GET /api/attendance/monthly?month=10&year=2026
    // ==========================================

    @GetMapping("/api/attendance/monthly")
    public List<Map<String, Object>> getMonthlyAttendance(
            @RequestParam int month,
            @RequestParam int year) {

        if (month < 1 || month > 12 || year < 2000 || year > 2100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Please provide a valid month and year."
            );
        }

        List<Map<String, Object>> report = new ArrayList<>();

        String sql =
                "SELECT s.student_id, s.student_name, s.standard, " +
                "COUNT(a.attendance_id) AS total_days, " +
                "SUM(CASE WHEN LOWER(a.status) = 'present' THEN 1 ELSE 0 END) AS present_days, " +
                "SUM(CASE WHEN LOWER(a.status) = 'absent' THEN 1 ELSE 0 END) AS absent_days, " +
                "SUM(CASE WHEN LOWER(a.status) = 'late' THEN 1 ELSE 0 END) AS late_days " +
                "FROM students s " +
                "LEFT JOIN attendance a ON a.student_id = s.student_id " +
                "AND MONTH(a.attendance_date) = ? " +
                "AND YEAR(a.attendance_date) = ? " +
                "GROUP BY s.student_id, s.student_name, s.standard " +
                "ORDER BY s.student_name";

        try (
                Connection con = dataSource.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setInt(1, month);
            ps.setInt(2, year);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();

                    int total = rs.getInt("total_days");
                    int present = rs.getInt("present_days");
                    int absent = rs.getInt("absent_days");
                    int late = rs.getInt("late_days");

                    row.put("studentId", rs.getInt("student_id"));
                    row.put("studentName", rs.getString("student_name"));
                    row.put("className", rs.getString("standard"));
                    row.put("totalDays", total);
                    row.put("presentDays", present);
                    row.put("absentDays", absent);
                    row.put("lateDays", late);

                    double percentage = total == 0
                            ? 0.0
                            : Math.round((present + late) * 10000.0 / total) / 100.0;

                    row.put("attendancePercentage", percentage);

                    report.add(row);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to generate monthly attendance report."
            );
        }

        return report;
    }
    
    // ==========================================
    // BATCH ATTENDANCE - GET STUDENTS FOR A DATE
    // GET /api/batches/{batchId}/attendance?date=2026-10-27
    // ==========================================

    @GetMapping("/api/batches/{batchId}/attendance")
    public List<Map<String, Object>> getBatchAttendance(
            @PathVariable int batchId,
            @RequestParam String date) {

        java.sql.Date attendanceDate;

        try {
            attendanceDate = java.sql.Date.valueOf(date);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Date must be in YYYY-MM-DD format.");
        }

        List<Map<String, Object>> list = new ArrayList<>();

        String sql =
                "SELECT s.student_id, s.student_name, s.standard, " +
                "s.division, s.phone, a.attendance_id, a.status " +
                "FROM batch_students bs " +
                "JOIN students s ON s.student_id = bs.student_id " +
                "LEFT JOIN attendance a ON a.attendance_id = (" +
                " SELECT MAX(a2.attendance_id) FROM attendance a2 " +
                " WHERE a2.student_id = s.student_id " +
                " AND a2.attendance_date = ?) " +
                "WHERE bs.batch_id = ? " +
                "ORDER BY s.student_name";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDate(1, attendanceDate);
            ps.setInt(2, batchId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();

                    row.put("studentId", rs.getInt("student_id"));
                    row.put("name", rs.getString("student_name"));
                    row.put("standard", rs.getString("standard"));
                    row.put("division", rs.getString("division"));
                    row.put("phone", rs.getString("phone"));
                    row.put("attendanceId", rs.getObject("attendance_id"));
                    row.put("status", rs.getString("status"));

                    list.add(row);
                }
            }

            return list;

        } catch (SQLException e) {
            e.printStackTrace();
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not load batch attendance.", e);
        }
    }

    // ==========================================
    // BATCH ATTENDANCE - SAVE OR UPDATE
    // POST /api/batches/{batchId}/attendance
    // JSON: {"date":"2026-10-27",
    //        "attendance":[{"studentId":1,"status":"Present"}]}
    // ==========================================

    @PostMapping("/api/batches/{batchId}/attendance")
    public Map<String, Object> saveBatchAttendance(
            @PathVariable int batchId,
            @RequestBody Map<String, Object> data) {

        Object dateValue = data.get("date");
        Object recordsValue = data.get("attendance");

        if (dateValue == null || !(recordsValue instanceof List<?>)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Date and attendance list are required.");
        }

        java.sql.Date attendanceDate;

        try {
            attendanceDate = java.sql.Date.valueOf(dateValue.toString());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Date must be in YYYY-MM-DD format.");
        }

        int saved = 0;
        List<?> records = (List<?>) recordsValue;

        String membershipSql =
                "SELECT student_id FROM batch_students " +
                "WHERE batch_id = ? AND student_id = ?";

        String findSql =
                "SELECT attendance_id FROM attendance " +
                "WHERE student_id = ? AND attendance_date = ? " +
                "ORDER BY attendance_id DESC LIMIT 1";

        String updateSql =
                "UPDATE attendance SET status = ? " +
                "WHERE attendance_id = ?";

        String insertSql =
                "INSERT INTO attendance " +
                "(student_id, attendance_date, status) VALUES (?, ?, ?)";

        try (Connection con = dataSource.getConnection()) {
            con.setAutoCommit(false);

            try (PreparedStatement member =
                         con.prepareStatement(membershipSql);
                 PreparedStatement find =
                         con.prepareStatement(findSql);
                 PreparedStatement update =
                         con.prepareStatement(updateSql);
                 PreparedStatement insert =
                         con.prepareStatement(insertSql)) {

                for (Object item : records) {
                    if (!(item instanceof Map<?, ?>)) {
                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Each attendance entry must be an object.");
                    }

                    Map<?, ?> record = (Map<?, ?>) item;
                    Object studentValue = record.get("studentId");
                    Object statusValue = record.get("status");

                    int studentId;
                    try {
                        studentId = Integer.parseInt(
                                String.valueOf(studentValue));
                        if (studentId <= 0) {
                            throw new NumberFormatException();
                        }
                    } catch (NumberFormatException e) {
                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "A valid studentId is required.");
                    }

                    if (statusValue == null) {
                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Attendance status is required.");
                    }

                    String status = statusValue.toString().trim();

                    if (!status.equalsIgnoreCase("Present")
                            && !status.equalsIgnoreCase("Absent")
                            && !status.equalsIgnoreCase("Late")) {
                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Status must be Present, Absent, or Late.");
                    }

                    status = status.substring(0, 1).toUpperCase()
                            + status.substring(1).toLowerCase();

                    // Prevent marking students outside this batch.
                    member.setInt(1, batchId);
                    member.setInt(2, studentId);

                    try (ResultSet rs = member.executeQuery()) {
                        if (!rs.next()) {
                            throw new ResponseStatusException(
                                    HttpStatus.BAD_REQUEST,
                                    "Student " + studentId
                                    + " is not assigned to this batch.");
                        }
                    }

                    // Update an existing record, otherwise insert it.
                    find.setInt(1, studentId);
                    find.setDate(2, attendanceDate);

                    Integer attendanceId = null;
                    try (ResultSet rs = find.executeQuery()) {
                        if (rs.next()) {
                            attendanceId = rs.getInt("attendance_id");
                        }
                    }

                    if (attendanceId != null) {
                        update.setString(1, status);
                        update.setInt(2, attendanceId);
                        update.executeUpdate();
                    } else {
                        insert.setInt(1, studentId);
                        insert.setDate(2, attendanceDate);
                        insert.setString(3, status);
                        insert.executeUpdate();
                    }

                    saved++;
                }

                con.commit();

            } catch (Exception e) {
                con.rollback();

                if (e instanceof ResponseStatusException) {
                    throw (ResponseStatusException) e;
                }

                throw e;

            } finally {
                con.setAutoCommit(true);
            }

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not save batch attendance.", e);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("saved", saved);
        response.put("message", "Batch attendance saved successfully.");

        return response;
    }

}
