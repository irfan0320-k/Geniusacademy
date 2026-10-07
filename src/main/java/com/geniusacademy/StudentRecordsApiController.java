package com.geniusacademy;

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
public class StudentRecordsApiController {

    private final String URL =
            "jdbc:mysql://localhost:3306/genius_academy";

    private final String USER = "root";

    // Apna existing MySQL password yahan rakho
    private final String PASSWORD = "Genius@2026";


    // ==========================================
    // GET FEES OF ONE STUDENT
    // ==========================================

    @GetMapping("/api/students/{id}/fees")
    public List<Map<String, Object>> getStudentFees(
            @PathVariable int id) {

        List<Map<String, Object>> fees =
                new ArrayList<>();

        String sql =
                "SELECT fee_id, student_id, amount, " +
                "payment_date, status " +
                "FROM fees " +
                "WHERE student_id = ? " +
                "ORDER BY payment_date DESC";


        try (
            Connection conn =
                    DriverManager.getConnection(
                            URL, USER, PASSWORD);

            PreparedStatement stmt =
                    conn.prepareStatement(sql)
        ) {

            stmt.setInt(1, id);

            ResultSet rs =
                    stmt.executeQuery();


            while (rs.next()) {

                Map<String, Object> fee =
                        new HashMap<>();

                fee.put(
                        "id",
                        rs.getInt("fee_id")
                );

                fee.put(
                        "studentId",
                        rs.getInt("student_id")
                );

                fee.put(
                        "amount",
                        rs.getDouble("amount")
                );

                fee.put(
                        "paymentDate",
                        rs.getDate("payment_date")
                );

                fee.put(
                        "status",
                        rs.getString("status")
                );

                fees.add(fee);
            }


        } catch (Exception e) {

            e.printStackTrace();
        }


        return fees;
    }


    // ==========================================
    // GET PERFORMANCE OF ONE STUDENT
    // ==========================================

    @GetMapping("/api/students/{id}/performance")
    public List<Map<String, Object>> getStudentPerformance(
            @PathVariable int id) {

        List<Map<String, Object>> performance =
                new ArrayList<>();


        String sql =
                "SELECT performance_id, subject, marks, exam_name " +
                "FROM performance " +
                "WHERE student_id = ? " +
                "ORDER BY performance_id DESC";


        try (
            Connection conn =
                    DriverManager.getConnection(
                            URL, USER, PASSWORD);

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
                        rs.getInt("performance_id")
                );

                record.put(
                        "subject",
                        rs.getString("subject")
                );

                record.put(
                        "marks",
                        rs.getDouble("marks")
                );

                record.put(
                        "exam",
                        rs.getString("exam_name")
                );

                performance.add(record);
            }

        } catch (Exception e) {

            e.printStackTrace();
        }


        return performance;
    }

}