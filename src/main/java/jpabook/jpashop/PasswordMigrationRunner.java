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

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
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
}
