package org.flyboy.bells.tower;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.smallrye.mutiny.Uni;
import musicpd.protocol.Status;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

/**
 * @author John J. Franey
 */
@QuarkusTest
class BelltowerResourceTest {

    @InjectMock
    LinuxMPC linuxMPC;

    public void setup() {
        Status.Response response = new Status().response(List.of("state: stop"), "OK");
        Mockito.when(linuxMPC.mpc(ArgumentMatchers.any(musicpd.protocol.Status.class))).thenReturn(Uni.createFrom().item(response));
    }

    @Test
    void testStatus() {
        setup();
        given()
                .when().get("/belltower/status")
                .then().statusCode(200).body(Matchers.matchesPattern("\\{\"state\":\"stop\",\"locked\":(true|false)\\}"));
    }


    @Test
    void testLock() {
        setup();
        given()
                .when().post("/belltower/lock")
                .then().statusCode(200).body(is("{\"state\":\"stop\",\"locked\":true}"));

    }

    @Test
    void testUnlock() {
        setup();
        given()
                .when().delete("/belltower/lock")
                .then().statusCode(200).body(is("{\"state\":\"stop\",\"locked\":false}"));
    }
}
