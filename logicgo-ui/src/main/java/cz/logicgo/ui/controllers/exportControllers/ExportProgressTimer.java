package cz.logicgo.ui.controllers.exportControllers;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.util.Duration;

import static cz.logicgo.core.misc.Messages.getFormatted;


public class ExportProgressTimer {
    private final Label label;
    private final ProgressBar progressBar;
    private final int[] progressCount;
    private Timeline timeline;
    private long startTime;
    private final boolean gen;

    public ExportProgressTimer(Label label, ProgressBar progressBar, int[] progressCount, boolean gen) {
        this.label = label;
        this.progressBar = progressBar;
        this.progressCount = progressCount;
        this.gen = gen;
    }

    public void initialize() {
        if (timeline != null) {
            timeline.stop();
            timeline = null;
        }
        label.setText("0%");
    }

    public void start() {
        startTime = System.currentTimeMillis();

        timeline = new Timeline(new KeyFrame(Duration.millis(500), event -> {
            double progress = progressBar.getProgress();
            int done = progressCount[0];
            int total = progressCount[1];

            if (progress > 0 && progress < 1) {
                long elapsed = System.currentTimeMillis() - startTime;
                long estimatedTotal = (long) (elapsed / progress);
                long remaining = estimatedTotal - elapsed;

                long seconds = remaining / 1000;
                long minutes = seconds / 60;
                seconds = seconds % 60;

                String eta = String.format("%d:%02d", minutes, seconds);
                String base = getFormatted("export.timer.base",
                        done, total, progress * 100, eta);

                if (gen) {
                    base = getFormatted("export.timer.gen", base);
                } else {
                    base = getFormatted("export.timer.image", base);
                }


                label.setText(base);
            } else if (progress >= 1.0) {
                label.setText(getFormatted("export.timer.finished", done, total));
            } else {
                label.setText(String.format("%d/%d - %.0f%%", done, total, progress * 100));
            }
        }));

        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    public void stop() {
        if (timeline != null) {
            timeline.stop();
            timeline = null;
        }
        Platform.runLater(() -> label.setText(getFormatted("export.timer.finished", progressCount[0], progressCount[1])));

    }

}
