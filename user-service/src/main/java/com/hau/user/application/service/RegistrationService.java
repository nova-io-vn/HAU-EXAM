package com.hau.user.application.service;

import com.hau.user.application.dto.RegistrationCommand;
import com.hau.user.application.port.in.RegistrationUseCase;
import com.hau.user.application.port.out.ProcessedEventStore;
import com.hau.user.domain.exception.DuplicateUserException;
import com.hau.user.domain.model.UserProfile;
import com.hau.user.domain.repository.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.time.Instant;

@Service
public class RegistrationService implements RegistrationUseCase {
    public static final String EVENT_TYPE = "USER_REGISTRATION_REQUESTED";
    private final UserProfileRepository repository; private final ProcessedEventStore events; private final Clock clock;
    public RegistrationService(UserProfileRepository repository, ProcessedEventStore events, Clock clock) { this.repository = repository; this.events = events; this.clock = clock; }
    @Override @Transactional
    public boolean createFromRegistration(RegistrationCommand c) {
        if (events.exists(c.eventId())) return false;
        if (repository.existsById(c.userId()) || repository.existsByLecturerCode(c.lecturerCode())) {
            events.record(c.eventId(), EVENT_TYPE, Instant.now(clock)); return false;
        }
        if (repository.existsByEmail(c.email())) throw new DuplicateUserException("Email is already in use");
        Instant now = Instant.now(clock);
        repository.save(UserProfile.pending(c.userId(), c.lecturerCode(), c.fullName(), c.dateOfBirth(), c.phone(), c.email(), c.address(), c.avatar(), c.facultyId(), now));
        events.record(c.eventId(), EVENT_TYPE, now); return true;
    }
}
