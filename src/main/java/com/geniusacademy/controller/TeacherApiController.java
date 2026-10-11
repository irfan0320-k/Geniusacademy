package com.geniusacademy.controller;

import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class TeacherApiController {

   private final String URL =
        System.getenv("SPRING_DATASOURCE_URL");

private final String USER =
        System.getenv("SPRING_DATASOURCE_USERNAME");

private final String PASSWORD =
        System.getenv("SPRING_DATASOURCE_PASSWORD");
    // =========================
    // GET ALL TEACHERS
    // =========================
    @GetMapping("/api/teachers")
    public List<Map<String, Object>> getTeachers() {

        List<Map<String, Object>> teachers = new ArrayList<>();

        String sql = "SELECT teacher_id, teacher_name, subject, phone, email, " +
                     "qualification, joining_date " +
                     "FROM teachers ORDER BY teacher_id DESC";

        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Map<String, Object> teacher = new HashMap<>();

                teacher.put("id", rs.getInt("teacher_id"));
                teacher.put("name", rs.getString("teacher_name"));
                teacher.put("subject", rs.getString("subject"));
                teacher.put("phone", rs.getString("phone"));
                teacher.put("email", rs.getString("email"));
                teacher.put("qualification", rs.getString("qualification"));
                teacher.put("joiningDate", rs.getDate("joining_date"));

                teachers.add(teacher);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return teachers;
    }


    // =========================
    // GET ONE TEACHER
    // =========================
    @GetMapping("/api/teachers/{id}")
    public Map<String, Object> getTeacher(@PathVariable int id) {

        Map<String, Object> teacher = new HashMap<>();

        String sql = "SELECT teacher_id, teacher_name, subject, phone, email, " +
                     "qualification, joining_date " +
                     "FROM teachers WHERE teacher_id = ?";

        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                teacher.put("id", rs.getInt("teacher_id"));
                teacher.put("name", rs.getString("teacher_name"));
                teacher.put("subject", rs.getString("subject"));
                teacher.put("phone", rs.getString("phone"));
                teacher.put("email", rs.getString("email"));
                teacher.put("qualification", rs.getString("qualification"));
                teacher.put("joiningDate", rs.getDate("joining_date"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return teacher;
    }


    // =========================
    // ADD TEACHER
    // =========================
    @PostMapping("/api/teachers")
    public Map<String, Object> addTeacher(
            @RequestBody Map<String, Object> data) {

        Map<String, Object> response = new HashMap<>();

        String sql = "INSERT INTO teachers " +
                     "(teacher_name, subject, phone, email, qualification, joining_date) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, getValue(data, "name"));
            ps.setString(2, getValue(data, "subject"));
            ps.setString(3, getValue(data, "phone"));
            ps.setString(4, getValue(data, "email"));
            ps.setString(5, getValue(data, "qualification"));

            String joiningDate = getValue(data, "joiningDate");

            if (joiningDate != null && !joiningDate.isEmpty()) {
                ps.setDate(6, java.sql.Date.valueOf(joiningDate));
            } else {
                ps.setDate(6, null);
            }

            ps.executeUpdate();

            response.put("success", true);
            response.put("message", "Teacher added successfully");

        } catch (Exception e) {

            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return response;
    }


    // =========================
    // UPDATE TEACHER
    // =========================
    @PutMapping("/api/teachers/{id}")
    public Map<String, Object> updateTeacher(
            @PathVariable int id,
            @RequestBody Map<String, Object> data) {

        Map<String, Object> response = new HashMap<>();

        String sql = "UPDATE teachers SET " +
                     "teacher_name = ?, " +
                     "subject = ?, " +
                     "phone = ?, " +
                     "email = ?, " +
                     "qualification = ?, " +
                     "joining_date = ? " +
                     "WHERE teacher_id = ?";

        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, getValue(data, "name"));
            ps.setString(2, getValue(data, "subject"));
            ps.setString(3, getValue(data, "phone"));
            ps.setString(4, getValue(data, "email"));
            ps.setString(5, getValue(data, "qualification"));

            String joiningDate = getValue(data, "joiningDate");

            if (joiningDate != null && !joiningDate.isEmpty()) {
                ps.setDate(6, java.sql.Date.valueOf(joiningDate));
            } else {
                ps.setDate(6, null);
            }

            ps.setInt(7, id);

            ps.executeUpdate();

            response.put("success", true);
            response.put("message", "Teacher updated successfully");

        } catch (Exception e) {

            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return response;
    }


    // =========================
    // DELETE TEACHER
    // =========================
    @DeleteMapping("/api/teachers/{id}")
    public Map<String, Object> deleteTeacher(@PathVariable int id) {

        Map<String, Object> response = new HashMap<>();

        String sql = "DELETE FROM teachers WHERE teacher_id = ?";

        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            ps.executeUpdate();

            response.put("success", true);
            response.put("message", "Teacher deleted successfully");

        } catch (Exception e) {

            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return response;
    }


    // =========================
    // HELPER METHOD
    // =========================
    private String getValue(Map<String, Object> data, String key) {

        Object value = data.get(key);

        if (value == null) {
            return null;
        }

        String text = value.toString().trim();

        if (text.isEmpty()) {
            return null;
        }

        return text;
    }
}