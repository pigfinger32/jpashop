package jpabook.jpashop.controller;

import jpabook.jpashop.Service.ItemService;
import jpabook.jpashop.Service.MemberService;
import jpabook.jpashop.Service.OrderService;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderDto;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.domain.OrderItemDTO;
import jpabook.jpashop.repository.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final MemberService memberService;
    private final ItemService itemService;

    @GetMapping("/orderItems")
    public String itemList(@ModelAttribute("orderSearch") OrderSearch orderSearch, Model model) {
        List<Member> members = memberService.findMembers();
        List<Item> items = itemService.findItems();

        model.addAttribute("members", members);
        model.addAttribute("items", items);

        List<Order> orders = orderService.findOrders(orderSearch);
        List<OrderItemDTO> itemList = orderService.findItemsOfPossible(orderSearch);
        model.addAttribute("itemList", itemList);
        model.addAttribute("date", orderSearch.getFindDate());

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
                        @RequestParam("term") int term) throws ParseException {

        //orderService.order(memberId, itemId, count);
        //orderDtoList 생성
        List<OrderDto> orderDtoList = new ArrayList<>();
        //%로 문자열을 나눠서 List로 담기
        for(String str : addItemList) {
            String[] strArr = str.split("%");
            OrderDto orderDto = new OrderDto();
            //strArr 순서 0.날짜 1.회원 2. 공연명 3.상품 4.수량 5.기간
            orderDto.setStartDate(strArr[0]);
            orderDto.setMemberId(Long.parseLong(strArr[1]));
            orderDto.setOrderName(strArr[2]);
            orderDto.setItemId(Long.parseLong(strArr[3]));
            orderDto.setCount(Integer.parseInt(strArr[4]));
            orderDto.setTerm(Integer.parseInt(strArr[5]));
            orderDtoList.add(orderDto);
        }
        orderService.order(orderDtoList, startDate, term);
        return "redirect:/";
    }

/*    @PostMapping(value="/order")
    public String order(@RequestParam("memberId") Long memberId,
                        //@RequestParam List<OrderDto> orderDtoList,
                        //@RequestParam("startDate") Long startDate,
                        @RequestParam("itemId") Long itemId,
                        @RequestParam("count") int count) {

        orderService.order(memberId, itemId, count);
        return "redirect:/orders";
    }*/

    @GetMapping("/orders")
    public String orderList(@ModelAttribute("orderSearch") OrderSearch orderSearch, Model model) {
        List<Order> orders = orderService.findOrders(orderSearch);
        model.addAttribute("orders", orders);
        model.addAttribute("date", orderSearch.getFindDate());

        return "order/orderList";
    }

    @PostMapping("/orders/{orderId}/cancel")
    public String cancelOrder(@PathVariable("orderId") Long orderId) {
        orderService.cancel(orderId);
        return "redirect:/orders";
    }

    @PostMapping("/orderItems/{orderId}/cancel")
    public String cancelOrderItems(@PathVariable("orderId") Long orderId) {
        orderService.cancel(orderId);
        return "redirect:/orders";
    }
}
