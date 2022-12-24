package org.flyboy.bells.tower;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.net.ConnectException;

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
    Belltower belltower;

    @Test
    public void testNoConnection() {
        Uni<BelltowerStatus> uni = Uni.createFrom().item(true)
                .onItem()
                .transformToUni(Unchecked.function(value -> {
                    throw new ConnectException("Connection refused: localhost/127.0.0.1:6600");
                }));

        Mockito.when(belltower.getStatus()).thenReturn(uni);
        given()
                .when().get("/belltower/status")
                .then().statusCode(500).body(Matchers.containsString("Connection refused: localhost/127.0.0.1:6600"));
    }

    @Test
    void testStatus() {

        Uni<BelltowerStatus> uni = Uni.createFrom().item(new BelltowerStatus(false, "stop"));
        Mockito.when(belltower.getStatus()).thenReturn(uni);

        given()
                .when().get("/belltower/status")
                .then().statusCode(200).body(is("{\"locked\":false,\"status\":\"stop\"}"));
    }


    @Test
    void testLock() {
        Uni<BelltowerStatus> uni = Uni.createFrom().item(new BelltowerStatus(true, "stop"));
        Mockito.when(belltower.getStatus()).thenReturn(uni);
        given()
                .when().post("/belltower/lock")
                .then().statusCode(200).body(is("{\"locked\":true,\"status\":\"stop\"}"));

    }

    @Test
    void testUnlock() {
        Uni<BelltowerStatus> uni = Uni.createFrom().item(new BelltowerStatus(false, "stop"));
        Mockito.when(belltower.getStatus()).thenReturn(uni);

        given()
                .when().delete("/belltower/lock")
                .then().statusCode(200).body(is("{\"locked\":false,\"status\":\"stop\"}"));
    }

    @Test
    public void testRingWhenLocked() {
        Uni<BelltowerStatus> uni = Uni.createFrom().item(true)
            .onItem()
            .transformToUni(value -> {
                //noinspection ReactiveStreamsThrowInOperator
                throw new BelltowerUnavailableException("Belltower is locked.");
            });

        Mockito.when(belltower.ring(anyString())).thenReturn(uni);

        given()
                .when().post("/belltower/ring?name=call-to-mass")
                .then().statusCode(409).body(containsString("Belltower is locked."));
    }

    @Test
    public void testRingWhenBusy() {
        Uni<BelltowerStatus> uni = Uni.createFrom().item(true)
                .onItem()
                .transformToUni(value -> {
                    throw new BelltowerUnavailableException("Belltower is busy.");
                });

        Mockito.when(belltower.ring(anyString())).thenReturn(uni);
        given()
                .when().post("/belltower/ring?name=call-to-mass")
                .then().statusCode(409).body(containsString("Belltower is busy."));

    }

    @Test
    public void testRingSuccess() {
        String sampleName = "call-to-mass";
        Uni<BelltowerStatus> uni = Uni.createFrom().item(new BelltowerStatus(false, "play"));
        Mockito.when(belltower.ring(sampleName)).thenReturn(uni);

        given()
                .when().post("/belltower/ring?name=" + sampleName)
                .then().statusCode(200).body(is("{\"locked\":false,\"status\":\"play\"}"));
    }

    @Test
    public void testRingFail() {
        String sampleName = "call-to-mass";

        Uni<BelltowerStatus> uni = Uni.createFrom().item(true)
                .onItem()
                .transformToUni(value -> {
                    //noinspection ReactiveStreamsThrowInOperator
                    throw new BelltowerException("Failed to play sample, error=33, text=some mock error");
                });
        Mockito.when(belltower.ring(sampleName)).thenReturn(uni);

        given()
                .when().post("/belltower/ring?name=" + sampleName)
                .then().statusCode(500).body(containsString("Failed to play sample, error=33, text=some mock error"));
    }


    @Test
    public void testRingSampleNotFound() {
        String sampleName = "call-to-mass";
        Uni<BelltowerStatus> uni = Uni.createFrom().item(true)
                .onItem()
                .transformToUni(value -> {
                    //noinspection ReactiveStreamsThrowInOperator
                    throw new BelltowerSampleNotFoundException(sampleName);
                });
        Mockito.when(belltower.ring(sampleName)).thenReturn(uni);

        given()
                .when().post("/belltower/ring?name=" + sampleName)
                .then().statusCode(404).body(containsString("\\\"call-to-mass\\\" not found."));

    }

}
