package jpabook.jpashop.controller;

import jpabook.jpashop.Service.ItemService;
import jpabook.jpashop.Service.MemberService;
import jpabook.jpashop.Service.OrderService;
import jpabook.jpashop.Service.UserSecurityService;
import jpabook.jpashop.domain.*;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Controller
@RequiredArgsConstructor
public class OrderController {
	
	//made by branch-swagger
	//made by branch-new_swagger
    private final OrderService orderService;
    private final MemberService memberService;
    private final ItemService itemService;
    private final UserSecurityService userSecurityService;

    @GetMapping("/layout")
    public String layout(@ModelAttribute("orderSearch") OrderSearch orderSearch, Model model) {
        //유저로그인체크
        String userId = userSecurityService.LoginUserCheck();
        if(userId == "anonymousUser") //로그인 안했다면
            return "login_form1"; //충돌테스트 수정 메소드 login_form으로 변경할것.

        List<Order> orders = orderService.findOrders(orderSearch);
        model.addAttribute("orders", orders);
        model.addAttribute("date", orderSearch.getFindDate());
        model.addAttribute("startDate", orderSearch.getFindDate());

        return "layout1"; //충돌테스트 수정 메소드 layout으로 변경할것.
    }
    //충돌테스트 추가 메소드
    @GetMapping("/layout2")
    public String layout2(@ModelAttribute("orderSearch") OrderSearch orderSearch, Model model) {
        //유저로그인체크
        String userId = userSecurityService.LoginUserCheck();
        if(userId == "anonymousUser") //로그인 안했다면
            return "login_form2";

        List<Order> orders = orderService.findOrders(orderSearch);
        model.addAttribute("orders", orders);
        model.addAttribute("date", orderSearch.getFindDate());
        model.addAttribute("startDate", orderSearch.getFindDate());

        return "layout2";
    }
    @GetMapping("/finish")
    public String finish(@ModelAttribute("orderSearch") OrderSearch orderSearch, Model model) {

        return "order/finish2";
    }

    @GetMapping("/orderItems")
    public String itemList(@ModelAttribute("orderSearch") OrderSearch orderSearch, Model model) {
        //유저로그인체크
        if(userSecurityService.LoginUserCheck() == "anonymousUser") //로그인 안했다면
            return "login_form";


        List<Member> members = memberService.findMembers();
        List<Item> items = itemService.findItems();

        model.addAttribute("members", members);
        model.addAttribute("items", items);

        //List<Order> orders = orderService.findOrders(orderSearch);
        List<OrderItemDTO> itemList = orderService.findItemsOfPossible(orderSearch);
        model.addAttribute("itemList", itemList);
        model.addAttribute("startDate", orderSearch.getFindDate());

        return "order/createOrder";
    }


    @GetMapping("/order")
    public String createForm(Model model) {
        List<Member> members = memberService.findMembers();
        List<Item> items = itemService.findItems();

        model.addAttribute("members", members);
        model.addAttribute("items", items);

        return "order/orderForm";
    }

    @PostMapping(value="/order")
    public String order(//@RequestParam List<OrderDto> orderDtoList,
                        @RequestParam (name="addItem", required=true) List<String> addItemList,
                        @RequestParam("startDate") String startDate,
                        @RequestParam("term") int term) throws ParseException, InterruptedException {

        //유저로그인체크
        String userId = userSecurityService.LoginUserCheck();
        Member member = memberService.findByLoginId(userId);

        //orderDtoList 생성
        List<OrderDto> orderDtoList = new ArrayList<>();
        //%로 문자열을 나눠서 List로 담기
        for(String str : addItemList) {
            String[] strArr = str.split("%");
            OrderDto orderDto = new OrderDto();
            //strArr 순서 0.날짜 1.회원 2. 공연명 3.상품 4.수량 5.기간
            orderDto.setStartDate(strArr[0]);
            orderDto.setMemberId(member.getId());
            orderDto.setMember(member);
            orderDto.setOrderName(strArr[2]);
            orderDto.setItemId(Long.parseLong(strArr[3]));
            orderDto.setCount(Integer.parseInt(strArr[4]));
            orderDto.setTerm(Integer.parseInt(strArr[5]));
            orderDtoList.add(orderDto);
        }
        //동시성TEST
//        int numberOfThreads = 2;
//        ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);
//        CountDownLatch latch = new CountDownLatch(numberOfThreads);
//        service.execute(() -> {
//            try {
//                orderService.order(orderDtoList, startDate, term);
//            } catch (ParseException e) {
//                throw new RuntimeException(e);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//            latch.countDown();
//        });
//        service.execute(() -> {
//            try {
//                orderService.order(orderDtoList, startDate, term);
//            } catch (ParseException e) {
//                throw new RuntimeException(e);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//            latch.countDown();
//        });
//        latch.await();
//
//        return "redirect:/";

        //동시성 TEST 끝
        orderService.order(orderDtoList, startDate, term);
        return "redirect:/";
    }

    @GetMapping("/orders")
    public String orderList(@ModelAttribute("orderSearch") OrderSearch orderSearch, Model model) {
        //유저로그인체크
        String userId = userSecurityService.LoginUserCheck();
        if(userId == "anonymousUser") //로그인 안했다면
            return "login_form";

        List<Order> orders = orderService.findOrders(orderSearch);
        model.addAttribute("orders", orders);
        model.addAttribute("date", orderSearch.getFindDate());
        model.addAttribute("startDate", orderSearch.getFindDate());

        return "order/orderList";
    }

    @GetMapping("/myOrders")
    public String myOrderList(@ModelAttribute("orderSearch") OrderSearch orderSearch, Model model) {
        //유저로그인체크
        if(userSecurityService.LoginUserCheck() == "anonymousUser") //로그인 안했다면
            return "login_form";

        //나의 로그인 정보로 이름을 구해 OrderSearch에 name을 넣어줌
        Authentication loggedinUser = SecurityContextHolder.getContext().getAuthentication();
        //*****로그인체크부분 추가할것.
        if ("anonymousUser".equals(loggedinUser.getName())){
            throw new IllegalStateException("로그인 후 이용하세요.");
        }
        String userId = loggedinUser.getName();//getName에 Login ID를 넣어놓음.
        Member member = memberService.findByLoginId(userId);
        orderSearch.setMemberName(member.getName());
        //<--
        List<Order> orders = orderService.findOrders(orderSearch);
        model.addAttribute("orders", orders);
        model.addAttribute("date", orderSearch.getFindDate());
        model.addAttribute("startDate", orderSearch.getFindDate());

        return "order/myOrderList";
    }

    @PostMapping("/orders/{orderId}/cancel")
    public String cancelOrder(@PathVariable("orderId") Long orderId) {
        orderService.cancle(orderId);
        return "redirect:/orders";
    }

    @PostMapping("/myOrders/{orderId}/cancel")
    public String cancelMyOrder(@PathVariable("orderId") Long orderId) {
        orderService.cancle(orderId);
        return "redirect:/myOrders";
    }

    @PostMapping("/orderItems/{orderId}/cancel")
    public String cancelOrderItems(@PathVariable("orderId") Long orderId) {
        orderService.cancle(orderId);
        return "redirect:/orders";
    }
}
