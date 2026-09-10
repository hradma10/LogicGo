package cz.logicgo.ui.misc;

import cz.logicgo.core.misc.ReflectionProvider;
import cz.logicgo.core.misc.annotations.PreloadCategory;
import javafx.scene.image.Image;
import org.reflections.Reflections;

import java.io.InputStream;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ImagePreLoader {

    private static final Map<Object, Image> IMAGE_CACHE = new ConcurrentHashMap<>();

    private static final String BASE_PATH = "/cz/logicgo/ui/images/games/";
    private static final String FALLBACK_IMAGE = BASE_PATH + "sudoku/sudoku_classic.png";
    private static final Image FALLBACK = loadFromPath(FALLBACK_IMAGE);
    private static final String[] EXTENSIONS = {".png", ".jpg", ".jpeg"};

    static {
        preloadAll();
    }

    public static void preloadAll() {
        Reflections reflections = ReflectionProvider.getInstance();
        Set<Class<?>> annotatedEnums = reflections.getTypesAnnotatedWith(PreloadCategory.class);

        for (Class<?> cls : annotatedEnums) {
            if (cls.isEnum()) {
                preloadEnum(cls);
            }
        }
    }

    private static void preloadEnum(Class<?> cls) {
        PreloadCategory cat = cls.getAnnotation(PreloadCategory.class);
        String folder = cat.folder();

        if (cat.byName()) {
            for (Object constant : cls.getEnumConstants()) {
                if (constant instanceof Enum<?> c) {
                    String name = getImageName(c, folder);
                    String pathBase = BASE_PATH + folder + "/" + name;

                    loadWithExtensions(c, pathBase);
                }
            }
        }

        for (String extra : cat.extra()) {
            String path = BASE_PATH + folder + "/" + extra;
            Image loadedImage = loadFromPath(path);
            if (loadedImage != null) {
                IMAGE_CACHE.put(extra, loadedImage);
            }
        }
    }

    private static String getImageName(Enum<?> c, String folder) {
        return folder + "_" + c.name().toLowerCase(Locale.ROOT);
    }

    private static void loadWithExtensions(Object key, String pathBase) {
        for (String ext : EXTENSIONS) {
            String fullPath = pathBase + ext;
            try (InputStream in = ImagePreLoader.class.getResourceAsStream(fullPath)) {
                if (in != null) {
                    IMAGE_CACHE.put(key, new Image(in, 256, 256, true, true));
                    return;
                }
            } catch (Exception ignored) {
            }
        }
        IMAGE_CACHE.put(key, FALLBACK);
    }

    private static Image loadFromPath(String path) {
        try (InputStream in = ImagePreLoader.class.getResourceAsStream(path)) {
            return in != null ? new Image(in, 256, 256, true, true) : null;
        } catch (Exception e) {
            return null;
        }
    }

    public static Image get(Enum<?> key) {
        if (key == null) return FALLBACK;

        Image cached = IMAGE_CACHE.get(key);
        if (cached != null && cached != FALLBACK) {
            return cached;
        }

        PreloadCategory cat = key.getClass().getAnnotation(PreloadCategory.class);
        if (cat != null) {
            for (Object constant : key.getClass().getEnumConstants()) {
                if (constant instanceof Enum<?> e && e.name().equalsIgnoreCase("CLASSIC")) {
                    Image classicFallback = IMAGE_CACHE.get(e);
                    if (classicFallback != null && classicFallback != FALLBACK) {
                        return classicFallback;
                    }
                }
            }
        }

        return FALLBACK;
    }

    public static Image getExtra(String extraFileName) {
        return IMAGE_CACHE.getOrDefault(extraFileName, FALLBACK);
    }

    public static void clear() {
        IMAGE_CACHE.clear();
    }
}
