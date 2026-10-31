package model;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * Model representing a Review and Rating given for a completed learning session.
 */
public class Review {
    private int reviewId;
    private int sessionId;
    private int reviewerId;
    private int rating; // 1 to 5
    private String comment;
    private Timestamp createdAt;

    // Enriched fields for UI
    private String reviewerName;
    private String reviewerDepartment;
    private String skillName;
    private Date sessionDate;

    public Review() {
    }

    public Review(int reviewId, int sessionId, int reviewerId, int rating, String comment, Timestamp createdAt) {
        this.reviewId = reviewId;
        this.sessionId = sessionId;
        this.reviewerId = reviewerId;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public Review(int sessionId, int reviewerId, int rating, String comment) {
        this.sessionId = sessionId;
        this.reviewerId = reviewerId;
        this.rating = rating;
        this.comment = comment;
    }

    public int getReviewId() {
        return reviewId;
    }

    public void setReviewId(int reviewId) {
        this.reviewId = reviewId;
    }

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public int getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(int reviewerId) {
        this.reviewerId = reviewerId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public void setReviewerName(String reviewerName) {
        this.reviewerName = reviewerName;
    }

    public String getReviewerDepartment() {
        return reviewerDepartment;
    }

    public void setReviewerDepartment(String reviewerDepartment) {
        this.reviewerDepartment = reviewerDepartment;
    }

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public Date getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(Date sessionDate) {
        this.sessionDate = sessionDate;
    }

    public String getStarsDisplay() {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            if (i <= rating) {
                sb.append("★");
            } else {
                sb.append("☆");
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "Review{" +
                "rating=" + rating +
                ", comment='" + comment + '\'' +
                ", reviewer='" + reviewerName + '\'' +
                '}';
    }
}
