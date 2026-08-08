package com.acme.modres.scheduling;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.messaging.servicebus.ServiceBusMessage;
import java.time.OffsetDateTime;

public class AzureServiceBusScheduler {
  private static final String NAMESPACE_ENV = "AZURE_SERVICEBUS_NAMESPACE";
  private static final String QUEUE_ENV = "AZURE_SERVICEBUS_QUEUE";

  public void scheduleAvailabilityCheck(String payload, OffsetDateTime scheduledTime) {
    String namespace = System.getenv(NAMESPACE_ENV);
    String queueName = System.getenv(QUEUE_ENV);

    if (namespace == null || namespace.trim().isEmpty() || queueName == null || queueName.trim().isEmpty()) {
      return;
    }

    try (ServiceBusSenderClient senderClient = new ServiceBusClientBuilder()
        .credential(namespace, new DefaultAzureCredentialBuilder().build())
        .sender()
        .queueName(queueName)
        .buildClient()) {
      ServiceBusMessage message = new ServiceBusMessage(payload);
      message.setScheduledEnqueueTime(scheduledTime);
      senderClient.scheduleMessage(message, scheduledTime);
    }
  }
}
