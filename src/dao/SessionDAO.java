package dao;

import model.Session;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Session entity.
 * Uses PreparedStatement for all queries.
 */
public class SessionDAO {

    /**
     * Create a scheduled session.
     */
    public int createSession(Session session) throws SQLException {
        String sql = "INSERT INTO SESSION (request_id, session_date, session_time, mode, location_or_link, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, session.getRequestId());
            stmt.setDate(2, session.getSessionDate());
            stmt.setTime(3, session.getSessionTime());
            stmt.setString(4, session.getMode());
            stmt.setString(5, session.getLocationOrLink().trim());
            stmt.setString(6, session.getStatus() != null ? session.getStatus() : Session.STATUS_SCHEDULED);

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        session.setSessionId(id);
                        return id;
                    }
                }
            }
        }
        return -1;
    }

    /**
     * Find session by ID.
     */
    public Session getById(int sessionId) throws SQLException {
        String sql = "SELECT s.*, lr.sender_id, lr.receiver_id, sk.skill_name, " +
                     "snd.name AS sender_name, rcv.name AS receiver_name, " +
                     "(SELECT COUNT(*) FROM REVIEW r WHERE r.session_id = s.session_id) AS review_count " +
                     "FROM SESSION s " +
                     "JOIN LEARNING_REQUEST lr ON s.request_id = lr.request_id " +
                     "JOIN SKILL sk ON lr.skill_id = sk.skill_id " +
                     "JOIN STUDENT snd ON lr.sender_id = snd.student_id " +
                     "JOIN STUDENT rcv ON lr.receiver_id = rcv.student_id " +
                     "WHERE s.session_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sessionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToSession(rs);
                }
            }
        }
        return null;
    }

    /**
     * Find session by Request ID.
     */
    public Session getByRequestId(int requestId) throws SQLException {
        String sql = "SELECT s.*, lr.sender_id, lr.receiver_id, sk.skill_name, " +
                     "snd.name AS sender_name, rcv.name AS receiver_name, " +
                     "(SELECT COUNT(*) FROM REVIEW r WHERE r.session_id = s.session_id) AS review_count " +
                     "FROM SESSION s " +
                     "JOIN LEARNING_REQUEST lr ON s.request_id = lr.request_id " +
                     "JOIN SKILL sk ON lr.skill_id = sk.skill_id " +
                     "JOIN STUDENT snd ON lr.sender_id = snd.student_id " +
                     "JOIN STUDENT rcv ON lr.receiver_id = rcv.student_id " +
                     "WHERE s.request_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, requestId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToSession(rs);
                }
            }
        }
        return null;
    }

    /**
     * Update session status (e.g. Scheduled -> Completed or Cancelled).
     */
    public boolean updateStatus(int sessionId, String status) throws SQLException {
        String sql = "UPDATE SESSION SET status = ? WHERE session_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, sessionId);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Get all sessions involving the specified student (as sender/learner or receiver/teacher).
     */
    public List<Session> getSessionsForStudent(int studentId) throws SQLException {
        List<Session> list = new ArrayList<>();
        String sql = "SELECT s.*, lr.sender_id, lr.receiver_id, sk.skill_name, " +
                     "snd.name AS sender_name, rcv.name AS receiver_name, " +
                     "(SELECT COUNT(*) FROM REVIEW r WHERE r.session_id = s.session_id) AS review_count " +
                     "FROM SESSION s " +
                     "JOIN LEARNING_REQUEST lr ON s.request_id = lr.request_id " +
                     "JOIN SKILL sk ON lr.skill_id = sk.skill_id " +
                     "JOIN STUDENT snd ON lr.sender_id = snd.student_id " +
                     "JOIN STUDENT rcv ON lr.receiver_id = rcv.student_id " +
                     "WHERE lr.sender_id = ? OR lr.receiver_id = ? " +
                     "ORDER BY s.session_date DESC, s.session_time DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            stmt.setInt(2, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToSession(rs));
                }
            }
        }
        return list;
    }

    /**
     * Get upcoming active sessions count for a student.
     */
    public int getUpcomingSessionsCount(int studentId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM SESSION s " +
                     "JOIN LEARNING_REQUEST lr ON s.request_id = lr.request_id " +
                     "WHERE (lr.sender_id = ? OR lr.receiver_id = ?) AND s.status = 'Scheduled'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            stmt.setInt(2, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    private Session mapRowToSession(ResultSet rs) throws SQLException {
        Session s = new Session();
        s.setSessionId(rs.getInt("session_id"));
        s.setRequestId(rs.getInt("request_id"));
        s.setSessionDate(rs.getDate("session_date"));
        s.setSessionTime(rs.getTime("session_time"));
        s.setMode(rs.getString("mode"));
        s.setLocationOrLink(rs.getString("location_or_link"));
        s.setStatus(rs.getString("status"));
        s.setCreatedAt(rs.getTimestamp("created_at"));

        s.setSenderId(rs.getInt("sender_id"));
        s.setReceiverId(rs.getInt("receiver_id"));
        s.setSkillName(rs.getString("skill_name"));
        s.setSenderName(rs.getString("sender_name"));
        s.setReceiverName(rs.getString("receiver_name"));
        s.setReviewed(rs.getInt("review_count") > 0);

        return s;
    }
}
