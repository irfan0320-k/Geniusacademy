package com.geniusacademy.controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DashboardApiController {

    // MySQL Database Connection
private final String URL =
        System.getenv("SPRING_DATASOURCE_URL");

private final String USER =
        System.getenv("SPRING_DATASOURCE_USERNAME");

private final String PASSWORD =
        System.getenv("SPRING_DATASOURCE_PASSWORD");


    // =========================================================
    // DASHBOARD SUMMARY
    // =========================================================

    @GetMapping("/api/dashboard")
    public Map<String, Object> getDashboardData() {

        Map<String, Object> data = new HashMap<>();

        try {

            Connection connection =
                    DriverManager.getConnection(URL, USER, PASSWORD);

            Statement statement = connection.createStatement();


            // Total Students
            ResultSet studentsResult =
                    statement.executeQuery(
                            "SELECT COUNT(*) FROM students"
                    );

            studentsResult.next();

            int totalStudents =
                    studentsResult.getInt(1);


            // Total Teachers
            ResultSet teachersResult =
                    statement.executeQuery(
                            "SELECT COUNT(*) FROM teachers"
                    );

            teachersResult.next();

            int totalTeachers =
                    teachersResult.getInt(1);


           // Total Fees
ResultSet feesResult =
        statement.executeQuery(
                "SELECT COALESCE(SUM(amount), 0) FROM fees");
feesResult.next();
double totalFees = feesResult.getDouble(1);

// Collected Fees
ResultSet collectedFeesResult =
        statement.executeQuery(
                "SELECT COALESCE(SUM(amount), 0) FROM fees WHERE status = 'Paid'");
collectedFeesResult.next();
double collectedFees = collectedFeesResult.getDouble(1);

// Pending Fees
double pendingFees = totalFees - collectedFees;


            // Total Attendance
           // Today's Attendance
// Latest Attendance
ResultSet attendanceResult =
        statement.executeQuery(
                "SELECT " +
                "COUNT(*) AS total, " +
                "SUM(CASE WHEN status = 'Present' THEN 1 ELSE 0 END) AS present " +
                "FROM attendance " +
                "WHERE attendance_date = (" +
                "SELECT MAX(attendance_date) FROM attendance" +
                ")"
        );

attendanceResult.next();

int totalAttendance =
        attendanceResult.getInt("total");

int presentToday =
        attendanceResult.getInt("present");

            // Total Performance Records
            ResultSet performanceResult =
                    statement.executeQuery(
                            "SELECT COUNT(*) FROM performance"
                    );

            performanceResult.next();

            int totalPerformance =
                    performanceResult.getInt(1);


            // Put data into response
            data.put("totalStudents", totalStudents);
            data.put("totalTeachers", totalTeachers);
            data.put("totalFees", totalFees);
            data.put("collectedFees", collectedFees);
            data.put("pendingFees", pendingFees); 
            data.put("totalAttendance", totalAttendance);
            data.put("presentToday", presentToday);
            data.put("totalPerformance", totalPerformance);


            connection.close();

        } catch (Exception e) {

            e.printStackTrace();

            data.put("error", e.getMessage());
        }


        return data;
    }



    // =========================================================
    // RECENT PERFORMANCE
    // =========================================================

    @GetMapping("/api/dashboard/performance")
    public List<Map<String, Object>> getRecentPerformance() {

        List<Map<String, Object>> performance =
                new ArrayList<>();


        try {

            Connection connection =
                    DriverManager.getConnection(URL, USER, PASSWORD);

            Statement statement =
                    connection.createStatement();


            /*
             * IMPORTANT:
             * students table uses student_name
             * NOT name
             */

            ResultSet resultSet =
                    statement.executeQuery(

                            "SELECT p.performance_id, " +
"s.student_name, " +
"p.subject, " +
"p.exam_name, " +
"p.marks " +
                            "FROM performance p " +

                            "JOIN students s " +
                            "ON p.student_id = s.student_id " +

                            "ORDER BY p.performance_id DESC " +

                            "LIMIT 5"
                    );


            while (resultSet.next()) {

                Map<String, Object> record =
                        new HashMap<>();


                record.put(
                        "id",
                        resultSet.getInt("performance_id")
                );


                record.put(
                        "student",
                        resultSet.getString("student_name")
                );


                record.put(
                        "exam",
                        resultSet.getString("exam_name")
                );
                record.put("subject", resultSet.getString("subject"));


                record.put(
                        "marks",
                        resultSet.getDouble("marks")
                );


                performance.add(record);
            }


            connection.close();

        } catch (Exception e) {

            e.printStackTrace();
        }


        return performance;
    }



    // =========================================================
    // RECENT STUDENTS
    // =========================================================

    @GetMapping("/api/dashboard/students")
public List<Map<String, Object>> getRecentStudents() {

    List<Map<String, Object>> students = new ArrayList<>();

    try {

        Connection connection =
                DriverManager.getConnection(URL, USER, PASSWORD);

        Statement statement =
                connection.createStatement();

        ResultSet resultSet = statement.executeQuery(
                "SELECT student_id, student_name, standard, phone " +
                "FROM students " +
                "ORDER BY student_id DESC " +
                "LIMIT 5"
        );

        while (resultSet.next()) {

            Map<String, Object> student = new HashMap<>();

            student.put(
                    "id",
                    resultSet.getInt("student_id")
            );

            student.put(
                    "name",
                    resultSet.getString("student_name")
            );

            student.put(
                    "className",
                    resultSet.getString("standard")
            );

            student.put(
                    "phone",
                    resultSet.getString("phone")
            );

            students.add(student);
        }

        connection.close();

    } catch (Exception e) {

        e.printStackTrace();
    }

    return students;
}
}