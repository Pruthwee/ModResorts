package com.acme.modres.cloud;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;

import java.time.OffsetDateTime;
import java.util.logging.Logger;

/**
 * Facade for Azure Service Bus scheduled messages. This replaces server-local
 * timers with cloud-managed scheduled message delivery when Service Bus settings
 * are present.
 */
public final class AzureServiceBusScheduler {
  private static final Logger LOGGER = Logger.getLogger(AzureServiceBusScheduler.class.getName());

  private static final String CONNECTION_STRING_ENV = "AZURE_SERVICEBUS_CONNECTION_STRING";
  private static final String QUEUE_NAME_ENV = "AZURE_SERVICEBUS_SCHEDULE_QUEUE";

  private AzureServiceBusScheduler() {
  }

  public static boolean scheduleAvailabilityCheck(String payload, OffsetDateTime scheduledEnqueueTime) {
    String connectionString = System.getenv(CONNECTION_STRING_ENV);
    String queueName = System.getenv(QUEUE_NAME_ENV);
    if (connectionString == null || connectionString.trim().isEmpty() || queueName == null || queueName.trim().isEmpty()) {
      LOGGER.info("Azure Service Bus scheduling is not configured; executing availability check synchronously.");
      return false;
    }

    try (ServiceBusSenderClient senderClient = new ServiceBusClientBuilder()
        .connectionString(connectionString)
        .sender()
        .queueName(queueName)
        .buildClient()) {
      senderClient.scheduleMessage(new ServiceBusMessage(payload), scheduledEnqueueTime);
      return true;
    }
  }
}
