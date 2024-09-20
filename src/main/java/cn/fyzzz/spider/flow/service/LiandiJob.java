package cn.fyzzz.spider.flow.service;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @author fyzzz
 * 2024/9/20 17:17
 */
@Service
public class LiandiJob extends AbstractJob{

    @Value("${liandi.username:11111111}")
    public String username;
    @Value("${liandi.password:11111111}")
    public String password;
    @Value("${liandi.headless:true}")
    public Boolean headless;

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
            page.navigate("https://ld246.com/login");
            logInfo("跳转到: {}", "https://ld246.com/login");
            final Locator usernameInput = page.locator("#nameOrEmail");
            final Locator passwordInput = page.locator("#loginPassword");
            usernameInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(5000));
            usernameInput.fill(username);
            page.waitForTimeout(1000);
            passwordInput.click();
            passwordInput.fill(password);
            page.waitForTimeout(1000);
            logInfo("填写用户名密码");
            final Locator loginButton = page.locator("#loginBtn");
            loginButton.click();
            page.waitForTimeout(1000);
            logInfo("点击登录");
            page.navigate("https://ld246.com/activity/checkin");
            logInfo("去签到页面：{}", "https://ld246.com/activity/checkin");
            Locator parent = page.locator("div.vditor-reset:nth-child(2)");
            Locator signIn = parent.locator("a.btn.green");
            if (isVisible(signIn)) {
                signIn.click();
                logInfo("签到成功");
                page.waitForTimeout(3000);
            } else {
                logInfo("今日已签到");
            }
            final Locator result = parent.locator("div.vditor-reset");
            logInfo(result.textContent());
            final Locator balance = page.locator("a.btn");
            logInfo(balance.textContent());
//            TimeUnit.SECONDS.sleep(30);
        }
    }

}
