import com.alibaba.excel.annotation.ExcelProperty;



/**
 * @author kerwin
 * @create 2022-10-26 10:41
 */
public class DemoData {
    //指定读取的行,类名必须小驼峰！！！不然识别不到！
    @ExcelProperty(index=0)
    private String subTime;

    @ExcelProperty(index=1)
    private String name;

    @ExcelProperty(index=2)
    private String studentClass;


    @ExcelProperty(index=3)
    private String pic;

    @ExcelProperty(index=4)
    private String author;

    //必须有2.x 版本的 get、set 方法，必须有，否则读取不出来数据！


    public String getSubTime() {
        return subTime;
    }

    public void setSubTime(String subTime) {
        this.subTime = subTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStudentClass() {
        return studentClass;
    }

    public void setStudentClass(String studentClass) {
        this.studentClass = studentClass;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    @Override
    public String toString() {
        return "DemoData{" +
                "subTime='" + subTime + '\'' +
                ", name='" + name + '\'' +
                ", studentClass='" + studentClass + '\'' +
                ", pic='" + pic + '\'' +
                ", author='" + author + '\'' +
                '}';
    }


}
