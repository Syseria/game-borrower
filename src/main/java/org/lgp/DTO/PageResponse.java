package org.lgp.DTO;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.List;

@RegisterForReflection
public record PageResponse<T>(
        List<T> data,
        String firstId,
        String lastId,
        boolean hasMore
) {}
