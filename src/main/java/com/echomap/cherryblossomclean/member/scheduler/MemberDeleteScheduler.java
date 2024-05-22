package com.echomap.cherryblossomclean.member.scheduler;

import com.echomap.cherryblossomclean.member.entity.Member;
import com.echomap.cherryblossomclean.member.repository.MemberRepository;
import com.echomap.cherryblossomclean.report.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MemberDeleteScheduler {

    private final MemberRepository memberRepository;
    private final ReportRepository reportRepository;

    @Scheduled(cron = "0 0 0 * * *") // 10분 마다 스케쥴러 실행
    @Transactional
    public void deleteExpireReports() {
        log.info("강제탈퇴 스케쥴러 동작");
        LocalDateTime expirationTime = LocalDateTime.now().minusHours(24);
        List<Member> expiredMembers = memberRepository.findByStatusTrueAndUpdatedAtBefore(expirationTime);

        for (Member member : expiredMembers) {
            reportRepository.deleteByMember(member);

            memberRepository.delete(member);
        }
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void deleteWithdrawnMembers() {
        LocalDateTime expirationDate = LocalDateTime.now().minusDays(7);
        List<Member> expiredMembers = memberRepository.findByIsWithdrawalRequestedTrueAndWithdrawalDateBefore(expirationDate);
        log.info("회원 탈퇴 스케쥴러 동작");
        for (Member member : expiredMembers) {
            reportRepository.deleteByMember(member);
            memberRepository.delete(member);
        }
    }
}
