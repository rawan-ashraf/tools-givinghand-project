package com.example.givinghand.resource;

import com.example.givinghand.entity.Notification;
import com.example.givinghand.entity.user;
import com.example.givinghand.util.SecurityUtil;

import jakarta.ejb.Stateless;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.util.List;

@Stateless
@Path("/notifications")
@Produces(MediaType.APPLICATION_JSON)
public class NotificationResource {

    @PersistenceContext(unitName = "givingHandPU")
    private EntityManager em;

    @GET
    public Response getNotifications(@Context SecurityContext sc) {

        String email = SecurityUtil.getEmail(sc);

        if (email == null) {
            return unauthorized("Not authenticated");
        }

        user u = em.createQuery(
                        "SELECT u FROM user u WHERE u.email = :email", user.class)
                .setParameter("email", email)
                .getResultStream()
                .findFirst()
                .orElse(null);

        if (u == null) {
            return unauthorized("User not found");
        }

        List<Notification> notifications = em.createQuery(
                        "SELECT n FROM Notification n ORDER BY n.timestamp DESC",
                        Notification.class)
                .getResultList();

        JsonArrayBuilder arr = Json.createArrayBuilder();
        System.out.println("User ID: " + u.getId());
        System.out.println("Notifications size: " + notifications.size());
        for (Notification n : notifications) {
            arr.add(Json.createObjectBuilder()
                    .add("event_type", n.getEventType())
                    .add("message", n.getMessage())
                    .add("timestamp", n.getTimestamp().toString()));
        }

        return Response.ok(arr.build()).build();
    }

    private Response unauthorized(String msg) {
        return Response.status(401)
                .entity(Json.createObjectBuilder().add("message", msg).build())
                .build();
    }
}