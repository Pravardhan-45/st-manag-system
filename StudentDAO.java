import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {
    // Note: We use environment variables for security when deploying to the cloud.
    private static final String URL = System.getenv("TIDB_URL") != null ? 
            System.getenv("TIDB_URL") : "jdbc:mysql://gateway01.ap-southeast-1.prod.aws.tidbcloud.com:4000/test?useSSL=true&tinyInt1isBit=false";
    private static final String USER = System.getenv("TIDB_USER") != null ? 
            System.getenv("TIDB_USER") : "2sXLGpohmYwBieY.root";
    private static final String PASSWORD = System.getenv("TIDB_PASSWORD") != null ? 
            System.getenv("TIDB_PASSWORD") : "LSvSJ9i4mLinzeEI";

    public Connection getConnection() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public boolean addStudent(Student s) {
        String query = "INSERT INTO students (name, age, course, marks) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, s.getName()); stmt.setInt(2, s.getAge());
            stmt.setString(3, s.getCourse()); stmt.setInt(4, s.getMarks());
            return stmt.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public List<Student> getAllStudents() {
        List<Student> list = new ArrayList<>();
        String query = "SELECT * FROM students ORDER BY id DESC";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(new Student(rs.getInt("id"), rs.getString("name"), rs.getInt("age"), rs.getString("course"), rs.getInt("marks")));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public boolean updateStudent(Student s) {
        String query = "UPDATE students SET name = ?, age = ?, course = ?, marks = ? WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, s.getName()); stmt.setInt(2, s.getAge());
            stmt.setString(3, s.getCourse()); stmt.setInt(4, s.getMarks());
            stmt.setInt(5, s.getId());
            return stmt.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public boolean deleteStudent(int id) {
        String query = "DELETE FROM students WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }
}
