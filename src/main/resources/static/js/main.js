(function () {
  /* ========= Preloader ======== */
  const preloader = document.querySelectorAll('#preloader')

  window.addEventListener('load', function () {
    if (preloader.length) {
      this.document.getElementById('preloader').style.display = 'none'
    }
  })

  /* ========= Add Box Shadow in Header on Scroll ======== */
  window.addEventListener('scroll', function () {
    const header = document.querySelector('.header')
    if (window.scrollY > 0) {
      header.style.boxShadow = '0px 0px 30px 0px rgba(200, 208, 216, 0.30)'
    } else {
      header.style.boxShadow = 'none'
    }
  })

  /* ========= sidebar toggle ======== */
  const sidebarNavWrapper = document.querySelector(".sidebar-nav-wrapper");
  const mainWrapper = document.querySelector(".main-wrapper");
  const menuToggleButton = document.querySelector("#menu-toggle");
  const menuToggleButtonIcon = document.querySelector("#menu-toggle i");
  const overlay = document.querySelector(".overlay");

  menuToggleButton.addEventListener("click", () => {
    sidebarNavWrapper.classList.toggle("active");
    overlay.classList.add("active");
    mainWrapper.classList.toggle("active");

    if (document.body.clientWidth > 1200) {
      if (menuToggleButtonIcon.classList.contains("lni-chevron-left")) {
        menuToggleButtonIcon.classList.remove("lni-chevron-left");
        menuToggleButtonIcon.classList.add("lni-menu");
      } else {
        menuToggleButtonIcon.classList.remove("lni-menu");
        menuToggleButtonIcon.classList.add("lni-chevron-left");
      }
    } else {
      if (menuToggleButtonIcon.classList.contains("lni-chevron-left")) {
        menuToggleButtonIcon.classList.remove("lni-chevron-left");
        menuToggleButtonIcon.classList.add("lni-menu");
      }
    }
  });
  overlay.addEventListener("click", () => {
    sidebarNavWrapper.classList.remove("active");
    overlay.classList.remove("active");
    mainWrapper.classList.remove("active");
  });
})();
let messageInterval;

// 상태별 메시지 목록
const messages = {
  loading: [
    "데이터를 로딩 중입니다...",
    "잠시만 기다려주세요...",
    "정보를 불러오고 있습니다..."
  ],
  processing: [
    "데이터를 처리 중입니다...",
    "연산을 수행 중입니다...",
    "결과를 계산하고 있습니다..."
  ],
  saving: [
    "데이터를 저장 중입니다...",
    "변경사항을 적용 중입니다...",
    "정보를 업데이트 중입니다..."
  ]
};

function showSpinner(taskType) {
  if (!messages[taskType]) {
    taskType = 'loading';
  }

  const messageElement = document.getElementById('preloader-message');
  const preloader = document.getElementById('preloader');

  const currentMessages = messages[taskType];
  let messageIndex = 0;

  preloader.style.display = 'flex';

  messageElement.textContent = currentMessages[messageIndex];

  if (messageInterval) {
    clearInterval(messageInterval);
  }

  messageInterval = setInterval(() => {
    messageIndex = (messageIndex + 1) % currentMessages.length;
    messageElement.textContent = currentMessages[messageIndex];
  }, 1000);
}

function hideSpinner() {
  const preloader = document.getElementById('preloader');

  if (messageInterval) {
    clearInterval(messageInterval);
    messageInterval = null;
  }

  preloader.style.display = 'none';
}

function fetchWithSpinner(url, options = {}, taskType = 'loading') {
  showSpinner(taskType);

  return fetch(url, options)
      .then(response => {
        return response.json();
      })
      .finally(() => {
        hideSpinner();
      });
}