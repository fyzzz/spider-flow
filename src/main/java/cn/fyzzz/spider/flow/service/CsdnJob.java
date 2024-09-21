package cn.fyzzz.spider.flow.service;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.Data;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 最后签到需要扫码，死了
 *
 * @author fyzzz
 * 2024/9/20 17:17
 */
@Setter
@Service
public class CsdnJob extends AbstractJob{

    @Value("${csdn.username:11111111}")
    private String username;
    @Value("${csdn.password:11111111}")
    private String password;
    @Value("${csdn.headless:true}")
    private Boolean headless;

    @XxlJob("liandiSignIn")
    public void signInJob(){
        logInfo("username: {}", username);
        logInfo("password: {}", password);
        final BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions();
        launchOptions.setHeadless(headless);
        launchOptions.setSlowMo(50);
        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.webkit().launch(launchOptions)) {
            Page page = browser.newPage();
//            page.setViewportSize(1920, 1080);
            page.navigate("https://www.csdn.net");
            logInfo("跳转到: {}", "https://www.csdn.net");
            Locator gotoLogin = page.locator("div.toolbar-container-right > div.onlyUser > div.toolbar-btn-login > a.toolbar-btn-loginfun");
            gotoLogin.click();
            page.waitForTimeout(3000);
            FrameLocator frame = page.frameLocator("iframe[name='passport_iframe']");
            Locator gotoPassword = frame.locator("div.login-box-tabs-items > span:nth-child(4)");
            gotoPassword.waitFor(new Locator.WaitForOptions().setTimeout(5000));
            gotoPassword.click();
            final Locator usernameInput = frame.locator("div.login-form > div:nth-child(1) > div > input");
            final Locator passwordInput = frame.locator("div.login-form > div:nth-child(2) > div > input");
            final Locator registerRule = frame.locator("div.login-inform > div > i");
            usernameInput.waitFor(new Locator.WaitForOptions().setTimeout(5000));
            usernameInput.fill(username);
            page.waitForTimeout(1000);
            passwordInput.click();
            passwordInput.fill(password);
            page.waitForTimeout(1000);
            registerRule.click();
            logInfo("填写用户名密码");
            Locator loginButton = frame.locator("div.login-form > div:nth-child(4) > button");
            loginButton.click();
            page.waitForTimeout(3000);
            page.navigate("https://www.csdn.net/vip");
            logInfo("去签到页面: {}", "https://www.csdn.net/vip");
            Locator signInButton = page.locator("#ClockIn > div.clock-in-records > div.clock-in-btn");
            if (isVisible(signInButton)) {
                signInButton.click();
                page.waitForTimeout(3000);
                logInfo("签到成功");
            } else {
                logInfo("今日已签到");
            }
            Locator result = page.locator("#ClockIn > div.clock-in-records > div.clock-in-times");
            logInfo(result.textContent());
//            final Locator loginButton = page.locator("#loginBtn");
//            loginButton.click();
//            page.waitForTimeout(1000);
//            logInfo("点击登录");
//            page.navigate("https://ld246.com/activity/checkin");
//            logInfo("去签到页面：{}", "https://ld246.com/activity/checkin");
//            Locator parent = page.locator("div.vditor-reset:nth-child(2)");
//            Locator signIn = parent.locator("a.btn.green");
//            if (isVisible(signIn)) {
//                signIn.click();
//                logInfo("签到成功");
//                page.waitForTimeout(3000);
//            } else {
//                logInfo("今日已签到");
//            }
//            final Locator result = parent.locator("div.vditor-reset");
//            logInfo(result.textContent());
//            final Locator balance = page.locator("a.btn");
//            logInfo(balance.textContent());
            TimeUnit.SECONDS.sleep(30);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
