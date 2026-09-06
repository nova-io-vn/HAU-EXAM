package com.userservice.application.port.in;

import com.userservice.application.dto.RegistrationCommand;
public interface RegistrationUseCase { boolean createFromRegistration(RegistrationCommand command); }
