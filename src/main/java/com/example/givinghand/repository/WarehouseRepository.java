package com.example.givinghand.repository;

import com.example.givinghand.entity.Warehouse;
import com.example.givinghand.entity.WarehouseItem;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@Stateless
public class WarehouseRepository {

    @PersistenceContext(unitName = "givingHandPU")  // was missing unitName
    private EntityManager em;

    public Warehouse findById(Long id) {
        return em.find(Warehouse.class, id);
    }

    public Warehouse save(Warehouse warehouse) {
        if (warehouse.getId() == null) {
            em.persist(warehouse);
            return warehouse;
        }
        return em.merge(warehouse);
    }

    public List<Warehouse> findByOwnerEmail(String email) {
        return em.createQuery(
                        "SELECT w FROM Warehouse w WHERE w.organization.email = :email", Warehouse.class)
                .setParameter("email", email)
                .getResultList();
    }

    public WarehouseItem findItemByNameAndWarehouse(Long warehouseId, String itemName) {
        List<WarehouseItem> results = em.createQuery(
                        "SELECT wi FROM WarehouseItem wi WHERE wi.warehouse.id = :wid AND wi.itemName = :name",
                        WarehouseItem.class)
                .setParameter("wid", warehouseId)
                .setParameter("name", itemName)
                .getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    public WarehouseItem saveItem(WarehouseItem item) {
        if (item.getId() == null) {
            em.persist(item);
            return item;
        }
        return em.merge(item);
    }

    public void updateQuantity(Long warehouseId, String itemName, int newQty) {
        em.createQuery(
                        "UPDATE WarehouseItem wi SET wi.quantity = :qty " +
                                "WHERE wi.warehouse.id = :wid AND wi.itemName = :name")
                .setParameter("qty", newQty)
                .setParameter("wid", warehouseId)
                .setParameter("name", itemName)
                .executeUpdate();
    }
}