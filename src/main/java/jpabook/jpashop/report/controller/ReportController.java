package jpabook.jpashop.report.controller;

import jpabook.jpashop.Service.UserSecurityService;
import jpabook.jpashop.domain.SettlementRowDTO;
import jpabook.jpashop.report.service.ReportService;
import jpabook.jpashop.repository.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final UserSecurityService userSecurityService;

    @GetMapping("/report")
    public String getReport(@ModelAttribute("orderSearch") OrderSearch orderSearch, Model model) {
        if (userSecurityService.LoginUserCheck() == "anonymousUser")
            return "login_form";

        // 월 기본값: 이번 달
        String month = orderSearch.getFindDate();
        if (month == null || month.isEmpty()) {
            month = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
            orderSearch.setFindDate(month);
        }

        List<SettlementRowDTO> rawRows = reportService.getSettlement(month);
        List<SettlementRowDTO> displayRows = buildDisplayRows(rawRows);

        // 합계 금액 (TOTAL 행)
        int grandTotal = displayRows.stream()
            .filter(r -> "TOTAL".equals(r.getRowType()))
            .mapToInt(SettlementRowDTO::getAmount).sum();

        model.addAttribute("reportList", displayRows);
        model.addAttribute("findMonth", month);
        model.addAttribute("grandTotal", grandTotal);

        return "report/report";
    }

    private List<SettlementRowDTO> buildDisplayRows(List<SettlementRowDTO> rawRows) {
        List<SettlementRowDTO> result = new ArrayList<>();
        int seq = 1;
        int grandTotal = 0;
        String currentName = null;
        int subtotalAmt = 0;

        for (SettlementRowDTO row : rawRows) {
            String name = row.getOrderName() != null ? row.getOrderName() : "";
            if (!name.equals(currentName)) {
                // 이전 공연 소계 행 추가
                if (currentName != null) {
                    result.add(makeSubtotal(currentName, subtotalAmt));
                    subtotalAmt = 0;
                }
                currentName = name;
            }
            row.setSeq(seq++);
            result.add(row);
            subtotalAmt += row.getAmount();
            grandTotal  += row.getAmount();
        }
        // 마지막 공연 소계
        if (currentName != null) {
            result.add(makeSubtotal(currentName, subtotalAmt));
        }
        // 총합계 행
        SettlementRowDTO total = new SettlementRowDTO();
        total.setRowType("TOTAL");
        total.setAmount(grandTotal);
        result.add(total);
        return result;
    }

    private SettlementRowDTO makeSubtotal(String orderName, int amount) {
        SettlementRowDTO sub = new SettlementRowDTO();
        sub.setRowType("SUBTOTAL");
        sub.setOrderName(orderName);
        sub.setAmount(amount);
        return sub;
    }
}
