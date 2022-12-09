package org.flyboy.bells.tower;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.smallrye.mutiny.Uni;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import javax.inject.Inject;
import java.net.ConnectException;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;

/**
 * @author John J. Franey
 */
@QuarkusTest
class BelltowerResourceTest {


    @InjectMock
    LinuxMPC linuxMPC;

    @Inject
    Belltower belltower;

    @BeforeEach
    public void beforeEach() {
        belltower.unlock();
    }

    @Test
    public void testNoConnection() {
        Mockito.when(linuxMPC.mpc(ArgumentMatchers.any(String.class)))
                .thenAnswer(invocation -> { throw new ConnectException("Connection refused: localhost/127.0.0.1:6600"); });
        given()
                .when().get("/mpd?cmd=status")
                .then().statusCode(500).body(Matchers.containsString("Connection refused: localhost/127.0.0.1:6600"));
    }

    @Test
    void testStatus() {

        Mockito.when(linuxMPC.mpc(anyString())).thenReturn(Uni.createFrom().item(List.of("state: stop")));

        given()
                .when().get("/belltower/status")
                .then().statusCode(200).body(Matchers.matchesPattern("\\{\"state\":\"stop\",\"locked\":(true|false)\\}"));
    }


    @Test
    void testLock() {

        Mockito.when(linuxMPC.mpc(anyString())).thenReturn(Uni.createFrom().item(List.of("state: stop")));

        given()
                .when().post("/belltower/lock")
                .then().statusCode(200).body(is("{\"state\":\"stop\",\"locked\":true}"));

    }

    @Test
    void testUnlock() {
        Mockito.when(linuxMPC.mpc(anyString())).thenReturn(Uni.createFrom().item(List.of("state: stop", "OK")));
        given()
                .when().delete("/belltower/lock")
                .then().statusCode(200).body(is("{\"state\":\"stop\",\"locked\":false}"));
    }

    @Test
    public void testRingWhenLocked() {
        // when locked, resource returns http status code LOCKED
        belltower.lock();
        given()
                .when().post("/belltower/ring?name=call-to-mass")
                .then().statusCode(409).body(containsString("Belltower is locked."));
    }

    @Test
    public void testRingWhenBusy() {
        Mockito.when(linuxMPC.mpc(anyString())).thenReturn(Uni.createFrom().item(List.of("state: play", "OK")));
        given()
                .when().post("/belltower/ring?name=call-to-mass")
                .then().statusCode(409).body(containsString("Belltower is busy."));

    }

    private static final List<String> okLine = List.of("OK");

    @Test
    public void testRingSuccess() {
        String sampleName = "call-to-mass";

        Mockito.when(linuxMPC.mpc(anyString())).thenReturn(
                Uni.createFrom().item(List.of("state: stop", "OK")),
                Uni.createFrom().item(List.of("state: play", "OK"))
        );

        given()
                .when().post("/belltower/ring?name=" + sampleName)
                .then().statusCode(200).body(is("{\"state\":\"play\",\"locked\":false}"));

    }

    @Test
    public void testRingFail() {
        String sampleName = "call-to-mass";

        Mockito.when(linuxMPC.mpc(anyString())).thenReturn(
                Uni.createFrom().item(List.of("state: stop", "OK")),
                Uni.createFrom().item(List.of("ACK [33@0] {add} some mock error"))
        );
        given()
                .when().post("/belltower/ring?name=" + sampleName)
                .then().statusCode(500).body(containsString("Failed to play sample, error=33, text=some mock error"));
    }


    @Test
    public void testRingSampleNotFound() {
        String sampleName = "call-to-mass";

        Mockito.when(linuxMPC.mpc(anyString())).thenReturn(
                Uni.createFrom().item(List.of("state: stop", "OK")),
                Uni.createFrom().item(List.of("ACK [50@0] {add} No such directory"))
        );

        given()
                .when().post("/belltower/ring?name=" + sampleName)
                .then().statusCode(404).body(containsString("\\\"call-to-mass\\\" not found."));

    }

}
