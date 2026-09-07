package com.userservice.application.port.in;

import com.userservice.application.dto.UserContact;
import java.util.UUID;

public interface UserContactQueryUseCase { UserContact find(UUID userId); }
