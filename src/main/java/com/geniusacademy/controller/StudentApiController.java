        package com.geniusacademy.controller;

        import java.sql.Connection;
        import java.sql.Date;
        import java.sql.PreparedStatement;
        import java.sql.ResultSet;
        import java.sql.SQLException;
        import java.util.ArrayList;
        import java.util.HashMap;
        import java.util.List;
        import java.util.Map;
        import javax.sql.DataSource;
        import org.springframework.web.bind.annotation.DeleteMapping;
        import org.springframework.web.bind.annotation.GetMapping;
        import org.springframework.web.bind.annotation.PathVariable;
        import org.springframework.web.bind.annotation.PostMapping;
        import org.springframework.web.bind.annotation.PutMapping;
        import org.springframework.web.bind.annotation.RequestBody;
        import org.springframework.web.bind.annotation.RequestMapping;
        import org.springframework.web.bind.annotation.RestController;

        @RestController
        @RequestMapping("/api/students")
        public class StudentApiController {
            private final DataSource dataSource;

            public StudentApiController(DataSource dataSource) {
                this.dataSource = dataSource;
            }

            @PostMapping
            public Map<String, Object> addStudent(@RequestBody Map<String, Object> student) {
                Map<String, Object> response = new HashMap<>();
                String sql = "INSERT INTO students "
                + "(student_name, standard, phone, parent_name, admission_date, status) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, getString(student, "name"));
            statement.setString(2, getString(student, "className"));
            statement.setString(3, getString(student, "phone"));
            statement.setString(4, getString(student, "parentName"));

            Date admissionDate = getSqlDate(getString(student, "joinDate"));
            if (admissionDate == null) {
                admissionDate = new Date(System.currentTimeMillis());
            }
            statement.setDate(5, admissionDate);
            statement.setString(6, "Active");

            int rowsInserted = statement.executeUpdate();
            response.put("success", rowsInserted > 0);
            response.put("message", rowsInserted > 0
                    ? "Student added successfully" : "Student was not added");
                }
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    // GET /api/students
    @GetMapping
    public List<Map<String, Object>> getStudents() {
        List<Map<String, Object>> students = new ArrayList<>();
        String sql = "SELECT student_id, student_name, standard, phone, "
                + "gender, date_of_birth, parent_name, parent_phone, address, admission_date "
                + "FROM students ORDER BY student_id DESC";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                students.add(mapStudent(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            students.add(error);
        }
        return students;
    }

    // GET /api/students/{id}
    @GetMapping("/{id}")
    public Map<String, Object> getStudentById(@PathVariable int id) {
        Map<String, Object> student = new HashMap<>();
        String sql = "SELECT student_id, student_name, standard, phone, "
                + "gender, date_of_birth, parent_name, parent_phone, address, admission_date "
                + "FROM students WHERE student_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    student = mapStudent(rs);
                } else {
                    student.put("success", false);
                    student.put("message", "Student not found");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            student.put("success", false);
            student.put("error", e.getMessage());
        }
        return student;
    }

    // PUT /api/students/{id}
    @PutMapping("/{id}")
    public Map<String, Object> updateStudent(@PathVariable int id,
            @RequestBody Map<String, Object> student) {
        Map<String, Object> response = new HashMap<>();
        String sql = "UPDATE students SET student_name = ?, standard = ?, phone = ?, "
                + "parent_name = ?, admission_date = ? WHERE student_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, getString(student, "name"));
            statement.setString(2, getString(student, "className"));
            statement.setString(3, getString(student, "phone"));
            statement.setString(4, getString(student, "parentName"));
            statement.setDate(5, getSqlDate(getString(student, "joinDate")));
            statement.setInt(6, id);

            int rowsUpdated = statement.executeUpdate();
            response.put("success", rowsUpdated > 0);
            response.put("message", rowsUpdated > 0
                    ? "Student updated successfully" : "Student not found");
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    // DELETE /api/students/{id}
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteStudent(@PathVariable int id) {
        Map<String, Object> response = new HashMap<>();
        String sql = "DELETE FROM students WHERE student_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            int rowsDeleted = statement.executeUpdate();
            response.put("success", rowsDeleted > 0);
            response.put("message", rowsDeleted > 0
                    ? "Student deleted successfully" : "Student not found");
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    private String getString(Map<String, Object> values, String key) {
        Object value = values.get(key);
        return value == null ? null : value.toString();
    }

    private Date getSqlDate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return Date.valueOf(value.trim());
    }

    private Map<String, Object> mapStudent(ResultSet rs) throws SQLException {
        Map<String, Object> student = new HashMap<>();
        student.put("id", rs.getInt("student_id"));
        student.put("name", rs.getString("student_name"));
        student.put("className", rs.getString("standard"));
        student.put("phone", rs.getString("phone"));
        student.put("gender", rs.getString("gender"));
        student.put("dateOfBirth", rs.getDate("date_of_birth"));
        student.put("parentName", rs.getString("parent_name"));
        student.put("parentPhone", rs.getString("parent_phone"));
        student.put("address", rs.getString("address"));
        student.put("joinDate", rs.getDate("admission_date"));
        return student;
    }
}