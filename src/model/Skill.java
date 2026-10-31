package model;

import java.util.Objects;

/**
 * Skill entity representing a subject or capability.
 */
public class Skill {
    private int skillId;
    private String skillName;
    private String category;

    public Skill() {
    }

    public Skill(int skillId, String skillName, String category) {
        this.skillId = skillId;
        this.skillName = skillName;
        this.category = category;
    }

    public Skill(String skillName, String category) {
        this.skillName = skillName;
        this.category = category;
    }

    public int getSkillId() {
        return skillId;
    }

    public void setSkillId(int skillId) {
        this.skillId = skillId;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Skill skill = (Skill) o;
        return skillId == skill.skillId || Objects.equals(skillName, skill.skillName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(skillId, skillName);
    }

    @Override
    public String toString() {
        return skillName + (category != null && !category.isEmpty() ? " (" + category + ")" : "");
    }
}
