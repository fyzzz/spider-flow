package cn.fyzzz.spider.flow.service;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * ikuuu签到
 *
 * @author fyzzz
 * 2024/9/18 14:14
 */
@Service
public class IkuuuJob extends AbstractJob{

    @Value("${ikuuu.username:11111111}")
    public String username;
    @Value("${ikuuu.password:11111111}")
    public String password;
    @Value("${ikuuu.url:https://ikuuu.pw}")
    public String url;
    @Value("${ikuuu.headless:true}")
    public Boolean headless;

    @XxlJob("ikuuuSignIn")
    public void signInJob(){
        logInfo("username: {}", username);
        logInfo("password: {}", password);
        final BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions();
        launchOptions.setHeadless(headless);
        launchOptions.setSlowMo(50);
        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.webkit().launch(launchOptions)) {
            Page page = browser.newPage();
            page.setViewportSize(1920, 1080);
            page.navigate(url);
            logInfo("跳转到{}", url);
            final Locator usernameInput = page.locator("#email");
            usernameInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(5000));
            final Locator passwordInput = page.locator("#password");
            usernameInput.fill(username);
            passwordInput.fill(password);
            logInfo("填写用户名密码");
            final Locator loginButton = page.locator(".login");
            loginButton.click();
            logInfo("点击登录");
            final Locator model = page.locator("#popup-ann-modal");
            if (isVisible(model)) {
                logInfo("有重要通知");
                final Locator readButton = model.locator(".modal-footer > .btn-primary");
                readButton.click();
            }
            final Locator checkIn = page.locator("#checkin-div > .btn-primary");
            if (isVisible(checkIn) && checkIn.textContent().contains("每日签到")) {
                checkIn.click();
                logInfo("签到成功");
            } else {
                logInfo("今日已签到");
            }
            page.waitForTimeout(3000);
            final Locator cards = page.locator(".card-statistic-2");
            final Locator targetCard = cards.nth(1);
            final Locator span = targetCard.locator(".card-body > span");
            logInfo("剩余流量：{}GB", span.textContent());
        }
    }

}
