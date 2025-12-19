/**
 * Error Pages Common JavaScript PayV 프로젝트 에러 페이지 공통 스크립트 위치:
 * src/main/webapp/resources/js/error-common.js
 */

// 에러 페이지 공통 유틸리티
const ErrorPageUtils = {
    
    /**
	 * 페이지 로드 시 에러 정보 로깅
	 */
    logErrorInfo: function(errorCode, errorType) {
        console.group('Error Page Information');
        console.log('Error Code:', errorCode);
        console.log('Error Type:', errorType);
        console.log('Timestamp:', new Date().toISOString());
        console.log('User Agent:', navigator.userAgent);
        console.log('Current URL:', window.location.href);
        console.log('Referrer:', document.referrer);
        console.groupEnd();
    },
    
    /**
	 * 에러 ID 생성
	 */
    generateErrorId: function() {
        return 'ERR-' + Date.now() + '-' + Math.random().toString(36).substr(2, 9);
    },
    
    /**
	 * 현재 시간 표시
	 */
    displayCurrentTime: function(elementId) {
        const element = document.getElementById(elementId);
        if (element) {
            element.textContent = new Date().toLocaleString('ko-KR');
        }
    },
    
    /**
	 * 에러 ID 표시
	 */
    displayErrorId: function(elementId) {
        const element = document.getElementById(elementId);
        if (element) {
            element.textContent = this.generateErrorId();
        }
    }
};

// 404 페이지 전용 기능
const Error404Utils = {
    
    /**
	 * 검색 기능 수행
	 */
    performSearch: function() {
        const query = document.querySelector('.search-input').value.trim();
        if (query) {
            // 검색 페이지로 이동 (실제 구현에 맞게 수정)
            window.location.href = '/search?q=' + encodeURIComponent(query);
        }
    },
    
    /**
	 * Enter 키로 검색
	 */
    searchOnEnter: function(event) {
        if (event.key === 'Enter') {
            this.performSearch();
        }
    },
    
    /**
	 * 검색 기능 초기화
	 */
    initSearchFeature: function() {
        const searchInput = document.querySelector('.search-input');
        const searchBtn = document.querySelector('.search-btn');
        
        if (searchInput && searchBtn) {
            searchInput.addEventListener('keypress', (e) => this.searchOnEnter(e));
            searchBtn.addEventListener('click', () => this.performSearch());
        }
    }
};

// 500 페이지 전용 기능
const Error500Utils = {
    
    /**
	 * 자동 새로고침 카운트다운
	 */
    startAutoRefresh: function(seconds = 30) {
        let timeLeft = seconds;
        const countdownElement = document.getElementById('countdown');
        const progressElement = document.getElementById('progress');
        
        if (!countdownElement || !progressElement) return;
        
        const timer = setInterval(() => {
            timeLeft--;
            countdownElement.textContent = timeLeft;
            
            // 프로그레스 바 업데이트
            const progress = ((seconds - timeLeft) / seconds) * 100;
            progressElement.style.width = progress + '%';
            
            if (timeLeft <= 0) {
                clearInterval(timer);
                location.reload();
            }
        }, 1000);
        
        // 페이지 클릭 시 타이머 정지
        document.addEventListener('click', () => {
            clearInterval(timer);
            const timerElement = document.querySelector('.refresh-timer');
            if (timerElement) {
                timerElement.style.display = 'none';
            }
        });
        
        return timer;
    }
};

// 일반 에러 페이지 전용 기능
const ErrorGeneralUtils = {
    
    /**
	 * 에러 상세정보 토글
	 */
    toggleErrorDetails: function() {
        const details = document.getElementById('errorDetails');
        const button = document.querySelector('.error-details-toggle');
        
        if (!details || !button) return;
        
        if (details.style.display === 'none' || details.style.display === '') {
            details.style.display = 'block';
            button.textContent = '기술적 세부사항 숨기기';
        } else {
            details.style.display = 'none';
            button.textContent = '기술적 세부사항 보기 (개발자용)';
        }
    },
    
    /**
	 * 오류 신고 기능
	 */
    reportError: function() {
        const errorId = ErrorPageUtils.generateErrorId();
        const errorInfo = {
            errorId: errorId,
            timestamp: new Date().toISOString(),
            userAgent: navigator.userAgent,
            url: window.location.href,
            referrer: document.referrer
        };
        
        // 실제로는 서버로 오류 정보 전송
        console.log('Error Report:', errorInfo);
        
        // 사용자에게 신고 완료 알림
        alert('오류가 신고되었습니다. 빠른 시일 내에 해결하겠습니다.');
        
        // 실제 구현 시에는 Ajax로 서버에 전송
        // fetch('/api/error-report', {
        // method: 'POST',
        // headers: {
        // 'Content-Type': 'application/json',
        // },
        // body: JSON.stringify(errorInfo)
        // });
    },
    
    /**
	 * 상세 에러 정보 로깅 (JSP 변수 포함)
	 */
    logDetailedError: function(jspErrorData) {
        console.group('Detailed Error Information');
        if (jspErrorData.exceptionType) {
            console.log('Exception Type:', jspErrorData.exceptionType);
        }
        if (jspErrorData.exceptionMessage) {
            console.log('Exception Message:', jspErrorData.exceptionMessage);
        }
        if (jspErrorData.requestURI) {
            console.log('Request URI:', jspErrorData.requestURI);
        }
        if (jspErrorData.statusCode) {
            console.log('Status Code:', jspErrorData.statusCode);
        }
        console.groupEnd();
    }
};

// 전역 함수로 노출 (JSP에서 호출할 수 있도록)
window.ErrorPageUtils = ErrorPageUtils;
window.Error404Utils = Error404Utils;
window.Error500Utils = Error500Utils;
window.ErrorGeneralUtils = ErrorGeneralUtils;

// 전역 함수 (JSP에서 직접 호출)
function performSearch() {
    Error404Utils.performSearch();
}

function searchOnEnter(event) {
    Error404Utils.searchOnEnter(event);
}

function toggleErrorDetails() {
    ErrorGeneralUtils.toggleErrorDetails();
}

function reportError() {
    ErrorGeneralUtils.reportError();
}

// DOM 로드 완료 시 공통 초기화
document.addEventListener('DOMContentLoaded', function() {
    // 에러 ID와 시간 표시
    ErrorPageUtils.displayErrorId('errorId');
    ErrorPageUtils.displayCurrentTime('errorTime');
    
    // 404 페이지 검색 기능 초기화
    if (document.querySelector('.search-input')) {
        Error404Utils.initSearchFeature();
    }
    
    // 500 페이지 자동 새로고침 시작
    if (document.getElementById('countdown')) {
        Error500Utils.startAutoRefresh(30);
    }
});