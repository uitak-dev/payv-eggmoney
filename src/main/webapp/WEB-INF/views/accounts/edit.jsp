<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>자산 수정</title>
<link rel="stylesheet" href="<c:url value='/resources/css/common.css'/>">
</head>

<body>
	<jsp:include page="/WEB-INF/views/common/header.jsp" />
	<jsp:include page="/WEB-INF/views/common/aside.jsp" />

	<main id="main" data-ledger-id="${ledgerId}">
	<div class="container">
		<h2>자산 수정</h2>

		<c:if test="${not empty error}">
			<div class="alert error">${error}</div>
		</c:if>

		<form method="post"
			action="<c:url value='/ledgers/${ledgerId}/accounts/${account.id}'/>"
			class="card">
			<div class="form-group">
				<label for="name">이름</label> <input type="text" id="name"
					name="name" value="${form.name}" required />
			</div>

			<div class="muted">* 현재 잔액은 거래 기록으로 관리됩니다.</div>

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
