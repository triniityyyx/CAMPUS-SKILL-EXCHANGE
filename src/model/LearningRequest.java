package model;

import java.sql.Timestamp;

/**
 * Model representing a Learning Request sent between students.
 * Lifecycle: PENDING -> ACCEPTED / REJECTED -> SCHEDULED -> COMPLETED -> REVIEWED
 */
public class LearningRequest {
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_ACCEPTED = "ACCEPTED";
    public static final String STATUS_REJECTED = "REJECTED";
    public static final String STATUS_SCHEDULED = "SCHEDULED";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_REVIEWED = "REVIEWED";

    private int requestId;
    private int senderId;
    private int receiverId;
    private int skillId;
    private String status;
    private Timestamp createdAt;

    // Enriched fields for easy UI rendering
    private String senderName;
    private String senderDepartment;
    private int senderYear;
    private String receiverName;
    private String receiverDepartment;
    private int receiverYear;
    private String skillName;
    private String skillCategory;

    public LearningRequest() {
        this.status = STATUS_PENDING;
    }

    public LearningRequest(int requestId, int senderId, int receiverId, int skillId, String status, Timestamp createdAt) {
        this.requestId = requestId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.skillId = skillId;
        this.status = status;
        this.createdAt = createdAt;
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(int receiverId) {
        this.receiverId = receiverId;
    }

    public int getSkillId() {
        return skillId;
    }

    public void setSkillId(int skillId) {
        this.skillId = skillId;
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

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderDepartment() {
        return senderDepartment;
    }

    public void setSenderDepartment(String senderDepartment) {
        this.senderDepartment = senderDepartment;
    }

    public int getSenderYear() {
        return senderYear;
    }

    public void setSenderYear(int senderYear) {
        this.senderYear = senderYear;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverDepartment() {
        return receiverDepartment;
    }

    public void setReceiverDepartment(String receiverDepartment) {
        this.receiverDepartment = receiverDepartment;
    }

    public int getReceiverYear() {
        return receiverYear;
    }

    public void setReceiverYear(int receiverYear) {
        this.receiverYear = receiverYear;
    }

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public String getSkillCategory() {
        return skillCategory;
    }

    public void setSkillCategory(String skillCategory) {
        this.skillCategory = skillCategory;
    }

    @Override
    public String toString() {
        return "LearningRequest{" +
                "id=" + requestId +
                ", skill='" + skillName + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
