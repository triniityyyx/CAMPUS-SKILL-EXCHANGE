package dao;

import model.Student;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Student entity.
 * Uses PreparedStatement for all queries.
 */
public class StudentDAO {

    /**
     * Create a new student in the database.
     *
     * @param student Student object to register
     * @return generated student_id, or -1 if failed
     * @throws SQLException on database error
     */
    public int createStudent(Student student) throws SQLException {
        String sql = "INSERT INTO STUDENT (name, email, password, department, year) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, student.getName().trim());
            stmt.setString(2, student.getEmail().trim().toLowerCase());
            stmt.setString(3, student.getPassword());
            stmt.setString(4, student.getDepartment().trim());
            stmt.setInt(5, student.getYear());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        student.setId(id);
                        return id;
                    }
                }
            }
        }
        return -1;
    }

    /**
     * Find student by email.
     */
    public Student findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM STUDENT WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToStudent(rs);
                }
            }
        }
        return null;
    }

    /**
     * Find student by ID.
     */
    public Student findById(int studentId) throws SQLException {
        String sql = "SELECT * FROM STUDENT WHERE student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToStudent(rs);
                }
            }
        }
        return null;
    }

    /**
     * Check if email already exists in database.
     */
    public boolean existsByEmail(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM STUDENT WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Get average rating and total review count for a student.
     */
    public double[] getAverageRatingAndCount(int studentId) throws SQLException {
        String sql = "SELECT COALESCE(AVG(r.rating), 0.0) AS avg_rating, COUNT(r.review_id) AS total_count " +
                     "FROM REVIEW r " +
                     "JOIN SESSION s ON r.session_id = s.session_id " +
                     "JOIN LEARNING_REQUEST lr ON s.request_id = lr.request_id " +
                     "WHERE ((lr.receiver_id = ? AND r.reviewer_id != ?) OR (lr.sender_id = ? AND r.reviewer_id != ?))";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            stmt.setInt(2, studentId);
            stmt.setInt(3, studentId);
            stmt.setInt(4, studentId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    double avg = rs.getDouble("avg_rating");
                    int count = rs.getInt("total_count");
                    return new double[]{avg, count};
                }
            }
        }
        return new double[]{0.0, 0};
    }

    /**
     * Retrieve all students.
     */
    public List<Student> getAllStudents() throws SQLException {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM STUDENT ORDER BY name ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Student s = mapRowToStudent(rs);
                students.add(s);
            }
        }
        return students;
    }

    /**
     * Map ResultSet row to Student object with average rating populated.
     */
    private Student mapRowToStudent(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setId(rs.getInt("student_id"));
        s.setName(rs.getString("name"));
        s.setEmail(rs.getString("email"));
        s.setPassword(rs.getString("password"));
        s.setDepartment(rs.getString("department"));
        s.setYear(rs.getInt("year"));

        // Fetch rating info
        double[] ratingInfo = getAverageRatingAndCount(s.getId());
        s.setAverageRating(ratingInfo[0]);
        s.setReviewCount((int) ratingInfo[1]);

        return s;
    }
}
