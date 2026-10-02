package com.opsflow.opsflow_backend.modules.identity.api;

import java.util.UUID;

public interface TenantAuthorization {

    AuthorizedTenant requireAccess(UUID organizationId);
}
