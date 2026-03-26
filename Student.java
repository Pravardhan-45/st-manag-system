public class Student {
    private int id;
    private String name;
    private int age;
    private String course;
    private int marks;

    public Student(String name, int age, String course, int marks) {
        this.name = name; this.age = age; this.course = course; this.marks = marks;
    }

    public Student(int id, String name, int age, String course, int marks) {
        this.id = id; this.name = name; this.age = age; this.course = course; this.marks = marks;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getCourse() { return course; }
    public int getMarks() { return marks; }

    // Simple JSON conversion for HTTP response
    public String toJson() {
        return String.format("{\"id\":%d, \"name\":\"%s\", \"age\":%d, \"course\":\"%s\", \"marks\":%d}",
                id, name, age, course, marks);
    }
}
