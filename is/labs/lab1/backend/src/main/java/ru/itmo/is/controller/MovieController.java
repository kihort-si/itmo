package ru.itmo.is.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import ru.itmo.is.dto.request.MovieRequestDto;
import ru.itmo.is.dto.response.MovieResponseDto;
import ru.itmo.is.dto.response.PersonResponseDto;
import ru.itmo.is.service.MovieService;
import ru.itmo.is.websocket.WebSocketMessageType;
import ru.itmo.is.websocket.WebSocketSessionManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/movies")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MovieController {

    @Inject
    private MovieService movieService;

    @Inject
    private WebSocketSessionManager webSocketSessionManager;

    @GET
    @Path("/")
    public Response getAllMovies(@QueryParam("sortBy") String sortBy,
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

        List<MovieResponseDto> movies = movieService.getAllMovies(sortBy, sortDir, filters);
        return Response.ok(movies).build();
    }

    @GET
    @Path("/{id}")
    public Response getMovieById(@PathParam("id") Integer id) {
        MovieResponseDto movie = movieService.getMovieById(id);
        if (movie == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(movie).build();
    }

    @POST
    public Response createMovie(MovieRequestDto movieDTO) {
        MovieResponseDto createdMovie = movieService.createMovie(movieDTO);
        webSocketSessionManager.broadcast(WebSocketMessageType.MOVIE);
        return Response.status(Response.Status.CREATED)
                .entity(createdMovie)
                .build();
    }

    @PUT
    @Path("/{id}")
    public Response updateMovie(@PathParam("id") Integer id, MovieRequestDto movieDTO) {
        MovieResponseDto updatedMovie = movieService.updateMovie(id, movieDTO);
        if (updatedMovie == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        webSocketSessionManager.broadcast(WebSocketMessageType.MOVIE);
        return Response.ok(updatedMovie).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteMovie(@PathParam("id") Integer id) {
        boolean deleted = movieService.deleteMovie(id);
        if (!deleted) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        webSocketSessionManager.broadcast(WebSocketMessageType.MOVIE);
        webSocketSessionManager.broadcast(WebSocketMessageType.PERSON);
        return Response.noContent().build();
    }

    @DELETE
    @Path("/by-genre/{genre}")
    public Response deleteOneByGenre(@PathParam("genre") String genre) {
        boolean deleted = movieService.deleteOneByGenre(genre);
        if (!deleted) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\":\"Фильм с таким жанром не найден\"}")
                    .build();
        }
        webSocketSessionManager.broadcast(WebSocketMessageType.MOVIE);
        return Response.ok()
                .entity("{\"message\":\"Фильм удален\"}")
                .build();
    }

    @GET
    @Path("/count-golden-palm-winners")
    public Response getGoldenPalmWinnersCount() {
        long count = movieService.countGoldenPalmWinners();
        return Response.ok()
                .entity("{\"totalGoldenPalms\":" + count + "}")
                .build();
    }

    @GET
    @Path("/golden-palm-count-less-than/{count}")
    public Response getMoviesWithGoldenPalmCountLessThan(@PathParam("count") long count) {
        List<MovieResponseDto> movies = movieService.getMoviesWithGoldenPalmCountLessThan(count);
        return Response.ok(movies).build();
    }

    @GET
    @Path("/screenwriters-without-oscars")
    public Response getScreenwritersWithoutOscars() {
        List<PersonResponseDto> screenwriters = movieService.getScreenwritersWithoutOscars();
        return Response.ok(screenwriters).build();
    }

    @POST
    @Path("/redistribute-oscars")
    public Response redistributeOscars(@QueryParam("from") String fromGenre,
                                       @QueryParam("to") String toGenre) {
        if (fromGenre == null || toGenre == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\":\"Не указаны жанры\"}")
                    .build();
        }

        boolean success = movieService.redistributeOscars(fromGenre, toGenre);
        if (!success) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\":\"Ошибка при перераспределении\"}")
                    .build();
        }

        webSocketSessionManager.broadcast(WebSocketMessageType.MOVIE);

        return Response.ok()
                .entity("{\"message\":\"Оскары успешно перераспределены\"}")
                .build();
    }
}
