package com.example.givinghand.service;

import com.example.givinghand.JMS.notificationSender;
import com.example.givinghand.entity.*;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.time.LocalDateTime;

@Stateless
public class DonationService {

    @PersistenceContext(unitName = "givingHandPU")
    private EntityManager em;
    @Inject
    private notificationSender notificationSender;

    public Donation commitToDonate(Long campaignId, String item, int qty, String userEmail) {

        user donor = em.createQuery(
                        "SELECT u FROM user u WHERE u.email = :email", user.class)
                .setParameter("email", userEmail)
                .getResultStream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("User not found"));

        Campaign campaign = em.find(Campaign.class, campaignId);
        if (campaign == null) {
            throw new RuntimeException("Campaign not found");
        }

        // increase committed quantity on the need item
        CampaignNeedItem needItem = findItem(campaign, item);
        needItem.setCommittedQuantity(needItem.getCommittedQuantity() + qty);

        Donation donation = new Donation();
        donation.setCampaign(campaign);
        donation.setItemName(item);
        donation.setQuantity(qty);
        donation.setDonor(donor);
        donation.setStatus(DonationStatus.COMMITTED);

        em.persist(donation);

        return donation;
    }

    public void updateStatus(Long donationId, DonationStatus newStatus) {

        Donation donation = em.find(Donation.class, donationId);
        if (donation == null) {
            throw new RuntimeException("Donation not found");
        }

        DonationStatus current = donation.getStatus();

        // COMMITTED → RECEIVED
        if (current == DonationStatus.COMMITTED && newStatus == DonationStatus.RECEIVED) {

            donation.setStatus(DonationStatus.RECEIVED);

            // update campaign received count
            CampaignNeedItem needItem = findItem(donation.getCampaign(), donation.getItemName());
            needItem.setReceivedQuantity(needItem.getReceivedQuantity() + donation.getQuantity());

            // send notification to donor
            notificationSender.sendDonationReceived(
                    donation.getDonor().getEmail(),
                    donation.getQuantity(),
                    donation.getItemName()
            );
        }

        // RECEIVED → DISTRIBUTED
        else if (current == DonationStatus.RECEIVED && newStatus == DonationStatus.DISTRIBUTED) {

            donation.setStatus(DonationStatus.DISTRIBUTED);

            // save to donation log
            DonationLog log = new DonationLog();
            log.setDonorEmail(donation.getDonor().getEmail());
            log.setCampaignId(donation.getCampaign().getId());
            log.setCampaignTitle(donation.getCampaign().getTitle());
            log.setItemName(donation.getItemName());
            log.setQuantity(donation.getQuantity());
            log.setDistributedAt(LocalDateTime.now());
            em.persist(log);

        }

        else {
            throw new RuntimeException("Invalid status transition from " + current + " to " + newStatus);
        }
    }


    public void updateCommitment(Long donationId, int newQty) {

        Donation d = em.find(Donation.class, donationId);
        if (d == null) throw new RuntimeException("Donation not found");

        if (d.getStatus() != DonationStatus.COMMITTED) {
            throw new RuntimeException("Cannot update after donation is received");
        }

        int oldQty = d.getQuantity();
        int diff   = newQty - oldQty;

        // adjust committed quantity on need item
        CampaignNeedItem needItem = findItem(d.getCampaign(), d.getItemName());
        needItem.setCommittedQuantity(needItem.getCommittedQuantity() + diff);

        d.setQuantity(newQty);
    }


    public void cancelCommitment(Long donationId) {

        Donation d = em.find(Donation.class, donationId);
        if (d == null) throw new RuntimeException("Donation not found");

        if (d.getStatus() != DonationStatus.COMMITTED) {
            throw new RuntimeException("Cannot cancel after donation is received");
        }

        // restore committed quantity on need item
        CampaignNeedItem needItem = findItem(d.getCampaign(), d.getItemName());
        needItem.setCommittedQuantity(needItem.getCommittedQuantity() - d.getQuantity());

        em.remove(d);
    }


    private CampaignNeedItem findItem(Campaign campaign, String itemName) {
        return campaign.getNeededItems()
                .stream()
                .filter(i -> i.getItemName().equalsIgnoreCase(itemName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Item '" + itemName + "' not found in campaign"));
    }
}