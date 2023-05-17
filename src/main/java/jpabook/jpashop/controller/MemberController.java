package jpabook.jpashop.controller;

import jpabook.jpashop.Service.MemberService;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

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
        member.setName(form.getName());
        member.setPw(form.getPw()); //2023-05-09 setPw추가
        member.setCompany(form.getCompany()); //2023-05-09 setCompany추가
        member.setAddress(address);
        member.setPhone(form.getPhone());
        member.setBizRegiNo(form.getBizRegiNo());

        memberService.join(member);
        return "redirect:/";
    }

    @GetMapping("/members")
    public String list(Model model) {
        List<Member> members = memberService.findMembers();
        for(Member member : members) {
            member.setPhone(phone_format(member.getPhone()));
            String bizRegiNo = member.getBizRegiNo().substring(3) + "-" + member.getBizRegiNo().substring(`2) + "-" + member.getBizRegiNo().substring(5);
            member.setBizRegiNo(phone_format(bizRegiNo));
        }
        model.addAttribute("members", members);
        return "members/memberList";
    }

    //전화번호 셋팅
    public String phone_format(String number) {
        String regEx = "(\\d{3})(\\d{3,4})(\\d{4})";
        return number.replaceAll(regEx, "$1-$2-$3");
    }


}
