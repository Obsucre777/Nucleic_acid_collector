
import cn.hutool.http.HttpUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.fastjson.JSON;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.ehcache.core.statistics.StoreOperationOutcomes;
import org.openqa.selenium.support.ui.Sleeper;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * @author kerwin
 * @create 2022-10-26 10:43
 */
public class DemoDataListener extends AnalysisEventListener<DemoData> {

    //有数据的
    private List<DemoData> list = new ArrayList<DemoData>();
    //checkList里放所有的
    //private HashMap checkList =  new HashMap<String,String>();

    //装所有人的信息
    private ArrayList<String> checkList = new ArrayList<>();
    //装已经提交上的待检查的信息
    //HashSet的查找和插入效率是最高的(o(1)的时间复杂度)，原来用的是ArrayList，
    //HashSet的查找效率远超ArrayList

    //submitList换成Students
    private HashSet<Student> students = new HashSet<>();

    //用于做去重的
    private LinkedHashSet<String> DeduplicatesubmitList = new LinkedHashSet<>();
    //最后结束的时候做一个遍历检查，如果不再chekList里就输出学号、班级、姓名。
    //学号是无意义的。

    //装没交的信息
    private ArrayList<String> notSubmitList = new ArrayList<>();

    //装有问题的
    private ArrayList<String> issueStudent = new ArrayList<>();

    //使用HashSet确保元素不重复
    private HashSet<String> simpleSubmit = new HashSet<>();

    private HashMap nameForSuffix  = new HashMap();

    private String checkClass;

    private String userName;

    private String suffix;

    private static String formatMonthandDay;

    private static String checktime;//ocr扫描的时间一般都是昨天，因为是收前一天的核酸

    private static String theDaybefYesterday;//前一天，对特殊情况的判断
    //特殊情况:当今天没交时，昨天叫前天的核酸(正常交核酸的时候)，容易被判断为核酸截图有问题

    private static String today;//用于对大量excel数据的提交时间进行过滤
    //由于会提交大量数据到Excel，我们需要过滤掉旧数据，将今天和昨天的数据筛选出来做去重
    private ArrayList<String> checkNum;
    //交核酸的人数
    private static int subPersonsNum=0;




    private BufferedImage img;


    public DemoDataListener() {
    }

    public DemoDataListener(String checkClass,String userName,ArrayList<String> checkNum) {
        this.checkClass = checkClass;
        this.userName = userName;
        this.checkNum = checkNum;
    }

    public static BufferedImage  getBufferImage(String url) throws IOException {
            URL toBufferdImage = new URL(url);
            return ImageIO.read(toBufferdImage);
    }

    //对两个变量的初始化
    static{

        SimpleDateFormat formatter = new SimpleDateFormat("MM-dd");

        theDaybefYesterday = formatter.format(new Date(System.currentTimeMillis()-(86400000L)*2));
        //日期检查，基本上是前一天的，这里测试所以往前推了4天
        //这里是ocr检查的日期
        Date date = new Date(System.currentTimeMillis()-(86400000L)*1);
        String monthAndDay = formatter.format(date);
        String[] split = monthAndDay.split("-");
        formatMonthandDay = split[0]+"月"+split[1]+"日";
        //checktime就是昨天21
        checktime = formatter.format(date);
        today=formatter.format(new Date(System.currentTimeMillis()));
    }

