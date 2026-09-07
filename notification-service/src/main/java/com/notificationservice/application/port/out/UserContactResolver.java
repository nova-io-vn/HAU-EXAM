package com.notificationservice.application.port.out;

import com.notificationservice.application.dto.UserContact;
import java.util.UUID;

public interface UserContactResolver { UserContact resolve(UUID userId); }
