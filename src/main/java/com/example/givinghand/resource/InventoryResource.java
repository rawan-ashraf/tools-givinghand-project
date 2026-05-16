package com.example.givinghand.resource;

import com.example.givinghand.JMS.notificationSender;
import com.example.givinghand.entity.*;
import com.example.givinghand.util.SecurityUtil;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import java.util.HashMap;
import java.util.Map;

@Stateless
@Path("/inventory")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class InventoryResource {

    @PersistenceContext(unitName = "givingHandPU")
    private EntityManager em;

    @Inject

    private notificationSender notificationSender;

    @POST
    @Path("/allocate")
    public Response allocate(Map<String, Object> body,
                             @Context SecurityContext sc) {

        if (!sc.isUserInRole("organization")) {
            return forbidden("Only organizations can allocate resources");
        }

        long warehouseId = Long.parseLong(body.get("warehouse_id").toString());
        long campaignId = Long.parseLong(body.get("campaign_id").toString());
        String itemName = body.get("item_name").toString();
        int quantity = Integer.parseInt(body.get("quantity").toString());

        Warehouse warehouse = em.find(Warehouse.class, warehouseId);

        if (warehouse == null) {
            return notFound("Warehouse not found");
        }

        WarehouseItem item = warehouse.findItem(itemName);

        if (item.getQuantity() < quantity) {
            return badRequest("Insufficient stock");
        }

        item.setQuantity(item.getQuantity() - quantity);

        CampaignNeedItem need = em.createQuery(
                        "SELECT n FROM CampaignNeedItem n WHERE n.campaign.id=:cid AND n.itemName=:name",
                        CampaignNeedItem.class)
                .setParameter("cid", campaignId)
                .setParameter("name", itemName)
                .getSingleResult();

        need.setReceivedQuantity(need.getReceivedQuantity() + quantity);

        Map<String, Object> resp = new HashMap<String, Object>();
        resp.put("message", "Resources allocated successfully");

        return Response.ok(resp).build();
    }

    private Response badRequest(String msg) {
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("message", msg);
        return Response.status(400).entity(m).build();
    }

    private Response forbidden(String msg) {
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("message", msg);
        return Response.status(403).entity(m).build();
    }

    private Response notFound(String msg) {
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("message", msg);
        return Response.status(404).entity(m).build();
    }
}