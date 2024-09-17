package cn.fyzzz.spider.flow.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import com.xxl.job.core.context.XxlJobHelper;
import groovy.util.logging.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * todo 描述
 *
 * @author fyzzz
 * @date 2024/9/12 17:34
 */
@Slf4j
public abstract class AbstractJob {

    private static Map<Class<?>, Logger> loggerMap = new ConcurrentHashMap<>();

    private String userDir = System.getProperty("user.dir");

    public void logInfo(String info, Object... vars) {
        final Logger logger = loggerMap.computeIfAbsent(getClass(), LoggerFactory::getLogger);
        final String message = StrUtil.format(info, vars);
        logger.info(message);
        XxlJobHelper.log(message);
    }

    public void logError(String info, Throwable e, Object... vars) {
        logInfo(info, vars);
        XxlJobHelper.log(e);
    }

    public File download(String url) {
        final String uuid = UUID.randomUUID().toString();
        File file = new File(userDir, uuid);
        HttpUtil.downloadFileFromUrl(url, file);
        return file;
    }

}
