package web.weblab4.controller;

import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import web.weblab4.database.UserDB;
import web.weblab4.models.User;
import web.weblab4.network.request.ResetRequest;
import web.weblab4.network.request.UserRequest;
import web.weblab4.network.response.UserResponse;
import web.weblab4.utils.JwtManager;
import web.weblab4.utils.PasswordHash;
import web.weblab4.utils.ResetTokenManager;

import java.util.Map;
import java.util.Optional;

@Path("/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserController {
    @EJB
    private UserDB userDB;
    @EJB
    private MailController mailController;
    @EJB
    private ResetTokenManager resetTokenManager;
    @EJB
    private JwtManager jwtManager;

    @POST
    @Path("/register")
    public Response register(UserRequest userRequest) {
        if (userRequest.getEmail() == null || userRequest.getPassword() == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Email and password must not be null").build();
        }

        User user = User.builder()
                .email(userRequest.getEmail())
                .password(PasswordHash.hash(userRequest.getPassword()))
                .token(jwtManager.generateRefreshToken(userRequest.getEmail())) // RefreshToken
                .resetToken(resetTokenManager.generateResetToken())
                .build();

        boolean success = userDB.registerUser(user);
        if (success) {
            mailController.sendRegistrationEmail(user.getEmail(), user.getEmail());

            String accessToken = jwtManager.generateAccessToken(userRequest.getEmail());

            return Response.ok(new UserResponse(accessToken, user.getToken())).build();
        } else {
            return Response.status(Response.Status.CONFLICT).entity("User already exists").build();
        }
    }


    @POST
    @Path("/login")
    public Response login(UserRequest userRequest) {
        if (userRequest.getEmail() == null || userRequest.getPassword() == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Email and password must not be null").build();
        }

        Optional<User> user = userDB.getUserByUsername(userRequest.getEmail());
        if (user.isPresent() && PasswordHash.checkPassword(userRequest.getPassword(), user.get().getPassword())) {
            String accessToken = jwtManager.generateAccessToken(user.get().getEmail());
            String refreshToken = jwtManager.generateRefreshToken(user.get().getEmail());

            userDB.setRefreshToken(user.get().getEmail(), refreshToken);

            return Response.ok(new UserResponse(accessToken, refreshToken)).build();
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid email or password").build();
        }
    }


    @POST
    @Path("/reset")
    public Response resetPassword(UserRequest userRequest) {
        if (userRequest.getEmail() == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid email").build();
        }

        Optional<User> user = userDB.getUserByUsername(userRequest.getEmail());
        if (user.isPresent()) {
            mailController.sendPasswordResetEmail(userRequest.getEmail(), user.get().getResetToken());
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid email").build();
        }
    }

    @POST
    @Path("/change")
    public Response changePassword(ResetRequest resetRequest) {
        if (resetRequest.getPassword() == null || resetRequest.getResetToken() == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Email and password must not be null").build();
        }

        String email = userDB.getEmailByResetToken(resetRequest.getResetToken());
        Optional<User> user = userDB.getUserByUsername(email);
        if (user.isPresent() && resetRequest.getResetToken().equals(user.get().getResetToken())) {
            userDB.changePassword(user.get().getEmail(), PasswordHash.hash(resetRequest.getPassword()), resetTokenManager.generateResetToken());
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid email or password").build();
        }
    }

    @POST
    @Path("/refresh")
    public Response refreshToken(Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        if (refreshToken == null || refreshToken.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Refresh token is required").build();
        }

        try {
            String newAccessToken = jwtManager.refreshAccessToken(refreshToken);
            return Response.ok().entity(Map.of("accessToken", newAccessToken)).build();
        } catch (RuntimeException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/check-token")
    public Response checkToken(Map<String, String> request) {
        String token = request.get("token");

        if (token == null || token.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(false).build();
        }

        boolean isValid = userDB.isValidResetToken(token);

        return Response.ok(isValid).build();
    }
}
