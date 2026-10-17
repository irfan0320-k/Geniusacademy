package com.geniusacademy.controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FeesApiController {

    private final String URL = System.getenv().getOrDefault(
        "SPRING_DATASOURCE_URL",
        "jdbc:mysql://localhost:3306/genius_academy"
);

private final String USER = System.getenv().getOrDefault(
        "SPRING_DATASOURCE_USERNAME",
        "root"
);

private final String PASSWORD = System.getenv().getOrDefault(
        "SPRING_DATASOURCE_PASSWORD",
        ""
);
    // ========================================
    // GET ALL FEES
    // ========================================

    @GetMapping("/api/fees")
    public List<Map<String, Object>> getAllFees() {

        List<Map<String, Object>> fees = new ArrayList<>();

        String sql =
                "SELECT fee_id, student_id, amount, payment_date, status " +
                "FROM fees " +
                "ORDER BY payment_date DESC, fee_id DESC";

        try (
            Connection conn =
                    DriverManager.getConnection(URL, USER, PASSWORD);

            PreparedStatement stmt =
                    conn.prepareStatement(sql);

            ResultSet rs =
                    stmt.executeQuery()
        ) {

            while (rs.next()) {

                Map<String, Object> fee = new HashMap<>();

                fee.put("id", rs.getInt("fee_id"));
                fee.put("studentId", rs.getInt("student_id"));
                fee.put("amount", rs.getDouble("amount"));
                fee.put("paymentDate", rs.getDate("payment_date"));
                fee.put("status", rs.getString("status"));

                fees.add(fee);
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return fees;
    }


    // ========================================
    // GET SINGLE FEE
    // ========================================

    @GetMapping("/api/fees/{id}")
    public Map<String, Object> getFeeById(
            @PathVariable int id) {

        Map<String, Object> fee = new HashMap<>();

        String sql =
                "SELECT fee_id, student_id, amount, " +
                "payment_date, status " +
                "FROM fees " +
                "WHERE fee_id = ?";

        try (
            Connection conn =
                    DriverManager.getConnection(URL, USER, PASSWORD);

            PreparedStatement stmt =
                    conn.prepareStatement(sql)
        ) {

            stmt.setInt(1, id);

            ResultSet rs =
                    stmt.executeQuery();

            if (rs.next()) {

                fee.put("id",
                        rs.getInt("fee_id"));

                fee.put("studentId",
                        rs.getInt("student_id"));

                fee.put("amount",
                        rs.getDouble("amount"));

                fee.put("paymentDate",
                        rs.getDate("payment_date"));

                fee.put("status",
                        rs.getString("status"));
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return fee;
    }


    // ========================================
    // ADD FEE
    // ========================================

    @PostMapping("/api/fees")
    public Map<String, Object> addFee(
            @RequestBody Map<String, Object> data) {

        Map<String, Object> response =
                new HashMap<>();

        String sql =
                "INSERT INTO fees " +
                "(student_id, amount, payment_date, status) " +
                "VALUES (?, ?, ?, ?)";

        try (
            Connection conn =
                    DriverManager.getConnection(URL, USER, PASSWORD);

            PreparedStatement stmt =
                    conn.prepareStatement(sql)
        ) {

            stmt.setInt(
                    1,
                    Integer.parseInt(
                            data.get("studentId").toString()
                    )
            );

            stmt.setDouble(
                    2,
                    Double.parseDouble(
                            data.get("amount").toString()
                    )
            );

            stmt.setDate(
                    3,
                    java.sql.Date.valueOf(
                            data.get("paymentDate").toString()
                    )
            );

            stmt.setString(
                    4,
                    data.get("status").toString()
            );

            int rows =
                    stmt.executeUpdate();

            if (rows > 0) {

                response.put(
                        "success",
                        true
                );

                response.put(
                        "message",
                        "Fee payment saved successfully!"
                );

            } else {

                response.put(
                        "success",
                        false
                );
            }

        } catch (Exception e) {

            e.printStackTrace();

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


    // ========================================
    // UPDATE FEE
    // ========================================

    @PutMapping("/api/fees/{id}")
    public Map<String, Object> updateFee(
            @PathVariable int id,
            @RequestBody Map<String, Object> data) {

        Map<String, Object> response =
                new HashMap<>();

        String sql =
                "UPDATE fees SET " +
                "student_id = ?, " +
                "amount = ?, " +
                "payment_date = ?, " +
                "status = ? " +
                "WHERE fee_id = ?";

        try (
            Connection conn =
                    DriverManager.getConnection(URL, USER, PASSWORD);

            PreparedStatement stmt =
                    conn.prepareStatement(sql)
        ) {

            stmt.setInt(
                    1,
                    Integer.parseInt(
                            data.get("studentId").toString()
                    )
            );

            stmt.setDouble(
                    2,
                    Double.parseDouble(
                            data.get("amount").toString()
                    )
            );

            stmt.setDate(
                    3,
                    java.sql.Date.valueOf(
                            data.get("paymentDate").toString()
                    )
            );

            stmt.setString(
                    4,
                    data.get("status").toString()
            );

            stmt.setInt(5, id);

            int rows =
                    stmt.executeUpdate();

            if (rows > 0) {

                response.put(
                        "success",
                        true
                );

                response.put(
                        "message",
                        "Fee updated successfully!"
                );

            } else {

                response.put(
                        "success",
                        false
                );

                response.put(
                        "message",
                        "Fee record not found."
                );
            }

        } catch (Exception e) {

            e.printStackTrace();

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


    // ========================================
    // DELETE FEE
    // ========================================

    @DeleteMapping("/api/fees/{id}")
    public Map<String, Object> deleteFee(
            @PathVariable int id) {

        Map<String, Object> response =
                new HashMap<>();

        String sql =
                "DELETE FROM fees WHERE fee_id = ?";

        try (
            Connection conn =
                    DriverManager.getConnection(URL, USER, PASSWORD);

            PreparedStatement stmt =
                    conn.prepareStatement(sql)
        ) {

            stmt.setInt(1, id);

            int rows =
                    stmt.executeUpdate();

            if (rows > 0) {

                response.put(
                        "success",
                        true
                );

                response.put(
                        "message",
                        "Fee deleted successfully!"
                );

            } else {

                response.put(
                        "success",
                        false
                );

                response.put(
                        "message",
                        "Fee record not found."
                );
            }

        } catch (Exception e) {

            e.printStackTrace();

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