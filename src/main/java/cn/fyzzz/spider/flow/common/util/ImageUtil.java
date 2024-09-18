package cn.fyzzz.spider.flow.common.util;

import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Size;

import java.io.File;

/**
 * 
 *
 * @author fyzzz
 * 2024/9/6 14:30
 */
public class ImageUtil {


    public static Mat thresh(Mat image, double thresh) {
        Mat thresholdedImage = new Mat();
        opencv_imgproc.threshold(image, thresholdedImage, thresh, 255, opencv_imgproc.THRESH_BINARY);
        return thresholdedImage;
    }

    public static Mat gray(Mat image) {
        Mat grayImage = new Mat();
        opencv_imgproc.cvtColor(image, grayImage, opencv_imgproc.COLOR_BGR2GRAY);
        return grayImage;
    }
    public static Mat gaussianBlur(Mat image) {
        Mat blurredImage = new Mat();
        opencv_imgproc.GaussianBlur(image, blurredImage, new Size(5, 5), 0);
        return blurredImage;
    }


    public static void showImage(Mat edgesImage) {
        // 将 BufferedImage 转换为 JavaCV 的 Frame
        try (OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat()) {
            Frame frame = converter.convert(edgesImage);
            // 创建一个窗口来显示图片
            CanvasFrame canvas = new CanvasFrame("Image Viewer", 1);
            canvas.showImage(frame);
            // 等待窗口关闭
            canvas.waitKey(0);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
