package com.tdc.kafka.kafka_coffee;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

@ApplicationScoped
public class StartServices {
    private Logger LOG = LoggerFactory.getLogger(StartServices.class);

    private Vertx vertx;

    @ConfigProperty(name = "kafka.bootstrap.servers")
    String bootstrapServers;

    @ConfigProperty(name = "kafka.group.id")
    String groupId;

    @ConfigProperty(name = "kafka.order.placed.topic")
    String orderPlacedTopic;

    @ConfigProperty(name = "kafka.order.preparation.finished.topic")
    String orderPreparationFinishedTopic;

    @ConfigProperty(name = "kafka.order.canceled.topic")
    String orderCanceledTopic;

    @ConfigProperty(name = "kafka.stock.updated.topic")
    String stockUpdatedTopic;

    void onStart(@Observes StartupEvent ev) {
        LOG.info("onStart...");
        this.vertx = Vertx.vertx();

        DeploymentOptions mainConfig = new DeploymentOptions();

        mainConfig.setConfig(new JsonObject().put("kafka",
                new JsonObject()
                        .put("bootstrap.servers", bootstrapServers)
                        .put("group.id", groupId)
                        .put("order.placed.topic", orderPlacedTopic)
                        .put("order.preparation.finished.topic", orderPreparationFinishedTopic)
                        .put("order.canceled.topic", orderCanceledTopic)
                        .put("stock.updated.topic", stockUpdatedTopic)
        ));

        LOG.info(mainConfig.getConfig().encodePrettily());

        vertx.rxDeployVerticle(new EventStreamVerticle(), mainConfig)
                .ignoreElement()
                .subscribe();
    }

    void onStop(@Observes ShutdownEvent ev) {
        LOG.info("onStop...");
        this.vertx.close();
    }
}
