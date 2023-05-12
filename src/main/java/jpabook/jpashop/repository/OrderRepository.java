package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Order;

import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.item.FlagSection;
import jpabook.jpashop.domain.item.Item;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Repository
public class OrderRepository {

    private final EntityManager em;
    private ItemRepository itemRepository;

    public OrderRepository(EntityManager em) {
        this.em = em;
    }

    public void save(Order order) {
        em.persist(order);
    }

    public Order findOne(Long id) {
        return em.find(Order.class, id);
    }

    public List<Order> findAllByString(OrderSearch orderSearch) {

        //String jpql = "select o from Order o left join o.member m  ";
        String jpql = "select o from OrderItem oi left join oi.order o left join o.member m left join oi.item i ";
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


/*//
        List<Order> resultList = query.getResultList();
        resultList.clear();

        Iterator<Order> iterator = tempList.iterator();

        int i = 0; int itemCnt = 0; String orderItem = "";
for(Order order : tempList) {
        while(iterator.hasNext()) {
                Order order = iterator.next();
                if((i == 0) || (orderItem == "")) {
                orderItem = order.getOrderName();
                itemCnt += order.getOrderCnt();//주문 수량 더하기
                i++;
                resultList.add(order);
                }else {
                if(orderItem == order.getOrderItemName()) {
                itemCnt += order.getOrderCnt();//주문 수량 더하기
                i++;
                resultList.add(order);
                } else {
                //가짜 Order객체를 만들어서 구간명, 수량을 입력한 후 List에 추가하자.
                Order fakeOrder = new Order();
                fakeOrder = order;


                OrderItem orderItem1 = new OrderItem();
                orderItem1.setCount(20);

                List<OrderItem> orderItemList = new ArrayList<>();
        orderItemList.add(orderItem1);

        fakeOrder.setOrderItems(orderItemList);

        resultList.add(fakeOrder);
        resultList.add(order);


        orderItem = order.getOrderItemName();
        itemCnt = 0;//주문 수량 더하기
        i++;
        }
        }
        }*/

