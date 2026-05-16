package com.example.givinghand.resource;

import com.example.givinghand.entity.Warehouse;
import com.example.givinghand.entity.WarehouseItem;
import com.example.givinghand.entity.user;
import com.example.givinghand.util.SecurityUtil;

import jakarta.ejb.Stateless;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.util.List;
import java.util.Map;

@Stateless
@Path("/warehouse")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class warehouseResource {

    @PersistenceContext(unitName = "givingHandPU")
    private EntityManager em;

    @POST
    @Path("/create")
    public Response createWarehouse(Map<String, String> body,
                                    @Context SecurityContext sc) {

        user org = authenticateOrganization(sc);
        if (org == null) return forbidden("Only organizations can create warehouses");

        String name = body.get("name");
        if (name == null || name.trim().isEmpty()) {
            return badRequest("Warehouse name required");
        }

        Warehouse w = new Warehouse(name, org);
        em.persist(w);

        return Response.status(201)
                .entity(Json.createObjectBuilder()
                        .add("message", "Warehouse created")
                        .add("warehouse_id", w.getId())
                        .build())
                .build();
    }


    @POST
    @Path("/{id}/add")
    public Response addInventory(@PathParam("id") long id,
                                 Map<String, Object> body,
                                 @Context SecurityContext sc) {

        user org = authenticateOrganization(sc);
        if (org == null) return forbidden("Only organizations allowed");

        Warehouse w = em.find(Warehouse.class, id);
        if (w == null) return notFound("Warehouse not found");

        if (w.getOrganization().getId() != org.getId())
            return forbidden("Not your warehouse");

        String itemName = (String) body.get("item_name");
        String category = (String) body.get("category");

        int quantity;
        try {
            quantity = Integer.parseInt(body.get("quantity").toString());
        } catch (Exception e) {
            return badRequest("Invalid quantity");
        }

        WarehouseItem item = w.findItem(itemName);

        if (item != null) {
            item.setQuantity(item.getQuantity() + quantity);
        } else {
            WarehouseItem newItem = new WarehouseItem(itemName, quantity, category, w);
            em.persist(newItem);
            w.getItems().add(newItem);
        }

        return Response.ok(jsonMsg("Inventory updated")).build();
    }


    @GET
    @Path("/{id}")
    public Response view(@PathParam("id") long id,
                         @Context SecurityContext sc) {

        user org = authenticateOrganization(sc);
        if (org == null) return forbidden("Only organizations allowed");

        Warehouse w = em.find(Warehouse.class, id);
        if (w == null) return notFound("Warehouse not found");

        if (w.getOrganization().getId() != org.getId())
            return forbidden("Not your warehouse");

        JsonArrayBuilder items = Json.createArrayBuilder();

        for (WarehouseItem i : w.getItems()) {
            items.add(Json.createObjectBuilder()
                    .add("item_name", i.getItemName())
                    .add("category", i.getCategory())
                    .add("quantity", i.getQuantity()));
        }

        JsonObjectBuilder res = Json.createObjectBuilder()
                .add("warehouse_id", w.getId())
                .add("warehouse_name", w.getName())
                .add("inventory", items);

        return Response.ok(res.build()).build();
    }


    @GET
    public Response list(@Context SecurityContext sc) {

        user org = authenticateOrganization(sc);
        if (org == null) return forbidden("Only organizations allowed");

        List<Warehouse> list = em.createQuery(
                        "SELECT w FROM Warehouse w WHERE w.organization.id = :id",
                        Warehouse.class)
                .setParameter("id", org.getId())
                .getResultList();

        JsonArrayBuilder arr = Json.createArrayBuilder();

        for (Warehouse w : list) {
            int total = w.getItems().stream()
                    .mapToInt(WarehouseItem::getQuantity)
                    .sum();

            arr.add(Json.createObjectBuilder()
                    .add("warehouse_id", w.getId())
                    .add("warehouse_name", w.getName())
                    .add("total_items", total));
        }

        return Response.ok(arr.build()).build();
    }

    // ================= AUTH =================
    private user authenticateOrganization(SecurityContext sc) {

        String email = SecurityUtil.getEmail(sc);
        if (email == null) return null;

        user u = em.createQuery(
                        "SELECT u FROM user u WHERE u.email = :email", user.class)
                .setParameter("email", email)
                .getResultStream()
                .findFirst()
                .orElse(null);

        if (u == null) return null;

        // manual role check (NO JAAS)
        if (!"organization".equalsIgnoreCase(u.getRole())) {
            return null;
        }

        return u;
    }


    private Response badRequest(String msg) {
        return Response.status(400).entity(jsonMsg(msg)).build();
    }

    private Response forbidden(String msg) {
        return Response.status(403).entity(jsonMsg(msg)).build();
    }

    private Response notFound(String msg) {
        return Response.status(404).entity(jsonMsg(msg)).build();
    }

    private jakarta.json.JsonObject jsonMsg(String msg) {
        return Json.createObjectBuilder()
                .add("message", msg)
                .build();
    }
}