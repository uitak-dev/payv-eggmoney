<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>자산 생성</title>
<link rel="stylesheet" href="<c:url value='/resources/css/common.css'/>">
</head>

<body>
	<jsp:include page="/WEB-INF/views/common/header.jsp" />
	<jsp:include page="/WEB-INF/views/common/aside.jsp" />

	<main id="main" data-ledger-id="${ledgerId}">
	<div class="container">
		<h2>새 계좌</h2>

		<c:if test="${not empty error}">
			<div class="alert error">${error}</div>
		</c:if>

		<form method="post"
			action="<c:url value='/ledgers/${ledgerId}/accounts'/>" class="card">
			<div class="form-group">
				<label for="name">이름</label> <input type="text" id="name"
					name="name" value="${form.name}" required />
			</div>

			<div class="form-group">
				<label for="type">유형</label> <select id="type" name="type" required>
					<c:forEach var="t" items="${accountTypes}">
						<option value="${t.name()}">${t.name()}</option>
					</c:forEach>
				</select>
			</div>

			<div class="form-group">
				<label for="openingBalanceWon">초기 잔액(원)</label> <input type="number"
					id="openingBalanceWon" name="openingBalanceWon" min="0" step="100"
					value="${form.openingBalanceWon}" />
			</div>
			<div class="muted">* 비워두면 0원으로 생성됩니다.</div>

			<div class="toolbar">
				<button type="submit" class="btn btn-primary">저장</button>
				<a class="btn" href="<c:url value='/ledgers/${ledgerId}/accounts'/>">취소</a>
			</div>
		</form>

	</div>
	</main>

	<script src="<c:url value='/resources/js/common.js'/>"></script>

</body>
</html>