    //每一条数据解析都会调用！
    @Override
    public void invoke(DemoData data, AnalysisContext analysisContext) {




//        System.out.println("这是一条数据");
//        System.out.println(JSON.toJSONString(data));


        if(data.getSubTime().split(" ")[0].split("-",2)[1].equals(today)||data.getSubTime().split(" ")[0].split("-",2)[1].equals(checktime)){
            System.out.println("解析到一条数据：{}");
            Student student = new Student(data.getStudentClass(), data.getName(), data.getPic(),data.getSubTime().split(" ")[0].split("-",2)[1]);
            System.out.println(JSON.toJSONString(data));
            //这里为了未来方便，前后设置两天的时间差，因为如果表里的数据太多，
            //比如有20条同一个人的数据，要进行太多次的删除了，所以不如直接从时间上判断一下。
            //如果不存在，直接添加
            //允许进入的时间
            //这是提交日期啊，不是核酸截图日期啊。
            //
            //1. 检查日期+1今天 2明天
            System.out.println(data.getSubTime().split(" ")[0]);
            System.out.println(checktime);
            System.out.println(today);
            if(!(students.contains(student))){
                students.add(student);
                //simpleSubmit在后面用于简单判断
                simpleSubmit.add(data.getStudentClass()+"\t"+data.getName());
            }else{ //如果存在，也就是说班级和姓名已经存在再students里了
                System.out.println(student);
                //删掉原来的，只根据class和name删，目的是为了去掉旧的pic地址
                students.remove(student);
                //添加新的，根据class和name添加，目的是为了添加新的pic
                students.add(student);
            }
        }
    }

//    public boolean hasNext(AnalysisContext analysisContext) {
//        return true;
//    }

    //所有数据解析完了都会来调用
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        subPersonsNum=students.size();

        if(students.size()==0){
            System.out.println("当前表为空哦~~~或者提交的截图时间过于久远");
            System.exit(0);
        }

        System.out.println("students.size="+students.size());

        System.out.println("submitList里的名单如下，有对复用表的优化，所以这里可能包含前一天的核酸");
        students.forEach(System.out::println);

        //ocr的准备

        String tessdatapath = "D:\\tools\\tessdata";

        Tesseract instance = new Tesseract();
        instance.setDatapath(tessdatapath);
        //先以eng读取
        instance.setLanguage("eng");
        //instance.setLanguage("chi_sim");

        //考虑到月份或日可能是单位或双位数
        System.out.println(formatMonthandDay);
        System.out.println("ocr检查的目标日期为:"+checktime);

        //读取图片的字节
        BufferedImage bufferedImage;
        //考虑到图片的格式不一，采用百分比的形式
        int height;
        //设置截取的矩形
        Rectangle rectangle;
        //采集后的结果
        String result;

        //ocr的准备结束

        //用于检查的file对象
        File checkFile;

        URL url;

        Graphics2D g;

