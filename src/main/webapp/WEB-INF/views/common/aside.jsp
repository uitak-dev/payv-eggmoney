<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<aside>
    <div class="account-title">
    <a href="<c:url value='/ledgers'/>" class="account-link">
        <c:choose>
            <c:when test="${not empty currentLedgerId}">
                ${currentAccountName}
            </c:when>
            <c:otherwise>
                가계부 목록
            </c:otherwise>
        </c:choose>
    </a>
</div>

    <hr class="aside-divider">

    <nav>
        <ul>
            <!-- ledgerId가 있을 때만 활성화 -->
            <c:choose>
                <c:when test="${not empty currentLedgerId}">
                    <li class="${currentPage == 'transaction' ? 'active' : ''}">
                        <a href="<c:url value='/ledgers/${currentLedgerId}/transaction'/>">내역</a>
                    </li>
                    <li class="${currentPage == 'calendar' ? 'active' : ''}">
                        <a href="<c:url value='/ledgers/${currentLedgerId}/transaction/calendar'/>">달력</a>
                    </li>
                    <li class="${currentPage == 'reports' ? 'active' : ''}">
                        <a href="<c:url value='/ledgers/${currentLedgerId}/insights/reports'/>">보고서</a>
                    </li>
                    <li class="${currentPage == 'categories' ? 'active' : ''}">
                        <a href="<c:url value='/ledgers/${currentLedgerId}/categories'/>">카테고리 설정</a>
                    </li>
                    <li class="${currentPage == 'accounts' ? 'active' : ''}">
                        <a href="<c:url value='/ledgers/${currentLedgerId}/accounts'/>">자산 설정</a>
                    </li>
                    <li class="${currentPage == 'budgets' ? 'active' : ''}">
                        <a href="<c:url value='/ledgers/${currentLedgerId}/budgets'/>">예산 설정</a>
                    </li>
                </c:when>
                <c:otherwise>
                    <!-- ledger 선택 안된 경우: 비활성화 -->
                    <li><span class="disabled">내역</span></li>
                    <li><span class="disabled">달력</span></li>
                    <li><span class="disabled">보고서</span></li>
                    <li><span class="disabled">카테고리 설정</span></li>
                    <li><span class="disabled">자산 설정</span></li>
                    <li><span class="disabled">예산 설정</span></li>
                </c:otherwise>
            </c:choose>

            <hr class="aside-divider">

            <!-- 커뮤니티는 ledgerId와 무관하게 전역 이동 -->
            <li class="${currentPage == 'boards' ? 'active' : ''}">
                <a href="<c:url value='/boards'/>">커뮤니티</a>
            </li>
        </ul>
    </nav>
</aside>
