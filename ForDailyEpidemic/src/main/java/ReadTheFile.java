import com.alibaba.excel.EasyExcel;

import java.util.ArrayList;

/**
 * @author kerwin
 * @create 2022-10-26 11:06
 */
public class ReadTheFile {

    String suffix = ".xlsx";
    String fileName;
    String filepath;
    String userName;

    //习惯性的提供一个空参构造器
    public ReadTheFile() {
    }

    public ReadTheFile(String filepath,String fileName,String userName) {
        this.fileName = fileName;
        this.filepath = filepath;
        this.userName = userName;
    }

    public void handleFile(String checkClass, ArrayList<String> checkNum){
        //拼接成最终的路径去读取文件
        String path = filepath+"\\"+fileName;
        System.out.println(path);
        //从handleFile里传入
        //new DemoDataListener()构造器里传参
        EasyExcel.read(path,DemoData.class,new DemoDataListener(checkClass,userName,checkNum)).sheet().doRead();
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }



}
