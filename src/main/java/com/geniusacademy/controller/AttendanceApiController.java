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
public class AttendanceApiController {

    private final String URL =
            "jdbc:mysql://localhost:3306/genius_academy";

    private final String USER = "root";

    private final String PASSWORD =
            "Genius@2026"; // Apna existing MySQL password yahan rakho


    // ==========================================
    // GET ALL ATTENDANCE
    // ==========================================

    @GetMapping("/api/attendance")
    public List<Map<String, Object>> getAllAttendance() {

        List<Map<String, Object>> list =
                new ArrayList<>();

        String sql =
                "SELECT attendance_id, student_id, " +
                "attendance_date, status " +
                "FROM attendance " +
                "ORDER BY attendance_date DESC, attendance_id DESC";

        try (
                Connection con =
                        DriverManager.getConnection(
                                URL, USER, PASSWORD);

                PreparedStatement ps =
                        con.prepareStatement(sql);

                ResultSet rs =
                        ps.executeQuery()
        ) {

            while (rs.next()) {

                Map<String, Object> row =
                        new HashMap<>();

                row.put(
                        "id",
                        rs.getInt("attendance_id")
                );

                row.put(
                        "studentId",
                        rs.getInt("student_id")
                );

                row.put(
                        "date",
                        rs.getDate("attendance_date")
                );

                row.put(
                        "status",
                        rs.getString("status")
                );

                list.add(row);
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return list;
    }


    // ==========================================
    // GET ONE ATTENDANCE
    // ==========================================

    @GetMapping("/api/attendance/{id}")
    public Map<String, Object> getAttendance(
            @PathVariable int id) {

        Map<String, Object> row =
                new HashMap<>();

        String sql =
                "SELECT attendance_id, student_id, " +
                "attendance_date, status " +
                "FROM attendance " +
                "WHERE attendance_id = ?";

        try (
                Connection con =
                        DriverManager.getConnection(
                                URL, USER, PASSWORD);

                PreparedStatement ps =
                        con.prepareStatement(sql)
        ) {

            ps.setInt(1, id);

            ResultSet rs =
                    ps.executeQuery();

            if (rs.next()) {

                row.put(
                        "id",
                        rs.getInt("attendance_id")
                );

                row.put(
                        "studentId",
                        rs.getInt("student_id")
                );

                row.put(
                        "date",
                        rs.getDate("attendance_date")
                );

                row.put(
                        "status",
                        rs.getString("status")
                );
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return row;
    }


    // ==========================================
    // ADD ATTENDANCE
    // ==========================================

    @PostMapping("/api/attendance")
    public Map<String, Object> addAttendance(
            @RequestBody Map<String, Object> data) {

        Map<String, Object> response =
                new HashMap<>();

        String sql =
                "INSERT INTO attendance " +
                "(student_id, attendance_date, status) " +
                "VALUES (?, ?, ?)";

        try (
                Connection con =
                        DriverManager.getConnection(
                                URL, USER, PASSWORD);

                PreparedStatement ps =
                        con.prepareStatement(sql)
        ) {

            ps.setInt(
                    1,
                    Integer.parseInt(
                            data.get("studentId").toString()
                    )
            );

            ps.setDate(
                    2,
                    java.sql.Date.valueOf(
                            data.get("date").toString()
                    )
            );

            ps.setString(
                    3,
                    data.get("status").toString()
            );

            ps.executeUpdate();

            response.put(
                    "success",
                    true
            );

            response.put(
                    "message",
                    "Attendance added successfully"
            );

        } catch (Exception e) {

            response.put(
                    "success",
                    false
            );

            response.put(
                    "message",
                    e.getMessage()
            );
        }

        return response;
    }


    // ==========================================
    // UPDATE ATTENDANCE
    // ==========================================

    @PutMapping("/api/attendance/{id}")
    public Map<String, Object> updateAttendance(
            @PathVariable int id,
            @RequestBody Map<String, Object> data) {

        Map<String, Object> response =
                new HashMap<>();

        String sql =
                "UPDATE attendance SET " +
                "student_id = ?, " +
                "attendance_date = ?, " +
                "status = ? " +
                "WHERE attendance_id = ?";

        try (
                Connection con =
                        DriverManager.getConnection(
                                URL, USER, PASSWORD);

                PreparedStatement ps =
                        con.prepareStatement(sql)
        ) {

            ps.setInt(
                    1,
                    Integer.parseInt(
                            data.get("studentId").toString()
                    )
            );

            ps.setDate(
                    2,
                    java.sql.Date.valueOf(
                            data.get("date").toString()
                    )
            );

            ps.setString(
                    3,
                    data.get("status").toString()
            );

            ps.setInt(4, id);

            ps.executeUpdate();

            response.put(
                    "success",
                    true
            );

            response.put(
                    "message",
                    "Attendance updated successfully"
            );

        } catch (Exception e) {

            response.put(
                    "success",
                    false
            );

            response.put(
                    "message",
                    e.getMessage()
            );
        }

        return response;
    }


    // ==========================================
    // DELETE ATTENDANCE
    // ==========================================

    @DeleteMapping("/api/attendance/{id}")
    public Map<String, Object> deleteAttendance(
            @PathVariable int id) {

        Map<String, Object> response =
                new HashMap<>();

        String sql =
                "DELETE FROM attendance " +
                "WHERE attendance_id = ?";

        try (
                Connection con =
                        DriverManager.getConnection(
                                URL, USER, PASSWORD);

                PreparedStatement ps =
                        con.prepareStatement(sql)
        ) {

            ps.setInt(1, id);

            ps.executeUpdate();

            response.put(
                    "success",
                    true
            );

            response.put(
                    "message",
                    "Attendance deleted successfully"
            );

        } catch (Exception e) {

            response.put(
                    "success",
                    false
            );

            response.put(
                    "message",
                    e.getMessage()
            );
        }

        return response;
    }
}