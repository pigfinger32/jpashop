package jpabook.jpashop.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportResDTO {
	private String startDate; //주문 시작일
	
    private Long memberId; //주문회원
    
    private Long itemId; //상품명

    private String orderName; //공연명
    
    private int orderPrice; //주문 가격
    
    private int count; //주문수량
    
    private OrderStatus status; //주문상태 [ORDER, CANCEL, PAYED]
    
    private String orderStartDate; //공연시작시간

    private String orderEndDate; //공연종료시간
    
    private int orderSum; //주문 합계 금액

}
