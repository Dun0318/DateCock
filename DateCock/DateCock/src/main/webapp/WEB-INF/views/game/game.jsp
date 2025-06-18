<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <title>하트 피하기 게임</title>
  <style>
    canvas {
      background: url('./image/게임배경.png') no-repeat center center;
      background-size: cover;
      display: block;
      margin: 30px auto;
      border: 3px solid #ffa5c3;
    }
  </style>
</head>
<body>
<!--  게임 스타트 버튼 -->
  <div style="text-align:center; margin-top:30px;">
    <button id="startButton" style="padding: 12px 24px; font-size: 18px; background-color: #ffb6c1; border: none; border-radius: 10px; cursor: pointer;">
      게임 시작
    </button>
  </div>
 
  <canvas id="gameCanvas" width="750" height="750"></canvas>
  <script>
    const canvas = document.getElementById("gameCanvas");
    const ctx = canvas.getContext("2d");

    const playerImg = new Image();
    playerImg.src = "./image/캐릭터.png";

    const heartImg = new Image();
    heartImg.src = "./image/하트.png";

    const player = { x: 180, y: 650, width: 60, height: 60, speed: 20 };
    const hearts = [];
    let score = 0;
    let gameOver = false;

    document.addEventListener("keydown", (e) => {
      if (e.key === "ArrowLeft" && player.x > 0) player.x -= player.speed;
      if (e.key === "ArrowRight" && player.x + player.width < canvas.width) player.x += player.speed;
    });

    function drawPlayer() {
      ctx.drawImage(playerImg, player.x, player.y, player.width, player.height);
    }

    function drawHeart(heart) {
      ctx.drawImage(heartImg, heart.x, heart.y, heart.size, heart.size);
    }

    function createHeart() {
      const x = Math.random() * (canvas.width - 60);
      hearts.push({ x: x, y: 0, size: 50, speed: 4 + Math.random() * 14 });
    }

    function checkCollision(heart) {
      return (
        heart.x + heart.size > player.x &&
        heart.x < player.x + player.width &&
        heart.y + heart.size > player.y &&
        heart.y < player.y + player.height
      );
    }

    function updateGame() {
      if (gameOver) return;

      ctx.clearRect(0, 0, canvas.width, canvas.height);
      drawPlayer();

      for (let i = 0; i < hearts.length; i++) {
        const heart = hearts[i];
        heart.y += heart.speed;
        drawHeart(heart);

        if (checkCollision(heart)) {
          gameOver = true;
          alert("💔 게임 오버! 점수: " + score);
          location.reload();
        }

        if (heart.y > canvas.height) {
          hearts.splice(i, 1);
          score++;
        }
      }

      ctx.fillStyle = "#333";
      ctx.font = "16px Arial";
      ctx.fillText("Score: " + score, 10, 20);

      requestAnimationFrame(updateGame);
    }

    // 이미지가 모두 로드된 후 시작
  let loaded = 0;
let heartInterval = null;

//  이미지 로딩만 체크, 실행은 버튼에서
[ playerImg, heartImg ].forEach(img => {
  img.onload = () => {
    loaded++;
    if (loaded === 2) {
      document.getElementById("startButton").disabled = false;
    }
  };
});

//  게임 시작 버튼 클릭 시 실행
document.getElementById("startButton").addEventListener("click", function () {
  if (loaded !== 2) return;

  this.style.display = "none";           // 버튼 숨김
  canvas.style.display = "block";        // 캔버스 보여줌
  heartInterval = setInterval(createHeart, 500); // 하트 생성 시작
  updateGame();                          // 게임 루프 시작
});

  </script>
</body>
</html>
