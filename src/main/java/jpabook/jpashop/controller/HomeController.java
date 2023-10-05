package jpabook.jpashop.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Slf4j
public class HomeController {

    @RequestMapping("/")
    public String home() {
        log.info("home controller");
        return "index";
    }
    @RequestMapping("/index")
    public String index() {
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
