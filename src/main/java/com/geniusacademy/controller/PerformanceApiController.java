
package com.geniusacademy.controller;

import org.springframework.web.bind.annotation.*;
import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

@RestController
@RequestMapping("/api/performance")
public class PerformanceApiController {

    private final DataSource dataSource;

    // Spring Boot automatically supplies the configured database
    public PerformanceApiController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // GET: Get all performance records
    @GetMapping
    public List<Map<String, Object>> getAllPerformance() {

        List<Map<String, Object>> list = new ArrayList<>();

        String sql = """
                SELECT performance_id, student_id, subject, marks, exam_name
                FROM performance
                ORDER BY performance_id DESC
                """;

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("id", rs.getInt("performance_id"));
                row.put("studentId", rs.getInt("student_id"));
                row.put("subject", rs.getString("subject"));
                row.put("marks", rs.getDouble("marks"));
                row.put("examName", rs.getString("exam_name"));
                list.add(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    // GET: Get one record by ID
    @GetMapping("/{id}")
    public Map<String, Object> getPerformance(@PathVariable int id) {

        String sql = """
                SELECT performance_id, student_id, subject, marks, exam_name
                FROM performance
                WHERE performance_id = ?
                """;

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("id", rs.getInt("performance_id"));
                    row.put("studentId", rs.getInt("student_id"));
                    row.put("subject", rs.getString("subject"));
                    row.put("marks", rs.getDouble("marks"));
                    row.put("examName", rs.getString("exam_name"));
                    return row;
                }
            }

            return Map.of(
                    "success", false,
                    "message", "Performance record not found"
            );

        } catch (SQLException e) {
            e.printStackTrace();
            return Map.of(
                    "success", false,
                    "message", "Database error while fetching performance"
            );
        }
    }

    // POST: Add a performance record
    @PostMapping
    public Map<String, Object> addPerformance(
            @RequestBody Map<String, Object> data) {

        try {
            int studentId = getStudentId(data);
            String subject = getRequiredText(data, "subject");
            double marks = getMarks(data);
            String examName = getRequiredText(data, "examName");

            String sql = """
                    INSERT INTO performance
                    (student_id, subject, marks, exam_name)
                    VALUES (?, ?, ?, ?)
                    """;

            try (Connection con = dataSource.getConnection();
                 PreparedStatement ps = con.prepareStatement(
                         sql, Statement.RETURN_GENERATED_KEYS)) {

                ps.setInt(1, studentId);
                ps.setString(2, subject);
                ps.setDouble(3, marks);
                ps.setString(4, examName);

                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        return Map.of(
                                "success", true,
                                "id", keys.getInt(1),
                                "message", "Performance added successfully"
                        );
                    }
                }

                return Map.of(
                        "success", true,
                        "message", "Performance added successfully"
                );
            }

        } catch (IllegalArgumentException e) {
            return Map.of("success", false, "message", e.getMessage());

        } catch (SQLException e) {
            e.printStackTrace();
            return Map.of(
                    "success", false,
                    "message", "Database error while adding performance"
            );
        }
    }

    // PUT: Update a performance record
    @PutMapping("/{id}")
    public Map<String, Object> updatePerformance(
            @PathVariable int id,
            @RequestBody Map<String, Object> data) {

        try {
            int studentId = getStudentId(data);
            String subject = getRequiredText(data, "subject");
            double marks = getMarks(data);
            String examName = getRequiredText(data, "examName");

            String sql = """
                    UPDATE performance
                    SET student_id = ?, subject = ?, marks = ?, exam_name = ?
                    WHERE performance_id = ?
                    """;

            try (Connection con = dataSource.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setInt(1, studentId);
                ps.setString(2, subject);
                ps.setDouble(3, marks);
                ps.setString(4, examName);
                ps.setInt(5, id);

                int affected = ps.executeUpdate();

                if (affected == 0) {
                    return Map.of(
                            "success", false,
                            "message", "Record not found or no changes made"
                    );
                }

                return Map.of(
                        "success", true,
                        "message", "Performance updated successfully"
                );
            }

        } catch (IllegalArgumentException e) {
            return Map.of("success", false, "message", e.getMessage());

        } catch (SQLException e) {
            e.printStackTrace();
            return Map.of(
                    "success", false,
                    "message", "Database error while updating performance"
            );
        }
    }

    // DELETE: Delete a performance record
    @DeleteMapping("/{id}")
    public Map<String, Object> deletePerformance(@PathVariable int id) {

        String sql = "DELETE FROM performance WHERE performance_id = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            int affected = ps.executeUpdate();

            if (affected == 0) {
                return Map.of(
                        "success", false,
                        "message", "Performance record not found"
                );
            }

            return Map.of(
                    "success", true,
                    "message", "Performance deleted successfully"
            );

        } catch (SQLException e) {
            e.printStackTrace();
            return Map.of(
                    "success", false,
                    "message", "Database error while deleting performance"
            );
        }
    }

    // Validate Student ID
    private int getStudentId(Map<String, Object> data) {
        Object value = data.get("studentId");

        if (value == null) {
            throw new IllegalArgumentException("Student ID is required");
        }

        try {
            int id = Integer.parseInt(value.toString());

            if (id <= 0) {
                throw new IllegalArgumentException(
                        "Student ID must be greater than zero"
                );
            }

            return id;

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid Student ID");
        }
    }

    // Validate required text
    private String getRequiredText(Map<String, Object> data, String key) {
        Object value = data.get(key);

        if (value == null || value.toString().trim().isEmpty()) {
            throw new IllegalArgumentException(key + " is required");
        }

        return value.toString().trim();
    }

    // Validate marks between 0 and 100
    private double getMarks(Map<String, Object> data) {
        Object value = data.get("marks");

        if (value == null) {
            throw new IllegalArgumentException("Marks are required");
        }

        try {
            double marks = Double.parseDouble(value.toString());

            if (!Double.isFinite(marks) || marks < 0 || marks > 100) {
                throw new IllegalArgumentException(
                        "Marks must be between 0 and 100"
                );
            }

            return marks;

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid marks");
        }
    }
}
