package com.amr.book.book;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.lang.NonNull;

public record BookRequest(
        Integer id,
        @NotNull(message="100")
        @NotEmpty(message = "100")
        String title,
        @NotNull(message="101")
        @NotEmpty(message = "101")
        String authorName,
        @NotNull(message="102")
        @NotEmpty(message = "102")
        String isbn,
        @NotNull(message="103")
        @NotEmpty(message = "103")
        String synopsis,
        @NotNull(message="104")
        @NotEmpty(message = "104")
        boolean shareable

) {

}
