package jpabook.jpashop;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

/**
 * 앱 시작 시 DB에 평문으로 저장된 비밀번호를 BCrypt로 마이그레이션한다.
 * $2a$ 접두사가 없는 비밀번호만 처리하므로 이미 암호화된 계정은 건드리지 않는다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PasswordMigrationRunner implements ApplicationRunner {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final DataSource dataSource;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        addActiveColumnIfNotExists();
        insertDefaultNoticesIfEmpty();

        List<Member> members = memberRepository.findAll();
        int count = 0;
        for (Member member : members) {
            String pw = member.getPw();
            if (pw != null && !pw.startsWith("$2a$")) {
                member.setPw(passwordEncoder.encode(pw));
                count++;
            }
        }
        if (count > 0) {
            log.info("비밀번호 마이그레이션 완료: {}개 계정 BCrypt 암호화", count);
        }
    }

    private void insertDefaultNoticesIfEmpty() {
        String[][] notices = {
            {"6월 예약 접수 일정 안내",        "6월 가로기 게첨 구간 예약 접수 일정을 안내드립니다. 예약 접수는 매월 첫째 주 화요일 오전 9시 30분부터 선착순으로 진행되오니 일정을 확인하시고 접수하시기 바랍니다.",                                                "2026.04.27"},
            {"5월 현수막 예약 접수시 필독사항", "5월 현수막(가로기) 게첨 예약 접수 시 반드시 확인하셔야 할 사항을 안내드립니다. 수수료는 신청 당일 17시까지 납부하여야 하며, 미납 시 예약이 자동 취소될 수 있습니다.",                                        "2026.03.24"},
            {"4월 현수막 예약 접수시 필독사항", "4월 현수막(가로기) 게첨 예약 접수 시 필독사항을 안내드립니다. 현수막은 게첨 부착일 3일 전까지 제출하셔야 합니다.",                                                                                             "2026.03.06"},
            {"4월 예약 접수 일정 변경 안내",   "4월 가로기 게첨 구간 예약 접수 일정이 변경되었음을 안내드립니다. 변경된 일정을 확인하시고 접수에 차질 없으시기 바랍니다.",                                                                                     "2026.01.29"},
            {"1월 현수막 예약 접수시 필독사항", "1월 현수막(가로기) 게첨 예약 접수 시 필독사항을 안내드립니다. 기업은행 193-110190-04-013 (주식회사아이비)으로 수수료를 납부하여 주시기 바랍니다.",                                                            "2025.12.26"},
            {"12월 현수막 예약 접수시 필독사항","12월 현수막(가로기) 게첨 예약 접수 시 필독사항을 안내드립니다. 자세한 사항은 민원상담 전화(010-8744-0026)로 문의하여 주시기 바랍니다.",                                                                       "2025.10.20"},
            {"11월분 게시대 예약 안내 건",     "11월 가로기 게첨 구간 예약 접수에 관한 안내입니다. 예약 접수 후 2일 이내에 수수료를 납부하지 않으면 예약이 자동 취소됩니다.",                                                                                   "2025.09.02"}
        };
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Notice");
                rs.next();
                if (rs.getInt(1) > 0) return;
            }
            String sql = "INSERT INTO Notice (subject, contents, createdDate) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (String[] n : notices) {
                    ps.setString(1, n[0]);
                    ps.setString(2, n[1]);
                    ps.setString(3, n[2]);
                    ps.executeUpdate();
                }
            }
            log.info("공지사항 기본 데이터 {}건 추가 완료", notices.length);
        } catch (Exception e) {
            log.warn("공지사항 초기 데이터 삽입 실패: {}", e.getMessage());
        }
    }

    private void addActiveColumnIfNotExists() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(
                "SELECT COUNT(*) FROM information_schema.columns " +
                "WHERE table_schema = DATABASE() AND table_name = 'orders' AND column_name = 'active'"
            );
            rs.next();
            if (rs.getInt(1) == 0) {
                stmt.executeUpdate(
                    "ALTER TABLE orders ADD COLUMN active TINYINT(1) NOT NULL DEFAULT 1"
                );
                log.info("orders 테이블에 active 컬럼 추가 완료");
            }
        } catch (Exception e) {
            log.error("active 컬럼 추가 실패: {}", e.getMessage());
        }
    }
}
