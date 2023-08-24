package jpabook.jpashop.domain;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter @Setter
public class Notice  {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "notice_id")
    private Long id;
    private String subject;
    private String contents;
    private String name;
    private String viewcnt;

}
