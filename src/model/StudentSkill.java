package model;

/**
 * Model representing a mapping between a Student and a Skill.
 * Type can be: 'OFFERED' or 'WANTED'
 */
public class StudentSkill {
    public static final String TYPE_OFFERED = "OFFERED";
    public static final String TYPE_WANTED = "WANTED";

    private int studentId;
    private int skillId;
    private String skillType; // OFFERED or WANTED
    private String skillName;
    private String category;

    public StudentSkill() {
    }

    public StudentSkill(int studentId, int skillId, String skillType) {
        this.studentId = studentId;
        this.skillId = skillId;
        this.skillType = skillType;
    }

    public StudentSkill(int studentId, int skillId, String skillType, String skillName, String category) {
        this.studentId = studentId;
        this.skillId = skillId;
        this.skillType = skillType;
        this.skillName = skillName;
        this.category = category;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getSkillId() {
        return skillId;
    }

    public void setSkillId(int skillId) {
        this.skillId = skillId;
    }

    public String getSkillType() {
        return skillType;
    }

    public void setSkillType(String skillType) {
        this.skillType = skillType;
    }

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isOffered() {
        return TYPE_OFFERED.equalsIgnoreCase(skillType);
    }

    public boolean isWanted() {
        return TYPE_WANTED.equalsIgnoreCase(skillType);
    }

    @Override
    public String toString() {
        return skillName + " [" + skillType + "]";
    }
}
