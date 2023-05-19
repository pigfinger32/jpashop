package jpabook.jpashop.domain;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;
@Getter @Setter
public class MemberDTO {

    private String userid;
    private String passwd;
    private String name;
    private String email;
    private Date join_date;

    @Override
    public String toString() {
        return "MemberDTO [userid=" + userid + ", passwd=" + passwd + ", name=" + name + ", email=" + email + ", join_date=" + join_date + "]";
    }
}
