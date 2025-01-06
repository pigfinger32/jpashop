package jpabook.jpashop.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jpabook.jpashop.Service.NoticeService;
import jpabook.jpashop.domain.Notice;

@Slf4j
@Controller
@RequiredArgsConstructor
public class HomeController {
	
	private final NoticeService noticeService;

    @RequestMapping("/")
    public String home(Model model) {
    	List<Notice> notices = noticeService.NoticeList();
        model.addAttribute("notices", notices);
        log.info("home controller");
        return "index";
    }
    @RequestMapping("/index")
    public String index(Model model) {
    	List<Notice> notices = noticeService.NoticeList();
        model.addAttribute("notices", notices);
        log.info("home controller");
        return "index";
    }
    @RequestMapping("/home")
    public String homeIndex() {
        log.info("home controller");
        return "home";
    }
    @RequestMapping("/information")
    public String information() {
        log.info("home information");
        return "information";
    }
    




}
