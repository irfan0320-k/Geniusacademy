package com.geniusacademy.controller;

import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.util.*;

@RestController
public class PerformanceApiController {

    private final String URL =
        System.getenv("SPRING_DATASOURCE_URL");

private final String USER =
        System.getenv("SPRING_DATASOURCE_USERNAME");

private final String PASSWORD =
        System.getenv("SPRING_DATASOURCE_PASSWORD");
    // =========================
    // GET ALL PERFORMANCE
    // =========================
    @GetMapping("/api/performance")
    public List<Map<String, Object>> getAllPerformance() {

        List<Map<String, Object>> list = new ArrayList<>();

        String sql =
                "SELECT performance_id, student_id, subject, marks, exam_name " +
                "FROM performance ORDER BY performance_id DESC";

        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
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

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // =========================
    // GET ONE PERFORMANCE
    // =========================
    @GetMapping("/api/performance/{id}")
    public Map<String, Object> getPerformance(@PathVariable int id) {

        Map<String, Object> row = new HashMap<>();

        String sql =
                "SELECT performance_id, student_id, subject, marks, exam_name " +
                "FROM performance WHERE performance_id = ?";

        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                row.put("id", rs.getInt("performance_id"));
                row.put("studentId", rs.getInt("student_id"));
                row.put("subject", rs.getString("subject"));
                row.put("marks", rs.getDouble("marks"));
                row.put("examName", rs.getString("exam_name"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return row;
    }

    // =========================
    // ADD PERFORMANCE
    // =========================
    @PostMapping("/api/performance")
    public Map<String, Object> addPerformance(
            @RequestBody Map<String, Object> data) {

        Map<String, Object> response = new HashMap<>();

        String sql =
                "INSERT INTO performance " +
                "(student_id, subject, marks, exam_name) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, Integer.parseInt(data.get("studentId").toString()));
            ps.setString(2, data.get("subject").toString());
            ps.setDouble(3, Double.parseDouble(data.get("marks").toString()));
            ps.setString(4, data.get("examName").toString());

            ps.executeUpdate();

            response.put("success", true);
            response.put("message", "Performance added successfully");

        } catch (Exception e) {

            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return response;
    }

    // =========================
    // UPDATE PERFORMANCE
    // =========================
    @PutMapping("/api/performance/{id}")
    public Map<String, Object> updatePerformance(
            @PathVariable int id,
            @RequestBody Map<String, Object> data) {

        Map<String, Object> response = new HashMap<>();

        String sql =
                "UPDATE performance SET " +
                "student_id = ?, subject = ?, marks = ?, exam_name = ? " +
                "WHERE performance_id = ?";

        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, Integer.parseInt(data.get("studentId").toString()));
            ps.setString(2, data.get("subject").toString());
            ps.setDouble(3, Double.parseDouble(data.get("marks").toString()));
            ps.setString(4, data.get("examName").toString());
            ps.setInt(5, id);

            ps.executeUpdate();

            response.put("success", true);
            response.put("message", "Performance updated successfully");

        } catch (Exception e) {

            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return response;
    }

    // =========================
    // DELETE PERFORMANCE
    // =========================
    @DeleteMapping("/api/performance/{id}")
    public Map<String, Object> deletePerformance(
            @PathVariable int id) {

        Map<String, Object> response = new HashMap<>();

        String sql =
                "DELETE FROM performance WHERE performance_id = ?";

        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            ps.executeUpdate();

            response.put("success", true);
            response.put("message", "Performance deleted successfully");

        } catch (Exception e) {

            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return response;
    }
}