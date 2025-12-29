package ru.itmo.is.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import ru.itmo.is.dto.request.PersonRequestDto;
import ru.itmo.is.dto.response.PersonResponseDto;
import ru.itmo.is.service.PersonService;
import ru.itmo.is.websocket.WebSocketMessageType;
import ru.itmo.is.websocket.WebSocketSessionManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/people")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PersonController {

    @Inject
    private PersonService personService;

    @Inject
    private WebSocketSessionManager webSocketSessionManager;

    @GET
    @Path("/")
    public Response getAllPeople(@QueryParam("sortBy") String sortBy,
                                 @QueryParam("sortDir") String sortDir,
                                 @Context UriInfo uriInfo) {
        MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
        Map<String, String> filters = new HashMap<>();

        for (String key : queryParams.keySet()) {
            if (key.startsWith("filter_")) {
                String filterKey = key.substring(7);
                String filterValue = queryParams.getFirst(key);
                filters.put(filterKey, filterValue);
            }
        }

        List<PersonResponseDto> people = personService.getAllPeople(sortBy, sortDir, filters);
        return Response.ok(people).build();
    }

    @GET
    @Path("/{id}")
    public Response getPersonById(@PathParam("id") Long id) {
        PersonResponseDto person = personService.getPersonById(id);
        if (person == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(person).build();
    }

    @POST
    public Response createPerson(PersonRequestDto personDTO) {
        try {
            PersonResponseDto createdPerson = personService.createPerson(personDTO);
            webSocketSessionManager.broadcast(WebSocketMessageType.PERSON);
            return Response.status(Response.Status.CREATED)
                    .entity(createdPerson)
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(e.getMessage())
                    .build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response updatePerson(@PathParam("id") Long id, PersonRequestDto personDTO) {
        try {
            PersonResponseDto updatedPerson = personService.updatePerson(id, personDTO);
            if (updatedPerson == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            webSocketSessionManager.broadcast(WebSocketMessageType.PERSON);
            return Response.ok(updatedPerson).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(e.getMessage())
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response deletePerson(@PathParam("id") Long id) {
        boolean deleted = personService.deletePerson(id);
        if (!deleted) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        webSocketSessionManager.broadcast(WebSocketMessageType.PERSON);
        webSocketSessionManager.broadcast(WebSocketMessageType.MOVIE);
        return Response.noContent().build();
    }
}
