package com.example.givinghand.service;

import com.example.givinghand.entity.Campaign;
import com.example.givinghand.entity.CampaignNeedItem;
import com.example.givinghand.repository.CampaignRepository;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Map;

@Stateless
public class CampaignService {

    @Inject
    private CampaignRepository campaignRepo;

    public Campaign createCampaign(String title, String description, String category,
                                   List<Map<String, Object>> neededItemsData,
                                   String ownerEmail) {
        Campaign campaign = new Campaign();
        campaign.setTitle(title);
        campaign.setDescription(description);
        campaign.setCategory(category);
        campaign.setOwnerEmail(ownerEmail);
        campaign.setStatus(Campaign.CampaignStatus.OPEN);

        if (neededItemsData != null) {
            for (Map<String, Object> itemData : neededItemsData) {
                CampaignNeedItem item = new CampaignNeedItem();
                item.setItemName((String) itemData.get("item_name"));
                Object qty = itemData.get("target_quantity");
                item.setTargetQuantity(qty instanceof Integer ? (Integer) qty : ((Number) qty).intValue());
                item.setCampaign(campaign);
                campaign.getNeededItems().add(item);
            }
        }

        return campaignRepo.save(campaign);
    }

    public void increaseReceivedQuantity(Long campaignId, String itemName, int quantity) {
        Campaign campaign = campaignRepo.findById(campaignId);
        if (campaign == null) throw new RuntimeException("Campaign not found");

        CampaignNeedItem needItem = campaign.getNeededItems().stream()
                .filter(i -> i.getItemName().equalsIgnoreCase(itemName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Item '" + itemName + "' not found in campaign"));

        needItem.setReceivedQuantity(needItem.getReceivedQuantity() + quantity);
        campaignRepo.save(campaign);
    }

    public Campaign updateStatus(Long campaignId, String status, String requesterEmail) {
        Campaign campaign = campaignRepo.findById(campaignId);
        if (campaign == null) throw new RuntimeException("Campaign not found");
        if (!campaign.getOwnerEmail().equals(requesterEmail))
            throw new SecurityException("Not authorized");

        campaign.setStatus(Campaign.CampaignStatus.valueOf(status.toUpperCase()));
        return campaignRepo.save(campaign);
    }

    public Campaign updateNeededItems(Long campaignId, List<Map<String, Object>> itemsData,
                                      String requesterEmail) {
        Campaign campaign = campaignRepo.findById(campaignId);
        if (campaign == null) throw new RuntimeException("Campaign not found");
        if (!campaign.getOwnerEmail().equals(requesterEmail))
            throw new SecurityException("Not authorized");

        campaign.getNeededItems().clear();

        for (Map<String, Object> itemData : itemsData) {
            CampaignNeedItem item = new CampaignNeedItem();
            item.setItemName((String) itemData.get("item_name"));
            Object qty = itemData.get("target_quantity");
            item.setTargetQuantity(qty instanceof Integer ? (Integer) qty : ((Number) qty).intValue());
            item.setCampaign(campaign);
            campaign.getNeededItems().add(item);
        }

        return campaignRepo.save(campaign);
    }

    public List<Campaign> getOpenCampaigns(String category) {
        if (category != null && !category.trim().isEmpty()) {
            return campaignRepo.findByCategory(category);
        }
        return campaignRepo.findAllOpen();
    }

    public Campaign getCampaignById(Long id) {
        Campaign c = campaignRepo.findById(id);
        if (c == null) throw new RuntimeException("Campaign not found");
        return c;
    }
}