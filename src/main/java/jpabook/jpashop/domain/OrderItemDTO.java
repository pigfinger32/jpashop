package jpabook.jpashop.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemDTO {
    private Long id;
    private String name;
    private int price;
    private int stockQuantity; //총수량
    private String startPlace;
    private String endPlace;
    private int usedStock; //거치수량
    private int curStock; //현재수량
}
