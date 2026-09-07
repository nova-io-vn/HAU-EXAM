package com.userservice.infrastructure.rabbitmq;

public final class RabbitNames {
    private RabbitNames() { }
    public static final String AUTH_EXCHANGE="auth.exchange", USER_EXCHANGE="user.exchange", RETRY_EXCHANGE="user.retry.exchange", DLX="user.dlx";
    public static final String REGISTRATION_KEY="user.registration.requested", REGISTRATION_QUEUE="user.registration.requested.queue";
    public static final String RETRY_QUEUE="user.registration.requested.retry.queue", RETRY_KEY="user.registration.requested.retry";
    public static final String DLQ="user.registration.requested.dlq", DLQ_KEY="user.registration.requested.dead";
    public static final String BOOTSTRAP_KEY="user.bootstrap-admin.requested", BOOTSTRAP_QUEUE="user.bootstrap-admin.requested.queue";
    public static final String BOOTSTRAP_RETRY_QUEUE="user.bootstrap-admin.requested.retry.queue", BOOTSTRAP_RETRY_KEY="user.bootstrap-admin.requested.retry";
    public static final String BOOTSTRAP_DLQ="user.bootstrap-admin.requested.dlq", BOOTSTRAP_DLQ_KEY="user.bootstrap-admin.requested.dead";
}
