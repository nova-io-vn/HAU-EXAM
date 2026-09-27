package com.userservice.application.port.out;

import com.userservice.application.model.SystemBranding;
import java.util.Optional;

public interface SystemBrandingRepository {
    Optional<SystemBranding> find();
    SystemBranding save(SystemBranding branding);
}
