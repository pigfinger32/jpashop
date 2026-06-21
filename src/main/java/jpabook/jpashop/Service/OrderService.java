package jpabook.jpashop.Service;

import jpabook.jpashop.domain.*;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.item.FlagSection;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;
    private final OrderItemRepository orderItemRepository;
    private final EntityManager em;
    private final SmsService smsService;

    //주문시 DB락을 건 수량검색
    public List<OrderItemDTO> findItemsOfPossibleWithDBLock(OrderSearch orderSearch) {
        //OrderItem 조회(날짜기준으로 조회)
        List<OrderItem> orderItems = orderItemRepository.findAllByStringWithDbLock(orderSearch);
        return findItemsOfPossibleMakeOrderItemDTOS(orderItems);
    }
    //단순 검색검색
    public List<OrderItemDTO> findItemsOfPossible(OrderSearch orderSearch) {
        //OrderItem 조회(날짜기준으로 조회)
        List<OrderItem> orderItems = orderItemRepository.findAllByString(orderSearch);
        return findItemsOfPossibleMakeOrderItemDTOS(orderItems);
    }

    private List<OrderItemDTO> findItemsOfPossibleMakeOrderItemDTOS(List<OrderItem> orderItems) {
        //Item 조회
        List<Item> items = itemRepository.findAll();
        //변환하여 넣을 DTOS리스트
        List<OrderItemDTO> orderItemDtos = new ArrayList<>();
        //같은이름으로 들어오는 수량을 더하기 위한 HASHMAP
        HashMap<String, Integer> hash = new HashMap<>();
        for (OrderItem o : orderItems) {
            hash.put(o.getItem().getName(), hash.getOrDefault(o.getItem().getName(), 0) + o.getCount());
        }

        for (Item i : items) {
            FlagSection flagSection = (FlagSection) i;
            OrderItemDTO orderItemDTO = getOrderItemDTO(hash, flagSection);
            if (!hash.isEmpty()) {//주문건이 하나라도 있으면, 수량 계산해서 넣어주기
                orderItemDTO.setUsedStock(hash.getOrDefault(flagSection.getName(), 0));
                orderItemDTO.setCurStock(flagSection.getStockQuantity() - hash.getOrDefault(flagSection.getName(), 0));
            } else {//주문건이 없으면 0으로 셋팅
                orderItemDTO.setUsedStock(0);
                orderItemDTO.setCurStock(flagSection.getStockQuantity());
            }
            orderItemDtos.add(orderItemDTO);
        }

        return orderItemDtos;
    }

    private static OrderItemDTO getOrderItemDTO(HashMap<String, Integer> hash, FlagSection flagSection) {
        OrderItemDTO orderItemDTO = new OrderItemDTO();
        orderItemDTO.setId(flagSection.getId());
        orderItemDTO.setPrice(flagSection.getPrice());
        orderItemDTO.setName(flagSection.getName());
        orderItemDTO.setStartPlace(flagSection.getStartPlace());
        orderItemDTO.setEndPlace(flagSection.getEndPlace());
        orderItemDTO.setStockQuantity(flagSection.getStockQuantity());
        orderItemDTO.setUsedStock(hash.getOrDefault(flagSection.getName(), 0));
        orderItemDTO.setCurStock(flagSection.getStockQuantity() - hash.getOrDefault(flagSection.getName(), 0));
        return orderItemDTO;
    }

    /**
     * DB 락 추가 주문 수량이 아이템의 수량을 초과했을 경우.
     * */
    //@Lock(LockModeType.PESSIMISTIC_WRITE)
    private void CheckOrderStock(List <OrderDto> orderDtoList, String startDate) {
        OrderSearch orderSearch = new OrderSearch();
        orderSearch.setFindDate(startDate);
        //주문한 날짜를 기준으로 아이템의 수량 계산 테이블을 조회
        List<OrderItemDTO> itemStockList = findItemsOfPossibleWithDBLock(orderSearch);
        for (OrderItemDTO itemStockDto : itemStockList) {
            for (OrderDto orderDto : orderDtoList) {
                if (itemStockDto.getId() == orderDto.getItemId()) {
                    if (itemStockDto.getCurStock() < orderDto.getCount()) {
                        throw new IllegalStateException(itemStockDto.getName()+ " 수량이 부족합니다. 주문 수량을 확인하세요.");
                    }
                }
            }
        }
    }


    /**
     * 주문
     * */
    @Transactional
    public synchronized Long order(List <OrderDto> orderDtoList, String startDate, int term) throws ParseException, InterruptedException {
        //주문 수량 확인 수량이 부족하면 뒤로
        CheckOrderStock(orderDtoList, startDate);
        //종료날짜 연산
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = sdf.parse(startDate);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, term);
        //시작날짜 종료날짜 입력
        String orderStartDate = startDate;
        String orderEndDate = sdf.format(cal.getTime());

        //order 주문생성준비
        Member member = orderDtoList.get(0).getMember();
        String orderName = orderDtoList.get(0).getOrderName();
        List<OrderItem> orderItems = new ArrayList<>();
        for(OrderDto orderDto : orderDtoList) {
            Item item = itemRepository.findOne(orderDto.getItemId());
            int basePrice  = (term == 15) ? 15000 : 20000;
            int extraPrice = "상업용".equals(orderDto.getEventType()) ? (term == 15 ? 6000 : 12000) : 0;
            orderItems.add(OrderItem.OrderItem(item, basePrice + extraPrice, orderDto.getCount()));
        }
        //주문 생성
        Order order = Order.createOrder(member, orderName,orderStartDate, orderEndDate, orderItems);

        //주문 저장
        orderRepository.save(order);

        //SMS용 총 수량·총 금액·납부 기한 계산
        int totalCount  = orderDtoList.stream().mapToInt(OrderDto::getCount).sum();
        int totalAmount = orderDtoList.stream().mapToInt(dto -> {
            int base  = (term == 15) ? 15000 : 20000;
            int extra = "상업용".equals(dto.getEventType()) ? (term == 15 ? 6000 : 12000) : 0;
            return (base + extra) * dto.getCount();
        }).sum();
        String payDeadline = LocalDate.parse(orderStartDate).plusDays(2).toString();

        //예약자 SMS: 신청 완료 + 입금 안내
        smsService.sendOrderConfirmation(member.getPhone(), orderName, orderStartDate, orderEndDate, totalAmount, payDeadline);
        //담당자 SMS: 새 예약 입금 확인 요청
        smsService.sendAdminNotification(orderName, member.getCompany(), orderStartDate, orderEndDate, totalCount);

        return order.getId();
    }

    /**
     * 주문 취소
     * JPA의 장점은 하단의 주문취소 함수하나로 설명할 수 있다.
     * 만약 JPA가 아닌 모든 쿼리를 디비에 직접 붙어서 사용하는 방식으로 개발한다면
     * 1.주문 엔티티를 조회해서 모든 데이터를 업데이트하는 쿼리를 날리고
     * 2.아이템 수량또한 조회해서 업데이트 쿼리를 날려야 함.
     * 그러나 JPA를 이용하면 dirty Checking을 통해서 엔티티에 변경된 값을 찾아서 업데이트 쿼리를 자동으로 던져줌.
     * */
    @Transactional
    public void cancle(Long orderId) {
        Order order = orderRepository.findOne(orderId);
        order.cancel(orderId);
        orderRepository.deleteById(orderId);
    }

    private int extractSectionNumber(String name) {
        if (name == null) return 0;
        String digits = name.replaceAll("[^0-9]", "");
        return digits.isEmpty() ? 0 : Integer.parseInt(digits);
    }

    @Transactional
    public void setGovDocPath(Long orderId, String path) {
        Order order = orderRepository.findOne(orderId);
        order.setGovDocPath(path);
    }

    @Transactional
    public void payed(Long orderId) {
        Order order = orderRepository.findOne(orderId);
        order.setStatus(OrderStatus.PAYED);
    }


    //검색
    public List<Order> findOrders(OrderSearch orderSearch) {
        List<Order> orders = orderRepository.findAllByString(orderSearch);
        List<Order> result = new ArrayList<>();
        for (Order order : orders) {
            for (OrderItem orderItem : order.getOrderItems()) {
                Order tmpOrder = new Order();
                tmpOrder.getOrderItems().add(orderItem);
                tmpOrder.setOrderDate(order.getOrderDate());
                tmpOrder.setOrderName(order.getOrderName());
                tmpOrder.setMember(order.getMember());
                tmpOrder.setOrderStartDate(order.getOrderStartDate());
                tmpOrder.setOrderEndDate(order.getOrderEndDate());
                tmpOrder.setStatus(order.getStatus());
                tmpOrder.setId(order.getId());
                result.add(tmpOrder);
            }
        }
        result.sort((a, b) -> {
            String nameA = a.getOrderItems().isEmpty() ? "" : a.getOrderItems().get(0).getItem().getName();
            String nameB = b.getOrderItems().isEmpty() ? "" : b.getOrderItems().get(0).getItem().getName();
            return Integer.compare(extractSectionNumber(nameA), extractSectionNumber(nameB));
        });
        return result;
    }
}


