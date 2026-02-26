package com.trikaar.module.admin.entity;

import com.trikaar.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Key-value admin configuration per business.
 * Used for system-wide settings like tax rates, business hours, receipt
 * templates, etc.
 */
@Entity
@Table(name = "admin_configs", uniqueConstraints = {
        @UniqueConstraint(name = "uk_admin_config_key_business", columnNames = { "config_key", "business_id" })
}, indexes = {
        @Index(name = "idx_admin_config_business", columnList = "business_id"),
        @Index(name = "idx_admin_config_category", columnList = "category")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminConfig extends BaseEntity {

    @Column(name = "config_key", nullable = false, length = 100)
    private String configKey;

    @Column(name = "config_value", nullable = false, length = 2000)
    private String configValue;

    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "data_type", nullable = false, length = 30)
    @Builder.Default
    private String dataType = "STRING"; // STRING, NUMBER, BOOLEAN, JSON

    @Column(name = "is_editable", nullable = false)
    @Builder.Default
    private boolean editable = true;
}
