package jpabook.jpashop.domain;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SettlementRowDTO {
    private String rowType;    // "DATA" | "SUBTOTAL" | "TOTAL"
    private int    seq;        // 순번 (DATA 행만)
    private String orderName;  // 공연명
    private String memberName; // 신청자
    private String sectionName;// 게첨 구간
    private int    count;      // 수량
    private int    unitPrice;  // 단가
    private int    amount;     // 금액 (count × unitPrice)
    private String startDate;  // 게첨 시작일
    private String endDate;    // 게첨 종료일
    private String statusLabel;// 신청 / 결제완료 / 취소
}
