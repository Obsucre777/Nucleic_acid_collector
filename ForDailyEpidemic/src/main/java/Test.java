import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

/**
 * @author kerwin
 * @create 2022-10-28 0:36
 */
public class Test {
    public static void main(String[] args) throws TesseractException {
        long startTime = System.currentTimeMillis();

        String[] arr =  new String[]{".jpg",".png",".jpg",".png",".jpg",".jpg",".jpg",".jpg",".jpg",".jpg"};

        String lagnguagePath = "D:\\IdeaProjects-Java\\ForDailyEpidemic\\tessdata";
        ITesseract instance = new Tesseract();
        instance.setDatapath(lagnguagePath);
        instance.setLanguage("chi_sim");

        for (int i = 0; i < 1; i++) {
            String path = "C:\\Users\\Obscure\\Desktop\\image\\test"+(i+1) + arr[i];
            System.out.println(path);
            // 识别图片的路径（修改为自己的图片路径）
//        String path = "C:\\Users\\Tang\\Desktop\\图片\\营业执照4.jpg";
            //C:\Users\Obscure\Desktop\image

//        String path = "C:\\Users\\Tang\\Desktop\\图片\\其他图片2.png";
            //String s1 = convertPng(path);
            // 语言库位置（修改为跟自己语言库文件夹的路径）


            File file = new File(path);


            //设置训练库的位置


            //chi_sim ：简体中文， eng    根据需求选择语言库

            String result = null;
            try {
                //long startTime = System.currentTimeMillis();
                result = instance.doOCR(file);
                System.out.println(result);
            } catch (TesseractException e) {
                System.out.println("图片有问题，正在处理..");
                //e.printStackTrace();
                String s1 = convertPng(path);
                Rectangle rectangle = new Rectangle(0, 400, 600, 450);
                String result2 = instance.doOCR(new File(s1),rectangle);
                System.out.println(result2);
            }

        }



        long endTime = System.currentTimeMillis();
        System.out.println("Time is：" + (endTime - startTime) + " 毫秒");
    }
    //转换图片为png格式
    public static String convertPng(String url) {
        String tarFilePath = url.substring(0, url.lastIndexOf(".")) + ".png";
        try {
            BufferedImage bufferedImage = ImageIO.read(new File(url));
            BufferedImage newBufferedImage = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
            newBufferedImage.createGraphics().drawImage(bufferedImage, 0, 0, Color.white, null);
            ImageIO.write(newBufferedImage, "png", new File(tarFilePath));
        } catch (IOException e) {
            return "";
        }
        return tarFilePath;
    }
}
