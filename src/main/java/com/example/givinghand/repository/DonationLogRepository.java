package com.example.givinghand.repository;

import com.example.givinghand.entity.DonationLog;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

@Stateless
public class DonationLogRepository {

    @PersistenceContext(unitName = "givingHandPU")   // was GivingHandPU
    private EntityManager em;

    public DonationLog save(DonationLog log) {
        em.persist(log);
        return log;
    }

    public List<DonationLog> findByDonorEmail(String email) {
        return em.createQuery(
                "SELECT d FROM DonationLog d WHERE d.donorEmail = :email",
                DonationLog.class
        ).setParameter("email", email).getResultList();
    }
}