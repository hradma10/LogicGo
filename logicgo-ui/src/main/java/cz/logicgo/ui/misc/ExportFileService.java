package cz.logicgo.ui.misc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

public class ExportFileService {

    private static String sharedDataPath = System.getenv("ProgramData");
    private static final String appFolderName = "LogicGo";
    public static final String appFolderExportsPath = Paths.get(sharedDataPath, appFolderName, "exports").toString();
    private static final int DELETE_OLDER_THAN = 24;

    static {
        runCleanupAsync();
    }

    public static String getSharedDataPath() {
        return sharedDataPath;
    }

    public static void setSharedDataPath(String sharedDataPath) {
        ExportFileService.sharedDataPath = sharedDataPath;
    }

    public static void resetSharedDataPath() {
        ExportFileService.sharedDataPath = System.getenv("ProgramData");
    }

    public static void runCleanupAsync() {
        Thread.ofVirtual().start(() -> {
            Path path = Paths.get(appFolderExportsPath);
            try {
                if (!Files.exists(path)) return;

                try (Stream<Path> files = Files.list(path)) {
                    files.forEach(ExportFileService::checkAndDelete);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static void copy(Path source, Path target) {
        try {
            if (Files.exists(source)) {

                Files.createDirectories(target.getParent());

                Files.copy(
                        source,
                        target,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.COPY_ATTRIBUTES
                );
            }
        } catch (IOException _) {
        }
    }


    private static void checkAndDelete(Path path) {
        try {
            if (Files.isDirectory(path)) return;

            BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
            Instant creationTime = attrs.creationTime().toInstant();

            if (creationTime.isBefore(Instant.now().minus(DELETE_OLDER_THAN, ChronoUnit.HOURS))) {
                Files.delete(path);
            }
        } catch (IOException e) {
        }
    }
}
