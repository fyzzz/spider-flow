package cn.fyzzz.playwright;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import java.nio.file.Paths;

/**
 * todo 描述
 *
 * @author fyzzz
 * @date 2024/9/12 11:27
 */
public class Demo1 {

    public static void main(String[] args) {
        final BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions();
        launchOptions.setHeadless(false);
        launchOptions.setSlowMo(50);
        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.webkit().launch(launchOptions)) {
            Page page = browser.newPage();
            page.navigate("https://baidu.com");
            page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("example.png")));
            while (true);
        }
    }

}
