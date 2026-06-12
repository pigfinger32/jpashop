package jpabook.jpashop.domain;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SettlementRowDTO {
    private String rowType;    // "DATA" | "TOTAL"
    private Long   orderId;    // 취소 버튼용
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
    private int    term;           // 기간(일): 15 or 30
    private String eventType;      // 구분: 공공기관용 / 상업용
    private int    associationFee; // 협회분 (count × 3,000)
    private int    operationFee;   // 운영분 (count × (기본단가 - 3,000))
    private int    cityStampFee;   // 시 인지대 (count × (단가 - 기본단가), 상업용만 발생)
}
