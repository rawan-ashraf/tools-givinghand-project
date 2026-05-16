package com.example.givinghand.repository;

import com.example.givinghand.entity.Campaign;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

@Stateless
public class CampaignRepository {

    @PersistenceContext(unitName = "givingHandPU")   // was GivingHandPU
    private EntityManager em;

    public Campaign save(Campaign campaign) {
        if (campaign.getId() == null) {
            em.persist(campaign);
            return campaign;
        } else {
            return em.merge(campaign);
        }
    }

    public Campaign findById(Long id) {
        return em.find(Campaign.class, id);
    }

    public List<Campaign> findAllOpen() {
        return em.createQuery(
                        "SELECT c FROM Campaign c WHERE c.status = :status",
                        Campaign.class)
                .setParameter("status", Campaign.CampaignStatus.OPEN)
                .getResultList();
    }

    public List<Campaign> findByCategory(String category) {
        return em.createQuery(
                        "SELECT c FROM Campaign c WHERE c.status = :status AND c.category = :cat",
                        Campaign.class)
                .setParameter("status", Campaign.CampaignStatus.OPEN)
                .setParameter("cat", category)
                .getResultList();
    }
}