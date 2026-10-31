package dao;

import model.LearningRequest;
import model.Review;
import model.Session;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Review entity.
 * Uses PreparedStatement for all queries.
 */
public class ReviewDAO {

    private final RequestDAO requestDAO = new RequestDAO();

    /**
     * Create a review for a completed session.
     * Also updates the parent LearningRequest status to REVIEWED.
     */
    public int createReview(Review review) throws SQLException {
        String sql = "INSERT INTO REVIEW (session_id, reviewer_id, rating, comment) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, review.getSessionId());
            stmt.setInt(2, review.getReviewerId());
            stmt.setInt(3, review.getRating());
            stmt.setString(4, review.getComment().trim());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        review.setReviewId(id);

                        // Find request_id from session and update request status to REVIEWED
                        updateRequestStatusForSession(review.getSessionId(), LearningRequest.STATUS_REVIEWED);

                        return id;
                    }
                }
            }
        }
        return -1;
    }

    private void updateRequestStatusForSession(int sessionId, String status) {
        String query = "SELECT request_id FROM SESSION WHERE session_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, sessionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int requestId = rs.getInt("request_id");
                    requestDAO.updateStatus(requestId, status);
                }
            }
        } catch (SQLException e) {
            System.err.println("Could not update request status to REVIEWED: " + e.getMessage());
        }
    }

    /**
     * Check if a student has already reviewed a session.
     */
    public boolean hasReviewed(int sessionId, int reviewerId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM REVIEW WHERE session_id = ? AND reviewer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sessionId);
            stmt.setInt(2, reviewerId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Get reviews received by a student (where other students reviewed them).
     */
    public List<Review> getReviewsReceived(int studentId) throws SQLException {
        List<Review> list = new ArrayList<>();
        String sql = "SELECT r.*, s.session_date, sk.skill_name, " +
                     "rev.name AS reviewer_name, rev.department AS reviewer_dept " +
                     "FROM REVIEW r " +
                     "JOIN SESSION s ON r.session_id = s.session_id " +
                     "JOIN LEARNING_REQUEST lr ON s.request_id = lr.request_id " +
                     "JOIN SKILL sk ON lr.skill_id = sk.skill_id " +
                     "JOIN STUDENT rev ON r.reviewer_id = rev.student_id " +
                     "WHERE ((lr.receiver_id = ? AND r.reviewer_id != ?) OR (lr.sender_id = ? AND r.reviewer_id != ?)) " +
                     "ORDER BY r.created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            stmt.setInt(2, studentId);
            stmt.setInt(3, studentId);
            stmt.setInt(4, studentId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToReview(rs));
                }
            }
        }
        return list;
    }

    /**
     * Get reviews given by a student.
     */
    public List<Review> getReviewsGiven(int studentId) throws SQLException {
        List<Review> list = new ArrayList<>();
        String sql = "SELECT r.*, s.session_date, sk.skill_name, " +
                     "rev.name AS reviewer_name, rev.department AS reviewer_dept " +
                     "FROM REVIEW r " +
                     "JOIN SESSION s ON r.session_id = s.session_id " +
                     "JOIN LEARNING_REQUEST lr ON s.request_id = lr.request_id " +
                     "JOIN SKILL sk ON lr.skill_id = sk.skill_id " +
                     "JOIN STUDENT rev ON r.reviewer_id = rev.student_id " +
                     "WHERE r.reviewer_id = ? " +
                     "ORDER BY r.created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToReview(rs));
                }
            }
        }
        return list;
    }

    private Review mapRowToReview(ResultSet rs) throws SQLException {
        Review r = new Review();
        r.setReviewId(rs.getInt("review_id"));
        r.setSessionId(rs.getInt("session_id"));
        r.setReviewerId(rs.getInt("reviewer_id"));
        r.setRating(rs.getInt("rating"));
        r.setComment(rs.getString("comment"));
        r.setCreatedAt(rs.getTimestamp("created_at"));

        r.setReviewerName(rs.getString("reviewer_name"));
        r.setReviewerDepartment(rs.getString("reviewer_dept"));
        r.setSkillName(rs.getString("skill_name"));
        r.setSessionDate(rs.getDate("session_date"));

        return r;
    }
}
