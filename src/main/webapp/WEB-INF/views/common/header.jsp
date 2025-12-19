<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<header>
  <div class="left-buttons">
    <button id="menu-button" style="background:none; border:none; cursor:pointer;">
      <img src="<c:url value='/resources/images/menu.png'/>" alt="Menu" height="32" />
    </button>
    <div class="logo" onclick="location.href='/'">
      <img id="logo-img" src="<c:url value='/resources/images/logo-part1.png'/>" alt="Logo Part 1" />
      <img id="logo-img" src="<c:url value='/resources/images/logo-part2.png'/>" alt="Logo Part 2" />
    </div>
  </div>
  <div class="right-buttons">
    <button id="mypage-button" style="background:none; border:none; cursor:pointer;">
      <img src="<c:url value='/resources/images/mypage.png'/>" alt="MyPage" height="32" />
    </button>
  </div>
</header>
