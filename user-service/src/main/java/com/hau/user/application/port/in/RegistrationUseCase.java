package com.hau.user.application.port.in;

import com.hau.user.application.dto.RegistrationCommand;
public interface RegistrationUseCase { boolean createFromRegistration(RegistrationCommand command); }
