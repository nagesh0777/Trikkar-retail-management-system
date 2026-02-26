package com.trikaar.module.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminConfigRequest {

    @NotBlank(message = "Config key is required")
    @Size(max = 100)
    private String configKey;

    @NotBlank(message = "Config value is required")
    @Size(max = 2000)
    private String configValue;

    @NotBlank(message = "Category is required")
    @Size(max = 50)
    private String category;

    @Size(max = 500)
    private String description;

    private String dataType;
}
