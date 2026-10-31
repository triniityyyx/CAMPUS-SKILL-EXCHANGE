package model;

/**
 * Student model class inheriting from User.
 * Demonstrates Inheritance and Encapsulation.
 */
public class Student extends User {
    private String department;
    private int year;
    private double averageRating;
    private int reviewCount;

    public Student() {
        super();
    }

    public Student(int id, String name, String email, String password, String department, int year) {
        super(id, name, email, password);
        this.department = department;
        this.year = year;
    }

    public Student(int id, String name, String email, String password, String department, int year, double averageRating, int reviewCount) {
        super(id, name, email, password);
        this.department = department;
        this.year = year;
        this.averageRating = averageRating;
        this.reviewCount = reviewCount;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }

    public String getFormattedRating() {
        if (reviewCount == 0) {
            return "No reviews yet";
        }
        return String.format("%.1f/5 (%d %s)", averageRating, reviewCount, reviewCount == 1 ? "review" : "reviews");
    }

    @Override
    public String toString() {
        return "Student{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", department='" + department + '\'' +
                ", year=" + year +
                ", avgRating=" + averageRating +
                '}';
    }
}