        //如果外层的"核酸截图"文件不存在，直接创建
        File outFile = new File("C:\\Users\\" + userName + "\\Desktop\\核酸截图");
        if(!(outFile.exists()))
            outFile.mkdir();
        //思路，遍历submitList，拿到bufferimage，然后判断是否ok再写入
        for (Student student : students) {
            //String[] split = studentInfo.split("\t");
            //System.out.println(Arrays.toString(split));
            //checkFile = new File("C:\\Users\\"+userName+"\\Desktop\\核酸截图\\"+formatMonthandDay+"核酸-" + split[0] + "\\" + split[1] + nameForSuffix.get(split[1]));
            checkFile = new File("C:\\Users\\"+userName+"\\Desktop\\核酸截图\\"+formatMonthandDay+"核酸-" + student.getStudentClass() + "\\" + student.getName() + ".jpg");

            //ImageIO.write()方法不会创建文件夹，所以我们需要判断父类的文件夹在不在
            if(!(checkFile.getParentFile().exists())){
                checkFile.getParentFile().mkdir();
            }

            //由于已经确保了students里存放是最新的数据，所以我们只需要判断
            //原来的文件夹是否存在，存在的话就不用管了(能存在一定是没问题的，即通过了扫描)
            //不存在的话需要把数据逐个扫描一下。
            if(!(checkFile.exists())){
                try {
                    //得到对应地址上的bufferdImage了，扫描一下看看符不符合规范
                    bufferedImage = getBufferImage(student.getPic());

                    height = bufferedImage.getHeight();
                    //rectangle = new Rectangle(0, (height/2)-100, 550, 100);
                    rectangle = new Rectangle(100, (height/2)-100, 600, 200);

                    try {
                        //先依据理想情况检查
                        result = instance.doOCR(bufferedImage,rectangle);
                        if(result.contains(checktime)){
                            System.out.println("ok");
                            boolean writeok = ImageIO.write(bufferedImage, "jpg", checkFile);
                            if(!writeok){
                                BufferedImage newbufferImage =  new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
                                //ImageIO.write(newbufferImage, "jpg", checkFile);
                                g = newbufferImage.createGraphics();
                                g.drawImage(bufferedImage,0,0,null);
                                boolean writeok2 = ImageIO.write(newbufferImage,"jpg", checkFile);
                                //System.out.println(checkFile);
                                System.out.println(writeok2);
                            }
                            //写入磁盘
                        }else if(result.contains(theDaybefYesterday)&&student.getSubTime().equals(checktime)){//全面检查没通过，提交时间是昨天(checktime)，且发的是前天的核酸，说明今天一定没交

                            //说明没交，那就得把它从students和simpleStudent里抹除，抹除之和在最后查的时候就会放在没有交的里面
//                                        students.remove(student);
//                                        simpleSubmit.remove(student.getStudentClass()+"\t"+student.getName());
                            //
                            //subPersonsNum--;
                            notSubmitList.add(student.getStudentClass()+"\t"+student.getName());
                        }
                        else{

//                            System.out.println(student.getName());
//                            System.out.println(student.getSubTime());
//                            System.out.println(theDaybefYesterday);
//
//                            System.out.println(result);
                            //根据优化版本的特殊情况检查
                            //理想情况不行，切换到特殊情况，此时用汉字读取效率高于用英文读取
                            instance.setLanguage("chi_sim");
                            rectangle = new Rectangle(0, 250, 600, bufferedImage.getHeight() - 250);
                            result = instance.doOCR(bufferedImage,rectangle);
                            if(result.contains(checktime)){

                                System.out.println(student.getStudentClass()+""+student.getName());
                                System.out.println("ok too");
                                //写入磁盘
                               boolean writeok =  ImageIO.write(bufferedImage, "jpg", checkFile);
                               //是否能够写好图片于原图有关
                                //对于无法直接转化成原图的再做一道处理
                               if(!writeok){
                                   BufferedImage newbufferImage =  new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
                                   //ImageIO.write(newbufferImage, "jpg", checkFile);
                                   g = newbufferImage.createGraphics();
                                   g.drawImage(bufferedImage,0,0,null);
                                   boolean writeok2 = ImageIO.write(newbufferImage,"jpg", checkFile);
                                  // System.out.println(checkFile);
                                   System.out.println(writeok2);
                               }else if(result.contains(theDaybefYesterday)&&student.getSubTime().equals(checktime)){//全面检查没通过，提交时间是昨天(checktime)，且发的是前天的核酸，说明今天一定没交

                                    //说明没交，那就得把它从students和simpleStudent里抹除，抹除之和在最后查的时候就会放在没有交的里面
//                                        students.remove(student);
//                                        simpleSubmit.remove(student.getStudentClass()+"\t"+student.getName());
                                    //
                                    //subPersonsNum--;
                                    notSubmitList.add(student.getStudentClass()+"\t"+student.getName());
                                }
//                               System.out.println(write);


                            }
                            else if(!result.contains(checktime)){
                                //优化版本的特殊情况不满足的话，做全面检查
                                result = instance.doOCR(bufferedImage);
                               // System.out.println(result);
                                if(result.contains(checktime)){
                                    System.out.println("ok toooooo");
                                    //写入磁盘
                                    boolean writeok = ImageIO.write(bufferedImage, "jpg", checkFile);
                                    if(!writeok){
                                        BufferedImage newbufferImage =  new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
                                        //ImageIO.write(newbufferImage, "jpg", checkFile);
                                        g = newbufferImage.createGraphics();
                                        g.drawImage(bufferedImage,0,0,null);
                                        boolean writeok2 = ImageIO.write(newbufferImage,"jpg", checkFile);
                                     //   System.out.println(checkFile);
                                        System.out.println(writeok2);
                                    }
                                }
                                //下面是全盘检查没通过的
                                else if(result.contains(theDaybefYesterday)&&student.getSubTime().equals(checktime)){//全面检查没通过，提交时间是昨天(checktime)，且发的是前天的核酸，说明今天一定没交

                                        //说明没交，那就得把它从students和simpleStudent里抹除，抹除之和在最后查的时候就会放在没有交的里面
//                                        students.remove(student);
//                                        simpleSubmit.remove(student.getStudentClass()+"\t"+student.getName());
                                    //
                                    //subPersonsNum--;
                                    notSubmitList.add(student.getStudentClass()+"\t"+student.getName());
                                }
                                else{ //全面检查没通过

                                    File humancheck = new File("C:\\Users\\" + userName + "\\Desktop\\核酸截图\\" + formatMonthandDay + "核酸-(人工审查)");

                                    //如果人工检查的文件夹不存在，创建一个人工检查的文件夹
                                    if(!(humancheck.exists())){
                                        humancheck.mkdir();
                                    }
                                    //对于没有通过扫描的，不进行磁盘的写入(或者考虑放到单独的一个文件夹里人工审核一下)
                                    issueStudent.add(student.getStudentClass()+"\t"+student.getName());

                                    //没有通过扫描的也可能写入不成功
                                    boolean writeok = ImageIO.write(bufferedImage, "jpg", new File("C:\\Users\\" + userName + "\\Desktop\\核酸截图\\" + formatMonthandDay + "核酸-(人工审查)\\" + student.getStudentClass() + " " + student.getName() + ".jpg"));
                                    //boolean writeok =  ImageIO.write(bufferedImage, "jpg", checkFile);
                                    //是否能够写好图片于原图有关
                                    //对于无法直接转化成原图的再做一道处理
                                    if(!writeok){
                                        BufferedImage newbufferImage =  new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
                                        //ImageIO.write(newbufferImage, "jpg", checkFile);
                                        g = newbufferImage.createGraphics();
                                        g.drawImage(bufferedImage,0,0,null);
                                        boolean writeok2 = ImageIO.write(newbufferImage,"jpg", new File("C:\\Users\\" + userName + "\\Desktop\\核酸截图\\" + formatMonthandDay + "核酸-(人工审查)\\" + student.getStudentClass() + " " + student.getName() + ".jpg"));
                                        // System.out.println(checkFile);
                                        System.out.println(writeok2);
                                    }
                                }
                            }
//                        else
//                            issueStudent.add(split[0]+"\t"+split[1]);
//                            System.out.println(split[0]+"\t"+split[1]+"的核酸截图时间出问题了，快去看看吧。");
                        }
                    } catch (TesseractException e) {
                        e.printStackTrace();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }else{
                System.out.println("该文件已check过了");
            }
//            else if(checkFile.exists()&&"correct".equals(split[3])){
//                //对于路径存在了的，但是重新提交了的照片，我们是需要扫描再重写的。
//                try {
//                    //得到对应地址上的bufferdImage了，扫描一下看看符不符合规范
//                    bufferedImage = getBufferImage(split[2]);
//
//                    height = bufferedImage.getHeight();
//                    //rectangle = new Rectangle(0, (height/2)-100, 550, 100);
//                    rectangle = new Rectangle(100, (height/2)-100, 600, 200);
//
//                    try {
//                        //先依据理想情况检查
//                        result = instance.doOCR(bufferedImage,rectangle);
//                        if(result.contains(checktime)){
//                            System.out.println("ok");
//                            ImageIO.write(bufferedImage, "jpg", checkFile);
//                            //写入磁盘
//                        }
//                        else{
//                            //根据优化版本的特殊情况检查
//                            //理想情况不行，切换到特殊情况，此时用汉字读取效率高于用英文读取
//                            instance.setLanguage("chi_sim");
//                            rectangle = new Rectangle(0, 250, 600, bufferedImage.getHeight() - 250);
//                            result = instance.doOCR(bufferedImage,rectangle);
//                            if(result.contains(checktime)){
//                                System.out.println("ok tooyouhua");
//                                //写入磁盘
//                                ImageIO.write(bufferedImage, "jpg", checkFile);
//                            }
//                            else if(!result.contains(checktime)){
//                                //优化版本的特殊情况不满足的话，做全面检查
//                                result = instance.doOCR(bufferedImage);
//                                if(result.contains(checktime)){
//                                    System.out.println("ok toooowuyouhua");
//                                    //写入磁盘
//                                    ImageIO.write(bufferedImage, "jpg", checkFile);
//                                }
//                                else{
//                                    //对于没有通过扫描的，不进行磁盘的写入(或者考虑放到单独的一个文件夹里人工审核一下)
//                                    issueStudent.add(split[0]+"\t"+split[1]);
//                                    ImageIO.write(bufferedImage,"jpg",new File("C:\\Users\\"+userName+"\\Desktop\\核酸截图\\"+formatMonthandDay+"核酸-(人工审查)\\"+split[0]+" "+split[1]+".jpg"));
//                                }
//                            }
////                        else
////                            issueStudent.add(split[0]+"\t"+split[1]);
////                            System.out.println(split[0]+"\t"+split[1]+"的核酸截图时间出问题了，快去看看吧。");
//                        }
//                    } catch (TesseractException e) {
//                        e.printStackTrace();
//                    }
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }

        }
        //----------到此为止，已经提交的已经处理完了，接下来要打印没交的，和核酸结果有问题的人-----

        //展示所有读取的数据
        //System.out.println(JSON.toJSONString(list));

        //先拿list判断是哪个班，只在
        //file可以是灵活的，所以每次读入到内存的也就不一样，检查标准就不同
        File file = new File("D:\\tools", checkClass);
        try {
            //new BufferedReader()
            BufferedReader br = new BufferedReader(new FileReader(file));

            String line=null;
            System.out.println("载入检查表数据如下");

            while((line = br.readLine())!=null){

                //加入的一行的数据从excel复制过来中间的制表符是\t，不是空格！
                String[] split1 = line.split("\t");
//                if(line.end){ //如果学号的尾号在是以传入的args[3+]参数中，则加入，否则不加入
//
//                }
//                for(int i=0;i<checkNum.s;i++){
//
//                }
                //没有参数，直接//网络211	3210214026	霍礼刚
                //System.out.println("here"+split1.length);
                if(checkNum==null)
                    checkList.add(split1[0]+"\t"+split1[2]); //只把班级和姓名加进去学号不加
                else{ //有需要检查的
//                    if()
                    for (String anum : checkNum) {
                        //如果是以需要检查的数字结尾的话
                        if (split1[2].endsWith(anum))
                            checkList.add(split1[0]+"\t"+split1[3]);
                    }
                }
                //checkList.
                //检查表写好
                //checkList.put(split[0],split[1]);
                System.out.println(line);

            }

           // System.out.println("list.size = "+list.size());
//            System.out.println("submitList.size = "+simpleSubmit.size());
//            System.out.println("students.size="+students.size());


                for (String checkStudent : checkList) {
                    //submitList.
                    if (!(simpleSubmit.contains(checkStudent))) {
                        //需要排序的时候，可以把没交的加到一个数组
                        //对数组做个sort排序，这样最后就会显示计、软、网、物网等
                        notSubmitList.add(checkStudent);
                       // System.out.println(checkStudent);
                    }
                }
                //对check

            System.out.println("本次共有"+(students.size()-notSubmitList.size())+"人交了核酸");
            System.out.println("检查表中人数 = "+checkList.size());

                if(notSubmitList.size()==0)
                    System.out.println("核酸已经收齐啦~");
                else {
                    //对没有提交的做个排序方便查看
                    notSubmitList.sort(new Comparator<String>() {
                        @Override
                        public int compare(String o1, String o2) {
                            return o1.compareTo(o2);
                        }
                    });

                }


            if(notSubmitList.size()!=0){
                System.out.println("没交核酸截图的名单如下，提醒一下他们吧~");
                notSubmitList.forEach(System.out::println);
            }else{
                System.out.println("今天所有同学都交了核酸截图！");
            }
            if(issueStudent.size()!=0){
                System.out.println("核酸截图有问题名单如下，快去看看吧");
                issueStudent.forEach(System.out::println);
            }else{
                System.out.println("已交同学的核酸截图时间都是正常的！");
            }





        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
