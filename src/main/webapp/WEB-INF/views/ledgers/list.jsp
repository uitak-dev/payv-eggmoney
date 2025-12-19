<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<%@ include file="/WEB-INF/views/common/aside.jsp" %>

<html>
<head>
    <title>가계부 목록</title>
    <link rel="stylesheet" href="<c:url value='/resources/css/common.css'/>" />
    <link rel="stylesheet" href="<c:url value='/resources/css/ledger.css'/>" />
    <script src="<c:url value='/resources/js/common.js'/>" defer></script>
</head>
<body>
<main class="ledger-main">
    <h1 class="ledger-title">가계부 목록</h1>

    <!-- 가계부 선택 드롭다운 + 열기 버튼 -->
<div class="ledger-select">
    <label for="ledgerSelect">가계부 선택</label>
    <select id="ledgerSelect" name="ledger" class="ledger-dropdown">
        <c:forEach var="l" items="${ledgers}">
            <option value="${l.id}">${l.name}</option>
        </c:forEach>
    </select>
    <button type="button" id="btnOpenLedger">열기</button>
</div>

<script>
document.getElementById("btnOpenLedger").addEventListener("click", function() {
    const selectedId = document.getElementById("ledgerSelect").value;
    if (selectedId) {
        // 선택한 가계부의 내역 페이지로 이동
        window.location.href = "/ledgers/" + selectedId + "/transaction";
    } else {
        alert("가계부를 선택해주세요.");
    }
});
</script>


    <!-- 새 가계부 추가 버튼 -->
    <p class="new-ledger-btn">
        <a href="<c:url value='/ledgers/new'/>" class="btn-create">+ 새 가계부</a>
    </p>

    <!-- 가계부 목록 테이블 -->
    <table class="ledger-table">
        <thead>
            <tr>
                <th>이름</th>
                <th>보기</th>
                <th>자산 설정</th>
                <th>예산 설정</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="l" items="${ledgers}">
                <tr>
                    <td>${l.name}</td>
                    <td><a href="<c:url value='/ledgers/${l.id}/transaction'/>" class="btn-open">열기</a></td>
                    <td><a href="<c:url value='/ledgers/${l.id}/accounts'/>" class="btn-asset-settings">자산 설정</a></td>
                    <td><a href="<c:url value='/ledgers/${l.id}/budgets'/>" class="btn-budget-settings">예산 설정</a></td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
</main>
</body>
</html>
