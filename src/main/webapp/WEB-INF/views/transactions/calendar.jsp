<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>월간 달력</title>
<link rel="stylesheet" href="<c:url value='/resources/css/common.css'/>">
<style>
.cal {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 8px;
}

.cal .dow {
  font-weight: bold;
  text-align: center;
  padding: 6px 0;
  border-bottom: 1px solid #ddd;
}

.cal .cell {
  border: 1px solid #e4e4e4;
  border-radius: 8px;
  min-height: 120px;
  padding: 8px;
  position: relative;
  background: #fff;
  cursor: pointer;
}

/* 달력 셀 hover (이번 달 날짜만) */
.cal .cell:hover {
  background: #ffe6e6;   /* 연한 분홍 */
  border-color: #f3bcbc;
}

/* 이번 달이 아닌 날짜 */
.cal .out {
  background: #f0f0f0;   /* 더 진한 회색 */
  color: #999;
  pointer-events: none;   /* 클릭/hover 이벤트 무시 */
}

.cal .date {
  font-size: 12px;
  position: absolute;
  top: 6px;
  right: 8px;
}

.cal .sum { font-size: 12px; margin-top: 18px; line-height: 1.3; }

.cal .txns { margin-top: 6px; font-size: 12px; list-style: none; padding-left: 0; }
.cal .txns li { display: flex; justify-content: space-between; gap: 6px; }

.cal .amt-in { color: #0a7; }
.cal .amt-out { color: #c33; }
</style>
</head>
<body>
  <jsp:include page="/WEB-INF/views/common/header.jsp" />
  <jsp:include page="/WEB-INF/views/common/aside.jsp" />

  <main id="main" data-ledger-id="${ledgerId}">
    <div class="container">
      <h2>달력 (${month})</h2>

      <!-- 공통 toolbar 스타일 적용 -->
      <div class="toolbar">
        <c:url var="prevUrl" value="/ledgers/${ledgerId}/transaction/calendar">
          <c:param name="month" value="${prevMonth}" />
        </c:url>
        <a class="btn btn-primary outline" href="${prevUrl}">◀ 이전달</a>

        <form method="get"
              action="<c:url value='/ledgers/${ledgerId}/transaction/calendar'/>"
              class="search-form">
          <input type="month" name="month" value="${month}" />
          <button type="submit" class="btn btn-primary">이동</button>
        </form>

        <c:url var="nextUrl" value="/ledgers/${ledgerId}/transaction/calendar">
          <c:param name="month" value="${nextMonth}" />
        </c:url>
        <a class="btn btn-primary outline" href="${nextUrl}">다음달 ▶</a>

        <a class="btn btn-primary"
           href="<c:url value='/ledgers/${ledgerId}/transaction?month=${month}'/>"
           style="margin-left:auto;">표 목록 보기</a>
      </div>

      <!-- 공통 card 스타일 적용 -->
      <div class="card">
        <strong>이 달 합계</strong> —
        <span class="amt-in" style="margin-left:8px;">수입: +${monthIncome}</span>
        <span class="amt-out" style="margin-left:12px;">지출: -${monthExpense}</span>
      </div>

      <!-- 요일 헤더 + 날짜 -->
      <div class="cal">
        <div class="dow">일</div>
        <div class="dow">월</div>
        <div class="dow">화</div>
        <div class="dow">수</div>
        <div class="dow">목</div>
        <div class="dow">금</div>
        <div class="dow">토</div>

        <c:forEach var="w" items="${weeks}">
          <c:forEach var="d" items="${w.days}">
            <div class="cell <c:if test='${!d.inMonth}'>out</c:if>" data-date="${d.date}">
              <div class="date">${fn:substring(d.date, 8, 10)}</div>

              <div class="sum">
                <c:if test="${not empty d.income}">
                  <div class="amt-in">+${d.income.substring(1)}</div>
                </c:if>
                <c:if test="${not empty d.expense}">
                  <div class="amt-out">-${d.expense.substring(1)}</div>
                </c:if>
              </div>

              <ul class="txns">
                <c:forEach var="t" items="${d.txns}">
                  <li>
                    <span>${t.categoryName}</span>
                    <span class="<c:out value='${t.type == "INCOME" ? "amt-in" : "amt-out"}'/>">${t.amount}</span>
                  </li>
                </c:forEach>
              </ul>
            </div>
          </c:forEach>
        </c:forEach>
      </div>
    </div>
  </main>

  <script src="<c:url value='/resources/js/common.js'/>"></script>
  <script>
    var newBaseUrl = '<c:url value="/ledgers/${ledgerId}/transaction/new"/>';
    document.addEventListener('click', function(e){
      var cell = e.target.closest('.cal .cell');
      if (!cell) return;
      var date = cell.getAttribute('data-date');
      if (!date) return;
      if (confirm('해당 날짜에 거래 내역을 추가하시겠습니까?')) {
        location.href = newBaseUrl + '?date=' + encodeURIComponent(date);
      }
    });
  </script>
</body>
</html>
