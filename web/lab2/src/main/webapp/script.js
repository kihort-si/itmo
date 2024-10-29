"use strict";

let x, y, r;

function validateX() {
    if (x === undefined || x === null || x === "") {
        showErrorMessage('Введите X', 'x');
        return false;
    } else if (isNaN(parseFloat(x)) || !isFinite(x)) {
        showErrorMessage('X должен быть числом', 'x');
        return false;
    } else {
        updateInputs();
        return true;
    }
}

function validateY() {
    if (y === undefined || y === null || y === "") {
        showErrorMessage('Введите Y', 'y');
        return false;
    } else if (isNaN(parseFloat(y)) || !isFinite(y)) {
        showErrorMessage('Y должен быть числом', 'y');
        return false;
    } else if (parseFloat(y) < -5 || parseFloat(y) > 5) {
        showErrorMessage('Y должен быть в диапазоне от -5 до 5', 'y');
        return false;
    } else return true;
}

function validateR() {
    if (!isNaN(parseFloat(r)) && isFinite(r)) return true;
    else {
        showErrorMessage('Введите R', 'r');
        return false;
    }
}

function validate() {
    let success = true;
    if (validateX()) {
        hideErrorMessage('x');
    } else success = false;
    if (validateY()) {
        hideErrorMessage('y');
    } else success = false;
    if (validateR()) {
        hideErrorMessage('r');
    } else success = false;
    return success
}

function showErrorMessage(text, coordinate) {
    let textPlace = document.getElementById(coordinate + 'Error');
    textPlace.textContent = text;
    textPlace.style.opacity = '1';
}

function hideErrorMessage(coordinate) {
    let textPlace = document.getElementById(coordinate + 'Error');
    textPlace.style.opacity = '0';
}

function setupButtons(className) {
    document.querySelectorAll('.' + className + ' input[type="button"]').forEach(button => {
        button.addEventListener('click', function () {
            document.querySelectorAll('.' + className + ' input[type="button"]').forEach(btn => {
                btn.classList.remove('active');
            });
            this.classList.add('active');
            updateValues();
            updateSVG();
        });
    })
}

function setupY() {
    document.getElementById('yInput').addEventListener('input', function () {
        y = document.getElementById('yInput').value.replace(',', '.');
    })
}

setupButtons('xButtons');
setupY();
setupButtons('rButtons');

function updateValues() {
    x = document.querySelector('.xButtons input.active')?.value;
    r = document.querySelector('.rButtons input.active')?.value;
}

function updateInputs() {
    document.getElementById('xInput').value = x;
    document.getElementById('yInput').value = y;
    document.getElementById('rInput').value = r;
}

function updateSVG() {
    const svg = document.getElementById('graph');
    svg.style.scale = r * 0.16;
}

document.getElementById('graphContainer').addEventListener('click', function(event) {
    const graphContainer = document.getElementById('graphContainer');
    const svg = document.getElementById('graph');

    if (r !== undefined) {
        if (graphContainer.contains(event.target)) {
            const rect = graphContainer.getBoundingClientRect();

            const xCoordinate = event.clientX - rect.left;
            const yCoordinate = event.clientY - rect.top;

            const svgX = (xCoordinate / rect.width) * svg.viewBox.baseVal.width;
            const svgY = (yCoordinate / rect.height) * svg.viewBox.baseVal.height;

            const resultX = -5 + ((svgX - 68.7706623764808)/(330.8971015795888-68.7706623764808)) * 10;
            const resultY = -5 + ((329.786111070345 - svgY)/(329.786111070345-68.65654958283815)) * 10;

            x = resultX;
            y = resultY;

            updateInputs();
            document.getElementById("mainForm").submit();
        }
    } else {
        alert("Ошибка: введите радиус")
    }
});

function placeDot(x, y, color) {
    const svg = document.getElementById("circles");
    const dot = document.createElementNS("http://www.w3.org/2000/svg", "circle");
    const svgX = 68.75 + 26.25 * (x + 5);
    const svgY = 330 - (y + 5) * (262.5 / 10);

    dot.setAttribute("cx", svgX);
    dot.setAttribute("cy", svgY);
    dot.setAttribute("r", 4);
    dot.setAttribute("fill", color);
    svg.appendChild(dot);
}