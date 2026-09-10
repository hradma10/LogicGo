package cz.logicgo.ui.misc.appStart;

import javafx.application.Platform;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.FileChannel;

import static cz.logicgo.ui.misc.windows.AlertBox.showOneInstanceDialog;


public class LockFileSemaphore extends FileLocker {
    private static final String LOCK_FILE_NAME = ".lock";
    private static final String PORT_FILE_NAME = ".port";
    private ServerSocket serverSocket;

    private Runnable onFocusRequest;
    private Runnable onTerminateRequest;

    public LockFileSemaphore() {
        super(LOCK_FILE_NAME);
    }

    public void setOnFocusRequest(Runnable onFocusRequest) {
        this.onFocusRequest = onFocusRequest;
    }

    public void setOnTerminateRequest(Runnable onTerminateRequest) {
        this.onTerminateRequest = onTerminateRequest;
    }

    @Override
    public boolean acquire() {
        try {
            Runnable focusRunnable = () -> sendRemoteCommand("FOCUS");
            Runnable terminateRunnable = () -> sendRemoteCommand("TERMINATE");
            File semaphoreFile = this.getSemaphoreFile();
            File appFolder = new File(this.getAppFolderPath());

            if (!(appFolder.exists() || appFolder.mkdirs())) {
                return false;
            }

            if (!(semaphoreFile.exists() || semaphoreFile.createNewFile())) {
                showOneInstanceDialog(focusRunnable, terminateRunnable);
                return false;
            }

            FileOutputStream fileStream = new FileOutputStream(semaphoreFile);
            FileChannel channel = fileStream.getChannel();
            setFileChannel(channel);

            var lock = channel.tryLock();
            setFileLock(lock);

            if (lock != null) {
                startServer();
                return true;
            } else {
                channel.close();
                fileStream.close();

                showOneInstanceDialog(focusRunnable, terminateRunnable);
                return false;
            }

        } catch (IOException e) {
            return false;
        }
    }

    private void startServer() {
        try {
            serverSocket = new ServerSocket(0, 50, InetAddress.getByName("127.0.0.1"));
            int port = serverSocket.getLocalPort();

            File portFile = new File(getAppFolderPath(), PORT_FILE_NAME);
            try (PrintWriter writer = new PrintWriter(portFile)) {
                writer.println(port);
            }
            portFile.deleteOnExit();

            Thread serverThread = new Thread(() -> {
                while (!serverSocket.isClosed()) {
                    try (Socket client = serverSocket.accept();
                         BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()))) {

                        String command = in.readLine();
                        if (command == null) continue;

                        switch (command) {
                            case "FOCUS" -> Platform.runLater(() -> {
                                if (onFocusRequest != null) onFocusRequest.run();
                            });
                            case "TERMINATE" -> Platform.runLater(() -> {
                                if (onTerminateRequest != null) onTerminateRequest.run();
                                release();
                                Platform.exit();
                                System.exit(0);
                            });
                            default -> {
                            }
                        }

                    } catch (IOException ignored) {
                    }
                }
            });
            serverThread.setDaemon(true);
            serverThread.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendRemoteCommand(String command) {
        File portFile = new File(getAppFolderPath(), PORT_FILE_NAME);

        for (int i = 0; i < 5; i++) {
            if (portFile.exists()) break;
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
            }
        }

        if (portFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(portFile))) {
                String portLine = reader.readLine();
                if (portLine != null) {
                    int port = Integer.parseInt(portLine);
                    try (Socket socket = new Socket("127.0.0.1", port);
                         PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
                        out.println(command);
                    }
                }
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public void release() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) serverSocket.close();
            new File(getAppFolderPath(), PORT_FILE_NAME).delete();
            new File(getAppFolderPath(), LOCK_FILE_NAME).delete();
        } catch (IOException ignored) {
        }
    }
}
