package com.example.givinghand.resource;

import com.example.givinghand.entity.Campaign;
import com.example.givinghand.service.CampaignService;
import com.example.givinghand.util.SecurityUtil;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/campaigns")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CampaignResource {

    @Inject
    private CampaignService campaignService;

    @PersistenceContext(unitName = "givingHandPU")
    private EntityManager em;

    @POST
    @Path("/create")
    public Response createCampaign(Map<String, Object> body,
                                   @Context SecurityContext sc) {

        String email = SecurityUtil.getEmail(sc);

        if (email == null || !sc.isUserInRole("organization")) {
            return forbidden("Only organizations can create campaigns.");
        }

        try {
            String title = get(body, "title");
            String description = get(body, "description");
            String category = get(body, "category");

            List<Map<String, Object>> items =
                    (List<Map<String, Object>>) body.get("needed_items");

            Campaign created = campaignService.createCampaign(
                    title, description, category, items, email
            );

            Map<String, Object> resp = new HashMap<String, Object>();
            resp.put("message", "Campaign created successfully");
            resp.put("campaign_id", created.getId());

            return Response.status(201).entity(resp).build();

        } catch (Exception e) {
            return serverError(e);
        }
    }

    @GET
    public Response getCampaigns(@QueryParam("category") String category) {

        List<Campaign> list = campaignService.getOpenCampaigns(category);

        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();

        for (Campaign c : list) {
            Map<String, Object> m = new HashMap<String, Object>();
            m.put("id", c.getId());
            m.put("title", c.getTitle());
            m.put("description", c.getDescription());
            m.put("category", c.getCategory());
            result.add(m);
        }

        return Response.ok(result).build();
    }

    private String get(Map<String, Object> body, String key) {
        Object v = body.get(key);
        return v != null ? v.toString() : null;
    }

    private Response forbidden(String msg) {
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("message", msg);
        return Response.status(403).entity(m).build();
    }

    private Response serverError(Exception e) {
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("message", e.getMessage());
        return Response.status(500).entity(m).build();
    }
}