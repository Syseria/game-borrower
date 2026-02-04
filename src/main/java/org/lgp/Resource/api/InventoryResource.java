package org.lgp.Resource.api;

import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.lgp.DTO.*;
import org.lgp.Entity.InventoryItem.Condition;
import org.lgp.Entity.InventoryItem.Status;
import org.lgp.Service.InventoryService;
import java.net.URI;

@Path("/api/inventory")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class InventoryResource {

    @Inject
    InventoryService inventoryService;

    @Inject
    SecurityIdentity identity;

    // =========================================================================
    // READ
    // =========================================================================

    @GET
    public PageResponse<InventoryItemResponseDTO> search(
            @QueryParam("gameId") String gameId,
            @QueryParam("status") String status,
            @QueryParam("condition") String condition,

            // Sorting
            @QueryParam("sortBy") @DefaultValue("gameId") String sortBy,
            @QueryParam("sortDir") @DefaultValue("asc") String sortdir,
            @QueryParam("pageSize") @DefaultValue("20") Integer pageSize,
            @QueryParam("firstId") String firstId,
            @QueryParam("lastId") String lastId,
            @QueryParam("isPrevious") @DefaultValue("false") boolean isPrevious
    ) {
        Status requestedStatus = (status != null) ? Status.fromString(status) : null;
        Condition requestedCondition = (condition != null) ? Condition.fromString(condition) : null;

        if (!identity.hasRole("maintainer")) {
            requestedStatus = Status.AVAILABLE;
        }

        InventorySearchCriteria criteria = InventorySearchCriteria.builder()
                .gameId(gameId)
                .status(requestedStatus)
                .condition(requestedCondition)
                .sortField(sortBy)
                .sortDir(sortdir)
                .pageSize(pageSize)
                .firstId(firstId)
                .lastId(lastId)
                .isPrevious(isPrevious)
                .build();

        return inventoryService.searchInventory(criteria);
    }

    @GET
    @Path("/{id}")
    public InventoryItemResponseDTO get(@PathParam("id") String id) {
        return inventoryService.getItem(id);
    }

    // =========================================================================
    // WRITE (Maintainer Only)
    // =========================================================================

    @POST
    @RolesAllowed("maintainer")
    public Response create(@Valid InventoryItemRequestDTO request) {
        String id = inventoryService.createItem(request);
        return Response.created(URI.create("/inventory/" + id)).build();
    }

    @PUT
    @Path("/{id}/lifecycle")
    @RolesAllowed("maintainer")
    public Response updateLifecycle(@PathParam("id") String id, @Valid InventoryStatusUpdateRequestDTO request) {
        inventoryService.transitionItem(
                id,
                Status.fromString(request.status()),
                Condition.fromString(request.condition()),
                request.details()
        );
        return Response.ok().build();
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("maintainer")
    public Response delete(@PathParam("id") String id) {
        inventoryService.deleteItem(id);
        return Response.noContent().build();
    }
}