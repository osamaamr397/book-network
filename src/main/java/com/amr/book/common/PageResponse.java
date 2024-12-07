package com.amr.book.common;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageResponse<T> {
    private List<T>content;
    private int number;
    private int size;
    private long totalElement;
    private int totalPages;
    private boolean first;
    private boolean last;

}
