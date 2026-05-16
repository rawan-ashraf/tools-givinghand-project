package com.example.givinghand.entity;
import jakarta.persistence.*;

@Entity
@Table(name = "campaign_need_items")
public class CampaignNeedItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String itemName;

    @Column(nullable = false)
    private int targetQuantity;

    @Column(nullable = false)
    private int receivedQuantity = 0;   // hena incremented bel warehouse allocation

    @Column(nullable = false)
    private int committedQuantity = 0;  // w hena incremented lama el donor y commit

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    // 3mlt constructors
    public CampaignNeedItem() {}
    public CampaignNeedItem(String itemName, int targetQuantity, Campaign campaign) {
        this.itemName       = itemName;
        this.targetQuantity = targetQuantity;
        this.campaign       = campaign;
    }

    // w hena helpers
    public int getRemainingNeeded() {
        return Math.max(0, targetQuantity - committedQuantity);
    }

    // getters w setters
    public Long getId() { return id; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public int getTargetQuantity() { return targetQuantity; }
    public void setTargetQuantity(int targetQuantity) { this.targetQuantity = targetQuantity; }

    public int getReceivedQuantity() { return receivedQuantity; }
    public void setReceivedQuantity(int receivedQuantity) { this.receivedQuantity = receivedQuantity; }

    public int getCommittedQuantity() { return committedQuantity; }
    public void setCommittedQuantity(int committedQuantity) { this.committedQuantity = committedQuantity; }

    public Campaign getCampaign() { return campaign; }
    public void setCampaign(Campaign campaign) { this.campaign = campaign; }
}