<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>403 - 접근 거부 | PayV</title>
<link rel="stylesheet"
	href="<c:url value='/resources/css/error-common.css' />">
</head>
<body class="bg-gradient-403">
	<div class="error-container">
		<div class="error-icon shake icon-danger">🚫</div>
		<div class="error-code code-403">403</div>
		<h1 class="error-title">접근이 거부되었습니다</h1>
		<p class="error-description">
			죄송합니다. 이 페이지에 접근할 권한이 없습니다.<br> 로그인이 필요하거나 관리자 권한이 필요할 수 있습니다.
		</p>

		<div class="action-buttons">
			<a href="<c:url value='/login' />" class="btn btn-secondary-403">로그인</a>
			<a href="<c:url value='/' />" class="btn btn-primary-403">홈으로 가기</a>
		</div>

		<div class="footer-text">계정 관련 문의는 고객센터로 연락해주세요.</div>
	</div>

	<script src="<c:url value='/resources/js/error-common.js' />"></script>
	<script>
		// 403 에러 로깅
		ErrorPageUtils.logErrorInfo('403', 'Access Denied');
	</script>
</body>
</html>