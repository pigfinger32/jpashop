package jpabook.jpashop.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Member extends BaseEntity {

    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    private String pw; //2023-05-09 pw 추가

    private String name;

    private String company; //2023-05-09 company 회사이름 추가


    @Embedded
    private Address address;

    private String phone; //2023-05-17 전화번호 추가
    
    private String bizRegiNo; //2023-05-17 사업자등록번호 추가

    @OneToMany(mappedBy = "member")
    private List<Order> orders = new ArrayList<>();


}
