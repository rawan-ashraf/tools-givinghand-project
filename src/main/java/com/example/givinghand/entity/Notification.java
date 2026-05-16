package com.example.givinghand.entity;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String eventType;          // el STOCK_LOW_ALERT, DONATION_RECEIVED

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(nullable = false)
    private Instant timestamp;

    // ell user elly el notification de bta3to
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private user recipient;

    // constructors
    public Notification() {}

    public Notification(String eventType, String message, user recipient) {
        this.eventType = eventType;
        this.message = message;
        this.recipient = recipient;
        this.timestamp = Instant.now();
    }

    // el setters w el getters
    public Long getId() { return id; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public user getRecipient() { return recipient; }
    public void setRecipient(user recipient) { this.recipient = recipient; }
}