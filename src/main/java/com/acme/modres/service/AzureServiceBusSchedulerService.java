package com.acme.modres.service;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Azure Service Bus service for distributed scheduling
 * Replaces java.util.Timer for cloud-native scheduled task execution
 */
@Service
public class AzureServiceBusSchedulerService {

    private final ServiceBusSenderClient senderClient;
    private static final String QUEUE_NAME = "modresorts-scheduled-tasks";

    public AzureServiceBusSchedulerService() {
        String serviceBusNamespace = System.getenv("AZURE_SERVICEBUS_NAMESPACE");
        if (serviceBusNamespace == null || serviceBusNamespace.isEmpty()) {
            serviceBusNamespace = System.getenv("AZURE_SERVICEBUS_FQDN");
        }
        
        if (serviceBusNamespace != null && !serviceBusNamespace.isEmpty()) {
            this.senderClient = new ServiceBusClientBuilder()
                    .credential(serviceBusNamespace, new DefaultAzureCredentialBuilder().build())
                    .sender()
                    .queueName(QUEUE_NAME)
                    .buildClient();
        } else {
            // Fallback: Service Bus not configured, use null (will be handled gracefully)
            this.senderClient = null;
            System.err.println("Warning: Azure Service Bus not configured. Scheduled tasks will not be executed.");
        }
    }

    /**
     * Schedule a task to be executed at a specific time
     * @param taskId Unique identifier for the task
     * @param taskData Task data as JSON string
     * @param scheduledTime When the task should be executed
     */
    public void scheduleTask(String taskId, String taskData, OffsetDateTime scheduledTime) {
        if (senderClient == null) {
            System.err.println("Warning: Cannot schedule task - Service Bus not configured");
            return;
        }

        try {
            ServiceBusMessage message = new ServiceBusMessage(taskData);
            message.setMessageId(taskId);
            message.setScheduledEnqueueTime(scheduledTime);
            
            senderClient.sendMessage(message);
        } catch (Exception e) {
            System.err.println("Error scheduling task: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Schedule a task to be executed after a delay
     * @param taskId Unique identifier for the task
     * @param taskData Task data as JSON string
     * @param delayMillis Delay in milliseconds
     */
    public void scheduleTaskWithDelay(String taskId, String taskData, long delayMillis) {
        OffsetDateTime scheduledTime = OffsetDateTime.now(ZoneOffset.UTC)
                .plus(Duration.ofMillis(delayMillis));
        scheduleTask(taskId, taskData, scheduledTime);
    }

    /**
     * Close the sender client
     */
    public void close() {
        if (senderClient != null) {
            senderClient.close();
        }
    }
}
