package model;

/**
 * Encapsulates the calculation details of Rule-Based Skill Matching.
 * Formula:
 * - Skill Match: 70%
 * - Category Match: 15%
 * - Rating Score: 15%
 * Total Score = 100%
 */
public class SkillMatchResult implements Comparable<SkillMatchResult> {
    private Student student;
    private Skill skill;
    private double skillMatchWeight;      // out of 70
    private double categoryMatchWeight;   // out of 15
    private double ratingWeight;          // out of 15
    private double totalScore;            // out of 100
    private String matchReason;

    public SkillMatchResult(Student student, Skill skill, double skillMatchWeight, double categoryMatchWeight, double ratingWeight, String matchReason) {
        this.student = student;
        this.skill = skill;
        this.skillMatchWeight = skillMatchWeight;
        this.categoryMatchWeight = categoryMatchWeight;
        this.ratingWeight = ratingWeight;
        this.totalScore = skillMatchWeight + categoryMatchWeight + ratingWeight;
        this.matchReason = matchReason;
    }

    public Student getStudent() {
        return student;
    }

    public Skill getSkill() {
        return skill;
    }

    public double getSkillMatchWeight() {
        return skillMatchWeight;
    }

    public double getCategoryMatchWeight() {
        return categoryMatchWeight;
    }

    public double getRatingWeight() {
        return ratingWeight;
    }

    public double getTotalScore() {
        return totalScore;
    }

    public String getFormattedTotalScore() {
        return String.format("%.1f%%", totalScore);
    }

    public String getMatchReason() {
        return matchReason;
    }

    @Override
    public int compareTo(SkillMatchResult other) {
        // Descending order of total score (highest match first)
        return Double.compare(other.totalScore, this.totalScore);
    }
}
