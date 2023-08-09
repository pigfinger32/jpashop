package jpabook.jpashop.repository;

import jpabook.jpashop.domain.OrderItem;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderItemRepository {
    @PersistenceContext
    private final EntityManager em;

    public void save(OrderItem orderItem) {
        em.persist(orderItem);
    }

    public OrderItem findOne(Long id) {
        return em.find(OrderItem.class, id);
    }

    public List<OrderItem> findAll() {
        return em.createQuery("select oi from OrderItem oi", OrderItem.class)
                .getResultList();
    }

    // 공연명을 통한 검색

    // 날짜를 통한 검색

    //주문시 수량조회 락추가
    //@Lock(value = LockModeType.PESSIMISTIC_WRITE)
    public List<OrderItem> findAllByStringWithDbLock(OrderSearch orderSearch) {

        return findAllByStringInMakingQuery(orderSearch);
    }

    public List<OrderItem> findAllByString(OrderSearch orderSearch) {

        return findAllByStringInMakingQuery(orderSearch);
    }

    private List<OrderItem> findAllByStringInMakingQuery(OrderSearch orderSearch) {
        //String jpql = "select o from Order o left join o.member m  ";
        String jpql = " select oi from OrderItem oi" +
                                " join fetch oi.order o " +
                                " join fetch o.member m " +
                                " join fetch oi.item i ";
        boolean isFirstCondition = true;

        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " o.status = :status";
        }

        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " m.name like :name";
        }

        //공연명 검색 2023-05-11 공연명 검색 추가
        if (StringUtils.hasText(orderSearch.getOrderName())) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " o.orderName like :orderName";
        }

        //공연명 검색 2023-05-11 공연시작 시간 검색 추가
        if (StringUtils.hasText(orderSearch.getFindDate())) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " o.orderStartDate <= :findDate";
            jpql += " and o.orderEndDate >= :findDate";
        }

        jpql += " order by i.name";


        TypedQuery<OrderItem> query = em.createQuery(jpql, OrderItem.class)
                .setMaxResults(1000);

        if (orderSearch.getOrderStatus() != null) {
            query = query.setParameter("status", orderSearch.getOrderStatus());
        }
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            query = query.setParameter("name", orderSearch.getMemberName());
        }

        //공연명 검색 2023-05-11 공연명 검색 추가
        if (StringUtils.hasText(orderSearch.getOrderName())) {
            query = query.setParameter("orderName", orderSearch.getOrderName());
        }
        //공연명 검색 2023-05-11 공연시작 시간 검색 추가
        if (StringUtils.hasText(orderSearch.getFindDate())) {
            query = query.setParameter("findDate", orderSearch.getFindDate());
        }

        List<OrderItem> resultList = query.getResultList();

        //return query.getResultList();
        return resultList;
    }

    public void deleteById(Long id) {
        OrderItem orderItem = em.find(OrderItem.class, id);
        em.remove(orderItem);
    }
}
