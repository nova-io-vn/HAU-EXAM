package com.notificationservice.infrastructure.audience;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class UserServiceAudienceResolverTest {
    @Test
    void resolvesCombinedRoleAndFacultyThroughAuthenticatedUserServiceApi() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("http://user-service:8082/api/v1/internal/users/audience?role=SUBJECT_ADMIN&facultyId=CNTT"))
                .andExpect(header("X-Internal-Service-Token", "test-token"))
                .andRespond(withSuccess("{\"success\":true,\"code\":\"SUCCESS\",\"message\":\"ok\",\"data\":[{\"userId\":\"00000000-0000-0000-0000-000000000001\",\"email\":\"admin@hau.edu.vn\"}]}", MediaType.APPLICATION_JSON));
        var resolver = new UserServiceAudienceResolver(builder, "http://user-service:8082", "test-token");

        var recipients = resolver.resolve("SUBJECT_ADMIN", "CNTT");

        assertThat(recipients).singleElement().satisfies(recipient -> {
            assertThat(recipient.userId().toString()).isEqualTo("00000000-0000-0000-0000-000000000001");
            assertThat(recipient.email()).isEqualTo("admin@hau.edu.vn");
        });
        server.verify();
    }
}
