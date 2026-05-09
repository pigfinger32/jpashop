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
