package com.example.givinghand.resource;

import com.example.givinghand.dto.Request;
import com.example.givinghand.dto.login;
import com.example.givinghand.dto.update;
import com.example.givinghand.entity.user;
import com.example.givinghand.security.TokenUtil;
import com.example.givinghand.service.UserService;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.HashMap;
import java.util.Map;

@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {
    @EJB
    private UserService userService;
    @PermitAll
    @POST // create data
    @Path("/register")
    public Response register(Request req) { // receives JSON input mapped into request

        String result = userService.register(req); // send data to ejb

        if (result.equals("User registered successfully.")) {
            return Response.status(Response.Status.CREATED)//201
                    .entity("{\"message\":\"" + result + "\"}")
                    .build();
        }

        return Response.status(Response.Status.BAD_REQUEST)//error 400
                .entity("{\"message\":\"" + result + "\"}")
                .build();
    }
    @POST
    @Path("/login")
    public Response login(login req) {

        user u = userService.login(req);

        if (u != null) {

            String token = TokenUtil.generateToken(u.getEmail(), u.getRole());

            Map<String, Object> res = new HashMap<String, Object>();
            res.put("message", "Login successful");
            res.put("token", token);
            res.put("role", u.getRole());

            return Response.ok(res).build();
        }

        Map<String, Object> err = new HashMap<String, Object>();
        err.put("message", "Invalid credentials");

        return Response.status(401).entity(err).build();
    }
    @PUT
    @Path("/profile")
    public Response updateProfile(update req) {

        String result = userService.updateProfile(req);

        if (result.equals("Profile updated successfully")) {
            return Response.ok("{\"message\":\"" + result + "\"}").build();
        }

        return Response.status(Response.Status.BAD_REQUEST)
                .entity("{\"message\":\"" + result + "\"}")
                .build();
    }


    @GET
    @Path("/test")
    public String test() {
        return "WORKING";

    }
//    @RolesAllowed("donor")
//    @POST
//    @Path("/donations/commit")
//    public Response commitDonation() {
//        return Response.ok("Donation committed").build();
//    }
//    @RolesAllowed("organization")
//    @POST
//    @Path("/campaign/create")
//    public Response createCampaign() {
//        return Response.ok("Campaign created").build();
//    }



}

