package jpabook.jpashop.Service;

import jpabook.jpashop.domain.Notice;
import jpabook.jpashop.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class NoticeService {
    private final NoticeRepository noticeRepository;

    public List<Notice> NoticeList() {
        return noticeRepository.findAll();
    }

    public Notice NoticeOne(Long uid) {
        return noticeRepository.findOne(uid);
    }
    public void ViewcntUpdate(Long uid) {
        noticeRepository.updateViewCnt(uid);
    }

    public Long NoticeAdd(Notice notice) {
        noticeRepository.save(notice);
        return notice.getId();
    }
}
