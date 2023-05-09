package jpabook.jpashop.controller;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;


@Getter @Setter
public class MemberForm {

    @NotEmpty(message = "회원 이름은 필수 입니다.")
    private String name;

    private String pw; //2023-05-09 MemberForm GetPw추가

    private String company; //2023-05-09 MemberForm GetCompany추가

    private String city;
    private String street;
    private String zipcode;
}
