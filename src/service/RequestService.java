package service;

import dao.RequestDAO;
import dao.SkillDAO;
import model.LearningRequest;

import java.sql.SQLException;
import java.util.List;

/**
 * Service managing learning requests between students.
 */
public class RequestService {

    private final RequestDAO requestDAO = new RequestDAO();
    private final SkillDAO skillDAO = new SkillDAO();

    /**
     * Send a learning request from current student to another student.
     */
    public LearningRequest sendRequest(int senderId, int receiverId, int skillId) throws Exception {
        if (senderId == receiverId) {
            throw new IllegalArgumentException("You cannot send a learning request to yourself!");
        }

        // Check if student offers this skill
        if (!skillDAO.hasStudentSkill(receiverId, skillId, "OFFERED")) {
            throw new IllegalArgumentException("The selected student is not currently offering this skill.");
        }

        // Check if active request already exists
        if (requestDAO.hasActiveRequest(senderId, receiverId, skillId)) {
            throw new IllegalArgumentException("An active or pending request for this skill already exists with this student.");
        }

        LearningRequest req = new LearningRequest();
        req.setSenderId(senderId);
        req.setReceiverId(receiverId);
        req.setSkillId(skillId);
        req.setStatus(LearningRequest.STATUS_PENDING);

        int id = requestDAO.createRequest(req);
        if (id <= 0) {
            throw new SQLException("Failed to create learning request.");
        }

        return requestDAO.getById(id);
    }

    /**
     * Accept a received learning request.
     */
    public boolean acceptRequest(int requestId, int currentStudentId) throws Exception {
        LearningRequest req = requestDAO.getById(requestId);
        if (req == null) {
            throw new IllegalArgumentException("Request not found.");
        }
        if (req.getReceiverId() != currentStudentId) {
            throw new IllegalArgumentException("You are not authorized to respond to this request.");
        }
        if (!LearningRequest.STATUS_PENDING.equalsIgnoreCase(req.getStatus())) {
            throw new IllegalArgumentException("Only pending requests can be accepted. Current status: " + req.getStatus());
        }

        return requestDAO.updateStatus(requestId, LearningRequest.STATUS_ACCEPTED);
    }

    /**
     * Reject a received learning request.
     */
    public boolean rejectRequest(int requestId, int currentStudentId) throws Exception {
        LearningRequest req = requestDAO.getById(requestId);
        if (req == null) {
            throw new IllegalArgumentException("Request not found.");
        }
        if (req.getReceiverId() != currentStudentId) {
            throw new IllegalArgumentException("You are not authorized to respond to this request.");
        }
        if (!LearningRequest.STATUS_PENDING.equalsIgnoreCase(req.getStatus())) {
            throw new IllegalArgumentException("Only pending requests can be rejected. Current status: " + req.getStatus());
        }

        return requestDAO.updateStatus(requestId, LearningRequest.STATUS_REJECTED);
    }

    /**
     * Get sent requests.
     */
    public List<LearningRequest> getSentRequests(int senderId) throws SQLException {
        return requestDAO.getSentRequests(senderId);
    }

    /**
     * Get received requests.
     */
    public List<LearningRequest> getReceivedRequests(int receiverId) throws SQLException {
        return requestDAO.getReceivedRequests(receiverId);
    }

    /**
     * Get pending count for dashboard.
     */
    public int getPendingCount(int receiverId) throws SQLException {
        return requestDAO.getPendingReceivedCount(receiverId);
    }
}
