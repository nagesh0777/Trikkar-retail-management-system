package com.trikaar.shared.audit;

import com.trikaar.shared.context.TenantContext;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Provides the current user's ID for JPA @CreatedBy / @LastModifiedBy auditing.
 * Reads from TenantContext which is populated from the JWT authentication
 * filter.
 */
@Component
public class AuditorAwareImpl implements AuditorAware<UUID> {

    @Override
    public Optional<UUID> getCurrentAuditor() {
        return Optional.ofNullable(TenantContext.getUserId());
    }
}
