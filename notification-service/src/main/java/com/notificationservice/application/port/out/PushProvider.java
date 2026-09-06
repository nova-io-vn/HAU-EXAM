package com.notificationservice.application.port.out;

import com.notificationservice.domain.model.DeviceToken;
import com.notificationservice.domain.model.Notification;

public interface PushProvider {
    PushResult send(DeviceToken token, Notification notification);
    enum PushResult { SENT, INVALID_TOKEN, FAILED, DISABLED }
}
