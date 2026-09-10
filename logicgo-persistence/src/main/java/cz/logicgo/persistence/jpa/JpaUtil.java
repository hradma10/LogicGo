package cz.logicgo.persistence.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class JpaUtil {
    private static final CompletableFuture<EntityManagerFactory> emfFuture = new CompletableFuture<>();

    private static final String sharedDataPath = System.getenv("ProgramData");
    private static final String appFolderName = "LogicGo";
    private static final String appFolderPath = sharedDataPath + File.separator + appFolderName;
    private static final String databaseFile = appFolderPath + File.separator + "data.db";

    private static boolean initialized = false;

    public static synchronized void initAsync() {
        if (initialized) return;
        initialized = true;

        CompletableFuture.runAsync(() -> {
            try {
                ensureDirectoryExists();

                String url = "jdbc:sqlite:" + databaseFile +
                        "?journal_mode=WAL&synchronous=NORMAL&busy_timeout=5000&cache_size=10000";


                Map<String, Object> props = new HashMap<>();
                props.put("jakarta.persistence.jdbc.url", url);
                props.put("jakarta.persistence.jdbc.driver", "org.sqlite.JDBC");
                props.put("hibernate.dialect", "org.hibernate.community.dialect.SQLiteDialect");
                props.put("hibernate.hbm2ddl.auto", "update");
                props.put("hibernate.show_sql", "false");
                props.put("hibernate.format_sql", "false");
                props.put("hibernate.use_sql_comments", "false");
                props.put("hibernate.bytecode.provider", "bytebuddy");
                props.put("hibernate.default_schema", "main");
                props.put("hibernate.boot.allow_jdbc_metadata_access", "false");
                props.put("hibernate.jdbc.batch_size", "30");
                props.put("hibernate.query.plan_cache_max_size", "128");
                props.put("hibernate.connection.provider_class", "hikaricp");
                props.put("hibernate.hikari.maximumPoolSize", "5");
                props.put("hibernate.hikari.minimumIdle", "1");
                props.put("hibernate.hikari.idleTimeout", "30000");
                props.put("hibernate.hikari.connectionTimeout", "5000");
                props.put("hibernate.hikari.maxLifetime", "1800000");

                EntityManagerFactory factory = Persistence.createEntityManagerFactory("logic-games-pu", props);
                emfFuture.complete(factory);
            } catch (Exception e) {
                emfFuture.completeExceptionally(e);
            }
        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (emfFuture.isDone() && !emfFuture.isCompletedExceptionally()) {
                emfFuture.join().close();
            }
        }));
    }

    private static void ensureDirectoryExists() {
        File dir = new File(appFolderPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public static EntityManagerFactory getEntityManagerFactory() {
        try {
            return emfFuture.join();
        } catch (Exception e) {
            throw new RuntimeException("Chyba při inicializaci databáze - EntityManagerFactory selhala", e);
        }
    }

    public static EntityManager getEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }

    public static void warmUp() {
        try {
            emfFuture.join();
        } catch (Exception ignored) {
        }
    }
}
