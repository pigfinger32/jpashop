package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Notice;
import jpabook.jpashop.domain.item.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class NoticeRepository {
    @PersistenceContext
    private final EntityManager em;
    private final JdbcTemplate jdbcTemplate;

    public void save(Notice notice) {
        em.persist(notice);
    }

    public Notice findOne(Long id) {
        return em.find(Notice.class, id);
    }

    public void deleteOne(Long id) { jdbcTemplate.update("delete from Notice where notice_id = ?", id);}

    public List<Notice> findAll() {
        return em.createQuery("select n from Notice n", Notice.class)
                .getResultList();
    }

    public void UpdateOne(Notice notice) {
        jdbcTemplate.update(("update Notice set subject=?, contents=? where notice_id=?")
                , notice.getSubject()
                , notice.getContents()
                , notice.getId()
        );
    }

    public void updateViewCnt(Long uid) {
        jdbcTemplate.update("update Notice set viewcnt = viewcnt + 1 where notice_id = ?", uid);
    }

    public List<Notice> findByName(String subject) {
        return em.createQuery("select n from Notice n where n.subject = :subject", Notice.class)
                .setParameter("subject", subject)
                .getResultList();
    }
}
