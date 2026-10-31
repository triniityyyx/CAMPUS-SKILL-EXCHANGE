package service;

import dao.RequestDAO;
import dao.ReviewDAO;
import dao.SessionDAO;
import model.LearningRequest;
import model.Review;
import model.Session;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.util.List;

/**
 * Service managing learning sessions and post-session reviews.
 */
public class SessionService {

    private final SessionDAO sessionDAO = new SessionDAO();
    private final RequestDAO requestDAO = new RequestDAO();
    private final ReviewDAO reviewDAO = new ReviewDAO();

    /**
     * Schedule a learning session for an accepted request.
     */
    public Session scheduleSession(int requestId, int studentId, Date date, Time time, String mode, String locationOrLink) throws Exception {
        LearningRequest req = requestDAO.getById(requestId);
        if (req == null) {
            throw new IllegalArgumentException("Learning request not found.");
        }

        // Student must be participant (sender or receiver)
        if (req.getSenderId() != studentId && req.getReceiverId() != studentId) {
            throw new IllegalArgumentException("You are not authorized to schedule a session for this request.");
        }

        // Request must be ACCEPTED or already SCHEDULED (rescheduling)
        if (!LearningRequest.STATUS_ACCEPTED.equalsIgnoreCase(req.getStatus()) &&
            !LearningRequest.STATUS_SCHEDULED.equalsIgnoreCase(req.getStatus())) {
            throw new IllegalArgumentException("Session can only be scheduled for ACCEPTED requests. Current status: " + req.getStatus());
        }

        if (date == null) {
            throw new IllegalArgumentException("Session date is required.");
        }
        if (time == null) {
            throw new IllegalArgumentException("Session time is required.");
        }
        if (mode == null || mode.trim().isEmpty()) {
            throw new IllegalArgumentException("Session mode (Online / Offline) is required.");
        }
        if (locationOrLink == null || locationOrLink.trim().isEmpty()) {
            throw new IllegalArgumentException("Location or meeting link cannot be empty.");
        }

        // Check if session record already exists for this request
        Session existing = sessionDAO.getByRequestId(requestId);
        if (existing != null) {
            existing.setSessionDate(date);
            existing.setSessionTime(time);
            existing.setMode(mode.trim());
            existing.setLocationOrLink(locationOrLink.trim());
            existing.setStatus(Session.STATUS_SCHEDULED);
            sessionDAO.updateStatus(existing.getSessionId(), Session.STATUS_SCHEDULED);
            requestDAO.updateStatus(requestId, LearningRequest.STATUS_SCHEDULED);
            return existing;
        }

        Session session = new Session();
        session.setRequestId(requestId);
        session.setSessionDate(date);
        session.setSessionTime(time);
        session.setMode(mode.trim());
        session.setLocationOrLink(locationOrLink.trim());
        session.setStatus(Session.STATUS_SCHEDULED);

        int sessionId = sessionDAO.createSession(session);
        if (sessionId <= 0) {
            throw new SQLException("Failed to schedule session in database.");
        }

        // Transition request status to SCHEDULED
        requestDAO.updateStatus(requestId, LearningRequest.STATUS_SCHEDULED);

        return sessionDAO.getById(sessionId);
    }

    /**
     * Mark a session as Completed.
     */
    public boolean completeSession(int sessionId, int studentId) throws Exception {
        Session session = sessionDAO.getById(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("Session not found.");
        }

        if (session.getSenderId() != studentId && session.getReceiverId() != studentId) {
            throw new IllegalArgumentException("You are not authorized to update this session.");
        }

        if (Session.STATUS_COMPLETED.equalsIgnoreCase(session.getStatus())) {
            throw new IllegalArgumentException("Session is already marked as Completed.");
        }

        boolean updated = sessionDAO.updateStatus(sessionId, Session.STATUS_COMPLETED);
        if (updated) {
            // Update request status to COMPLETED
            requestDAO.updateStatus(session.getRequestId(), LearningRequest.STATUS_COMPLETED);
        }
        return updated;
    }

    /**
     * Cancel a session.
     */
    public boolean cancelSession(int sessionId, int studentId) throws Exception {
        Session session = sessionDAO.getById(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("Session not found.");
        }

        if (session.getSenderId() != studentId && session.getReceiverId() != studentId) {
            throw new IllegalArgumentException("You are not authorized to cancel this session.");
        }

        return sessionDAO.updateStatus(sessionId, Session.STATUS_CANCELLED);
    }

    /**
     * Submit a rating and review for a completed session.
     */
    public Review submitReview(int sessionId, int reviewerId, int rating, String comment) throws Exception {
        Session session = sessionDAO.getById(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("Session not found.");
        }

        // Validate session is completed
        if (!Session.STATUS_COMPLETED.equalsIgnoreCase(session.getStatus())) {
            throw new IllegalArgumentException("Cannot review a session that is not completed! Current session status: " + session.getStatus());
        }

        // Reviewer must be participant
        if (session.getSenderId() != reviewerId && session.getReceiverId() != reviewerId) {
            throw new IllegalArgumentException("You were not a participant in this session.");
        }

        // Validate rating range
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5 stars.");
        }

        if (comment == null || comment.trim().isEmpty()) {
            throw new IllegalArgumentException("Review comment cannot be blank.");
        }

        // Check if student already reviewed this session
        if (reviewDAO.hasReviewed(sessionId, reviewerId)) {
            throw new IllegalArgumentException("You have already submitted a review for this session.");
        }

        Review review = new Review();
        review.setSessionId(sessionId);
        review.setReviewerId(reviewerId);
        review.setRating(rating);
        review.setComment(comment.trim());

        int reviewId = reviewDAO.createReview(review);
        if (reviewId <= 0) {
            throw new SQLException("Failed to save review in database.");
        }

        return review;
    }

    /**
     * Get all sessions for a student.
     */
    public List<Session> getSessionsForStudent(int studentId) throws SQLException {
        return sessionDAO.getSessionsForStudent(studentId);
    }

    /**
     * Get upcoming sessions count.
     */
    public int getUpcomingSessionsCount(int studentId) throws SQLException {
        return sessionDAO.getUpcomingSessionsCount(studentId);
    }

    /**
     * Get reviews received by a student.
     */
    public List<Review> getReviewsReceived(int studentId) throws SQLException {
        return reviewDAO.getReviewsReceived(studentId);
    }

    /**
     * Get reviews given by a student.
     */
    public List<Review> getReviewsGiven(int studentId) throws SQLException {
        return reviewDAO.getReviewsGiven(studentId);
    }

    /**
     * Check if user has reviewed session.
     */
    public boolean hasReviewed(int sessionId, int studentId) throws SQLException {
        return reviewDAO.hasReviewed(sessionId, studentId);
    }
}
