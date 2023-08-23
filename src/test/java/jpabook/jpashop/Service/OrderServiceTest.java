package jpabook.jpashop.Service;

import jpabook.jpashop.domain.*;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.exception.NotEnoughStockException;
import jpabook.jpashop.repository.OrderRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
//@Transactional
@AutoConfigureMockMvc
public class OrderServiceTest {

    @Autowired EntityManager em;
    @Autowired OrderService orderService;
    @Autowired OrderRepository orderRepository;
    @Autowired MemberService memberService;
    @Test
    public void orderTest() throws Exception {
        //given
        Member member = new Member();
        member.setName("abc");
        String startDate = "2023-08-09";
        int term = 30;
        //log.info("makeReservation 동시성 테스트 시작");

        // Given
        //log.info("makeReservation 동시성 테스트 준비");


        //orderDtoList 생성
        List<OrderDto> orderDtoList = new ArrayList<>();
        List<OrderDto> orderDtoList2 = new ArrayList<>();
        //orderDtoList채워넣기
        makeOrderDTO(member, startDate, term, orderDtoList);
        makeOrderDTO(member, startDate, term, orderDtoList2);

//        // When
//        //log.info("makeReservation 동시성 테스트 진행");
//        int numberOfThreads = 2;
//        ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);
//        CountDownLatch latch = new CountDownLatch(numberOfThreads);
//        service.execute(() -> {
//            try {
//                orderService.order(orderDtoList, startDate, term);
//            } catch (ParseException e) {
//                throw new RuntimeException(e);
//            }
//            latch.countDown();
//        });
//        service.execute(() -> {
//            try {
//                orderService.order(orderDtoList2, startDate, term);
//            } catch (ParseException e) {
//                throw new RuntimeException(e);
//            }
//            latch.countDown();
//        });
//        latch.await();

        // Then


    }
    @Test
    public void selectMember() {
        Member member = memberService.findByLoginId("admin");
        System.out.println("member.getName() = " + member.getName());
        return;
    }
    @Test
    public void insertMember() {
        Member member = new Member();
        member.setName("ajsep");
        Long memberId = memberService.join(member);
        System.out.println("member.getMemberId() = " + memberService.findOne(memberId));

        return;
    }

    private static void makeOrderDTO(Member member, String startDate, int term, List<OrderDto> orderDtoList) {
        OrderDto orderDto = new OrderDto();
        //strArr 순서 0.날짜 1.회원 2. 공연명 3.상품 4.수량 5.기간
        orderDto.setStartDate(startDate);
        orderDto.setMemberId(member.getId());
        orderDto.setMember(member);
        orderDto.setOrderName("TEST동시성");
        orderDto.setItemId(Long.parseLong("2"));
        orderDto.setCount(Integer.parseInt("10"));
        orderDto.setTerm(term);
        orderDtoList.add(orderDto);
    }

//    @Test(expected = NotEnoughStockException.class)
//    public void 상품주문_주문수량초과() throws Exception {
//        //given
//        Member member = createMember();
//        Item item = createBook("시골 JPA", 10000, 10);
//
//        int orderItem = 11;
//
//        //when
//        orderService.order(member.getId(),item.getId(),orderItem);
//
//        //then
//        fail("재고 수량 부족 예외가 발생해야 한다.");
//    }
//
//    @Test
//    public void 주문취소() throws Exception {
//        //given
//        Member member = createMember();
//        Book item = createBook("시골 JPA", 10000, 10);
//
//        int orderCount = 2;
//
//        Long orderId = orderService.order(member.getId(), item.getId(), orderCount);
//        //when
//        orderService.cancel(orderId);
//
//        //then
//        Order getOrder = orderRepository.findOne(orderId);
//
//        assertEquals("주문 취소시 상태는 CANCEL 이다",OrderStatus.CANCEL, getOrder.getStatus());
//        assertEquals("주문이 취소된 상품은 그만큼 재고가 증가해야 한다.",10, item.getStockQuantity());
//
//    }


    private Member createMember() {
        Member member = new Member();
        member.setName("회원1");
        member.setAddress(new Address("서울", "강가", "123-123"));
        em.persist(member);
        return member;
    }
    private Book createBook(String name, int price, int stockQuantity) {
        Book book = new Book();
        book.setName(name);
        book.setStockQuantity(stockQuantity);
        book.setPrice(price);
        em.persist(book);
        return book;
    }

}