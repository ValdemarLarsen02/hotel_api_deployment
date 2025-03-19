package dat.controllers.impl;

import dat.config.ApplicationConfig;
import dat.config.HibernateConfig;
import dat.daos.impl.HotelDAO;
import dat.dtos.HotelDTO;
import dat.entities.Hotel;
import dat.security.controllers.SecurityController;
import dat.security.daos.SecurityDAO;
import dat.security.entities.User;
import dat.security.exceptions.ValidationException;
import dk.bugelhartmann.UserDTO;
import io.javalin.Javalin;
import io.restassured.common.mapper.TypeRef;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HotelControllerTest {

    private final static EntityManagerFactory emf = HibernateConfig.getEntityManagerFactoryForTest();
    private final static SecurityController securityController = SecurityController.getInstance();
    private final static SecurityDAO securityDAO = new SecurityDAO(emf);
    private static Javalin app;
    private static Hotel[] hotels;
    private static Hotel california, hilton;
    private static UserDTO userDTO, adminDTO;
    private static String userToken, adminToken;
    private static final String BASE_URL = "http://localhost:7070/api";

    @BeforeAll
    void setUpAll() {
        HibernateConfig.setTest(true);

        // Start api
        app = ApplicationConfig.startServer(7070);
    }

    @BeforeEach
    void setUp() {
        System.out.println("🔹 Running setUp()...");

        // Debug konfigurationsværdier
        System.out.println("🔹 DB_HOST: " + System.getenv("DB_HOST"));
        System.out.println("🔹 DB_PORT: " + System.getenv("DB_PORT"));
        System.out.println("🔹 DB_NAME: " + System.getenv("DB_NAME"));
        System.out.println("🔹 DB_USERNAME: " + System.getenv("DB_USERNAME"));
        System.out.println("🔹 DB_PASSWORD: " + System.getenv("DB_PASSWORD"));
        System.out.println("🔹 SECRET_KEY: " + (System.getenv("SECRET_KEY") != null ? "SET ✅" : "❌ NOT FOUND"));
        System.out.println("🔹 ISSUER: " + System.getenv("ISSUER"));
        System.out.println("🔹 TOKEN_EXPIRE_TIME: " + System.getenv("TOKEN_EXPIRE_TIME"));

        // Fortsæt som normalt
        try {
            UserDTO verifiedUser = securityDAO.getVerifiedUser(userDTO.getUsername(), userDTO.getPassword());
            UserDTO verifiedAdmin = securityDAO.getVerifiedUser(adminDTO.getUsername(), adminDTO.getPassword());
            userToken = "Bearer " + securityController.createToken(verifiedUser);
            adminToken = "Bearer " + securityController.createToken(verifiedAdmin);
        } catch (ValidationException e) {
            throw new RuntimeException("❌ ERROR: Could not create token!", e);
        }
    }


    @AfterEach
    void tearDown() {
        try (EntityManager em = emf.createEntityManager()){
            em.getTransaction().begin();
            em.createQuery("DELETE FROM User").executeUpdate();
            em.createQuery("DELETE FROM Room ").executeUpdate();
            em.createQuery("DELETE FROM Hotel ").executeUpdate();
            em.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AfterAll
    void tearDownAll() {
        ApplicationConfig.stopServer(app);
    }

    @Test
    void readAll() {
        System.out.println("usertoken: " + userToken);
        System.out.println("admintoken: " + adminToken);
        List<HotelDTO> hotelDTO =
                given()
                        .when()
                        .header("Authorization", userToken)
                        .get(BASE_URL + "/hotels")
                        .then()
                        .statusCode(200)
                        .body("size()", is(2))
                        .log().all()
                        .extract()
                        .as(new TypeRef<List<HotelDTO>>() {});

        assertThat(hotelDTO.size(), is(2));
        assertThat(hotelDTO.get(0).getHotelName(), is("Hotel California"));
        assertThat(hotelDTO.get(1).getHotelName(), is("Hilton"));
    }
}