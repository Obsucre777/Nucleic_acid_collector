/**
 * @author kerwin
 * @create 2022-11-02 19:00
 */
public class Student {

    private String studentClass;
    private String name;
    private String pic;
    private String subTime;

    public Student() {
    }

    public Student(String studentClass, String name, String pic,String subTime) {
        this.studentClass = studentClass;
        this.name = name;
        this.pic = pic;
        this.subTime = subTime;
    }

    public String getStudentClass() {
        return studentClass;
    }

    public void setStudentClass(String studentClass) {
        this.studentClass = studentClass;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public String getSubTime() {
        return subTime;
    }

    public void setSubTime(String subTime) {
        this.subTime = subTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Student student = (Student) o;

        if (studentClass != null ? !studentClass.equals(student.studentClass) : student.studentClass != null)
            return false;
        return name != null ? name.equals(student.name) : student.name == null;
    }

    @Override
    public int hashCode() {
        int result = studentClass != null ? studentClass.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Student{" +
                "studentClass='" + studentClass + '\'' +
                ", name='" + name + '\'' +
                ", pic='" + pic + '\'' +
                '}';
    }
}
