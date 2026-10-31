package dao;

import model.Skill;
import model.Student;
import model.StudentSkill;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Skill and StudentSkill entities.
 * Uses PreparedStatement for all queries.
 */
public class SkillDAO {

    private final StudentDAO studentDAO = new StudentDAO();

    /**
     * Get all predefined skills.
     */
    public List<Skill> getAllSkills() throws SQLException {
        List<Skill> skills = new ArrayList<>();
        String sql = "SELECT * FROM SKILL ORDER BY category ASC, skill_name ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                skills.add(mapRowToSkill(rs));
            }
        }
        return skills;
    }

    /**
     * Find skill by ID.
     */
    public Skill findById(int skillId) throws SQLException {
        String sql = "SELECT * FROM SKILL WHERE skill_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, skillId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToSkill(rs);
                }
            }
        }
        return null;
    }

    /**
     * Find skill by exact name (case-insensitive).
     */
    public Skill findByName(String name) throws SQLException {
        String sql = "SELECT * FROM SKILL WHERE LOWER(skill_name) = LOWER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToSkill(rs);
                }
            }
        }
        return null;
    }

    /**
     * Find or create skill.
     */
    public Skill findOrCreateSkill(String name, String category) throws SQLException {
        Skill existing = findByName(name);
        if (existing != null) {
            return existing;
        }

        String sql = "INSERT INTO SKILL (skill_name, category) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, name.trim());
            stmt.setString(2, (category != null && !category.trim().isEmpty()) ? category.trim() : "General");
            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return new Skill(rs.getInt(1), name.trim(), (category != null ? category.trim() : "General"));
                    }
                }
            }
        }
        return findByName(name);
    }

    /**
     * Get student's skills filtered by type (OFFERED or WANTED).
     */
    public List<StudentSkill> getStudentSkills(int studentId, String skillType) throws SQLException {
        List<StudentSkill> list = new ArrayList<>();
        String sql = "SELECT ss.student_id, ss.skill_id, ss.skill_type, s.skill_name, s.category " +
                     "FROM STUDENT_SKILL ss " +
                     "JOIN SKILL s ON ss.skill_id = s.skill_id " +
                     "WHERE ss.student_id = ? AND ss.skill_type = ? " +
                     "ORDER BY s.skill_name ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            stmt.setString(2, skillType);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    StudentSkill ss = new StudentSkill(
                            rs.getInt("student_id"),
                            rs.getInt("skill_id"),
                            rs.getString("skill_type"),
                            rs.getString("skill_name"),
                            rs.getString("category")
                    );
                    list.add(ss);
                }
            }
        }
        return list;
    }

    /**
     * Check if student already has this skill mapping.
     */
    public boolean hasStudentSkill(int studentId, int skillId, String skillType) throws SQLException {
        String sql = "SELECT COUNT(*) FROM STUDENT_SKILL WHERE student_id = ? AND skill_id = ? AND skill_type = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            stmt.setInt(2, skillId);
            stmt.setString(3, skillType);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Add skill to student (OFFERED or WANTED).
     */
    public boolean addStudentSkill(int studentId, int skillId, String skillType) throws SQLException {
        if (hasStudentSkill(studentId, skillId, skillType)) {
            return false;
        }
        String sql = "INSERT INTO STUDENT_SKILL (student_id, skill_id, skill_type) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            stmt.setInt(2, skillId);
            stmt.setString(3, skillType);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Remove skill from student.
     */
    public boolean removeStudentSkill(int studentId, int skillId, String skillType) throws SQLException {
        String sql = "DELETE FROM STUDENT_SKILL WHERE student_id = ? AND skill_id = ? AND skill_type = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            stmt.setInt(2, skillId);
            stmt.setString(3, skillType);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Get count of skills for a student by type.
     */
    public int getSkillCountByStudent(int studentId, String skillType) throws SQLException {
        String sql = "SELECT COUNT(*) FROM STUDENT_SKILL WHERE student_id = ? AND skill_type = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            stmt.setString(2, skillType);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /**
     * Search skills matching a search query (substring / keyword).
     */
    public List<Skill> searchSkills(String keyword) throws SQLException {
        List<Skill> results = new ArrayList<>();
        String sql = "SELECT * FROM SKILL WHERE LOWER(skill_name) LIKE LOWER(?) OR LOWER(category) LIKE LOWER(?) ORDER BY skill_name ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String pattern = "%" + keyword.trim() + "%";
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRowToSkill(rs));
                }
            }
        }
        return results;
    }

    /**
     * Get all students offering a specific skill, excluding current logged-in student.
     */
    public List<Student> getStudentsOfferingSkill(int skillId, int excludeStudentId) throws SQLException {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT st.* FROM STUDENT st " +
                     "JOIN STUDENT_SKILL ss ON st.student_id = ss.student_id " +
                     "WHERE ss.skill_id = ? AND ss.skill_type = 'OFFERED' AND st.student_id != ? " +
                     "ORDER BY st.name ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, skillId);
            stmt.setInt(2, excludeStudentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    students.add(studentDAO.findById(rs.getInt("student_id")));
                }
            }
        }
        return students;
    }

    private Skill mapRowToSkill(ResultSet rs) throws SQLException {
        return new Skill(
                rs.getInt("skill_id"),
                rs.getString("skill_name"),
                rs.getString("category")
        );
    }
}
