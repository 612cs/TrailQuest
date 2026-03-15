package com.sheng.hikingbackend.common;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PageResponse<T> {

    private List<T> list;
    private long pageNum;
    private long pageSize;
    private long total;
    private long totalPages;

    public static <T> PageResponse<T> of(List<T> list, long pageNum, long pageSize, long total) {
        long totalPages = pageSize == 0 ? 0 : (long) Math.ceil((double) total / pageSize);
        return PageResponse.<T>builder()
                .list(list)
                .pageNum(pageNum)
                .pageSize(pageSize)
                .total(total)
                .totalPages(totalPages)
                .build();
    }
}
