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

const messages = {
  loading: [
    "광고 계정을 불러오는 중... 예산은 숨고 있대요.",
    "캠페인 기획서 PDF를 AI가 읽고 있어요… 활자가 많네요.",
    "광고 요정이 Meta Ads Manager에 몰래 로그인 중...",
    "전 세계 광고 데이터를 한 손에 쥐는 중… 조심스럽게!",
    "인사이트 정령이 UTM 태그를 정리하고 있어요...",
    "크리에이티브를 세팅 중입니다… 썸네일 골라주는 중!",
    "Meta 서버에 손 흔드는 중… 응답 기다리는 중이에요.",
    "비즈니스 매니저의 문을 노크하는 중입니다..."
  ],

  processing: [
    "타겟 오디언스를 분석 중… 35세 남성이 제일 반응하네요.",
    "광고 세트와 캠페인을 연결 중… 선 넘지 않게 조심!",
    "비즈니스 로직이 ROI를 따지고 있어요… 싸움날 기세.",
    "페이스북 픽셀과 대화 중입니다… 소셜한 성격이네요.",
    "알고리즘이 소재 성능을 비교 분석 중입니다...",
    "머신러닝이 CTR 예측치로 박터지게 싸우는 중!",
    "광고 소재에 하트 누른 유저들 분석 중이에요...",
    "크리에이티브별 도달률을 정렬 중… 숫자가 요동쳐요!"
  ],

  saving: [
    "광고 캠페인을 Meta에 업로드 중… 광고계의 로켓발사!",
    "변경사항 저장 중… AI가 승인 여부를 눈치보는 중이에요.",
    "예산을 Meta에 봉인 중… 다음 달 카드값이 걱정돼요.",
    "업로드 중… 광고 소재가 클라우드로 여행 중이에요.",
    "캠페인 구조를 안전하게 저장 중입니다… 구조도 깔끔하게!",
    "백오피스에 로그를 정리 중이에요… 깨끗이 청소 완료!",
    "API를 통해 광고 세트를 배치 중… 줄 세우는 건 기본!",
    "업데이트 사항을 메타버스 우체통에 넣는 중이에요."
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

  preloader.classList.add('visible');

  messageElement.textContent = currentMessages[messageIndex];

  if (messageInterval) clearInterval(messageInterval);

  let lastMessageIndex = -1;

  messageInterval = setInterval(() => {
    messageElement.style.opacity = 0;

    setTimeout(() => {
      let randomIndex;

      do {
        randomIndex = Math.floor(Math.random() * currentMessages.length);
      } while (randomIndex === lastMessageIndex && currentMessages.length > 1);

      lastMessageIndex = randomIndex;
      messageElement.textContent = currentMessages[randomIndex];
      messageElement.style.opacity = 1;
    }, 300);
  }, 3000);
}

function hideSpinner() {
  const preloader = document.getElementById('preloader');
  const messageElement = document.getElementById('preloader-message');

  if (messageInterval) {
    clearInterval(messageInterval);
    messageInterval = null;
  }
  preloader.classList.remove('visible');

  setTimeout(() => {
    preloader.style.display = 'none';
  }, 500);
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