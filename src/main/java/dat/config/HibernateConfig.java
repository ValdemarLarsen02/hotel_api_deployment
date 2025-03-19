package dat.config;

import dat.entities.Hotel;
import dat.entities.Room;
import dat.security.entities.Role;
import dat.security.entities.User;
import dat.utils.Utils;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

import java.util.Properties;

public class HibernateConfig {

    private static EntityManagerFactory emf;
    private static EntityManagerFactory emfTest;
    private static Boolean isTest = false;

    public static void setTest(Boolean test) {
        isTest = test;
    }

    public static Boolean getTest() {
        return isTest;
    }

    public static EntityManagerFactory getEntityManagerFactory() {
        if (emf == null)
            emf = createEMF(getTest());
        return emf;
    }

    public static EntityManagerFactory getEntityManagerFactoryForTest() {
        if (emfTest == null){
            setTest(true);
            emfTest = createEMF(getTest());  // No DB needed for test
        }
        return emfTest;
    }

    // TODO: IMPORTANT: Add Entity classes here for them to be registered with Hibernate
    private static void getAnnotationConfiguration(Configuration configuration) {
        configuration.addAnnotatedClass(Hotel.class);
        configuration.addAnnotatedClass(Room.class);
        configuration.addAnnotatedClass(User.class);
        configuration.addAnnotatedClass(Role.class);
    }

    private static EntityManagerFactory createEMF(boolean forTest) {
        try {
            Configuration configuration = new Configuration();
            Properties props = new Properties();
            // Set the properties
            setBaseProperties(props);
            if (forTest) {
                props = setTestProperties(props);
            } else if (System.getenv("DEPLOYED") != null) {
                setDeployedProperties(props);
            } else {
                props = setDevProperties(props);
            }
            configuration.setProperties(props);
            getAnnotationConfiguration(configuration);

            ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                    .applySettings(configuration.getProperties())
                    .build();
            SessionFactory sf = configuration.buildSessionFactory(serviceRegistry);
            EntityManagerFactory emf = sf.unwrap(EntityManagerFactory.class);
            return emf;
        }
        catch (Throwable ex) {
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    private static Properties setBaseProperties(Properties props) {
        props.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        props.put("hibernate.connection.driver_class", "org.postgresql.Driver");
        props.put("hibernate.hbm2ddl.auto", "update");
        props.put("hibernate.current_session_context_class", "thread");
        props.put("hibernate.show_sql", "true");
        props.put("hibernate.format_sql", "true");
        props.put("hibernate.use_sql_comments", "true");
        return props;
    }

    private static Properties setDeployedProperties(Properties props) {
        String DBName = System.getenv("DB_NAME");
        props.setProperty("hibernate.connection.url", System.getenv("CONNECTION_STR") + DBName);
        props.setProperty("hibernate.connection.username", System.getenv("DB_USERNAME"));
        props.setProperty("hibernate.connection.password", System.getenv("DB_PASSWORD"));
        return props;
    }

    private static Properties setDevProperties(Properties props) {
        String DBName = Utils.getPropertyValue("DB_NAME", "config.properties");
        String DBHost = Utils.getPropertyValue("DB_HOST", "config.properties");
        String DBPort = Utils.getPropertyValue("DB_PORT", "config.properties");
        String DBUsername = Utils.getPropertyValue("DB_USERNAME", "config.properties");
        String DBPassword = Utils.getPropertyValue("DB_PASSWORD", "config.properties");
        String DBUseSSL = Utils.getPropertyValue("DB_USE_SSL", "config.properties");

        String jdbcUrl = "jdbc:postgresql://" + DBHost + ":" + DBPort + "/" + DBName;

        if ("true".equalsIgnoreCase(DBUseSSL != null ? DBUseSSL.trim() : "")) {
            jdbcUrl += "?sslmode=require";
        }


        props.put("hibernate.connection.url", jdbcUrl);
        props.put("hibernate.connection.username", DBUsername);
        props.put("hibernate.connection.password", DBPassword);
        return props;
    }


    private static Properties setTestProperties(Properties props) {
        String DBName = Utils.getPropertyValue("DB_NAME", "config.properties");
        String DBHost = Utils.getPropertyValue("DB_HOST", "config.properties");
        String DBPort = Utils.getPropertyValue("DB_PORT", "config.properties");
        String DBUsername = Utils.getPropertyValue("DB_USERNAME", "config.properties");
        String DBPassword = Utils.getPropertyValue("DB_PASSWORD", "config.properties");
        String DBUseSSL = Utils.getPropertyValue("DB_USE_SSL", "config.properties");

        String jdbcUrl = "jdbc:postgresql://" + DBHost + ":" + DBPort + "/" + DBName;

        if ("true".equalsIgnoreCase(DBUseSSL != null ? DBUseSSL.trim() : "")) {
            jdbcUrl += "?sslmode=require";
        }

        props.put("hibernate.connection.url", jdbcUrl);
        props.put("hibernate.connection.username", DBUsername);
        props.put("hibernate.connection.password", DBPassword);
        props.put("hibernate.archive.autodetection", "class");
        props.put("hibernate.show_sql", "true");
        props.put("hibernate.hbm2ddl.auto", "create-drop"); // Eller "validate" afhængigt af dine behov
        return props;
    }

}
