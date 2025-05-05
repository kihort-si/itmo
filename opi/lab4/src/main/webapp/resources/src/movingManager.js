// Получаем элемент
const movingElement = document.getElementById('movingElement');

function getRandomArbitrary(min, max) {
    return Math.random() * (max - min) + min;
}

let posX = Math.ceil(getRandomArbitrary(0, window.innerWidth- movingElement.offsetWidth));
let posY = Math.ceil(getRandomArbitrary(0, window.innerHeight- movingElement.offsetHeight));

// Скорость перемещения
let speedX = getRandomArbitrary(-3, 3); // Скорость по оси X
let speedY = getRandomArbitrary(-3, 3); // Скорость по оси Y

function moveElement() {
    // Изменяем координаты
    posX += speedX;
    posY += speedY;

    // Получаем размеры окна и элемента
    const windowWidth = window.innerWidth;
    const windowHeight = window.innerHeight + window.scrollY;
    const elementWidth = movingElement.offsetWidth;
    const elementHeight = movingElement.offsetHeight;

    // Проверяем границы и изменяем направление при столкновении
    if (posX + elementWidth > windowWidth || posX < 0) {
        speedX = -speedX; // Меняем направление по оси X
    }

    if (posY + elementHeight > windowHeight || posY < window.scrollY) {
        speedY = -speedY; // Меняем направление по оси Y
    }

    // Устанавливаем новые координаты
    movingElement.style.left = posX + 'px';
    movingElement.style.top = posY + 'px';

    // Запускаем функцию снова через 16 миллисекунд (примерно 60 FPS)
    requestAnimationFrame(moveElement);
}

// Запускаем анимацию
moveElement();