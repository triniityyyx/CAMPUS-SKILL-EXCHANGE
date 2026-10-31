package dao;

import model.LearningRequest;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for LearningRequest entity.
 * Uses PreparedStatement for all queries.
 */
public class RequestDAO {

    /**
     * Create a new learning request.
     */
    public int createRequest(LearningRequest req) throws SQLException {
        String sql = "INSERT INTO LEARNING_REQUEST (sender_id, receiver_id, skill_id, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, req.getSenderId());
            stmt.setInt(2, req.getReceiverId());
            stmt.setInt(3, req.getSkillId());
            stmt.setString(4, req.getStatus() != null ? req.getStatus() : LearningRequest.STATUS_PENDING);

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        req.setRequestId(id);
                        return id;
                    }
                }
            }
        }
        return -1;
    }

    /**
     * Check if a pending or active request already exists between sender and receiver for this skill.
     */
    public boolean hasActiveRequest(int senderId, int receiverId, int skillId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM LEARNING_REQUEST " +
                     "WHERE sender_id = ? AND receiver_id = ? AND skill_id = ? " +
                     "AND status IN ('PENDING', 'ACCEPTED', 'SCHEDULED')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, senderId);
            stmt.setInt(2, receiverId);
            stmt.setInt(3, skillId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Update request status.
     * Transitions: PENDING -> ACCEPTED / REJECTED -> SCHEDULED -> COMPLETED -> REVIEWED
     */
    public boolean updateStatus(int requestId, String status) throws SQLException {
        String sql = "UPDATE LEARNING_REQUEST SET status = ? WHERE request_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, requestId);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Find request by ID.
     */
    public LearningRequest getById(int requestId) throws SQLException {
        String sql = "SELECT lr.*, s.skill_name, s.category, " +
                     "snd.name AS sender_name, snd.department AS sender_dept, snd.year AS sender_year, " +
                     "rcv.name AS receiver_name, rcv.department AS receiver_dept, rcv.year AS receiver_year " +
                     "FROM LEARNING_REQUEST lr " +
                     "JOIN SKILL s ON lr.skill_id = s.skill_id " +
                     "JOIN STUDENT snd ON lr.sender_id = snd.student_id " +
                     "JOIN STUDENT rcv ON lr.receiver_id = rcv.student_id " +
                     "WHERE lr.request_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, requestId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToRequest(rs);
                }
            }
        }
        return null;
    }

    /**
     * Get all requests sent by student.
     */
    public List<LearningRequest> getSentRequests(int senderId) throws SQLException {
        List<LearningRequest> list = new ArrayList<>();
        String sql = "SELECT lr.*, s.skill_name, s.category, " +
                     "snd.name AS sender_name, snd.department AS sender_dept, snd.year AS sender_year, " +
                     "rcv.name AS receiver_name, rcv.department AS receiver_dept, rcv.year AS receiver_year " +
                     "FROM LEARNING_REQUEST lr " +
                     "JOIN SKILL s ON lr.skill_id = s.skill_id " +
                     "JOIN STUDENT snd ON lr.sender_id = snd.student_id " +
                     "JOIN STUDENT rcv ON lr.receiver_id = rcv.student_id " +
                     "WHERE lr.sender_id = ? " +
                     "ORDER BY lr.created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, senderId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToRequest(rs));
                }
            }
        }
        return list;
    }

    /**
     * Get all requests received by student.
     */
    public List<LearningRequest> getReceivedRequests(int receiverId) throws SQLException {
        List<LearningRequest> list = new ArrayList<>();
        String sql = "SELECT lr.*, s.skill_name, s.category, " +
                     "snd.name AS sender_name, snd.department AS sender_dept, snd.year AS sender_year, " +
                     "rcv.name AS receiver_name, rcv.department AS receiver_dept, rcv.year AS receiver_year " +
                     "FROM LEARNING_REQUEST lr " +
                     "JOIN SKILL s ON lr.skill_id = s.skill_id " +
                     "JOIN STUDENT snd ON lr.sender_id = snd.student_id " +
                     "JOIN STUDENT rcv ON lr.receiver_id = rcv.student_id " +
                     "WHERE lr.receiver_id = ? " +
                     "ORDER BY lr.created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, receiverId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToRequest(rs));
                }
            }
        }
        return list;
    }

    /**
     * Get count of pending received requests for dashboard.
     */
    public int getPendingReceivedCount(int receiverId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM LEARNING_REQUEST WHERE receiver_id = ? AND status = 'PENDING'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, receiverId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    private LearningRequest mapRowToRequest(ResultSet rs) throws SQLException {
        LearningRequest req = new LearningRequest();
        req.setRequestId(rs.getInt("request_id"));
        req.setSenderId(rs.getInt("sender_id"));
        req.setReceiverId(rs.getInt("receiver_id"));
        req.setSkillId(rs.getInt("skill_id"));
        req.setStatus(rs.getString("status"));
        req.setCreatedAt(rs.getTimestamp("created_at"));

        req.setSkillName(rs.getString("skill_name"));
        req.setSkillCategory(rs.getString("category"));
        req.setSenderName(rs.getString("sender_name"));
        req.setSenderDepartment(rs.getString("sender_dept"));
        req.setSenderYear(rs.getInt("sender_year"));
        req.setReceiverName(rs.getString("receiver_name"));
        req.setReceiverDepartment(rs.getString("receiver_dept"));
        req.setReceiverYear(rs.getInt("receiver_year"));

        return req;
    }
}
