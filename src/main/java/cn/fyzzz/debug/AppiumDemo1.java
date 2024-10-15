package cn.fyzzz.debug;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.net.MalformedURLException;
import java.net.URL;

public class AppiumDemo1 {

    public static void main(String[] args) throws MalformedURLException {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability("platformName", "Android");
        capabilities.setCapability("deviceName", "192.168.56.103:5555");
        capabilities.setCapability("appPackage", "com.xingin.xhs");
        capabilities.setCapability("appActivity", "com.xingin.login.activity.WelcomeActivity");
        AndroidDriver androidDriver = new AndroidDriver(new URL("http://192.168.5.131:4723/wd/hub"), capabilities);
    }

}
