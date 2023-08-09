package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Order;

import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrdersDTO;
import jpabook.jpashop.domain.item.FlagSection;
import jpabook.jpashop.domain.item.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepository {
    @PersistenceContext
    private final EntityManager em;


    public void save(Order order) {
        em.persist(order);
        em.flush();//
    }

    public Order findOne(Long id) {
        return em.find(Order.class, id);
    }


    public void deleteById(Long id) {
        Order order1 = em.find(Order.class, id);
        em.remove(order1);
    }


    public List<Order> findAllByString(OrderSearch orderSearch) {

        //String jpql = "select o from Order o left join o.member m  ";
        //String jpql = "select o from OrderItem oi left join oi.order o left join o.member m left join oi.item i ";
        String jpql =
                " select o from Order o " +
                        " join fetch o.member m" +
                        " join fetch o.orderItems oi" +
                        " join fetch oi.item oi";
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

        jpql += " order by oi.name";


        TypedQuery<Order> query = em.createQuery(jpql, Order.class)
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

        List<Order> resultList = query.getResultList();

        //return query.getResultList();
        return resultList;
    }

    /**
     * JPA Criteria
     */
    public List<Order> findAllByCriteria(OrderSearch orderSearch) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> o = cq.from(Order.class);
        Join<Object, Object> m = o.join("member", JoinType.INNER);

        List<Predicate> criteria = new ArrayList<>();

        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            Predicate status = cb.equal(o.get("status"), orderSearch.getOrderStatus());
            criteria.add(status);
        }
        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            Predicate name =
                    cb.like(m.<String>get("name"), "%" + orderSearch.getMemberName() + "%");
            criteria.add(name);
        }

        cq.where(cb.and(criteria.toArray(new Predicate[criteria.size()])));
        TypedQuery<Order> query = em.createQuery(cq).setMaxResults(1000);
        return query.getResultList();
    }

}



