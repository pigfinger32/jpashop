package jpabook.jpashop.controller;

import jpabook.jpashop.Service.MemberService;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.SessionConst;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/login")
    public String login() {
        return "login_form";
    }

    @GetMapping("/members/new")
    public String createForm(Model model) {

        model.addAttribute("memberForm", new MemberForm());
        return "members/createMemberForm";
    }

    @PostMapping("/members/new")
    public String create(@Valid MemberForm form, BindingResult result) {

        if (result.hasErrors()) {
            return "members/createMemberForm";
        }

        Address address = new Address(form.getCity(), form.getStreet(), form.getZipcode());
        Member member = new Member();
        member.setLoginId(form.getLoginId());
        member.setName(form.getName());
        member.setPw(form.getPw()); //2023-05-09 setPw추가
        member.setCompany(form.getCompany()); //2023-05-09 setCompany추가
        member.setAddress(address);
        member.setPhone(form.getPhone().replaceAll("-",""));
        member.setBizRegiNo(form.getBizRegiNo().replaceAll("-",""));

        memberService.join(member);
        return "redirect:/";
    }

    @GetMapping("/members")
    public String list(Model model) {
        List<Member> members = memberService.findMembers();
        for(Member member : members) {
            member.setPhone(phone_format(member.getPhone()));
            String bizRegiNo = member.getBizRegiNo().substring(0,3) + "-" + member.getBizRegiNo().substring(3,5) + "-" + member.getBizRegiNo().substring(5,10);
            member.setBizRegiNo(bizRegiNo);
        }
        model.addAttribute("members", members);
        return "members/memberList";
    }

    //전화번호 셋팅
    public String phone_format(String number) {
        String regEx = "(\\d{3})(\\d{3,4})(\\d{4})";
        return number.replaceAll(regEx, "$1-$2-$3");
    }

//    @GetMapping("members/login")
//    public String login(Model model) {
//        model.addAttribute("loginForm", new LoginForm());
//        return "members/login";
//    }
//
//    @PostMapping("members/loginDo")
//    public String loginDo(@Valid @ModelAttribute LoginForm form, BindingResult bindingResult, HttpServletRequest request ) {
//        if (bindingResult.hasErrors()) {
//            return "/members/login";
//        }
//        List<Member> loginMemberList = memberService.login(form.getLoginId(), form.getPw());
//
//
//        if (loginMemberList.isEmpty()) {
//            bindingResult.reject("loginFail", "아이디 또는 비밀번호가 맞지 않습니다.");
//            return "/members/login";
//        }
//
//        //로그인 성공 처리
//        //세션이 있으면 있는 세션 반환, 없으면 신규 세션을 생성
//        HttpSession session = request.getSession();
//        //세션에 로그인 회원 정보 보관
//        session.setAttribute(SessionConst.LOGIN_MEMBER, loginMemberList.get(0));
//
//        return "redirect:/";
//    }
//
//    @PostMapping("members/logout")
//    public String logoutV3(HttpServletRequest request) {
//        HttpSession session = request.getSession(false);
//        if (session != null) {
//            session.invalidate();
//        }
//        return "redirect:/";
//    }


}
