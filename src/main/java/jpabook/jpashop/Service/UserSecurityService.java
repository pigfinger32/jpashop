package jpabook.jpashop.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.UserRole;
import jpabook.jpashop.repository.MemberRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import javax.servlet.http.Cookie;

@RequiredArgsConstructor
@Service
public class UserSecurityService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        Optional<Member> member = this.memberRepository.findByUserName(loginId);
        if (!member.isPresent()) {
            throw new UsernameNotFoundException("사용자를 찾을수 없습니다.");
        }
        Member siteUser = member.get();

        List<GrantedAuthority> authorities = new ArrayList<>();
        if ("admin".equals(loginId)) {
            authorities.add(new SimpleGrantedAuthority(UserRole.ADMIN.getValue()));
        } else {
            authorities.add(new SimpleGrantedAuthority(UserRole.USER.getValue()));
        }
        siteUser.setName(siteUser.getLoginId());
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodedPassword = passwordEncoder.encode(siteUser.getPw());

        return new User(siteUser.getName(), encodedPassword, authorities);
    }
}