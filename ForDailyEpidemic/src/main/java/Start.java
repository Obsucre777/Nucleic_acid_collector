import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.edge.EdgeDriver;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

/**
 * @author kerwin
 * @create 2022-10-25 21:24
 */
public class Start {
    public static void main(String[] args) throws InterruptedException, UnsupportedEncodingException {

//        Scanner scanner = new Scanner(System.in);
//        String s = scanner.nextLine();
        //驱动路径
        String path = args[0];

        //1. 通过java代码直接获取到当前用户。
//        Map<String, String> computerInfo = System.getenv();
        String userName = System.getProperty("user.name");//gbk

        userName = new String(userName.getBytes(),"utf-8");
        System.out.println(userName);

//        computerInfo.get()

        //String userName = args[1];
        //String url = "https://docs.qq.com/form/page/DTHlCWGhlWUZGbmdk?u=4f37de58c6f044218ebf8dc80c08e88c#/result";

        String url = args[1];
        //每次要修改的位置↑

        //为了配合colletsheet表名组合成文件名。
        String suffix = "（收集结果）";


        //2. 需要外部输入(可自定义)
//        String colletsheet = "核酸收集";
       // String colletsheet = args[2];
        //每次要修改的位置↑

        //3. 需要外部输入(当前用户的用户名，但一次定义后无序再次输入)
       // String userName ="Obscure";



        //输入路径: 拼成文件输入的路径(这里是使用edgedriver的默认下载路径)
        //关于输出路径: 输出路径必须有D盘，有D盘即可
        String filepath = "C:\\Users\\"+userName+"\\Downloads";

        //(1) path与url为必备，为了获取指定页面
        //(2) 其余参数的目的则是为了判断的确获取到了文件
        GetTheFile getTheFile = new GetTheFile(path, url,userName,filepath);

        if(getTheFile.getfile()){

            System.out.println("已经获取到文件，准备读取并且写入到路径");

            //这里应该是一个表名后面自动加上收集结果
            //（收集结果）

            //等待文件下载完毕，不然极易可能有文件但里面没有数据导致读取不到数据。
            Thread.sleep(2000);
            //
            ReadTheFile readTheFile = new ReadTheFile(filepath,getTheFile.getFilename(),userName);
            //这里传入要执行哪个班儿的。
            String checkclass = args[2];
            //用于指定checknum，如果参数大于等于3的时候再做checknum，否则就都检查
            ArrayList<String> checkNum = null;
            if(args.length>=4){
                checkNum = new ArrayList<>();
                for(int i=3;i<args.length;i++){
                    checkNum.add(args[i]);
                }
            }

            readTheFile.handleFile(checkclass+".txt",checkNum);

            File file = new File(filepath, getTheFile.getFilename());
            //删除掉中间运行的excel文件
            //
            file.delete();

        }else{
            System.out.println("文件获取失败了。T T");
        }


    }
}
