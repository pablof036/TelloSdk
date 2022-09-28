package io.github.pablof036.tellosdk.camera;

import io.github.pablof036.tellosdk.api.TelloApi;
import nu.pattern.OpenCV;
import org.opencv.core.Mat;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import java.awt.*;
import java.io.File;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * Manages the drone's video stream.
 */
public class Camera {
    static {
        OpenCV.loadLocally();
    }

    private final TelloApi telloApi;
    private VideoCapture capture;
    private Timer captureTimer;
    private final ReentrantLock lastImageMutex = new ReentrantLock();
    private Mat lastImage;

    public Camera(TelloApi telloApi) {
        this.telloApi = telloApi;
    }

    /**
     * Starts the video stream
     *
     * @param onNewImage callback which will be used each time an image is received
     */
    public CompletableFuture<Void> startStream(Consumer<Image> onNewImage) {
        Objects.requireNonNull(onNewImage);
        return telloApi
                .startVideoStream()
                .thenApply(u -> {
                    capture = new VideoCapture();
                    capture.open("udp://0.0.0.0:11111", Videoio.CAP_FFMPEG);
                    this.captureTimer = new Timer();
                    captureTimer.scheduleAtFixedRate(
                            new TimerTask() {
                                @Override
                                public void run() {
                                    Mat raw = new Mat();
                                    if (capture.read(raw)) {
                                        setLastImage(raw);
                                        onNewImage.accept(HighGui.toBufferedImage(raw));
                                    }
                                }
                            }, 0, 42
                    );
                    return null;
                });
    }

    /**
     * Stops the video stream.
     */
    public void stopStream() {
        captureTimer.cancel();
        captureTimer.purge();
        captureTimer = null;
        capture.release();
        telloApi.stopVideoStream();
    }

    private Mat getLastImage() {
        Mat copy = new Mat();
        lastImageMutex.lock();
        lastImage.copyTo(copy);
        lastImageMutex.unlock();
        return copy;
    }

    private void setLastImage(Mat image) {
        lastImageMutex.lock();
        lastImage = image;
        lastImageMutex.unlock();
    }

    /**
     * Get the latest image from the stream and saves it in the desired path
     * @param file image destination path
     */
    public void saveImage(File file) {
        if (captureTimer == null) {
            throw new RuntimeException("Video stream must be running to save an image");
        }
        Mat lastImage = getLastImage();
        Imgcodecs.imwrite(file.getAbsolutePath(), lastImage);
    }
}
