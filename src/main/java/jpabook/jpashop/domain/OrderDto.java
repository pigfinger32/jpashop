package jpabook.jpashop.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class OrderDto {
    //주문용

    private String startDate; //주문 시작일
    private Long memberId; //주문회원


    private String orderName; //공연명

    private Long itemId; //상품명

    private int count; //주문수량

    private int term; //주문기간

    //////
    private Member member; //주문 회원객체
}
