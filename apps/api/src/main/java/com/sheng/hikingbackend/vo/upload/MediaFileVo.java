package com.sheng.hikingbackend.vo.upload;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.sheng.hikingbackend.entity.MediaFile;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MediaFileVo {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long mediaId;
    private String url;
    private String objectKey;
    private String bizType;

    public static MediaFileVo from(MediaFile mediaFile) {
        return MediaFileVo.builder()
                .mediaId(mediaFile.getId())
                .url(mediaFile.getUrl())
                .objectKey(mediaFile.getObjectKey())
                .bizType(mediaFile.getBizType())
                .build();
    }
}
