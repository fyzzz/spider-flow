package cn.fyzzz.spider.flow.service;

import cn.fyzzz.spider.flow.pojo.JuejinParam;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.BoundingBox;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Rect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static cn.fyzzz.spider.flow.common.util.ImageUtil.gaussianBlur;
import static cn.fyzzz.spider.flow.common.util.ImageUtil.gray;

/**
 *
 * @author fyzzz
 * 2024/9/12 14:20
 */
@Data
@Service
@EqualsAndHashCode(callSuper = true)
public class JinjiangJob extends AbstractJob{

    @Value("${jingjiang.username:1111111}")
    private String defaultUsername;
    @Value("${jingjiang.password:1111111}")
    private String defaultPassword;
    @Value("${juejin.url:https://my.jjwxc.net/login.php}")
    private String url;
    @Value("${juejin.headless:true}")
    private Boolean headless;
    @Value("${juejin.debug:false}")
    private Boolean debug;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @XxlJob("jingjiangSignIn")
    public void signInJob(){
        // 设置参数
        String jobParam = XxlJobHelper.getJobParam();
        String username = null;
        String password = null;
        if (StrUtil.isNotBlank(jobParam)) {
            try {
                JuejinParam juejinParam = objectMapper.readValue(jobParam, JuejinParam.class);
                username = juejinParam.getUsername();
                password = juejinParam.getPassword();
            } catch (JsonProcessingException e) {
                logError("任务参数解析异常，使用默认参数。", e);
            }
        }
        if (StrUtil.isEmpty(username)) {
            username = defaultUsername;
        }
        if (StrUtil.isEmpty(password)) {
            password = defaultPassword;
        }
        logInfo("username: {}", username);
        logInfo("password: {}", password);
        final BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions();
        launchOptions.setHeadless(headless);
        launchOptions.setSlowMo(50);
        long serial = System.currentTimeMillis();
        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.webkit().launch(launchOptions);
             BrowserContext browserContext = browser.newContext();) {
            Page page = browserContext.newPage();
//            page.setViewportSize(1920, 1080);
            page.navigate(url);
            logInfo("跳转到{}", url);
            if (Boolean.TRUE.equals(debug)) {
                page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get( serial+"goto.png")));
            }
            final Locator usernameInput = page.locator("input[name='loginname']");
            usernameInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(10000));
            final Locator passwordInput = page.locator("input[name='loginpassword']");
            final Locator registerRule = page.locator("input#loginregisterRule");
            usernameInput.click();
            usernameInput.fill(username);
            passwordInput.click();
            passwordInput.fill(password);
            registerRule.click();
            logInfo("填写用户名密码");
            final Locator loginButton = page.locator("button#window_loginbutton");
            loginButton.click();
            logInfo("点击登录, 等待页面响应10s");
            page.waitForTimeout(10000);
            Locator signSpan = page.locator("div#t_user_signin > span");
            signSpan.click();
            page.waitForTimeout(5000);
            List<Page> pages = browserContext.pages();
            Page signPage = pages.get(pages.size()-1);
            Locator signImg = signPage.locator("div#signin-button > img");
            signImg.click();
            page.waitForTimeout(3000);
            Locator result = signPage.locator("body > div.banner > div > div:nth-child(2)");
            logInfo(result.textContent());
            TimeUnit.SECONDS.sleep(30);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



}
