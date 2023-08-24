package jpabook.jpashop.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.FetchType.*;

@Entity
@Table(name = "orders")
@Getter @Setter
//@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order implements Comparable<Order> {

    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    //@ManyToOne(fetch = LAZY)로 변경 2023-04-15
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();


    private LocalDateTime orderDate; //주문시간

    private String orderStartDate; //공연시작시간

    private String orderEndDate; //공연종료시간

    private String orderName; //공연명

    @Enumerated(EnumType.STRING)
    private OrderStatus status; //주문상태 [ORDER, CANCEL, PAYED]

    @Override
    public int compareTo(Order o) {
        return this.getOrderItems().get(0).getItem().getName().compareTo(o.getOrderItems().get(0).getItem().getName());
    }

    //==연관관계 메서드==//
    public void setMember(Member member) {
        this.member = member;
        member.getOrders().add(this);
    }

    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

//    public void setDelivery(Delivery delivery) {
//        this.delivery = delivery;
//        delivery.setOrder(this);
//    }

    //==생성 메서드=//
    public static Order createOrder(Member member, String orderName, String orderStartDate, String orderEndDate, OrderItem... orderItems ) {
        Order order = new Order();
        order.setMember(member);
        //order.setDelivery(delivery);
        for (OrderItem orderItem : orderItems) {
            order.addOrderItem(orderItem);
        }
        order.setStatus(OrderStatus.ORDER);
        order.setOrderDate(LocalDateTime.now());
        order.setOrderName(orderName);
        order.setOrderStartDate(orderStartDate);
        order.setOrderEndDate(orderEndDate);

        return order;
    }
    public static Order createOrder(Member member, String orderName, String orderStartDate, String orderEndDate, List<OrderItem> orderItems ) {
        Order order = new Order();
        order.setMember(member);
        //order.setDelivery(delivery);
        for (OrderItem orderItem : orderItems) {
            order.addOrderItem(orderItem);
        }
        order.setStatus(OrderStatus.ORDER);
        order.setOrderDate(LocalDateTime.now());
        order.setOrderName(orderName);
        order.setOrderStartDate(orderStartDate);
        order.setOrderEndDate(orderEndDate);

        return order;
    }

    //public static Order createOrder(Member member, Delivery delivery, OrderItem... orderItems ) {
    public static Order createOrder(Member member, OrderItem... orderItems ) {
        Order order = new Order();
        order.setMember(member);
        //order.setDelivery(delivery);
        for (OrderItem orderItem : orderItems) {
            order.addOrderItem(orderItem);
        }
        order.setStatus(OrderStatus.ORDER);
        order.setOrderDate(LocalDateTime.now());

        return order;
    }

    //==비즈니스 로직=//
    /**
    * 주문취소
    */
    public void cancel(Long orderId) {
        Authentication loggedinUser = SecurityContextHolder.getContext().getAuthentication();
        String userId = loggedinUser.getName();//getName에 Login ID를 넣어놓음.
        //*****admin 인 경우 무조건 삭제
        if ("admin".equals(userId.toLowerCase())){
            ;//통과 무조건 삭제
        }else {
            if (getStatus() == OrderStatus.PAYED) {
                throw new IllegalStateException("이미 결제된 상품은 취소가 불가능합니다. 관리자에게 문의하세요.");
            }
        }
    }

    /**
     * 주문 결제완료
     */
    public void myOrderCancle(Long orderId) {
        if (getStatus() == OrderStatus.PAYED) {
            throw new IllegalStateException("이미 결제된 상품은 취소가 불가능합니다. 관리자에게 문의하세요.");
        }
//        if (delivery.getStatus() == DeliveryStatus.COMP) {
//        }
        this.setStatus(OrderStatus.PAYED);
    }

    /**
     * 전체 주문 가격 조회
     */
    public int getTotalPrice() {
        int totalPrice = 0;
        for (OrderItem orderItem : orderItems) {
            totalPrice += orderItem.getTotalPrice();
        }
        return totalPrice;
    }
    /**
     * 주문 수량 조회
     */
    public int getOrderCnt() {
        int orderCnt = 0;
        for (OrderItem orderItem : orderItems) {
            orderCnt =  orderItem.getCnt();
            break;
        }
        return orderCnt;
    }

    /**
     * 주문 수량 조회
     */
    public String getOrderItemName() {
        String orderItemName = "";
        for (OrderItem orderItem : orderItems) {
            orderItemName =  orderItem.getItem().getName();
            break;
        }
        return orderItemName;
    }

}
