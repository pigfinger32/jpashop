package jpabook.jpashop.report.repository;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.ReportResDTO;
import jpabook.jpashop.domain.SettlementRowDTO;
import jpabook.jpashop.repository.OrderSearch;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ReportRepository {
	@PersistenceContext
    private final EntityManager em;
	
    public List<SettlementRowDTO> getSettlement(String month) {
        String sql =
            "SELECT od.orderName, m.name AS memberName, " +
            "       DATEDIFF(od.orderEndDate, od.orderStartDate) AS term, " +
            "       SUM(oi.count) AS totalCount, " +
            "       MAX(oi.orderPrice) AS unitPrice, " +
            "       od.orderStartDate, od.orderEndDate, od.status " +
            "FROM orders od " +
            "JOIN OrderItem oi ON od.order_id = oi.order_id " +
            "JOIN Member m ON od.member_id = m.member_id " +
            "WHERE DATE_FORMAT(od.orderStartDate, '%Y-%m') = :month " +
            "GROUP BY od.order_id, od.orderName, m.name, od.orderStartDate, od.orderEndDate, od.status " +
            "ORDER BY od.orderName, od.orderStartDate";

        Query query = em.createNativeQuery(sql);
        query.setParameter("month", month);

        List<Object[]> rows = query.getResultList();
        List<SettlementRowDTO> result = new ArrayList<>();
        for (Object[] row : rows) {
            SettlementRowDTO dto = new SettlementRowDTO();
            dto.setRowType("DATA");
            dto.setOrderName((String) row[0]);
            dto.setMemberName((String) row[1]);
            dto.setTerm(row[2] != null ? ((Number) row[2]).intValue() : 30);
            int count = row[3] != null ? ((Number) row[3]).intValue() : 0;
            int unitPrice = row[4] != null ? ((Number) row[4]).intValue() : 0;
            dto.setCount(count);
            dto.setUnitPrice(unitPrice);
            dto.setAmount(count * unitPrice);
            dto.setAssociationFee(count * 3000);
            dto.setOperationFee(count * (unitPrice - 3000));
            dto.setStartDate((String) row[5]);
            dto.setEndDate((String) row[6]);
            String st = (String) row[7];
            dto.setStatusLabel("PAYED".equals(st) ? "결제완료" : "CANCEL".equals(st) ? "취소" : "신청");
            result.add(dto);
        }
        return result;
    }

    public List<SettlementRowDTO> getMyOrders(Long memberId) {
        String sql =
            "SELECT od.order_id, od.orderName, COALESCE(m.company, m.name) AS companyName, " +
            "       DATEDIFF(od.orderEndDate, od.orderStartDate) AS term, " +
            "       SUM(oi.count) AS totalCount, " +
            "       MAX(oi.orderPrice) AS unitPrice, " +
            "       od.orderStartDate, od.orderEndDate, od.status " +
            "FROM orders od " +
            "JOIN OrderItem oi ON od.order_id = oi.order_id " +
            "JOIN Member m ON od.member_id = m.member_id " +
            "WHERE od.active = 1 " +
            (memberId != null ? "AND od.member_id = :memberId " : "") +
            "GROUP BY od.order_id, od.orderName, m.company, m.name, od.orderStartDate, od.orderEndDate, od.status " +
            "ORDER BY od.orderStartDate DESC, od.orderName";

        Query query = em.createNativeQuery(sql);
        if (memberId != null) query.setParameter("memberId", memberId);

        List<Object[]> rows = query.getResultList();
        List<SettlementRowDTO> result = new ArrayList<>();
        int seq = 1;
        for (Object[] row : rows) {
            SettlementRowDTO dto = new SettlementRowDTO();
            dto.setSeq(seq++);
            dto.setRowType("DATA");
            dto.setOrderId(row[0] != null ? ((Number) row[0]).longValue() : null);
            dto.setOrderName((String) row[1]);
            dto.setMemberName((String) row[2]);
            dto.setTerm(row[3] != null ? ((Number) row[3]).intValue() : 30);
            int count    = row[4] != null ? ((Number) row[4]).intValue() : 0;
            int unitPrice = row[5] != null ? ((Number) row[5]).intValue() : 0;
            dto.setCount(count);
            dto.setUnitPrice(unitPrice);
            dto.setAmount(count * unitPrice);
            dto.setStartDate((String) row[6]);
            dto.setEndDate((String) row[7]);
            String st = (String) row[8];
            dto.setStatusLabel("PAYED".equals(st) ? "결제완료" : "CANCEL".equals(st) ? "취소" : "신청");
            result.add(dto);
        }
        return result;
    }

	 public List<ReportResDTO> getOrderReport(OrderSearch orderSearch) {
	        // 네이티브 SQL 쿼리 작성 시작
	        StringBuilder sql = new StringBuilder();
	        sql.append("SELECT ");
	        sql.append("    od.orderStartDate AS startDate, "); // 인덱스 0
	        sql.append("    od.member_id AS memberId, "); // 인덱스 1
            sql.append("    m.name AS memberName, ");     // *** 새로 추가된 회원명, 인덱스 2 ***
	        sql.append("    oi.item_id AS itemId, "); // 인덱스 3 (기존 2에서 이동)
	        sql.append("    od.orderName AS orderName, "); // 인덱스 4 (기존 3에서 이동)
	        sql.append("    oi.orderPrice AS orderPrice, "); // 인덱스 5 (기존 4에서 이동)
	        sql.append("    oi.count AS count, "); // 인덱스 6 (기존 5에서 이동)
	        sql.append("    od.status AS status, "); // 인덱스 7 (기존 6에서 이동)
	        sql.append("    od.orderStartDate AS orderStartDate, "); // 인덱스 8 (기존 7에서 이동)
	        sql.append("    od.orderEndDate AS orderEndDate, "); // 인덱스 9 (기존 8에서 이동)
	        // 롤업된 총합을 위해 COUNT * ORDERPRICE의 합계를 사용
	        sql.append("    SUM(oi.count * oi.orderPrice) AS orderSum "); // 인덱스 10 (기존 9에서 이동)
	        sql.append("FROM ");
	        sql.append("    orders od ");
	        sql.append("LEFT JOIN ");
	        sql.append("    OrderItem oi ON od.order_id = oi.order_id ");
            sql.append(" JOIN Member m ON od.member_id = m.member_id"); // *** Member 테이블 항상 조인 ***

	        // WHERE 절 조건 추가
	        boolean isFirstCondition = true;

	        // 주문 상태 검색
	        if (orderSearch.getOrderStatus() != null) {
	            if (isFirstCondition) {
	                sql.append(" WHERE");
	                isFirstCondition = false;
	            } else {
	                sql.append(" AND");
	            }
	            sql.append(" od.status = :status");
	        }

	        // 회원 이름 검색 (Member 테이블 조인은 위에서 이미 했습니다)
	        if (StringUtils.hasText(orderSearch.getMemberName())) {
	            if (isFirstCondition) {
	                sql.append(" WHERE");
	                isFirstCondition = false;
	            } else {
	                sql.append(" AND");
	            }
	            sql.append(" m.name LIKE :name");
	        }

	        // 공연명 검색
	        if (StringUtils.hasText(orderSearch.getOrderName())) {
	            if (isFirstCondition) {
	                sql.append(" WHERE");
	                isFirstCondition = false;
	            } else {
	                sql.append(" AND");
	            }
	            sql.append(" od.orderName LIKE :orderName");
	        }

	        // 조회 날짜 검색 (orderStartDate, orderEndDate 범위 검색)
	        if (StringUtils.hasText(orderSearch.getFindDate())) {
	            if (isFirstCondition) {
	                sql.append(" WHERE");
	                isFirstCondition = false;
	            } else {
	                sql.append(" AND");
	            }
	            sql.append(" od.orderStartDate <= :findDate");
	            sql.append(" AND od.orderEndDate >= :findDate");
	        }

	        sql.append(" GROUP BY ");
	        sql.append("    od.order_id ");
	        sql.append(" WITH ROLLUP "); // 전체 합계를 위한 ROLLUP 추가

	        // 정렬 (MySQL에서 NULL LAST 처리를 위해 IS NULL ASC 사용)
	        sql.append(" ORDER BY ");
	        // od.order_id가 NULL인 경우 (롤업 ROW)를 마지막으로 보내고, 그 다음 od.order_id로 오름차순 정렬
	        sql.append("    od.order_id IS NULL ASC, od.order_id ASC, ");
	        // oi.item_id가 NULL인 경우를 마지막으로 보내고, 그 다음 oi.item_id로 오름차순 정렬
	        sql.append("    oi.item_id IS NULL ASC, oi.item_id ASC ");

	        // 네이티브 쿼리 생성
	        Query nativeQuery = em.createNativeQuery(sql.toString());

	        // 파라미터 바인딩
	        if (orderSearch.getOrderStatus() != null) {
	            nativeQuery.setParameter("status", orderSearch.getOrderStatus().name()); // Enum 값을 String으로 변환하여 바인딩
	        }
	        if (StringUtils.hasText(orderSearch.getMemberName())) {
	             nativeQuery.setParameter("name", "%" + orderSearch.getMemberName() + "%"); // LIKE 검색을 위해 % 추가
	        }
	        if (StringUtils.hasText(orderSearch.getOrderName())) {
	            nativeQuery.setParameter("orderName", "%" + orderSearch.getOrderName() + "%"); // LIKE 검색을 위해 % 추가
	        }
	        if (StringUtils.hasText(orderSearch.getFindDate())) {
	            nativeQuery.setParameter("findDate", orderSearch.getFindDate()); // 날짜 형식에 맞게 바인딩
	        }

	        // 결과를 ReportResDTO로 매핑 (Object[] 배열 형태로 반환되므로 수동 매핑 필요)
	        List<Object[]> resultList = nativeQuery.getResultList();
	        List<ReportResDTO> dtoList = new ArrayList<>();
	        
	        int rowCnt = 1; // 행 카운트 초기화

	        for (Object[] row : resultList) {
	            ReportResDTO dto = new ReportResDTO();

	            // 현재 행이 결과 리스트의 마지막 행인지 확인 (ROLLUP 결과 행으로 가정)
	            if(rowCnt == resultList.size()) {
	            	// 마지막 ROW (총합 행):
	            	// '공연 종료 시간' (새로운 인덱스 9)에 '합계' 문자열 설정
	            	dto.setOrderEndDate("합계");
		            // '주문 합계 금액' (새로운 인덱스 10)에 총합 금액 설정
		            // SUM 결과는 보통 Long 또는 BigDecimal로 나오므로 int로 형변환
		            dto.setOrderSum(row[10] != null ? ((Number) row[10]).intValue() : 0); // SUM(oi.count * oi.orderPrice)

		            // 나머지 필드들 (0-8)은 여기서 별도로 설정하지 않습니다.
		            // ReportResDTO 객체가 생성될 때 객체 타입 필드는 null, 기본 타입(int 등)은 0으로 초기화된 상태를 그대로 사용합니다.

	            } else {
	            	// 일반 ROW: 모든 컬럼 값을 DTO에 매핑
	            	// SQL 쿼리의 SELECT 절 순서와 Object[] 배열의 인덱스가 일치해야 합니다.

		            dto.setStartDate((String) row[0]); // od.orderStartDate - 인덱스 0

		            // memberId (인덱스 1)
		            dto.setMemberId(row[1] != null ? ((Number) row[1]).longValue() : null);

		            // memberName (새로운 인덱스 2)
	                dto.setMemberName((String) row[2]);

		            // itemId (새로운 인덱스 3) - 원래 인덱스 2
		            dto.setItemId(row[3] != null ? ((Number) row[3]).longValue() : null); // oi.item_id

		            dto.setOrderName((String) row[4]); // od.orderName - 원래 인덱스 3

		            // orderPrice (새로운 인덱스 5) - 원래 인덱스 4
		            dto.setOrderPrice(row[5] != null ? ((Number) row[5]).intValue() : 0); // oi.orderPrice

		            // count (새로운 인덱스 6) - 원래 인덱스 5
		            dto.setCount(row[6] != null ? ((Number) row[6]).intValue() : 0); // oi.count

		            // status (새로운 인덱스 7) - 원래 인덱스 6
		            String statusStr = (String) row[7];
		            if (statusStr != null) {
		                try {
		                     dto.setStatus(OrderStatus.valueOf(statusStr));
		                } catch (IllegalArgumentException e) {
		                    dto.setStatus(null); // 매핑할 수 없는 상태값인 경우
		                }
		            } else {
		                dto.setStatus(null); // DB 값이 NULL인 경우
		            }

		            dto.setOrderStartDate((String) row[8]); // od.orderStartDate - 원래 인덱스 7
		            dto.setOrderEndDate((String) row[9]); // od.orderEndDate - 원래 인덱스 8
		            // orderSum (새로운 인덱스 10) - 원래 인덱스 9
		            // 이 ROW의 합계
		            dto.setOrderSum(row[10] != null ? ((Number) row[10]).intValue() : 0); // SUM(oi.count * oi.orderPrice)
	            }

	            rowCnt++; // 행 카운트 증가
	            dtoList.add(dto);
	        }

	        return dtoList;
	    }

}
