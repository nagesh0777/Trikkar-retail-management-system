package com.trikaar.module.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminConfigResponse {

    private String id;
    private String configKey;
    private String configValue;
    private String category;
    private String description;
    private String dataType;
    private boolean editable;
}
