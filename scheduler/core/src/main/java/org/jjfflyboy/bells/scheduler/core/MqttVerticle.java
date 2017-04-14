package org.jjfflyboy.bells.scheduler.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectReader;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
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
        Settings settings = new PropertySettings();
        String clientId = settings.getMqttClientId();
        String broker = settings.getMqttBroker();
        String username = settings.getMqttUserName();
        String password = settings.getMqttPassword();
        String topicRoot = settings.getMqttTopicRoot();

        LOGGER.info("mqtt broker={}, clientId={}, username={}, topicRoot={}", broker, clientId, username, topicRoot);

        if(topicRoot != null) {
            topic = topicRoot + topic;
        }

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

                LOGGER.debug("received command: '{}', topic={}", cmd, topic);
                if(cmd.startsWith("play")) {
                    vertx.eventBus().send("bell-tower.player", cmd);
                }
                if(cmd.startsWith("peal")) {
                    JsonObject msg = new JsonObject()
                            .put("song", "peal-single")
                            .put("command", "play")
                            .put("playTime", 120);
                    vertx.eventBus().send("bell-tower.segmented", msg);
                }
                if(cmd.startsWith("stop")) {
                    JsonObject msg = new JsonObject()
                            .put("command", "stop");
                    vertx.eventBus().send("bell-tower.segmented", msg);
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
            }
        });

        try {
            LOGGER.debug("Connecting to broker");
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setAutomaticReconnect(true);
            if(password != null) {
                options.setPassword(password.toCharArray());
            }
            if(username != null) {
                options.setUserName(username);
            }
            mqttClient.connect(options);
        } catch(MqttException e) {
            LOGGER.error("cannot subscribe.", e);
            startFuture.fail(e);
            return;
        }

        try {
            String opsTopic = topic + "/ops/#";
            LOGGER.debug("subscribing to broker, topic={}", opsTopic);
            mqttClient.subscribe(opsTopic);
        } catch(MqttException e) {
            LOGGER.error("cannot subscribe.", e);
            startFuture.fail(e);
            return;
        }


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
