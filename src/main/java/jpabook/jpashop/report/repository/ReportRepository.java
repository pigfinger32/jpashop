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
import jpabook.jpashop.repository.OrderSearch;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ReportRepository {
	@PersistenceContext
    private final EntityManager em;
	
	 public List<ReportResDTO> getOrderReport(OrderSearch orderSearch) {
	        // 네이티브 SQL 쿼리 작성 시작
	        StringBuilder sql = new StringBuilder();
	        sql.append("SELECT ");
	        sql.append("    od.orderStartDate AS startDate, "); // ReportResDTO 필드에 맞게 매핑
	        sql.append("    od.member_id AS memberId, "); // ReportResDTO 필드에 맞게 매핑
	        sql.append("    oi.item_id AS itemId, "); // OrderItem의 item_id 사용 (롤업 시 null이 될 수 있음)
	        sql.append("    od.orderName AS orderName, ");
	        sql.append("    oi.orderPrice AS orderPrice, "); // OrderItem의 orderPrice 사용 (롤업 시 null이 될 수 있음)
	        sql.append("    oi.count AS count, "); // OrderItem의 count 사용 (롤업 시 null이 될 수 있음)
	        sql.append("    od.status AS status, ");
	        sql.append("    od.orderStartDate AS orderStartDate, ");
	        sql.append("    od.orderEndDate AS orderEndDate, ");
	        // 롤업된 총합을 위해 COUNT * ORDERPRICE의 합계를 사용
	        sql.append("    SUM(oi.count * oi.orderPrice) AS orderSum ");
	        sql.append("FROM ");
	        sql.append("    orders od ");
	        sql.append("LEFT JOIN ");
	        sql.append("    OrderItem oi ON od.order_id = oi.order_id ");

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

	        // 회원 이름 검색 (Member 테이블 조인이 필요합니다. 여기서는 orders 테이블에 member_id가 있으므로 직접 사용할 수 있으나, 회원이름으로 검색하려면 Member 테이블 조인이 필요합니다.)
	        // 회원 이름을 기준으로 검색하려면 orders 테이블과 Member 테이블을 JOIN 해야 합니다.
	        // SQL 쿼리에 Member 테이블 JOIN 추가
	        if (StringUtils.hasText(orderSearch.getMemberName())) {
	             sql.append(" JOIN Member m ON od.member_id = m.member_id"); // Member 테이블 조인
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
	        
	        int rowCnt = 1;

	        for (Object[] row : resultList) {
	            ReportResDTO dto = new ReportResDTO();
	            
	            if(rowCnt == resultList.size()) {
	            	dto.setOrderEndDate("합계"); // od.orderEndDate
		            // row[9]는 orderSum int 타입으로 매핑 (SUM 결과는 보통 Long 또는 BigDecimal로 나오므로 형변환 필요)
		            dto.setOrderSum(row[9] != null ? ((Number) row[9]).intValue() : 0); // SUM(oi.count * oi.orderPrice)
	            	
	            } else {
	            	// 결과 컬럼 순서에 맞게 매핑합니다.
		            // SQL 쿼리의 SELECT 절 순서와 일치해야 합니다.
		            dto.setStartDate((String) row[0]); // od.orderStartDate
		            // row[1]은 MemberId Long 타입으로 매핑 시 Long.valueOf(row[1].toString()) 또는 (Long) row[1] 사용 (DB 드라이버에 따라 다름)
		            dto.setMemberId(row[1] != null ? ((Number) row[1]).longValue() : null); // od.member_id
		            // row[2]는 ItemId Long 타입으로 매핑
		            dto.setItemId(row[2] != null ? ((Number) row[2]).longValue() : null); // oi.item_id
		            dto.setOrderName((String) row[3]); // od.orderName
		            // row[4]는 orderPrice int 타입으로 매핑
		            dto.setOrderPrice(row[4] != null ? ((Number) row[4]).intValue() : 0); // oi.orderPrice
		            // row[5]는 count int 타입으로 매핑
		            dto.setCount(row[5] != null ? ((Number) row[5]).intValue() : 0); // oi.count
		            // row[6]은 status String 또는 Enum 타입으로 매핑 (DB에 저장된 형식에 따라)
		            // String으로 가져와서 Enum으로 변환하거나, 필요에 따라 String으로 그대로 사용
		            String statusStr = (String) row[6];
		            if (statusStr != null) {
		                try {
		                     dto.setStatus(OrderStatus.valueOf(statusStr));
		                } catch (IllegalArgumentException e) {
		                    dto.setStatus(null); // 매핑할 수 없는 상태값인 경우
		                }
		            } else {
		                dto.setStatus(null); // 롤업 ROW의 경우 NULL
		            }
		            dto.setOrderStartDate((String) row[7]); // od.orderStartDate
		            dto.setOrderEndDate((String) row[8]); // od.orderEndDate
		            // row[9]는 orderSum int 타입으로 매핑 (SUM 결과는 보통 Long 또는 BigDecimal로 나오므로 형변환 필요)
		            dto.setOrderSum(row[9] != null ? ((Number) row[9]).intValue() : 0); // SUM(oi.count * oi.orderPrice)
	            }

	            
	            rowCnt++;
	            dtoList.add(dto);
	        }

	        return dtoList;
	    }

}
