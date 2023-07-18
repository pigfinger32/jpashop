package jpabook.jpashop.controller;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

@Getter @Setter
public class LoginForm {

    @NotEmpty(message = "회원 아이디는 필수 입니다.")
    private String loginId;

    @NotEmpty(message = "비밀번호를 입력해 주세요.")
    private String pw;

}
