package jpabook.jpashop.controller;

import jpabook.jpashop.Service.NoticeService;
import jpabook.jpashop.domain.Notice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.jni.Time;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
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

    @GetMapping("notices/{uid}")
    public String notice(@PathVariable("uid") long id, Model model) {
        noticeService.ViewcntUpdate(id);
        Notice result = noticeService.NoticeOne(id);
        Notice notice = result;
        model.addAttribute("notice", notice);
        log.info("notice={}", notice);
        return "notices/notice";
    }
    @GetMapping("notices/add")
    public String addForm(Model model) {
        model.addAttribute("notice", new Notice());
        return "notices/addForm";
    }
    @PostMapping("notices/add")
    public String addNotice(@ModelAttribute Notice notice, RedirectAttributes redirectAttributes) {
        notice.setViewcnt("0");
        if(notice.getName() == "")
            notice.setName("ADMIN");
        //notice.setCreatedDate(LocalDateTime.now());
        noticeService.NoticeAdd(notice);
        return "redirect:/notices/notices";
    }

    @RequestMapping("notices/{uid}/delete")
    public String deleteNotice(@PathVariable long uid) {
        noticeService.NoticeDelete(uid);
        return "redirect:/notices/notices";
    }
    @GetMapping("notices/{uid}/edit")
    public String edit(@PathVariable long uid, Model model) {
        Notice notice = noticeService.NoticeOne(uid);
        model.addAttribute("notice", notice);
        log.info("notice={}", notice);

        return "notices/editForm";
    }

    @PostMapping("notices/{uid}/edit")
    public String edit(@PathVariable long uid, @ModelAttribute Notice notice) {
        notice.setId(uid);
        noticeService.NoticeUpdate(notice);
        return "redirect:/notices/{uid}";
    }

}
