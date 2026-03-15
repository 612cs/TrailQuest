package com.sheng.hikingbackend.vo.upload;

import com.sheng.hikingbackend.entity.MediaFile;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MediaFileVo {

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
