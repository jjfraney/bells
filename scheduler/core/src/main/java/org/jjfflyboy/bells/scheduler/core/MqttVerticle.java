package org.jjfflyboy.bells.scheduler.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectReader;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.TimeZone;

/**
 * @author jfraney
 */
public class MqttVerticle  extends AbstractVerticle {
    private static Logger LOGGER = LoggerFactory.getLogger(MqttVerticle.class);

    private String topic = "bell-tower";
    private String broker = "tcp://iot.eclipse.org:1883";
    private String clientId = "bt9s9s8s08s08s08030";
    private MqttClient mqttClient;
    private MemoryPersistence persistence = new MemoryPersistence();

    @Override
    public void start(Future<Void> startFuture) throws Exception {

        vertx.eventBus().consumer("bell-tower.scheduler.status", status -> {
            LOGGER.debug("got a scheduler status message, {}", status.body());
            Object object = deserializeFromBellTower(status, SchedulerVerticle.SchedulerStatus.class);
            publishToBroker(object);
        });

        mqttClient = new MqttClient(broker, clientId, persistence);

        mqttClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                LOGGER.info("mqtt connection was lost.", cause);
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                String cmd = new String(message.getPayload());
                LOGGER.debug("received command: '{}'", cmd);
                if(cmd.startsWith("play")) {
                    vertx.eventBus().send("bell-tower.player", cmd);
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
            }
        });

        try {
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setAutomaticReconnect(true);
            mqttClient.connect(options);
        } catch(MqttException e) {
            startFuture.fail(e);
        }
        mqttClient.subscribe("bell-tower/ops");


        // when finished, must call this
        startFuture.complete();
    }

    private Object deserializeFromBellTower(Message<Object> status, Class<?> cz) {
        Object object;
        try {
            String bdy = (String)status.body();
            ObjectReader reader = Json.mapper
                    .readerFor(cz)
                    .with(TimeZone.getDefault());
            object = reader.readValue(bdy);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return object;
    }

    private void publishToBroker(Object object) {
        String json;
        try {
            json = Json.mapper
                    .writer()
                    .forType(object.getClass())
                    //.withFeatures(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                    .withDefaultPrettyPrinter()
                    .writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        MqttMessage message = new MqttMessage(json.getBytes());
        try {

            mqttClient.publish(topic, message);
        } catch (MqttException e) {
            LOGGER.error("mqtt client fail.", e);
        }
    }

}
