<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title></title>
<link rel="stylesheet" href="<c:url value='/resources/css/common.css'/>">
</head>

<body>
	<jsp:include page="/WEB-INF/views/common/header.jsp" />
	<jsp:include page="/WEB-INF/views/common/aside.jsp" />

	<main id="main" data-ledger-id="${ledgerId}">
	<div class="container">
		<h2>예산 한도 변경</h2>

		<c:if test="${not empty error}">
			<div class="alert error">${error}</div>
		</c:if>

		<div class="card" style="margin-bottom: 10px;">
			<div>
				<strong>카테고리:</strong> ${categoryName}
			</div>
			<div>
				<strong>월:</strong> ${month}
			</div>
		</div>

		<form method="post"
			action="<c:url value='/ledgers/${ledgerId}/budgets/${budget.id}'/>"
			class="card">
			<input type="hidden" name="month" value="${form.month}" />

			<div class="form-group">
				<label for="limit">한도(원)</label> <input type="number" id="limit"
					name="limit" min="0" step="100" value="${form.limit}" required />
			</div>

			<div class="toolbar">
				<button type="submit" class="btn btn-primary">저장</button>
				<a class="btn"
					href="<c:url value='/ledgers/${ledgerId}/budgets?month=${month}'/>">취소</a>
			</div>
		</form>

	</div>
	</main>

	<script src="<c:url value='/resources/js/common.js'/>"></script>

</body>
</html>
