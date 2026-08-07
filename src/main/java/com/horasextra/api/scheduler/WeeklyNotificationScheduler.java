package com.horasextra.api.scheduler;

import com.horasextra.api.service.NotificationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class WeeklyNotificationScheduler {

    private final NotificationService notificationService;

    public WeeklyNotificationScheduler(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Scheduled(cron = "${horasextra.scheduler.weekly-cron:0 0 7 * * MON}")
    public void runWeeklyNotifications() {
        notificationService.notifyCoordinatorsWithPendingRequests();
    }
}
