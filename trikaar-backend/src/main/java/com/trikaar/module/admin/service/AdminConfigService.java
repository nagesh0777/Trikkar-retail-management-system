package com.trikaar.module.admin.service;

import com.trikaar.module.admin.dto.AdminConfigRequest;
import com.trikaar.module.admin.dto.AdminConfigResponse;

import java.util.List;

public interface AdminConfigService {

    AdminConfigResponse createOrUpdateConfig(AdminConfigRequest request);

    List<AdminConfigResponse> getAllConfigs();

    List<AdminConfigResponse> getConfigsByCategory(String category);

    AdminConfigResponse getConfigByKey(String key);

    String getConfigValue(String key, String defaultValue);

    void deleteConfig(String key);
}
