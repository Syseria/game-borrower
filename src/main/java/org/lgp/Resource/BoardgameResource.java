package org.lgp.Resource;

import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.lgp.Entity.Boardgame.BoardgameRequest;
import org.lgp.Entity.Boardgame.BoardgameResponse;
import org.lgp.Service.BoardgameService;

import java.net.URI;
import java.util.List;

@Path("/games")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class BoardgameResource {

    @Inject
    BoardgameService boardgameService;

    @GET
    public List<BoardgameResponse> getAll() {
        return boardgameService.getAllBoardgames();
    }

    @GET
    @Path("/{id}")
    public BoardgameResponse get(@PathParam("id") String id) {
        return boardgameService.getBoardgame(id);
    }

    @POST
    @RolesAllowed("maintainer")
    public Response create(@Valid BoardgameRequest request) {
        String id = boardgameService.createBoardgame(request);
        return Response.created(URI.create("/games/" + id)).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed("maintainer")
    public Response update(@PathParam("id") String id, @Valid BoardgameRequest request) {
        boardgameService.updateBoardgame(id, request);
        return Response.ok().build();
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("maintainer")
    public Response delete(@PathParam("id") String id) {
        boardgameService.deleteBoardgame(id);
        return Response.noContent().build();
    }
}