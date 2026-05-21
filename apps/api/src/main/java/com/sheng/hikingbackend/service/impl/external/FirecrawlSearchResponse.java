package com.sheng.hikingbackend.service.impl.external;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FirecrawlSearchResponse(
        boolean success,
        FirecrawlSearchData data,
        String warning,
        String error) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FirecrawlSearchData(
            List<FirecrawlSearchItem> web,
            List<FirecrawlSearchItem> images,
            List<FirecrawlSearchItem> news) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FirecrawlSearchItem(
            String url,
            String title,
            String description,
            String imageUrl,
            String markdown,
            Metadata metadata) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Metadata(
            @JsonProperty("og:image") String ogImage,
            @JsonProperty("imageUrl") String imageUrl,
            String sourceURL) {
    }
}
