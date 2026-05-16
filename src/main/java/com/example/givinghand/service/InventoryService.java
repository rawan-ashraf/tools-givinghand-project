package com.example.givinghand.service;

import com.example.givinghand.entity.Warehouse;
import com.example.givinghand.entity.WarehouseItem;
import com.example.givinghand.entity.user;
import com.example.givinghand.repository.WarehouseRepository;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

@Stateless
public class InventoryService {

    @Inject
    private WarehouseRepository warehouseRepo;

    @Inject
    private CampaignService campaignService;



    // el creation bta3 warehouse

    public Warehouse createWarehouse(String name, user organization) {
        Warehouse wh = new Warehouse(name, organization);
        return warehouseRepo.save(wh);
    }

    // add aw update inventory item

    public Warehouse addInventoryItem(Long warehouseId, String itemName,
                                      int quantity, String category,
                                      String requesterEmail) {
        Warehouse wh = getOwnedWarehouse(warehouseId, requesterEmail);

        WarehouseItem existing = warehouseRepo.findItemByNameAndWarehouse(warehouseId, itemName);
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + quantity);
            warehouseRepo.saveItem(existing);
        } else {
            WarehouseItem item = new WarehouseItem(itemName, quantity, category, wh);
            wh.getItems().add(item);
        }

        return warehouseRepo.save(wh);
    }

    // el JTA-protected allocation

    @Transactional
    public void allocateResources(Long warehouseId, Long campaignId,
                                  String itemName, int quantity,
                                  String requesterEmail) {

        Warehouse wh = getOwnedWarehouse(warehouseId, requesterEmail);

        // el khatwa 1: check & reduce warehouse stock
        WarehouseItem item = warehouseRepo.findItemByNameAndWarehouse(warehouseId, itemName);
        if (item == null)
            throw new RuntimeException("Item '" + itemName + "' not found in warehouse");
        if (item.getQuantity() < quantity)
            throw new RuntimeException("Insufficient stock. Available: " + item.getQuantity());

        int newQty = item.getQuantity() - quantity;
        warehouseRepo.updateQuantity(warehouseId, itemName, newQty);  // DB write 1

        // el khatwa 2: increase campaign received count
        campaignService.increaseReceivedQuantity(campaignId, itemName, quantity);

        if (newQty < 10) {
            triggerLowStockAlert(wh, itemName, newQty);
        }
    }

    // el view warehouse dashboard

    public List<Warehouse> getWarehousesByOwner(String ownerEmail) {
        return warehouseRepo.findByOwnerEmail(ownerEmail);
    }

    public Warehouse getWarehouseById(Long warehouseId, String requesterEmail) {
        return getOwnedWarehouse(warehouseId, requesterEmail);
    }

    // el internal helpers

    private Warehouse getOwnedWarehouse(Long warehouseId, String requesterEmail) {
        Warehouse wh = warehouseRepo.findById(warehouseId);
        if (wh == null) throw new RuntimeException("Warehouse not found");
        if (!wh.getOrganization().getEmail().equals(requesterEmail))
            throw new SecurityException("Not authorized to access this warehouse");
        return wh;
    }

    private void triggerLowStockAlert(Warehouse wh, String itemName, int remaining) {
        System.out.println("[JMS-PENDING] Low stock alert: " + itemName +
                " in warehouse " + wh.getId() + " = " + remaining);
    }
}