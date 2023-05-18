package jpabook.jpashop.controller;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class FlagSectionForm {
    private Long id;

    private String name;
    private int price;
    private int stockQuantity;
    private String startPlace;
    private String endPlace;
}
