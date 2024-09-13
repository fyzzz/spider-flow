package cn.fyzzz.spider.flow.service;

import cn.hutool.core.io.FileUtil;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.BoundingBox;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static cn.fyzzz.spider.flow.common.util.ImageUtil.gaussianBlur;
import static cn.fyzzz.spider.flow.common.util.ImageUtil.gray;

/**
 * todo 描述
 *
 * @author fyzzz
 * @date 2024/9/12 14:20
 */
@Service
public class JueJinJob extends AbstractJob{

    @Value("${juejin.username:1111111}")
    private String username;
    @Value("${juejin.password:1111111}")
    private String password;
    @Value("${juejin.url:https://juejin.cn/user/center/signin}")
    private String url;
    @Value("${juejin.headless:true}")
    private Boolean headless;

    public static void main(String[] args) {
        final JueJinJob jueJinJob = new JueJinJob();
        jueJinJob.username = "1111111111";
        jueJinJob.password = "1111111111";
        jueJinJob.signInJob();
        System.out.println(System.getProperty("user.dir"));
    }

    @XxlJob("juejinSignIn")
    public void signInJob() {
        final BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions();
        launchOptions.setHeadless(headless);
        launchOptions.setSlowMo(50);
        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.webkit().launch(launchOptions)) {
            Page page = browser.newPage();
            page.setViewportSize(1920, 1080);
            page.navigate(url);
            logInfo("跳转到{}", url);
            final Locator openLogin = page.locator(".login-button");
            // 等待元素可见，超时时间设为 5000 毫秒
            openLogin.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(5000));
            openLogin.click();
            logInfo("去登录");
            final Locator goPassword = page.locator(".clickable");
            goPassword.click();
            logInfo("切换到密码登录");
            final Locator usernameInput = page.locator("input[name='loginPhoneOrEmail']");
            usernameInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(5000));
            final Locator passwordInput = page.locator("input[name='loginPassword']");
            usernameInput.fill(username);
            passwordInput.fill(password);
            logInfo("填写用户名密码");
            final Locator loginButton = page.locator(".btn-login");
            loginButton.click();
            logInfo("点击登录");
            crack(page);
            page.waitForTimeout(3000);
            final Locator signInButton = page.locator("button.signin.btn");
            try {
                signInButton.waitFor(new Locator.WaitForOptions().setTimeout(3000));
                signInButton.click();
                logInfo("签到成功");
                page.waitForTimeout(3000);
            } catch (Exception e) {
                try {
                    page.locator("button.signedin.btn").waitFor(new Locator.WaitForOptions().setTimeout(3000));
                    logInfo("今日已签到");
                } catch (Exception ex) {
                    logInfo("异常了, {}", ex.getMessage());
                    throw new RuntimeException(ex);
                }
            }
            final Locator span = page.locator("div.figures div.large-card span.figure");
            final String value = span.textContent();
            logInfo("当前矿石数：{}", value);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void crack(Page page) throws InterruptedException {
        logInfo("开始破解验证码");
        for (int i = 0; i < 10; i++) {
            FrameLocator frameLocator = page.frameLocator("iframe");
            final Locator bigImageLocator = frameLocator.locator("#captcha_verify_image");
            final Locator smallImageLocator = frameLocator.locator("#captcha-verify_img_slide");
            final BoundingBox bigImageBox;
            try {
                bigImageBox = bigImageLocator.boundingBox(new Locator.BoundingBoxOptions().setTimeout(5000));
            } catch (TimeoutError e) {
                logInfo("第{}次破解成功", i);
                return;
            }
            logInfo("第{}次尝试", i + 1);
            final String bigImageUrl = bigImageLocator.getAttribute("src");
            final String smallImageUrl = smallImageLocator.getAttribute("src");
            final File bigImage = download(bigImageUrl);
            final File smallImage = download(smallImageUrl);
            final BoundingBox smallImageBox = smallImageLocator.boundingBox();
            final double smallY = smallImageBox.height;
            final double startY = Math.abs(bigImageBox.y - smallImageBox.y);
            final Integer distance = computeX(bigImage, smallImage, smallY, startY);
            logInfo("计算出右移距离：{}", distance);
            // 获取目标元素的定位器
            Locator targetElement = frameLocator.locator(".captcha-slider-icon");
            targetElement.waitFor();
            // 获取鼠标对象
            Mouse mouse = page.mouse();
            // 移动鼠标到目标元素上
            mouse.move(targetElement.boundingBox().x, targetElement.boundingBox().y);
            page.waitForTimeout(300);
            // 长按鼠标
            mouse.down();
            page.waitForTimeout(300);
            // 向右移动一段距离
            mouse.move(targetElement.boundingBox().x + distance, targetElement.boundingBox().y, new Mouse.MoveOptions().setSteps(20));

            page.waitForTimeout(300);
            // 释放鼠标
            mouse.up();
        }
        throw new RuntimeException("破解验证码失败");
    }


    public Integer computeX(File big, File small, double smallY, double startY) {
        try (Mat image = opencv_imgcodecs.imread(big.getAbsolutePath());
             Mat target = opencv_imgcodecs.imread(small.getAbsolutePath());) {
            Mat imageGray = gray(image);
            imageGray = gaussianBlur(imageGray);
            Mat targetGray = gray(target);
            targetGray = gaussianBlur(targetGray);
            Mat result = new Mat();
            int targetRows = targetGray.rows();
            double percent = smallY / targetRows;
            startY = Math.round(startY / percent);
            Mat searchAres = new Mat(imageGray, new Rect(0, (int) startY, image.cols(), targetRows));
            opencv_imgproc.matchTemplate(
                    searchAres, // 搜索区域
                    targetGray, // 模板
                    result // 匹配结果存放的矩阵
                    ,opencv_imgproc.TM_CCOEFF_NORMED
            );

            // 找到最相似的位置
            DoublePointer minVal = new DoublePointer(1);
            DoublePointer maxVal = new DoublePointer(1);
            Point minPt = new Point();
            Point maxPt = new Point();
            opencv_core.minMaxLoc(result, minVal, maxVal, minPt, maxPt, null);

            // 在最相似的位置（这里是 minPt）绘制一个矩形
            opencv_imgproc.rectangle(image, new Rect(minPt.x(), minPt.y() + (int) startY, targetGray.cols(), targetRows), new Scalar(0, 255, 0, 0.0));
            opencv_imgcodecs.imwrite(System.currentTimeMillis() + "_search.jpeg", searchAres);
            opencv_imgcodecs.imwrite(System.currentTimeMillis() + ".jpeg", image);

            return (int) Math.round(minPt.x() * percent);
        } finally {
            FileUtil.del(big);
            FileUtil.del(small);
        }
    }



}
