package jpabook.jpashop.report.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import jpabook.jpashop.Service.ItemService;
import jpabook.jpashop.Service.MemberService;
import jpabook.jpashop.Service.UserSecurityService;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.ReportResDTO;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.report.service.ReportService;
import jpabook.jpashop.repository.OrderSearch;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ReportController {
	
	private final ReportService reportService;
    private final MemberService memberService;
    private final ItemService itemService;
    private final UserSecurityService userSecurityService;
	
	@GetMapping("/report")
    public String getOrderReport(@ModelAttribute("orderSearch") OrderSearch orderSearch, Model model) {
        //유저로그인체크
        if(userSecurityService.LoginUserCheck() == "anonymousUser") //로그인 안했다면
            return "login_form";


        List<Member> members = memberService.findMembers();
        List<Item> items = itemService.findItems();

        model.addAttribute("members", members);
        model.addAttribute("items", items);

        //List<Order> orders = orderService.findOrders(orderSearch);
        List<ReportResDTO> reportList = reportService.getOrderReport(orderSearch);
        model.addAttribute("reportList", reportList);
        model.addAttribute("startDate", orderSearch.getFindDate());

        return "report/report";
    }

}
