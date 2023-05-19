package jpabook.jpashop.domain;

import org.springframework.beans.factory.annotation.Autowired;

public class MemberDAOImpl implements MemberDAO{

    @Autowired
    SqlSession sqlSession; //Sqlsession 의존관계 주입
    @Override
    public String loginCheck(MemberDTO dto) {
        return
    }
}
