package org.lgp.Resource;

import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.lgp.Entity.Boardgame.BoardgameSearchCriteria;
import org.lgp.Entity.Boardgame.BoardgameRequestDTO;
import org.lgp.Entity.Boardgame.BoardgameResponseDTO;
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

    // =========================================================================
    // READ
    // =========================================================================

    @GET
    public List<BoardgameResponseDTO> getAll() { return boardgameService.searchBoardgames(BoardgameSearchCriteria.builder().build()); }

    @GET
    @Path("/{id}")
    public List<BoardgameResponseDTO> get(@PathParam("id") String id) {
        BoardgameSearchCriteria criteria = BoardgameSearchCriteria.builder()
                .id(id)
                .build();
        return boardgameService.searchBoardgames(criteria);
    }

    // =========================================================================
    // WRITE (Maintainer Only)
    // =========================================================================

    @POST
    @RolesAllowed("maintainer")
    public Response create(@Valid BoardgameRequestDTO request) {
        String id = boardgameService.createBoardgame(request);
        return Response.created(URI.create("/games/" + id)).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed("maintainer")
    public Response update(@PathParam("id") String id, @Valid BoardgameRequestDTO request) {
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