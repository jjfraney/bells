package org.jjfflyboy.bells.scheduler.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectReader;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
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
    private final String opsTopic = topic + "/ops/#";

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
        vertx.eventBus().consumer("bell-tower.player.status", status -> {
            LOGGER.debug("got a player status message, {}", status.body());
            Object object = deserializeFromBellTower(status, PlayerVerticle.PlayerStatus.class);
            publishToBroker(object);
        });

        mqttClient = new MqttClient(broker, clientId, persistence);

        mqttClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                LOGGER.info("mqtt connection established, serverURI={}, reconnect={}", serverURI, reconnect);
                vertx.executeBlocking(future -> {
                    try {
                        LOGGER.debug("subscribing to broker, topic={}", opsTopic);
                        mqttClient.subscribe(opsTopic);
                    } catch (MqttException e) {
                        LOGGER.error("cannot subscribe.", e);
                        future.fail(e);
                    }
                }, res -> {
                    LOGGER.debug("subscribe succeeds");
                });
            }

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
                } else if(cmd.startsWith("peal")) {
                    JsonObject msg = makeSegmentedSongMessage(cmd, "wedding-peal-reg-interval");
                    vertx.eventBus().send("bell-tower.segmented", msg);
                } else if(cmd.startsWith("toll")) {
                    JsonObject msg = makeSegmentedSongMessage(cmd, "funeral-toll");
                    vertx.eventBus().send("bell-tower.segmented", msg);
                } else if(cmd.startsWith("stop")) {
                    JsonObject msg = new JsonObject()
                            .put("command", "stop");
                    vertx.eventBus().send("bell-tower.segmented", msg);
                } else if(cmd.startsWith("status")) {
                    JsonObject msg = new JsonObject()
                            .put("command", "status");
                    vertx.eventBus().publish("bell-tower", msg);
                }
            }

            /**
             * a beginning, middle and end segments in the player.
             * @param cmd from the mqtt message
             * @param bellSong the root name of the segments
             * @return
             */
            private JsonObject makeSegmentedSongMessage(String cmd, String bellSong) {
                Integer playTime = parsePlayTime(cmd);

                JsonObject msg = new JsonObject()
                        .put("song", bellSong)
                        .put("command", "play");
                if(playTime != null) {
                    msg.put("playTime", playTime);
                }
                return msg;
            }

            private Integer parsePlayTime(String cmd) {
                Integer playTime = null;
                String [] opts = cmd.split(" ");
                if(opts.length == 2) {
                    String playTimeArg = opts[1];
                    try {
                        playTime = Integer.parseInt(playTimeArg);
                    } catch(NumberFormatException e) {
                        LOGGER.error("Cannot parse second field for number.  command={}", cmd);
                    }
                }
                return playTime;
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
            }
        });

        mqttConnect(username, password);

        if(! startFuture.isComplete()) {
            startFuture.complete();
        }
    }

    private void mqttConnect(String username, String password) {
        try {
            LOGGER.debug("Connecting to broker, uri={}", mqttClient.getServerURI());
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
            Integer retryTimeout = 300;
            vertx.setTimer(retryTimeout * 1000, (l) -> {
                mqttConnect(username, password);
            });
            LOGGER.error("cannot connect, error={}, cause exception={}, cause message={}, retry timeout={}",
                    e.getMessage(), e.getCause().getClass().getName(), e.getCause().getMessage(), retryTimeout);
        }
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
            if(mqttClient.isConnected()) {
                mqttClient.publish(topic, message);
            } else {
                LOGGER.error("cannot publish to mqtt broker, client is not connected.");
            }
        } catch (MqttException e) {
            LOGGER.error("mqtt client fail.", e);
        }
    }

}
