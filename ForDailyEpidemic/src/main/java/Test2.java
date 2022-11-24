import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author kerwin
 * @create 2022-10-29 12:01
 */
public class Test2 {
    public static void main(String[] args) throws IOException, TesseractException {
//        new IIOImage()
        long begain = System.currentTimeMillis();
        String[] arr =  new String[]{".jpg",".jpg",".jpg",".png",".png",".jpg",".jpg",".jpg",".jpg",".jpg"};
        String tessdatapath = "D:\\IdeaProjects-Java\\ForDailyEpidemic\\tessdata";
        Tesseract instance = new Tesseract();
        instance.setDatapath(tessdatapath);
        //instance.setLanguage("chi_sim");
        SimpleDateFormat formatter = new SimpleDateFormat("YYYY-MM-dd");
        Date date = new Date(System.currentTimeMillis() - (86400000)*6);
        String checktime = formatter.format(date);
        //考虑到月份或日可能是单位或双位数
        System.out.println(checktime);
        for (int i = 0; i < 10; i++) {

            long time1 = System.currentTimeMillis();
           BufferedImage bufferedImage = ImageIO.read(new File("C:\\Users\\Obscure\\Desktop\\image\\test"+(i+1)+arr[i]));
           long time2 = System.currentTimeMillis();
            System.out.println("读取文件所需时间"+(time2-time1));
           int height = bufferedImage.getHeight();
           //bufferedImage.createGraphics().drawImage(bufferedImage, , );
           //Rectangle rectangle = new Rectangle(0, 1100, 550, 200);
           //主要获取采样时间，昨天做的核酸，就是昨天采样的核酸
            //rectangle = new Rectangle(100, (height/2)-100, 600, 200);
           Rectangle rectangle = new Rectangle(100, (height/2)-100, 600, 200);
           long ocrTime1 = System.currentTimeMillis();
           String result = instance.doOCR(bufferedImage,rectangle);


           //理想状态
            if(result.contains(checktime)){
                instance.setLanguage("eng");
                System.out.println("ok");
                long ocrTime2 = System.currentTimeMillis();
                System.out.println("第"+(i+1)+"次"+(ocrTime2-ocrTime1));
               // System.out.println(result);
            }
            else {
                //次理想状态
                instance.setLanguage("chi_sim");
                rectangle = new Rectangle(0, 250, 600, bufferedImage.getHeight() - 250);
                String result2 = instance.doOCR(bufferedImage, rectangle);
                if (result2.contains(checktime)) {
                    System.out.println("ok too");
                    long ocrTime2 = System.currentTimeMillis();
                    System.out.println("第"+(i+1)+"次"+(ocrTime2-ocrTime1));
                    //System.out.println(result2);
                }else{
                    String result3 = instance.doOCR(bufferedImage);
                    if(result3.contains(checktime))
                        System.out.println("ok toooooooo");
                    else
                        System.out.println("not ok 截图有问题");
                }
            }
           System.out.println(result);

           System.out.println("i="+i);

        }
        long end = System.currentTimeMillis();
        System.out.println(end-begain);
    }
}
