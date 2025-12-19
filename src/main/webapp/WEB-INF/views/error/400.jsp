<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>400 - 잘못된 요청 | PayV</title>
<link rel="stylesheet"
	href="<c:url value='/resources/css/error-common.css' />">
</head>
<body class="bg-gradient-400">
	<div class="error-container">
		<div class="error-icon bounce icon-warning">⚠️</div>
		<div class="error-code code-400">400</div>
		<h1 class="error-title">잘못된 요청</h1>
		<p class="error-description">
			요청하신 내용에 문제가 있습니다.<br> 입력하신 정보를 다시 확인해주세요.
		</p>

		<div class="action-buttons">
			<a href="javascript:history.back()" class="btn btn-secondary">이전
				페이지</a> <a href="<c:url value='/' />" class="btn btn-primary">홈으로 가기</a>
		</div>

		<div class="footer-text">문제가 계속 발생하면 고객센터에 문의해주세요.</div>
	</div>

	<script src="<c:url value='/resources/js/error-common.js' />"></script>
	<script>
		// 400 에러 로깅
		ErrorPageUtils.logErrorInfo('400', 'Bad Request');
	</script>
</body>
</html>