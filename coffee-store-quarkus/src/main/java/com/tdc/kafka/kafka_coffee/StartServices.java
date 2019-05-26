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

    @ConfigProperty(name = "mysql.host")
    String mysqlHost;

    @ConfigProperty(name = "mysql.username")
    String mysqlUsername;

    @ConfigProperty(name = "mysql.password")
    String mysqlPassword;

    @ConfigProperty(name = "mysql.database")
    String mysqlDatabase;

    @ConfigProperty(name = "kafka.bootstrap.servers")
    String bootstrapServers;

    @ConfigProperty(name = "kafka.group.id")
    String groupId;

    @ConfigProperty(name = "kafka.order.submitted.topic")
    String orderSubmittedTopic;

    @ConfigProperty(name = "kafka.order.placed.topic")
    String orderPlacedTopic;

    @ConfigProperty(name = "kafka.order.confirmed.topic")
    String orderConfirmedTopic;

    @ConfigProperty(name = "kafka.order.preparation.started.topic")
    String orderPreparationStartedTopic;

    @ConfigProperty(name = "kafka.order.preparation.finished.topic")
    String orderPreparationFinishedTopic;

    @ConfigProperty(name = "kafka.order.canceled.topic")
    String orderCanceledTopic;

    void onStart(@Observes StartupEvent ev) {
        LOG.info("onStart...");
        this.vertx = Vertx.vertx();

        DeploymentOptions mainConfig = new DeploymentOptions();

        mainConfig.setConfig(new JsonObject()
                .put("mysql", new JsonObject()
                        .put("host", mysqlHost)
                        .put("username", mysqlUsername)
                        .put("password", mysqlPassword)
                        .put("database", mysqlDatabase)
                )
                .put("kafka", new JsonObject()
                        .put("bootstrap.servers", bootstrapServers)
                        .put("group.id", groupId)
                        .put("order.submitted.topic", orderSubmittedTopic)
                        .put("order.placed.topic", orderPlacedTopic)
                        .put("order.confirmed.topic", orderConfirmedTopic)
                        .put("order.preparation.started.topic", orderPreparationStartedTopic)
                        .put("order.preparation.finished.topic", orderPreparationFinishedTopic)
                        .put("order.canceled.topic", orderCanceledTopic)
        ));

        LOG.info(mainConfig.getConfig().encodePrettily());

        vertx.rxDeployVerticle(new RepositoryVerticle(), mainConfig)
                .ignoreElement()
                .andThen(vertx.rxDeployVerticle(new EventStreamVerticle(), mainConfig))
                .ignoreElement()
                .andThen(vertx.rxDeployVerticle(new UserInputEventStream(), mainConfig))
                .ignoreElement()
                .subscribe();
    }

    void onStop(@Observes ShutdownEvent ev) {
        LOG.info("onStop...");
        this.vertx.close();
    }
}
