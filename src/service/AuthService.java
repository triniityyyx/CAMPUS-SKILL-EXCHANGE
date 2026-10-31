package service;

import dao.StudentDAO;
import model.Student;

import java.sql.SQLException;
import java.util.regex.Pattern;

/**
 * Service handling authentication, registration, and user validation.
 */
public class AuthService {

    private final StudentDAO studentDAO = new StudentDAO();
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    /**
     * Authenticate student with email and password.
     */
    public Student login(String email, String password) throws Exception {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email address cannot be empty.");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }

        email = email.trim();
        Student student = studentDAO.findByEmail(email);

        if (student == null) {
            throw new IllegalArgumentException("No account found with this email address.");
        }

        if (!student.getPassword().equals(password)) {
            throw new IllegalArgumentException("Incorrect password. Please try again.");
        }

        return student;
    }

    /**
     * Register a new student account.
     */
    public Student register(String name, String email, String password, String department, int year) throws Exception {
        // Validation rules
        if (name == null || name.trim().length() < 2) {
            throw new IllegalArgumentException("Full Name must be at least 2 characters long.");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email address cannot be empty.");
        }
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new IllegalArgumentException("Please enter a valid email format (e.g. name@campus.edu).");
        }
        if (password == null || password.trim().length() < 4) {
            throw new IllegalArgumentException("Password must be at least 4 characters long.");
        }
        if (department == null || department.trim().isEmpty()) {
            throw new IllegalArgumentException("Please select or enter your Department.");
        }
        if (year < 1 || year > 6) {
            throw new IllegalArgumentException("Please select a valid academic Year (1-6).");
        }

        email = email.trim().toLowerCase();

        // Check duplicate email
        if (studentDAO.existsByEmail(email)) {
            throw new IllegalArgumentException("An account with email '" + email + "' already exists.");
        }

        Student student = new Student();
        student.setName(name.trim());
        student.setEmail(email);
        student.setPassword(password);
        student.setDepartment(department.trim());
        student.setYear(year);

        int newId = studentDAO.createStudent(student);
        if (newId <= 0) {
            throw new SQLException("Failed to create student account in the database.");
        }

        student.setId(newId);
        return student;
    }
}
