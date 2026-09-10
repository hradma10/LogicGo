package cz.logicgo.ui.controllers;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.win32.StdCallLibrary;
import javafx.stage.Stage;

public class WindowsDarkModeHelper {

    private interface Dwmapi extends StdCallLibrary {
        Dwmapi INSTANCE = Native.load("dwmapi", Dwmapi.class);

        int DwmSetWindowAttribute(
                WinDef.HWND hwnd,
                int dwAttribute,
                WinDef.BOOLByReference pvAttribute,
                int cbAttribute
        );
    }

    private interface User32Extra extends StdCallLibrary {
        User32Extra INSTANCE = Native.load("user32", User32Extra.class);

        WinDef.HWND FindWindowA(String lpClassName, String lpWindowName);
    }

    private static final int DWMWA_USE_IMMERSIVE_DARK_MODE = 20;
    private static final int DWMWA_USE_IMMERSIVE_DARK_MODE_OLD = 19;

    public static void enableDarkMode(Stage stage) {
        String os = System.getProperty("os.name").toLowerCase();
        if (!os.contains("win")) {
            return;
        }

        String title = stage.getTitle();
        if (title == null || title.isEmpty()) {
            return;
        }

        WinDef.HWND hwnd = User32Extra.INSTANCE.FindWindowA(null, title);
        if (hwnd != null) {
            WinDef.BOOLByReference darkMode = new WinDef.BOOLByReference(new WinDef.BOOL(true));

            int result = Dwmapi.INSTANCE.DwmSetWindowAttribute(
                    hwnd,
                    DWMWA_USE_IMMERSIVE_DARK_MODE,
                    darkMode,
                    WinDef.BOOL.SIZE
            );

            if (result != 0) {
                Dwmapi.INSTANCE.DwmSetWindowAttribute(
                        hwnd,
                        DWMWA_USE_IMMERSIVE_DARK_MODE_OLD,
                        darkMode,
                        WinDef.BOOL.SIZE
                );
            }
        }
    }
}
