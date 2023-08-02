package jpabook.jpashop.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.FetchType.LAZY;

@Getter
@Setter
public class OrdersDTO {
//조회용 OrdersDTO

    private Long id;

    private Member member;

    private List<OrderItem> orderItems = new ArrayList<>();

    private LocalDateTime orderDate; //주문시간

    private String orderStartDate; //공연시작시간

    private String orderEndDate; //공연종료시간

    private String orderName; //공연명

    @Enumerated(EnumType.STRING)
    private OrderStatus status; //주문상태 [ORDER, CANCEL, PAYED]
}
