document.addEventListener("DOMContentLoaded", function() {
    const menuBtn = document.getElementById("menu-button");
    const aside = document.querySelector("aside");
    const mainContent = document.querySelector("main");
    const mypageBtn = document.getElementById("mypage-button");
    
    menuBtn.addEventListener("click", () => {
    	aside.classList.toggle("hide");
        mainContent.classList.toggle("shrink");
    });
    
    mypageBtn.addEventListener("click", () => {
        window.location.href = "/mypage"; // 마이페이지 URL에 맞춰 변경하세요.
    });
});
