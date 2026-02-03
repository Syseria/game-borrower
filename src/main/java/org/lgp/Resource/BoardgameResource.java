package org.lgp.Resource;

import com.google.api.Page;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.lgp.DTO.BoardgameSearchCriteria;
import org.lgp.DTO.BoardgameRequestDTO;
import org.lgp.DTO.BoardgameResponseDTO;
import org.lgp.DTO.PageResponse;
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
    public PageResponse<BoardgameResponseDTO> search(
            // Filtering
            @QueryParam("id") String id,
            @QueryParam("title") String title,
            @QueryParam("publisher") String publisher,
            @QueryParam("minPlayers") Integer minPlayers,
            @QueryParam("maxPlayers") Integer maxPlayers,
            @QueryParam("minAge") Integer minAge,
            @QueryParam("minTime") Integer minTime,
            @QueryParam("hasVideo") Boolean hasVideo,

            // Sorting
            @QueryParam("sortBy") @DefaultValue("title") String sortBy,
            @QueryParam("sortDir") @DefaultValue("asc") String sortDir,
            @QueryParam("pageSize") @DefaultValue("20") Integer pageSize,
            @QueryParam("firstId") String firstId,
            @QueryParam("lastId") String lastId,
            @QueryParam("isPrevious") @DefaultValue("false") boolean isPrevious
    ) {
        BoardgameSearchCriteria criteria = BoardgameSearchCriteria.builder()
                .id(id)
                .title(title)
                .publisher(publisher)
                .minPlayers(minPlayers)
                .maxPlayers(maxPlayers)
                .minAge(minAge)
                .minTime(minTime)
                .hasVideo(hasVideo)
                .sortField(sortBy)
                .sortDir(sortDir)
                .pageSize(pageSize)
                .firstId(firstId)
                .lastId(lastId)
                .isPrevious(isPrevious)
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