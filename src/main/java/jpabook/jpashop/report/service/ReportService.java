package jpabook.jpashop.report.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.ReportResDTO;
import jpabook.jpashop.domain.SettlementRowDTO;
import jpabook.jpashop.report.repository.ReportRepository;
import jpabook.jpashop.repository.ItemRepository;
import jpabook.jpashop.repository.MemberRepository;
import jpabook.jpashop.repository.OrderItemRepository;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ReportService {
	
	private final OrderRepository orderRepository;
	private final ReportRepository reportRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;
    private final OrderItemRepository orderItemRepository;
    private final EntityManager em;
    
    public List<ReportResDTO> getOrderReport(OrderSearch orderSearch) {
        return reportRepository.getOrderReport(orderSearch);
    }

    public List<SettlementRowDTO> getSettlement(String month) {
        return reportRepository.getSettlement(month);
    }


}
