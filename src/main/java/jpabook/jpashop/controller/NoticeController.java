package jpabook.jpashop.controller;

import jpabook.jpashop.Service.NoticeService;
import jpabook.jpashop.domain.Notice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
public class NoticeController {
    private final NoticeService noticeService;

    @GetMapping("notices/notices")
    public String notices(Model model) {
        List<Notice> notices = noticeService.NoticeList();
        model.addAttribute("notices", notices);
        return "notices/notices";
    }

    @GetMapping("/{uid}")
    public String notice(@PathVariable("uid") Long uid, Model model) {
        noticeService.ViewcntUpdate(uid);
        Notice result = noticeService.NoticeOne(uid);
        Notice notice = result;
        model.addAttribute("notice", notice);
        log.info("notice={}", notice);
        return "notices/notices";
    }
}
