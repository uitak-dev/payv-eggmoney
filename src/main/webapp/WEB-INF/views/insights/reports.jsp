<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <title>보고서</title>

  <!-- 공통 / 레포트 CSS -->
  <link rel="stylesheet" href="<c:url value='/resources/css/common.css'/>">
  <link rel="stylesheet" href="<c:url value='/resources/css/reports.css'/>">

  <!-- Google Charts -->
  <script src="https://www.gstatic.com/charts/loader.js"></script>

  <!-- 서버 데이터를 JS 전역 변수로 전달 -->
  <script>
    const pieData = ${pieDataJson};
    const monthlyData = ${chartDataJson};
  </script>

  <!-- 공용/레포트 JS -->
  <script src="<c:url value='/resources/js/reports.js'/>" defer></script>
  <script src="<c:url value='/resources/js/common.js'/>" defer></script>
</head>

<body>
  <jsp:include page="/WEB-INF/views/common/header.jsp" />
  <jsp:include page="/WEB-INF/views/common/aside.jsp" />

  <main id="main" data-ledger-id="${ledgerId}">
    <div class="container reports-container">
      <h2>보고서</h2>

      <!-- 탭 버튼 -->
      <div class="tab-buttons">
        <button id="tab-categories" class="tab-btn ${tab eq 'categories' ? 'active' : ''}" type="button">
          카테고리별 지출
        </button>
        <button id="tab-monthly" class="tab-btn ${tab eq 'monthly' ? 'active' : ''}" type="button">
          월별 수입/지출
        </button>
      </div>

      <!-- 카테고리별 지출 -->
      <section id="section-categories" class="section ${tab eq 'categories' ? 'active' : ''}">
        <h3>${month} 카테고리별 지출</h3>

        <div class="toolbar">
          <form method="get"
                action="<c:url value='/ledgers/${ledgerId}/insights/reports'/>"
                class="search-form">
            <input type="hidden" name="tab" value="categories"/>
            <input type="month" name="month" value="${month}" />
            <button class="btn btn-primary" type="submit">조회</button>
          </form>

          <a class="btn btn-primary"
             href="<c:url value='/ledgers/${ledgerId}/transaction?month=${month}'/>">
            거래 목록
          </a>
        </div>

        <div class="card">
          <strong>총 지출</strong>
          <span class="amt-out" style="margin-left:8px;">
          - <fmt:formatNumber value="${totalExpense}" pattern="#,###"/>
          </span>
        </div>

        <div id="no-data" class="no-data" style="display:none;">해당 월의 지출 데이터가 없습니다.</div>
        <div id="chart-categories" class="chart-container"></div>
      </section>

      <!-- 월별 수입/지출 -->
      <section id="section-monthly" class="section ${tab eq 'monthly' ? 'active' : ''}">
        <h3>${year}년 월별 수입/지출</h3>

        <div class="toolbar">
          <div class="toolbar-left">
            <a class="btn btn-primary outline"
               href="<c:url value='/ledgers/${ledgerId}/insights/reports?year=${prevYear}&tab=monthly'/>">
              ◀ 이전해
            </a>

            <form method="get"
                  action="<c:url value='/ledgers/${ledgerId}/insights/reports'/>"
                  class="search-form">
              <input type="hidden" name="tab" value="monthly"/>
              <input type="number" name="year" value="${year}" min="2000" max="2100" />
              <button class="btn btn-primary" type="submit">이동</button>
            </form>

            <a class="btn btn-primary outline"
               href="<c:url value='/ledgers/${ledgerId}/insights/reports?year=${nextYear}&tab=monthly'/>">
              다음해 ▶
            </a>
          </div>

          <a class="btn btn-primary"
             href="<c:url value='/ledgers/${ledgerId}/transaction?month=${year}-01'/>">
            거래 목록
          </a>
        </div>

        <div class="card">
          <strong>연간 합계</strong> 
          <span class="amt-in" style="margin-left:8px;">
            수입: + <fmt:formatNumber value="${sumIncome}" pattern="#,###"/>
          </span>
          <span class="amt-out" style="margin-left:12px;">
            지출: - <fmt:formatNumber value="${sumExpense}" pattern="#,###"/>
          </span>
        </div>

        <div id="chart-monthly" class="chart-container"></div>
      </section>
    </div>
  </main>
</body>
</html>
