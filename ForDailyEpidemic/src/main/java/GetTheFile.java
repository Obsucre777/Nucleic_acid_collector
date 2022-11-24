import cn.hutool.core.net.url.UrlPath;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;

import java.io.File;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author kerwin
 * @create 2022-10-26 18:19
 */
public class GetTheFile {
    private String edgeDriverpath;
    private String url;
    private String filename;
    private String UserName;
    private String filepath;
    //细节上注意字体
    private String suffix = "（收集结果）.xlsx";

    public GetTheFile() {
    }

    //    public GetTheFile(String edgeDriverpath, String url) {
//        this.edgeDriverpath = edgeDriverpath;
//        this.url = url;
//    }


    public String getFilename() {
        return filename;
    }

    public GetTheFile(String edgeDriverpath, String url, String userName, String filepath) {
        this.edgeDriverpath = edgeDriverpath;
        this.url = url;
        UserName = userName;
        this.filepath = filepath;
    }

    public String getEdgeDriverpath() {
        return edgeDriverpath;
    }

    public void setEdgeDriverpath(String edgeDriverpath) {
        this.edgeDriverpath = edgeDriverpath;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean getfile() throws InterruptedException {


        //edgeDriverpath = "D:\\develop_tools\\driver\\edgedriver_win64\\msedgedriver.exe";
        //驱动的配置
        System.setProperty("webdriver.edge.driver",edgeDriverpath);
        EdgeOptions edgeOptions = new EdgeOptions();
        //配置不显示浏览器
        edgeOptions.addArguments("--headless");

        EdgeDriver edgeDriver = new EdgeDriver(edgeOptions);
        Map<String, Object> params = new HashMap<>();
        params.put("behavior", "allow");
        params.put("downloadPath", "C:\\Users\\"+UserName+"\\Downloads");
        edgeDriver.executeCdpCommand("Browser.setDownloadBehavior",params);

        edgeDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
//        edgeDriver.
        //获取页面
        edgeDriver.get(url);

        //页面的自动化代码
//*[@id="header-login-btn"]
        WebElement login = edgeDriver.findElement(By.xpath("//*[@id=\"header-login-btn\"]/div"));
        login.click();
        //1.
        //Thread.sleep(3000);
        ////*[@id="docs-component-login-container"]/div[2]/div[2]/div/div[1]/div[1]/ul/li[2]/span
        ////*[@id="header-login-btn"]/div"
        System.out.println("正在奔向腾讯文档获取数据...");
        WebElement loginforQQ = edgeDriver.findElement(By.xpath("//*[@id=\"docs-component-login-container\"]/div[2]/div[2]/div/div[1]/div[1]/ul/li[2]/span"));

        loginforQQ.click();

        WebElement frime = edgeDriver.findElement(By.xpath("//*[@id=\"login_frame\"]"));

        //切换到ifrime标签上
        edgeDriver.switchTo().frame(frime);


//        Thread.sleep(1000);
        WebElement loginNow = edgeDriver.findElement(By.className("img_out_focus"));
//        String text = element.getText();
//        System.out.println(text);
        loginNow.click();

        //So far so good

        //这里的等待必须要有，得等页面加载好，等
        //16点24分
        System.out.println("整个过程可能需要1~2分钟，您可以先处理其他事情或起来活动一下，稍后再看...");
        Thread.sleep(5000);
        edgeDriver.navigate().refresh();
        //Thread.sleep(2000);

        WebElement forfileName = edgeDriver.findElement(By.className("form-header-title-content"));
//        String startFilename =  forfileName.getText();
        filename = forfileName.getText()+suffix;

        Thread.sleep(1000);
        ////*[@id="root"]/div[1]/div/div[1]/ul/li[2]
        WebElement statistic = edgeDriver.findElement(By.xpath("//*[@id=\"root\"]/div[1]/div/div[1]/ul/li[2]"));
        statistic.click();

        //这里很容易出bug
        //Thread.sleep(5000);

        ////*[@id="root"]/div[2]/div/div/div[2]/div/div[3]/div/div/div[3]/div/div/div
        //WebElement more = edgeDriver.findElement(By.xpath("//*[@id=\"root\"]/div[2]/div/div/div[1]/div/div[3]/div/div/div[3]/div/div/div"));
        WebElement more = edgeDriver.findElement(By.className("relate-more-options-icon"));
        more.click();

       // Thread.sleep(5000);
//        WebElement num = edgeDriver.findElement(By.className("num"));
//        int total = Integer.parseInt(num.getText());
//        if(total==0){
//            System.out.println("当前表为空哦~");
//            edgeDriver.quit();
//            System.exit(0);
//        }
        //核酸收集Test100（收集结果）.xlsx


        //点完more按钮他得反应一段时间
        //
        Thread.sleep(2000);
        WebElement download = edgeDriver.findElement(By.xpath("//div[text()='导出结果到本地表格']"));
        download.click();

        //到此为止正常来说应该是下载完毕了

        //最容易出问题的俩按钮

        Thread.sleep(2000);

        WebElement sure = edgeDriver.findElement(By.xpath("//div[text()='仅导出表格']"));

        sure.click();


        //对文件是否存在的判断

        long startTime = System.currentTimeMillis();
        //核酸收集Test1（收集结果）
        System.out.println(filepath);
        System.out.println(filename);
        File file = new File(filepath, filename);
        //把对象拉到外面，避免反复创建。
        long endTime;
        //只要文件不存在，且判断超时5000ms直接返回false
        while(!(file.exists())){

            endTime = System.currentTimeMillis();

            //这里注意别减反了啊~
            if(endTime-startTime>8000){
                return false;
            }

            //适当的线程休眠，避免过度执行while浪费内存
            Thread.sleep(1000);


        }
        //关闭资源，之前几次都忘了关，导致越用越卡，在后台运行，用后台管理器才关掉
        edgeDriver.close();
        return true;
    }
}
