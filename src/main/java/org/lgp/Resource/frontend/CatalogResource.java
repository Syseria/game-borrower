package org.lgp.Resource.frontend;

import io.quarkus.qute.TemplateInstance;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.lgp.DTO.BoardgameSearchCriteria;
import org.lgp.DTO.PageResponse;
import org.lgp.DTO.BoardgameResponseDTO;
import org.lgp.Service.BoardgameService;

@Path("/catalog")
@Authenticated
public class CatalogResource {

    @Inject
    SecurityIdentity identity;

    @Inject
    BoardgameService boardgameService;

    @CheckedTemplate(requireTypeSafeExpressions = false)
    public static class Templates {
        public static native TemplateInstance index(PageResponse<BoardgameResponseDTO> page);
        public static native TemplateInstance detail(BoardgameResponseDTO game);
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance showCatalog(
            @QueryParam("sortBy") @DefaultValue("title") String sortBy,
            @QueryParam("sortDir") @DefaultValue("asc") String sortDir,
            @QueryParam("lastId") String lastId,
            @QueryParam("firstId") String firstId,
            @QueryParam("isPrevious") @DefaultValue("false") boolean isPrevious
    ) {
        BoardgameSearchCriteria criteria = BoardgameSearchCriteria.builder()
                .sortField(sortBy)
                .sortDir(sortDir)
                .lastId(lastId)
                .firstId(firstId)
                .isPrevious(isPrevious)
                .pageSize(12)
                .build();

        return Templates.index(boardgameService.searchBoardgames(criteria)).data("identity", identity);
    }
}