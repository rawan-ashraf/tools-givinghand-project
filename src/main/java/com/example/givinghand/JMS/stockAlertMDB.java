package com.example.givinghand.JMS;

import com.example.givinghand.entity.Notification;
import com.example.givinghand.entity.Warehouse;
import com.example.givinghand.entity.user;
import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.MessageDriven;
import jakarta.inject.Inject;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.TextMessage;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.io.StringReader;

@MessageDriven(
        name = "StockAlertMDB",
        activationConfig = {
                @ActivationConfigProperty(
                        propertyName = "destinationLookup",
                        propertyValue = "java:/queue/GivingHandNotifications"
                ),
                @ActivationConfigProperty(
                        propertyName = "destinationType",
                        propertyValue = "jakarta.jms.Queue"
                ),
                @ActivationConfigProperty(
                        propertyName = "acknowledgeMode",
                        propertyValue = "Auto-acknowledge"
                )
        }
)
public class stockAlertMDB implements MessageListener {

    @PersistenceContext(unitName = "givingHandPU")
    private EntityManager em;

    // "onMessage"
    @Override
    public void onMessage(Message message) {
        try {
            if (!(message instanceof TextMessage textMessage)) {
                System.err.println("[StockAlertMDB] Received non-text message, ignoring.");
                return;
            }

            String body = textMessage.getText();
            System.out.println("[StockAlertMDB] Received message: " + body);

            // Parse el JSON event
            JsonObject event = Json.createReader(new StringReader(body)).readObject();
            String eventType = event.getString("event_type", "UNKNOWN");
            switch (eventType) {
                case "STOCK_LOW_ALERT"    -> handleLowStockAlert(event);
                case "DONATION_RECEIVED"  -> handleDonationReceived(event);
                default -> System.out.println("[StockAlertMDB] Unknown event type: " + eventType);
            }

        } catch (Exception e) {
            System.err.println("[StockAlertMDB] Error processing message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // el event handlers
    private void handleLowStockAlert(JsonObject event) {
        long warehouseId = event.getJsonNumber("warehouse_id").longValue();
        String itemName  = event.getString("item_name");
        int remaining    = event.getInt("remaining_quantity");
        String msg       = event.getString("message");

        System.out.println("[StockAlertMDB] LOW STOCK ALERT - Warehouse " + warehouseId
                + " | Item: " + itemName + " | Remaining: " + remaining);

        // Look up el warehouse to find el owning organization
        Warehouse warehouse = em.find(Warehouse.class, warehouseId);
        if (warehouse == null) {
            System.err.println("[StockAlertMDB] Warehouse " + warehouseId + " not found, cannot persist notification.");
            return;
        }

        Notification notification = new Notification("STOCK_LOW_ALERT", msg, warehouse.getOrganization());
        em.persist(notification);

        System.out.println("[StockAlertMDB] Notification persisted for org: "
                + warehouse.getOrganization().getEmail());
    }
    private void handleDonationReceived(JsonObject event) {
        String donorEmail = event.getString("donor_email");
        String itemName   = event.getString("item_name");
        int quantity      = event.getInt("quantity");
        String msg        = event.getString("message");

        System.out.println("[StockAlertMDB] DONATION RECEIVED — Donor: " + donorEmail
                + " | Item: " + itemName + " | Qty: " + quantity);

        // Look up el motabr3
        user donor = em.createQuery(
                        "SELECT u FROM user u WHERE u.email = :email", user.class)
                .setParameter("email", donorEmail)
                .getResultStream()
                .findFirst()
                .orElse(null);

        if (donor == null) {
            System.err.println("[StockAlertMDB] Donor " + donorEmail + " not found, cannot persist notification.");
            return;
        }

        Notification notification = new Notification("DONATION_RECEIVED", msg, donor);
        em.persist(notification);

        System.out.println("[StockAlertMDB] Notification persisted for donor: " + donorEmail);
    }
}