package cz.logicgo.ui.misc.appStart;

import java.io.File;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;


public abstract class FileLocker {
    private final String globalDataPath = System.getenv("ProgramData");
    private final String appFolderName = "LogicGo";
    private final String appFolderPath = globalDataPath + File.separator + appFolderName;
    private final String filePath;

    private File semaphoreFile;
    private FileChannel fileChannel;
    private FileLock fileLock;

    public FileLocker(String fileName) {
        this.filePath = appFolderPath + File.separator + fileName;
        this.semaphoreFile = new File(filePath);

        ensureDirectoryExists();
    }

    private void ensureDirectoryExists() {
        File folder = new File(appFolderPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    public String getGlobalDataPath() {
        return globalDataPath;
    }

    public String getAppFolderName() {
        return appFolderName;
    }

    public String getAppFolderPath() {
        return appFolderPath;
    }

    public String getFilePath() {
        return filePath;
    }

    public FileChannel getFileChannel() {
        return fileChannel;
    }

    public void setFileChannel(FileChannel fileChannel) {
        this.fileChannel = fileChannel;
    }

    public FileLock getFileLock() {
        return fileLock;
    }

    public void setFileLock(FileLock fileLock) {
        this.fileLock = fileLock;
    }

    public File getSemaphoreFile() {
        return semaphoreFile;
    }

    public void setSemaphoreFile(File semaphoreFile) {
        this.semaphoreFile = semaphoreFile;
    }

    public boolean isAcquired() {
        return fileLock != null && fileLock.isValid();
    }

    public abstract boolean acquire();

    public abstract void release();
}
