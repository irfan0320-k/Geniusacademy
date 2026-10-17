package com.geniusacademy.controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Date;
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
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/api/students")
public class StudentApiController {
    
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
    // ==========================================
    // ADD STUDENT
    // ==========================================

    @PostMapping
    public Map<String, Object> addStudent(
            @RequestBody Map<String, Object> student) {

        Map<String, Object> response = new HashMap<>();

        try {
            Connection connection =
                    DriverManager.getConnection(URL, USER, PASSWORD);

            String sql =
                    "INSERT INTO students " +
                    "(student_name, standard, phone, parent_name, admission_date, status) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

            PreparedStatement statement =
                    connection.prepareStatement(sql);

            statement.setString(1,
                    String.valueOf(student.get("name")));

            statement.setString(2,
                    String.valueOf(student.get("className")));

            statement.setString(3,
                    String.valueOf(student.get("phone")));

            statement.setString(4,
                    String.valueOf(student.get("parentName")));

            String joinDate =
                    String.valueOf(student.get("joinDate"));

            if (joinDate != null &&
                    !joinDate.equals("null") &&
                    !joinDate.isEmpty()) {

                statement.setDate(5, Date.valueOf(joinDate));

            } else {
                statement.setDate(
                        5,
                        new Date(System.currentTimeMillis())
                );
            }

            statement.setString(6, "Active");

            int rowsInserted =
                    statement.executeUpdate();

            statement.close();
            connection.close();

            if (rowsInserted > 0) {
                response.put("success", true);
                response.put(
                        "message",
                        "Student added successfully"
                );
            } else {
                response.put("success", false);
                response.put(
                        "message",
                        "Student was not added"
                );
            }

        } catch (Exception e) {

            e.printStackTrace();

            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return response;
    }


    // ==========================================
    // GET ALL STUDENTS
    // ==========================================

    @GetMapping
    public List<Map<String, Object>> getStudents() {

        List<Map<String, Object>> students =
                new ArrayList<>();

        try {

            Connection connection =
                    DriverManager.getConnection(
                            URL,
                            USER,
                            PASSWORD
                    );

            String sql =
                    "SELECT student_id, student_name, " +
                    "standard, phone, gender, date_of_birth, " +
                    "parent_name, parent_phone, address, " +
                    "admission_date " +
                    "FROM students " +
                    "ORDER BY student_id DESC";

            PreparedStatement statement =
                    connection.prepareStatement(sql);

            ResultSet resultSet =
                    statement.executeQuery();

            while (resultSet.next()) {

                Map<String, Object> student =
                        new HashMap<>();

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

                student.put(
                        "gender",
                        resultSet.getString("gender")
                );

                student.put(
                        "dateOfBirth",
                        resultSet.getString("date_of_birth")
                );

                student.put(
                        "parentName",
                        resultSet.getString("parent_name")
                );

                student.put(
                        "parentPhone",
                        resultSet.getString("parent_phone")
                );

                student.put(
                        "address",
                        resultSet.getString("address")
                );

                student.put(
                        "joinDate",
                        resultSet.getString("admission_date")
                );

                students.add(student);
            }

            resultSet.close();
            statement.close();
            connection.close();

        } catch (Exception e) {

    e.printStackTrace();

    Map<String, Object> error = new HashMap<>();
    error.put("error", e.getMessage());

    students.clear();
    students.add(error);
}

                return students;
        }


    // ==========================================
    // GET ONE STUDENT BY ID
    // ==========================================

    @GetMapping("/api/students/{id}")
    public Map<String, Object> getStudentById(
            @PathVariable int id) {

        Map<String, Object> student =
                new HashMap<>();

        try {

            Connection connection =
                    DriverManager.getConnection(
                            URL,
                            USER,
                            PASSWORD
                    );

            String sql =
                    "SELECT student_id, student_name, " +
                    "standard, phone, parent_name, " +
                    "admission_date " +
                    "FROM students " +
                    "WHERE student_id = ?";

            PreparedStatement statement =
                    connection.prepareStatement(sql);

            statement.setInt(1, id);

            ResultSet resultSet =
                    statement.executeQuery();

            if (resultSet.next()) {

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

                student.put(
                        "parentName",
                        resultSet.getString("parent_name")
                );

                student.put(
                        "joinDate",
                        resultSet.getString("admission_date")
                );
            }

            resultSet.close();
            statement.close();
            connection.close();

        } catch (Exception e) {

            e.printStackTrace();
            student.put("error", e.getMessage());
        }

        return student;
    }


    // ==========================================
    // UPDATE STUDENT
    // ==========================================

    @PutMapping("/api/students/{id}")
    public Map<String, Object> updateStudent(
            @PathVariable int id,
            @RequestBody Map<String, Object> student) {

        Map<String, Object> response =
                new HashMap<>();

        try {

            Connection connection =
                    DriverManager.getConnection(
                            URL,
                            USER,
                            PASSWORD
                    );

            String sql =
                    "UPDATE students SET " +
                    "student_name = ?, " +
                    "standard = ?, " +
                    "phone = ?, " +
                    "parent_name = ?, " +
                    "admission_date = ? " +
                    "WHERE student_id = ?";

            PreparedStatement statement =
                    connection.prepareStatement(sql);

            statement.setString(
                    1,
                    String.valueOf(student.get("name"))
            );

            statement.setString(
                    2,
                    String.valueOf(student.get("className"))
            );

            statement.setString(
                    3,
                    String.valueOf(student.get("phone"))
            );

            statement.setString(
                    4,
                    String.valueOf(student.get("parentName"))
            );

            String joinDate =
                    String.valueOf(student.get("joinDate"));

            if (joinDate != null &&
                    !joinDate.equals("null") &&
                    !joinDate.isEmpty()) {

                statement.setDate(
                        5,
                        Date.valueOf(joinDate)
                );

            } else {

                statement.setDate(5, null);
            }

            statement.setInt(6, id);

            int rowsUpdated =
                    statement.executeUpdate();

            statement.close();
            connection.close();

            if (rowsUpdated > 0) {

                response.put("success", true);

                response.put(
                        "message",
                        "Student updated successfully"
                );

            } else {

                response.put("success", false);

                response.put(
                        "message",
                        "Student not found"
                );
            }

        } catch (Exception e) {

            e.printStackTrace();

            response.put("success", false);

            response.put(
                    "message",
                    e.getMessage()
            );
        }

        return response;
    }


    // ==========================================
    // DELETE STUDENT
    // ==========================================

    @DeleteMapping("/api/students/{id}")
    public Map<String, Object> deleteStudent(
            @PathVariable int id) {

        Map<String, Object> response =
                new HashMap<>();

        try {

            Connection connection =
                    DriverManager.getConnection(
                            URL,
                            USER,
                            PASSWORD
                    );

            String sql =
                    "DELETE FROM students " +
                    "WHERE student_id = ?";

            PreparedStatement statement =
                    connection.prepareStatement(sql);

            statement.setInt(1, id);

            int rowsDeleted =
                    statement.executeUpdate();

            statement.close();
            connection.close();

            if (rowsDeleted > 0) {

                response.put("success", true);

                response.put(
                        "message",
                        "Student deleted successfully"
                );

            } else {

                response.put("success", false);

                response.put(
                        "message",
                        "Student not found"
                );
            }

        } catch (Exception e) {

            e.printStackTrace();

            response.put("success", false);

            response.put(
                    "message",
                    e.getMessage()
            );
        }

        return response;
    }
}