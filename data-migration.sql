-- ================================================================
-- 가로기 데이터 초기화 스크립트
-- 실행 방법 (VM SSH 접속 후):
--   docker compose exec db mysql -uroot -p"${MYSQL_ROOT_PASSWORD}" jpashop < data-migration.sql
--
-- ※ 주의: 아래 company 이름이 DB와 다를 경우 WHERE 절을 수정하세요.
--         Item name이 '1구간', '2구간' 형식이 아닌 경우도 수정 필요.
-- ================================================================

-- 1. 기존 예약/결산 데이터 전체 삭제
DELETE FROM OrderItem;
DELETE FROM orders;

-- ================================================================
-- 2. 한올엔터테인 / 가)베베핀뮤지컬 (상업용, 30일, 단가 32,000원)
--    구간별 수량: 1·2·5·6·7·8구간 5개씩, 9·11·13구간 10개씩,
--                10·12구간 5개씩  → 합계 70개
-- ================================================================
INSERT INTO orders (member_id, orderName, orderStartDate, orderEndDate, orderDate, status, active)
SELECT
    m.member_id,
    '가)베베핀뮤지컬',
    '2026-06-01',
    '2026-06-30',
    NOW(),
    'PAYED',
    1
FROM Member m
WHERE m.company LIKE '%한올%'
LIMIT 1;

SET @o1 = LAST_INSERT_ID();

INSERT INTO OrderItem (order_id, item_id, orderPrice, count)
SELECT @o1, item_id, 32000, 5  FROM Item WHERE name = '1구간'  LIMIT 1;
INSERT INTO OrderItem (order_id, item_id, orderPrice, count)
SELECT @o1, item_id, 32000, 5  FROM Item WHERE name = '2구간'  LIMIT 1;
INSERT INTO OrderItem (order_id, item_id, orderPrice, count)
SELECT @o1, item_id, 32000, 5  FROM Item WHERE name = '5구간'  LIMIT 1;
INSERT INTO OrderItem (order_id, item_id, orderPrice, count)
SELECT @o1, item_id, 32000, 5  FROM Item WHERE name = '6구간'  LIMIT 1;
INSERT INTO OrderItem (order_id, item_id, orderPrice, count)
SELECT @o1, item_id, 32000, 5  FROM Item WHERE name = '7구간'  LIMIT 1;
INSERT INTO OrderItem (order_id, item_id, orderPrice, count)
SELECT @o1, item_id, 32000, 5  FROM Item WHERE name = '8구간'  LIMIT 1;
INSERT INTO OrderItem (order_id, item_id, orderPrice, count)
SELECT @o1, item_id, 32000, 10 FROM Item WHERE name = '9구간'  LIMIT 1;
INSERT INTO OrderItem (order_id, item_id, orderPrice, count)
SELECT @o1, item_id, 32000, 5  FROM Item WHERE name = '10구간' LIMIT 1;
INSERT INTO OrderItem (order_id, item_id, orderPrice, count)
SELECT @o1, item_id, 32000, 10 FROM Item WHERE name = '11구간' LIMIT 1;
INSERT INTO OrderItem (order_id, item_id, orderPrice, count)
SELECT @o1, item_id, 32000, 5  FROM Item WHERE name = '12구간' LIMIT 1;
INSERT INTO OrderItem (order_id, item_id, orderPrice, count)
SELECT @o1, item_id, 32000, 10 FROM Item WHERE name = '13구간' LIMIT 1;

-- ================================================================
-- 3. 소라광고 / 가)여수에국체음악제 (공공기관용, 30일, 단가 20,000원)
--    구간별 수량: 6구간 5개, 7구간 10개, 8구간 5개 → 합계 20개
-- ================================================================
INSERT INTO orders (member_id, orderName, orderStartDate, orderEndDate, orderDate, status, active)
SELECT
    m.member_id,
    '가)여수에국체음악제',
    '2026-06-01',
    '2026-06-30',
    NOW(),
    'PAYED',
    1
FROM Member m
WHERE m.company LIKE '%소라%'
LIMIT 1;

SET @o2 = LAST_INSERT_ID();

INSERT INTO OrderItem (order_id, item_id, orderPrice, count)
SELECT @o2, item_id, 20000, 5  FROM Item WHERE name = '6구간'  LIMIT 1;
INSERT INTO OrderItem (order_id, item_id, orderPrice, count)
SELECT @o2, item_id, 20000, 10 FROM Item WHERE name = '7구간'  LIMIT 1;
INSERT INTO OrderItem (order_id, item_id, orderPrice, count)
SELECT @o2, item_id, 20000, 5  FROM Item WHERE name = '8구간'  LIMIT 1;

-- ================================================================
-- 결과 확인
-- ================================================================
SELECT o.order_id, o.orderName, m.company, o.orderStartDate, o.orderEndDate,
       o.status, SUM(oi.count) AS totalCount, MAX(oi.orderPrice) AS unitPrice
FROM orders o
JOIN Member m ON o.member_id = m.member_id
JOIN OrderItem oi ON o.order_id = oi.order_id
GROUP BY o.order_id, o.orderName, m.company, o.orderStartDate, o.orderEndDate, o.status;
