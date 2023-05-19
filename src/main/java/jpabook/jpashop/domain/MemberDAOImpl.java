package jpabook.jpashop.domain;

import org.springframework.beans.factory.annotation.Autowired;

public class MemberDAOImpl implements MemberDAO{

    @Autowired
    @Override
    public String loginCheck(MemberDTO dto) {
        return null;
    }
}
