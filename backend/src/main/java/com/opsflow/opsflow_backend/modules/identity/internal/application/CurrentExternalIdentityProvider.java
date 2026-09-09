package com.opsflow.opsflow_backend.modules.identity.internal.application;

import com.opsflow.opsflow_backend.modules.identity.internal.domain.ExternalIdentity;

public interface CurrentExternalIdentityProvider {

    ExternalIdentity getCurrent();
}
