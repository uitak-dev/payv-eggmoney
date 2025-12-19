<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>500 - 서버 오류 | PayV</title>
<style>
/* PayV 브랜드 컬러에 맞춘 500 페이지 스타일 */
* {
	margin: 0;
	padding: 0;
	box-sizing: border-box;
}

body {
	font-family: 'Malgun Gothic', '맑은고딕', sans-serif;
	background-color: #F5F3E7;
	min-height: 100vh;
	display: flex;
	align-items: center;
	justify-content: center;
	color: #333;
}

.error-container {
	background: white;
	border-radius: 20px;
	padding: 60px 40px;
	text-align: center;
	box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
	max-width: 500px;
	width: 90%;
	animation: fadeInUp 0.6s ease-out;
}

@
keyframes fadeInUp {from { opacity:0;
	transform: translateY(30px);
}

to {
	opacity: 1;
	transform: translateY(0);
}

}
.logo-section {
	margin-bottom: 30px;
}

.piggy-icon {
	width: 80px;
	height: auto;
	margin-bottom: 15px;
}

.error-code {
	font-size: 72px;
	font-weight: bold;
	color: #FF6B9D;
	margin-bottom: 10px;
	text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.1);
}

.error-title {
	font-size: 28px;
	font-weight: 600;
	color: #2c3e50;
	margin-bottom: 15px;
}

.error-description {
	font-size: 16px;
	color: #666;
	line-height: 1.6;
	margin-bottom: 30px;
}

.action-buttons {
	display: flex;
	gap: 15px;
	justify-content: center;
	flex-wrap: wrap;
}

.btn {
	padding: 12px 30px;
	border: none;
	border-radius: 25px;
	font-size: 16px;
	font-weight: 500;
	text-decoration: none;
	display: inline-block;
	transition: all 0.3s ease;
	cursor: pointer;
}

.btn-primary {
	background: linear-gradient(135deg, #FF6B9D 0%, #FF8FA3 100%);
	color: white;
}

.btn-primary:hover {
	transform: translateY(-2px);
	box-shadow: 0 10px 20px rgba(255, 107, 157, 0.3);
	color: white;
	text-decoration: none;
}

.btn-secondary {
	background: transparent;
	color: #FF6B9D;
	border: 2px solid #FF6B9D;
}

.btn-secondary:hover {
	background: #FF6B9D;
	color: white;
	transform: translateY(-2px);
	text-decoration: none;
}

.footer-text {
	margin-top: 30px;
	font-size: 14px;
	color: #999;
}
</style>
</head>
<body>
	<div class="error-container">
		<div class="logo-section">
			<img src="<c:url value='/resources/images/logo-part1.png'/>"
				alt="PayV 로고" class="piggy-icon">
		</div>

		<div class="error-code">500</div>
		<h1 class="error-title">서버 오류가 발생했습니다</h1>
		<p class="error-description">
			죄송합니다. 일시적인 서버 문제가 발생했습니다.<br> 잠시 후 다시 시도해주세요.
		</p>

		<div class="action-buttons">
			<button onclick="location.reload()" class="btn btn-secondary">새로고침</button>
			<a href="<c:url value='/' />" class="btn btn-primary">홈으로 가기</a>
		</div>

		<div class="footer-text">문제가 계속 발생하면 시스템 관리자에게 문의해주세요.</div>
	</div>
</body>
</html>