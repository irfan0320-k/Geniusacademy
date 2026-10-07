package com.geniusacademy.controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StudentAttendanceApiController {

    private final String URL =
            "jdbc:mysql://localhost:3306/genius_academy";

    private final String USER = "root";

    // Apna existing MySQL password yahan rakho
    private final String PASSWORD = "Genius@2026";


    // ==========================================
    // GET ATTENDANCE OF ONE STUDENT
    // ==========================================

    @GetMapping("/api/students/{id}/attendance")
    public List<Map<String, Object>> getStudentAttendance(
            @PathVariable int id) {

        List<Map<String, Object>> attendance =
                new ArrayList<>();


        String sql =
                "SELECT attendance_id, student_id, " +
                "attendance_date, status " +
                "FROM attendance " +
                "WHERE student_id = ? " +
                "ORDER BY attendance_date DESC";


        try (
            Connection conn =
                    DriverManager.getConnection(
                            URL,
                            USER,
                            PASSWORD
                    );

            PreparedStatement stmt =
                    conn.prepareStatement(sql)
        ) {

            stmt.setInt(1, id);

            ResultSet rs =
                    stmt.executeQuery();


            while (rs.next()) {

                Map<String, Object> record =
                        new HashMap<>();


                record.put(
                        "id",
                        rs.getInt("attendance_id")
                );


                record.put(
                        "studentId",
                        rs.getInt("student_id")
                );


                record.put(
                        "date",
                        rs.getDate("attendance_date")
                );


                record.put(
                        "status",
                        rs.getString("status")
                );


                attendance.add(record);
            }


        } catch (Exception e) {

            e.printStackTrace();
        }


        return attendance;
    }

}