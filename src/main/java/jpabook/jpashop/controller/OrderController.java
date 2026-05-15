package jpabook.jpashop.controller;

import jpabook.jpashop.Service.ItemService;
import jpabook.jpashop.Service.MemberService;
import jpabook.jpashop.Service.OrderService;
import jpabook.jpashop.Service.UserSecurityService;
import jpabook.jpashop.domain.*;
import jpabook.jpashop.report.service.ReportService;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import jpabook.jpashop.domain.OrderItem;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Controller
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final MemberService memberService;
    private final ItemService itemService;
    private final UserSecurityService userSecurityService;
    private final ObjectMapper objectMapper;
    private final ReportService reportService;

    @GetMapping("/sectionStatus")
    public String sectionStatus(@ModelAttribute("orderSearch") OrderSearch orderSearch, Model model) {
        List<OrderItemDTO> itemList = orderService.findItemsOfPossible(orderSearch);
        model.addAttribute("itemList", itemList);
        model.addAttribute("startDate", orderSearch.getFindDate());
        return "order/sectionStatus";
    }

    @GetMapping("/layout")
    public String layout(@ModelAttribute("orderSearch") OrderSearch orderSearch, Model model) {
        //유저로그인체크
        String userId = userSecurityService.LoginUserCheck();
        if(userId == "anonymousUser") //로그인 안했다면
            return "login_form";

        List<Order> orders = orderService.findOrders(orderSearch);
        model.addAttribute("orders", orders);
        model.addAttribute("date", orderSearch.getFindDate());
        model.addAttribute("startDate", orderSearch.getFindDate());

        return "layout";
    }
    @GetMapping("/finish")
    public String finish(@ModelAttribute("orderSearch") OrderSearch orderSearch, Model model) {

        return "order/finish";
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
        String userId = userSecurityService.LoginUserCheck();
        if(userId == "anonymousUser")
            return "login_form";

        if (orderSearch.getFindDate() == null || orderSearch.getFindDate().isEmpty()) {
            orderSearch.setFindDate(LocalDate.now().toString());
        }

        List<Order> orders = orderService.findOrders(orderSearch);
        model.addAttribute("orders", orders);
        model.addAttribute("date", orderSearch.getFindDate());
        model.addAttribute("startDate", orderSearch.getFindDate());
        model.addAttribute("countOrder",  orders.stream().filter(o -> o.getStatus() == OrderStatus.ORDER).count());
        model.addAttribute("countPayed",  orders.stream().filter(o -> o.getStatus() == OrderStatus.PAYED).count());
        model.addAttribute("countCancel", orders.stream().filter(o -> o.getStatus() == OrderStatus.CANCEL).count());

        String calendarJson;
        try {
            List<Order> allOrders = orderService.findOrders(new OrderSearch());
            calendarJson = objectMapper.writeValueAsString(buildCalendarEvents(allOrders));
        } catch (Exception e) {
            calendarJson = "[]";
        }
        model.addAttribute("calendarEventsJson", calendarJson);

        return "order/orderList";
    }

    // 공연별 기본 색상 팔레트 (채도 낮춘 버전)
    private static final String[] COLOR_PALETTE = {
        "#4a6cc4","#28a88a","#d4701a","#6438b0","#1890a0",
        "#5a7280","#1e7ed4","#c84818","#6e9a30","#882098",
        "#00a0b8","#c81858","#007d72","#c47e00","#5c30a0"
    };

    private List<Map<String, Object>> buildCalendarEvents(List<Order> allOrders) {
        Map<String, String> nameColorMap = new LinkedHashMap<>();
        Map<Long, Map<String, Object>> eventMap = new LinkedHashMap<>();

        for (Order o : allOrders) {
            Long id = o.getId();
            if (!eventMap.containsKey(id)) {

                String bgColor, borderColor, textColor;

                if (o.getStatus() == OrderStatus.CANCEL) {
                    bgColor     = "#9e9e9e";
                    borderColor = "#757575";
                    textColor   = "#ffffff";
                } else {
                    String name = o.getOrderName() != null ? o.getOrderName() : "";
                    if (!nameColorMap.containsKey(name)) {
                        nameColorMap.put(name, COLOR_PALETTE[nameColorMap.size() % COLOR_PALETTE.length]);
                    }
                    bgColor     = nameColorMap.get(name);
                    borderColor = nameColorMap.get(name);
                    textColor   = "#ffffff";
                }

                String calEnd;
                try { calEnd = LocalDate.parse(o.getOrderEndDate()).plusDays(1).toString(); }
                catch (Exception e) { calEnd = o.getOrderEndDate(); }

                Map<String, Object> ev = new LinkedHashMap<>();
                ev.put("id",              id);
                ev.put("title",           o.getOrderName());
                ev.put("start",           o.getOrderStartDate());
                ev.put("end",             calEnd);
                ev.put("backgroundColor", bgColor);
                ev.put("borderColor",     borderColor);
                ev.put("textColor",       textColor);

                Map<String, Object> props = new LinkedHashMap<>();
                props.put("memberName", o.getMember().getName());
                props.put("status",     o.getStatus().name());
                props.put("startDate",  o.getOrderStartDate());
                props.put("endDate",    o.getOrderEndDate());
                props.put("items",      new ArrayList<Map<String, Object>>());
                ev.put("extendedProps", props);
                eventMap.put(id, ev);
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>)
                ((Map<String, Object>) eventMap.get(id).get("extendedProps")).get("items");
            for (OrderItem oi : o.getOrderItems()) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("name",  oi.getItem().getName());
                item.put("count", oi.getCount());
                items.add(item);
            }
        }
        return new ArrayList<>(eventMap.values());
    }

    private int[] hexToRgb(String hex) {
        return new int[]{
            Integer.parseInt(hex.substring(1, 3), 16),
            Integer.parseInt(hex.substring(3, 5), 16),
            Integer.parseInt(hex.substring(5, 7), 16)
        };
    }

    @PostMapping("/orders/{orderId}/payed")
    public String payedOrder(@PathVariable("orderId") Long orderId) {
        orderService.payed(orderId);
        return "redirect:/orders";
    }

    @GetMapping("/myOrders")
    public String myOrderList(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();
        if ("anonymousUser".equals(userId)) return "login_form";

        boolean isAdmin = "admin".equals(userId);
        java.util.List<SettlementRowDTO> orders;
        if (isAdmin) {
            orders = reportService.getMyOrders(null);
        } else {
            Member member = memberService.findByLoginId(userId);
            orders = reportService.getMyOrders(member.getId());
        }

        model.addAttribute("orders", orders);
        model.addAttribute("isAdmin", isAdmin);
        return "order/myOrderList";
    }

    @PostMapping("/orders/{orderId}/cancel")
    public String cancelOrder(@PathVariable("orderId") Long orderId) {
        orderService.cancle(orderId);
        return "redirect:/orders";
    }

    @PostMapping("/myOrders/{orderId}/payed")
    public String payedMyOrder(@PathVariable("orderId") Long orderId) {
        orderService.payed(orderId);
        return "redirect:/myOrders";
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
