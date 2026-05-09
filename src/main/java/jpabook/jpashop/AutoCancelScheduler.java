package jpabook.jpashop;

import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AutoCancelScheduler {

    @PersistenceContext
    private EntityManager em;

    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void autoCancelExpiredOrders() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(2);

        List<Order> expired = em.createQuery(
            "SELECT o FROM Order o WHERE o.status = :status AND o.active = true AND o.orderDate < :cutoff",
            Order.class)
            .setParameter("status", OrderStatus.ORDER)
            .setParameter("cutoff", cutoff)
            .getResultList();

        for (Order order : expired) {
            order.setStatus(OrderStatus.CANCEL);
            order.setActive(false);
        }

        if (!expired.isEmpty()) {
            log.info("자동 취소 처리: {}건 (기준: {})", expired.size(), cutoff.toLocalDate());
        }
    }
}
