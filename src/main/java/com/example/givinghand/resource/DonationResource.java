package com.example.givinghand.resource;

import com.example.givinghand.JMS.notificationSender;
import com.example.givinghand.dto.DonationRequest;
import com.example.givinghand.entity.Donation;
import com.example.givinghand.entity.DonationStatus;
import com.example.givinghand.service.DonationService;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import java.util.*;

@Path("/donations")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class DonationResource {

    @PersistenceContext(unitName = "givingHandPU")
    private EntityManager em;

    @Inject
    private DonationService donationService;

    @Inject
    private notificationSender notificationSender;


    @POST
    @Path("/commit")
    public Response commit(DonationRequest request,
                           @Context SecurityContext sc) {

        String email = getUser(sc);

        if (request == null ||
                request.getCampaignId() == null ||
                request.getItemName() == null ||
                request.getQuantity() <= 0) {

            return Response.status(400).entity(msg("Invalid request", null)).build();
        }

        try {
            Donation donation = donationService.commitToDonate(
                    request.getCampaignId(),
                    request.getItemName(),
                    request.getQuantity(),
                    email
            );

            return Response.ok(msg(
                    "Commitment recorded. Please drop off items at the warehouse.",
                    donation.getId()
            )).build();

        } catch (Exception e) {
            return Response.status(500).entity(msg(e.getMessage(), null)).build();
        }
    }


    @PUT
    @Path("/update/{id}")
    public Response update(@PathParam("id") Long id,
                           DonationRequest request,
                           @Context SecurityContext sc) {

        String email = getUser(sc);

        Donation d = em.find(Donation.class, id);
        if (d == null) return Response.status(404).build();

        if (!d.getDonor().getEmail().equals(email))
            return Response.status(403).build();

        try {
            donationService.updateCommitment(id, request.getQuantity());
            return Response.ok(msg("Commitment updated", id)).build();

        } catch (Exception e) {
            return Response.status(400).entity(msg(e.getMessage(), null)).build();
        }
    }


    @DELETE
    @Path("/delete/{id}")
    public Response delete(@PathParam("id") Long id,
                           @Context SecurityContext sc) {

        String email = getUser(sc);

        Donation d = em.find(Donation.class, id);
        if (d == null) return Response.status(404).build();

        if (!d.getDonor().getEmail().equals(email))
            return Response.status(403).build();

        try {
            donationService.cancelCommitment(id);
            return Response.ok(msg("Commitment cancelled", null)).build();

        } catch (Exception e) {
            return Response.status(400).entity(msg(e.getMessage(), null)).build();
        }
    }


    @PUT
    @Path("/{id}/received")
    public Response markReceived(@PathParam("id") Long id) {
        donationService.updateStatus(id, DonationStatus.RECEIVED);

        Donation d = em.find(Donation.class, id);
        if (d != null) {
            notificationSender.sendDonationReceived(
                    d.getDonor().getEmail(),
                    d.getQuantity(),
                    d.getItemName()
            );
        }

        return Response.ok(msg("Donation marked as RECEIVED", id)).build();
    }

    @PUT
    @Path("/{id}/distributed")
    public Response markDistributed(@PathParam("id") Long id) {
        donationService.updateStatus(id, DonationStatus.DISTRIBUTED);
        return Response.ok(msg("Donation marked as DISTRIBUTED", id)).build();
    }

    @GET
    @Path("/history")
    public Response history(@Context SecurityContext sc) {

        String email = getUser(sc);

        List<Donation> donations = em.createQuery(
                        "SELECT d FROM Donation d WHERE d.donor.email = :email AND d.status = :status",
                        Donation.class)
                .setParameter("email", email)
                .setParameter("status", DonationStatus.DISTRIBUTED)
                .getResultList();

        List<Map<String, Object>> result = new ArrayList<>();

        for (Donation d : donations) {
            Map<String, Object> map = new HashMap<>();
            map.put("campaign_id", d.getCampaign().getId());
            map.put("item_name", d.getItemName());
            map.put("quantity", d.getQuantity());
            map.put("status", d.getStatus().toString());
            result.add(map);
        }

        return Response.ok(result).build();
    }


    private String getUser(SecurityContext sc) {
        return (sc.getUserPrincipal() != null)
                ? sc.getUserPrincipal().getName()
                : "test@donor.com";
    }

    private Map<String, Object> msg(String m, Long id) {
        Map<String, Object> map = new HashMap<>();
        map.put("message", m);
        if (id != null) map.put("donation_id", id);
        return map;
    }
}