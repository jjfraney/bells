package org.flyboy.bells.ringer;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.smallrye.mutiny.Uni;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import java.net.ConnectException;
import java.util.List;

import static io.restassured.RestAssured.given;

/**
 * @author John J. Franey
 */
@QuarkusTest
public class MpdResourceTest {

    @InjectMock
    LinuxMPC linuxMPC;


    @Test
    public void testNoConnection() {
        Mockito.when(linuxMPC.mpc(ArgumentMatchers.any(String.class)))
                .thenAnswer(invocation -> { throw new ConnectException("Connection refused: localhost/127.0.0.1:6600"); });
        given()
                .when().get("/mpd?cmd=status")
                .then().statusCode(500).body(Matchers.containsString("Connection refused: localhost/127.0.0.1:6600"));
    }

    @Test
    public void testStringCommand() {
        List<String> mpcResult = List.of("state=off", "volume=10");

        Mockito.when(linuxMPC.mpc(ArgumentMatchers.any(String.class)))
                .thenReturn(Uni.createFrom().item(mpcResult));

        JsonArrayBuilder builder = Json.createArrayBuilder();
        mpcResult.stream().map(s -> s + "\n").forEach(s -> builder.add(s));
        String expected = builder.build().toString();

        given()
                .when().get("/mpd?cmd=status")
                .then().body(Matchers.is(expected));
    }
}
