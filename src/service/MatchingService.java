package service;

import dao.SkillDAO;
import dao.StudentDAO;
import model.Skill;
import model.SkillMatchResult;
import model.Student;
import model.StudentSkill;

import java.sql.SQLException;
import java.util.*;

/**
 * Rule-Based Skill Matching Service.
 * <p>
 * NOTE: This is a deterministic, rule-based algorithmic scoring engine.
 * It is NOT Artificial Intelligence or Machine Learning.
 * <p>
 * Scoring Rules:
 * 1. Skill Match:    Up to 70% weight
 * 2. Category Match: Up to 15% weight
 * 3. Rating Score:   Up to 15% weight
 * Total Max Score:  100%
 */
public class MatchingService {

    private final SkillDAO skillDAO = new SkillDAO();
    private final StudentDAO studentDAO = new StudentDAO();

    /**
     * Search and match students offering skills matching the query string.
     * Results are sorted by highest rule-based match score.
     *
     * @param currentStudentId Currently logged-in student (excluded from results)
     * @param skillQuery       Search query keyword or skill name
     * @return List of SkillMatchResult sorted descending by match score
     */
    public List<SkillMatchResult> findMatches(int currentStudentId, String skillQuery) throws SQLException {
        List<SkillMatchResult> results = new ArrayList<>();

        String cleanQuery = (skillQuery == null) ? "" : skillQuery.trim().toLowerCase();

        // 1. Retrieve current student's skills for mutual exchange matching
        List<StudentSkill> myOffered = skillDAO.getStudentSkills(currentStudentId, StudentSkill.TYPE_OFFERED);
        List<StudentSkill> myWanted = skillDAO.getStudentSkills(currentStudentId, StudentSkill.TYPE_WANTED);

        Set<Integer> myOfferedSkillIds = new HashSet<>();
        for (StudentSkill ss : myOffered) myOfferedSkillIds.add(ss.getSkillId());

        Set<String> myWantedCategories = new HashSet<>();
        for (StudentSkill ss : myWanted) {
            if (ss.getCategory() != null) myWantedCategories.add(ss.getCategory().toLowerCase());
        }

        // 2. Find matching skills
        List<Skill> matchedSkills = cleanQuery.isEmpty() ? skillDAO.getAllSkills() : skillDAO.searchSkills(cleanQuery);

        // Keep track of visited (studentId, skillId) pairs to prevent duplicates
        Set<String> visited = new HashSet<>();

        for (Skill skill : matchedSkills) {
            // Find students offering this specific skill
            List<Student> offeringStudents = skillDAO.getStudentsOfferingSkill(skill.getSkillId(), currentStudentId);

            for (Student candidate : offeringStudents) {
                String pairKey = candidate.getId() + "_" + skill.getSkillId();
                if (visited.contains(pairKey)) {
                    continue;
                }
                visited.add(pairKey);

                // --- 1. SKILL MATCH SCORE (Max 70%) ---
                double skillScore;
                String skillReason;
                String skillLower = skill.getSkillName().toLowerCase();

                if (cleanQuery.isEmpty()) {
                    skillScore = 65.0;
                    skillReason = "Catalog match";
                } else if (skillLower.equals(cleanQuery)) {
                    skillScore = 70.0;
                    skillReason = "Exact skill match (70%)";
                } else if (skillLower.startsWith(cleanQuery)) {
                    skillScore = 65.0;
                    skillReason = "Prefix skill match (65%)";
                } else {
                    skillScore = 58.0;
                    skillReason = "Partial keyword match (58%)";
                }

                // --- 2. CATEGORY & MUTUAL EXCHANGE MATCH SCORE (Max 15%) ---
                double categoryScore;
                String categoryReason;

                // Check if candidate wants any skill that current student offers (Mutual barter opportunity!)
                List<StudentSkill> candidateWanted = skillDAO.getStudentSkills(candidate.getId(), StudentSkill.TYPE_WANTED);
                boolean hasMutualExchange = false;
                String mutualSkillName = null;
                for (StudentSkill cw : candidateWanted) {
                    if (myOfferedSkillIds.contains(cw.getSkillId())) {
                        hasMutualExchange = true;
                        mutualSkillName = cw.getSkillName();
                        break;
                    }
                }

                if (hasMutualExchange) {
                    categoryScore = 15.0;
                    categoryReason = "Mutual Barter Match! They want your '" + mutualSkillName + "' (15%)";
                } else if (skill.getCategory() != null && myWantedCategories.contains(skill.getCategory().toLowerCase())) {
                    categoryScore = 12.0;
                    categoryReason = "Category match: You want skills in " + skill.getCategory() + " (12%)";
                } else {
                    categoryScore = 8.0;
                    categoryReason = "General domain match (8%)";
                }

                // --- 3. RATING SCORE (Max 15%) ---
                double ratingScore;
                String ratingReason;
                if (candidate.getReviewCount() > 0) {
                    // Rating between 1.0 and 5.0 mapped to 15 points
                    ratingScore = (candidate.getAverageRating() / 5.0) * 15.0;
                    ratingReason = String.format("Rating: %.1f★ (%d reviews) (%.1f%%)",
                            candidate.getAverageRating(), candidate.getReviewCount(), ratingScore);
                } else {
                    // New user baseline rating: 4.0 out of 5
                    ratingScore = (4.0 / 5.0) * 15.0; // 12.0 points
                    ratingReason = "New student baseline rating (12.0%)";
                }

                String fullExplanation = skillReason + " | " + categoryReason + " | " + ratingReason;

                SkillMatchResult result = new SkillMatchResult(
                        candidate,
                        skill,
                        skillScore,
                        categoryScore,
                        ratingScore,
                        fullExplanation
                );
                results.add(result);
            }
        }

        // 3. Sort by highest total score
        Collections.sort(results);
        return results;
    }
}
