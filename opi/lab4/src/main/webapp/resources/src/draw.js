let baseR = 25;
const colorHit = "#258a17"
const colorMiss = "#f32a15"

let svg = document.getElementById('paint');

function drawPoint(x, y, r, isHit) {

    svg = document.getElementById('paint');

    let circle = document.createElementNS("http://www.w3.org/2000/svg", 'circle');
    circle.setAttribute('cx', (x / r * baseR + 150).toString());
    circle.setAttribute('cy', (-y / r * baseR + 150).toString());
    circle.setAttribute('r', '4');

    circle.setAttribute('fill', isHit ? colorHit : colorMiss);

    svg.appendChild(circle);

}