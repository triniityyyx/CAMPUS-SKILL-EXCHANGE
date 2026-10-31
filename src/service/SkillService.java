package service;

import dao.SkillDAO;
import model.Skill;
import model.StudentSkill;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

/**
 * Service managing skill catalog and student skill assignments.
 */
public class SkillService {

    private final SkillDAO skillDAO = new SkillDAO();

    public static final List<String> DEFAULT_CATEGORIES = Arrays.asList(
            "Programming",
            "Computer Science",
            "Web & App",
            "Design",
            "Data & AI",
            "Media & Arts",
            "Soft Skills",
            "Business",
            "Engineering",
            "General"
    );

    /**
     * Add skill to student (OFFERED or WANTED).
     */
    public boolean addSkillToStudent(int studentId, String skillName, String category, String skillType) throws Exception {
        if (skillName == null || skillName.trim().isEmpty()) {
            throw new IllegalArgumentException("Skill name cannot be empty.");
        }
        if (skillType == null || (!skillType.equalsIgnoreCase(StudentSkill.TYPE_OFFERED) && !skillType.equalsIgnoreCase(StudentSkill.TYPE_WANTED))) {
            throw new IllegalArgumentException("Invalid skill type: must be OFFERED or WANTED.");
        }

        skillName = skillName.trim();
        category = (category == null || category.trim().isEmpty()) ? "General" : category.trim();

        Skill skill = skillDAO.findOrCreateSkill(skillName, category);
        if (skill == null) {
            throw new SQLException("Could not register skill: " + skillName);
        }

        if (skillDAO.hasStudentSkill(studentId, skill.getSkillId(), skillType.toUpperCase())) {
            throw new IllegalArgumentException("You have already added '" + skillName + "' to your " + skillType.toLowerCase() + " skills.");
        }

        return skillDAO.addStudentSkill(studentId, skill.getSkillId(), skillType.toUpperCase());
    }

    /**
     * Remove skill from student.
     */
    public boolean removeSkillFromStudent(int studentId, int skillId, String skillType) throws Exception {
        return skillDAO.removeStudentSkill(studentId, skillId, skillType);
    }

    /**
     * Get skills offered by student.
     */
    public List<StudentSkill> getOfferedSkills(int studentId) throws SQLException {
        return skillDAO.getStudentSkills(studentId, StudentSkill.TYPE_OFFERED);
    }

    /**
     * Get skills student wants to learn.
     */
    public List<StudentSkill> getWantedSkills(int studentId) throws SQLException {
        return skillDAO.getStudentSkills(studentId, StudentSkill.TYPE_WANTED);
    }

    /**
     * Get all catalog skills.
     */
    public List<Skill> getAllSkills() throws SQLException {
        return skillDAO.getAllSkills();
    }

    /**
     * Search skills by query.
     */
    public List<Skill> searchSkills(String query) throws SQLException {
        if (query == null || query.trim().isEmpty()) {
            return getAllSkills();
        }
        return skillDAO.searchSkills(query);
    }

    /**
     * Predefined skill categories for UI dropdown.
     */
    public List<String> getCategories() {
        return DEFAULT_CATEGORIES;
    }
}
