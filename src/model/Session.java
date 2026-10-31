package model;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

/**
 * Model representing a Scheduled Learning Session.
 * Statuses: Scheduled, Completed, Cancelled
 */
public class Session {
    public static final String STATUS_SCHEDULED = "Scheduled";
    public static final String STATUS_COMPLETED = "Completed";
    public static final String STATUS_CANCELLED = "Cancelled";

    public static final String MODE_ONLINE = "Online";
    public static final String MODE_OFFLINE = "Offline";

    private int sessionId;
    private int requestId;
    private Date sessionDate;
    private Time sessionTime;
    private String mode;
    private String locationOrLink;
    private String status;
    private Timestamp createdAt;

    // Enriched fields for UI
    private String skillName;
    private int senderId;
    private String senderName;
    private int receiverId;
    private String receiverName;
    private boolean isReviewed;

    public Session() {
        this.status = STATUS_SCHEDULED;
    }

    public Session(int sessionId, int requestId, Date sessionDate, Time sessionTime, String mode, String locationOrLink, String status) {
        this.sessionId = sessionId;
        this.requestId = requestId;
        this.sessionDate = sessionDate;
        this.sessionTime = sessionTime;
        this.mode = mode;
        this.locationOrLink = locationOrLink;
        this.status = status;
    }

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public Date getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(Date sessionDate) {
        this.sessionDate = sessionDate;
    }

    public Time getSessionTime() {
        return sessionTime;
    }

    public void setSessionTime(Time sessionTime) {
        this.sessionTime = sessionTime;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getLocationOrLink() {
        return locationOrLink;
    }

    public void setLocationOrLink(String locationOrLink) {
        this.locationOrLink = locationOrLink;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(int receiverId) {
        this.receiverId = receiverId;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public boolean isReviewed() {
        return isReviewed;
    }

    public void setReviewed(boolean reviewed) {
        isReviewed = reviewed;
    }

    public boolean isCompleted() {
        return STATUS_COMPLETED.equalsIgnoreCase(status);
    }

    public boolean isScheduled() {
        return STATUS_SCHEDULED.equalsIgnoreCase(status);
    }

    @Override
    public String toString() {
        return "Session{" +
                "id=" + sessionId +
                ", skill='" + skillName + '\'' +
                ", date=" + sessionDate +
                ", time=" + sessionTime +
                ", status='" + status + '\'' +
                '}';
    }
}
