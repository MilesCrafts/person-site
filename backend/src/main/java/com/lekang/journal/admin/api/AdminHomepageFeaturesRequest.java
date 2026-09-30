package com.lekang.journal.admin.api;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;

public record AdminHomepageFeaturesRequest(
    @NotNull @Size(max = 5) List<@NotNull @Positive Long> articleIds
) {
}
