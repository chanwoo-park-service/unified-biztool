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
let hideTimeoutId;

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

  rowProcessing: [
    "광고 계정 정보를 불러오는 중입니다...",
    "픽셀 연결 상태를 확인하고 있어요...",
    "캠페인 설정값을 정리 중입니다...",
    "광고 세트 타겟팅을 구성 중이에요...",
    "광고 형식을 분석하고 있어요… 이미지냐, 슬라이드냐 그것이 문제!",
    "랜딩페이지와 소재를 연결 중입니다...",
    "오디언스 조건을 검토하고 있어요… 연령, 성별, 위치 등 꼼꼼하게!",
    "소재 제목과 설명을 분석 중… 마케팅 감성 충전 중입니다!",
    "광고 계정과 페이지의 연결 상태를 검증하는 중이에요...",
    "예산과 입찰가 조건을 정렬 중입니다...",
    "기타 요청사항을 확인하고 있어요… 특별 지시가 있을까요?",
    "전체 구조를 검토 중… 캠페인, 세트, 소재가 제자리에 있는지 확인 중!",
    "최종 광고 요청을 생성 중입니다… Meta API에 전송 준비 완료!",
    "데이터 유효성을 체크 중… 빠진 항목은 없는지 확인하고 있어요."
  ],

  excelSaving: [
    "엑셀에서 광고 계정 정보를 추출 중… 숫자들이 줄을 서고 있어요.",
    "캠페인 목표를 해석 중입니다… '판매'? 좋아요, 바로 준비할게요!",
    "광고 소재명을 복사 중… 파일명이 너무 창의적이에요!",
    "위치와 언어 설정을 분석 중… 전 세계를 향한 광고 준비 완료!",
    "예산을 계산 중… 0 하나 더 붙여도 괜찮으신가요?",
    "연령별 타겟을 분류 중… 20대가 가장 반응 좋아요!",
    "업로드 페이지명 확인 중… 이 링크, 믿어도 될까요?",
    "크리에이티브 형식을 판별 중… '슬라이드', 멋진 선택이에요!",
    "광고 세트를 정렬 중… 광고팀도 이렇게 질서 정연하진 않아요.",
    "광고 제목과 문구를 스캔 중… 카피라이터 부럽지 않네요!",
    "기타 요청사항을 읽는 중… 특이사항은 잘 메모했어요!",
    "전체 엑셀 데이터를 구조화 중… JSON이 예뻐지고 있어요.",
    "Meta 광고 설정을 최종 반영 중… API도 긴장하네요.",
    "변경 사항 저장 중… 이제 진짜로 발사 준비 완료!",
    "업로드가 거의 완료됐어요… 클라우드에 광고가 날아가고 있어요!"
  ]
};

function showSpinner(taskType) {
  if (!messages[taskType]) {
    taskType = 'loading';
  }

  const messageElement = document.getElementById('preloader-message');
  const preloader = document.getElementById('preloader');

  if (hideTimeoutId) {
    clearTimeout(hideTimeoutId);
    hideTimeoutId = null;
  }

  const currentMessages = messages[taskType];

  preloader.style.display = 'flex';

  preloader.classList.add('visible');

  messageElement.textContent = currentMessages[Math.floor(Math.random() * currentMessages.length)];

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
  }, 2500);
}

function hideSpinner(success = true, finalMessage = '') {
  const preloader = document.getElementById('preloader');
  const messageElement = document.getElementById('preloader-message');

  if (messageInterval) {
    clearInterval(messageInterval);
    messageInterval = null;
  }

  if (hideTimeoutId) {
    clearTimeout(hideTimeoutId);
    hideTimeoutId = null;
  }

  if (finalMessage) {
    const prefix = success ? '✅' : '❌';
    messageElement.style.opacity = 0;

    setTimeout(() => {
      messageElement.textContent = `${prefix} ${finalMessage}`;
      messageElement.style.opacity = 1;
    }, 300);

    hideTimeoutId = setTimeout(() => {
      preloader.classList.remove('visible');

      hideTimeoutId = setTimeout(() => {
        preloader.style.display = 'none';
        hideTimeoutId = null;
      }, 500);
    }, 1800);

  } else {
    preloader.classList.remove('visible');

    hideTimeoutId = setTimeout(() => {
      preloader.style.display = 'none';
      hideTimeoutId = null;
    }, 500);
  }
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