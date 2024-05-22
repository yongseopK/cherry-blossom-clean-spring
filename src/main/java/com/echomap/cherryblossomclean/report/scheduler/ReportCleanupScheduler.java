package com.echomap.cherryblossomclean.report.scheduler;

import com.echomap.cherryblossomclean.report.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReportCleanupScheduler {
    private final ReportRepository reportRepository;

    @Scheduled(cron = "0 0 0 * * *")
    //@Scheduled(fixedRate = 30000)
    public void deleteExpireReports() {
        LocalDateTime expirationTime = LocalDateTime.now().minusHours(24);
        reportRepository.deleteByStatusTrueAndUpdatedAtBefore(expirationTime);
    }
}
