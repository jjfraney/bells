package org.flyboy.bells.belfry;

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
        Uni<BellStatus> uni = Uni.createFrom().item(true)
                .onItem()
                .transformToUni(Unchecked.function(value -> {
                    throw new ConnectException("Connection refused: localhost/127.0.0.1:6600");
                }));

        Mockito.when(belltower.getStatus()).thenReturn(uni);
        given()
                .when().get("/belltower")
                .then().statusCode(500).body(Matchers.containsString("Connection refused: localhost/127.0.0.1:6600"));
    }

    @Test
    void testStatus() {

        Uni<BellStatus> uni = Uni.createFrom().item(new BellStatus(false, "stop"));
        Mockito.when(belltower.getStatus()).thenReturn(uni);

        given()
                .when().get("/belltower")
                .then().statusCode(200).body(is("{\"locked\":false,\"status\":\"stop\"}"));
    }


    @Test
    void testLock() {
        Uni<BellStatus> uni = Uni.createFrom().item(new BellStatus(true, "stop"));
        Mockito.when(belltower.lock()).thenReturn(uni);
        given()
                .when().put("/belltower/lock")
                .then().statusCode(200).body(is("{\"locked\":true,\"status\":\"stop\"}"));
    }

    @Test
    void testUnlock() {
        Uni<BellStatus> uni = Uni.createFrom().item(new BellStatus(false, "stop"));
        Mockito.when(belltower.unlock()).thenReturn(uni);

        given()
                .when().delete("/belltower/lock")
                .then().statusCode(200).body(is("{\"locked\":false,\"status\":\"stop\"}"));
    }

    @Test
    public void testRingWhenLocked() {
        Uni<BellStatus> uni = Uni.createFrom().item(true)
            .onItem()
            .transformToUni(value -> {
                //noinspection ReactiveStreamsThrowInOperator
                throw new BellsUnavailableException("Belltower is locked.");
            });

        Mockito.when(belltower.ring(anyString())).thenReturn(uni);

        given()
                .when().put("/belltower/ring?pattern=call-to-mass")
                .then().statusCode(409).body(containsString("Belltower is locked."));
    }

    @Test
    public void testRingWhenBusy() {
        Uni<BellStatus> uni = Uni.createFrom().item(true)
                .onItem()
                .transformToUni(value -> {
                    //noinspection ReactiveStreamsThrowInOperator
                    throw new BellsUnavailableException("Belltower is busy.");
                });

        Mockito.when(belltower.ring(anyString())).thenReturn(uni);
        given()
                .when().put("/belltower/ring?pattern=call-to-mass")
                .then().statusCode(409).body(containsString("Belltower is busy."));

    }

    @Test
    public void testRingSuccess() {
        String sampleName = "call-to-mass";
        Uni<BellStatus> uni = Uni.createFrom().item(new BellStatus(false, "play"));
        Mockito.when(belltower.ring(sampleName)).thenReturn(uni);

        given()
                .when().put("/belltower/ring?pattern=" + sampleName)
                .then().statusCode(200).body(is("{\"locked\":false,\"status\":\"play\"}"));
    }

    @Test
    public void testRingFail() {
        String sampleName = "call-to-mass";

        Uni<BellStatus> uni = Uni.createFrom().item(true)
                .onItem()
                .transformToUni(value -> {
                    //noinspection ReactiveStreamsThrowInOperator
                    throw new BelfryException("Failed to play sample, error=33, text=some mock error");
                });
        Mockito.when(belltower.ring(sampleName)).thenReturn(uni);

        given()
                .when().put("/belltower/ring?pattern=" + sampleName)
                .then().statusCode(500).body(containsString("Failed to play sample, error=33, text=some mock error"));
    }

    @Test
    public void testRingStopSuccess() {
        Uni<BellStatus> uni = Uni.createFrom().item(new BellStatus(false, "stop"));
        Mockito.when(belltower.stop()).thenReturn(uni);

        given()
                .when().delete("/belltower/ring")
                .then().statusCode(200).body(is("{\"locked\":false,\"status\":\"stop\"}"));
    }

    @Test
    public void testRingSampleNotFound() {
        String sampleName = "call-to-mass";
        Uni<BellStatus> uni = Uni.createFrom().item(true)
                .onItem()
                .transformToUni(value -> {
                    //noinspection ReactiveStreamsThrowInOperator
                    throw new BellPatternNotFoundException(sampleName);
                });
        Mockito.when(belltower.ring(sampleName)).thenReturn(uni);

        given()
                .when().put("/belltower/ring?pattern=" + sampleName)
                .then().statusCode(404).body(containsString("\\\"call-to-mass\\\" not found."));

    }
}
