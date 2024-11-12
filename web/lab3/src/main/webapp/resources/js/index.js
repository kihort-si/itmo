function setupButtons() {
    document.querySelectorAll('.xButtons input').forEach(button => {
        button.addEventListener('click', function () {
            document.querySelectorAll('.xButtons input').forEach(btn => {
                btn.classList.remove('active');
            });
            this.classList.add('active');
            document.getElementById('form:x').value = this.value;
        });
    });
}

document.addEventListener('DOMContentLoaded', function () {
    setupButtons();
});

function sendCoordinates(event) {
    const container = document.getElementById('graphContainer');
    const rect = container.getBoundingClientRect();

    const clickX = event.clientX - rect.left;
    const clickY = event.clientY - rect.top;

    const x = ((clickX - 40) / 320) * 10 - 5;
    const y = 5 - ((clickY - 40) / 320) * 10;

    document.getElementById('form:graphForm:xGraph').value = x;
    document.getElementById('form:graphForm:yGraph').value = y;

    updateValues();
    document.getElementById('form:graphForm:submit').click();
}

function getSelectedRValue() {
    const selectedRadio = document.querySelector('input[name="form:r"]:checked');
    return selectedRadio ? selectedRadio.value : null;
}

function updateSVG() {
    const svg = document.getElementById('graph');
    svg.style.scale = getSelectedRValue() / 5;
    updateSVGWithPoints();
}

document.addEventListener('DOMContentLoaded', function() {
    updateSVGWithPoints();
});

function updateValues() {
    document.getElementById('form:x').value = document.getElementById('form:graphForm:xGraph').value;
    document.getElementById('form:y').value = 0;
}

function updateSVGWithPoints() {
    const svgContainer = document.getElementById('circles');
    const resultTable = document.getElementById('form:resultTable');

    svgContainer.innerHTML = '';

    const rows = resultTable.querySelectorAll('tbody tr');
    rows.forEach(row => {
        const x = parseFloat(row.cells[0].innerText.replace(',', '.'));
        const y = parseFloat(row.cells[1].innerText.replace(',', '.'));

        const svgX = ((x + 5) * 320) / 10 + 40;
        const svgY = ((5 - y) * 320) / 10 + 40;
        const r = getSelectedRValue();
        let color;
        if (r === null) {
            color = (getColors(x, y, 5) === true) ? 'green' : 'red';
        } else {
            color = (getColors(x, y, r) === true) ? 'green' : 'red';
        }

        const circle = document.createElementNS('http://www.w3.org/2000/svg', 'circle');
        circle.setAttribute('cx', svgX.toFixed(4));
        circle.setAttribute('cy', svgY.toFixed(4));
        circle.setAttribute('r', 5);
        circle.setAttribute('fill', color);

        svgContainer.appendChild(circle);
    });
}

function getColors(x, y, r) {
    let isHit;
    if (x >= 0 && y >= 0) isHit = (x <= r && (y <= r / 2));
    if (x > 0 && y < 0) isHit = ((x <= r) && (y >= -(r / 2)) && (x + Math.abs(2*y) <= r));
    if (x < 0 && y < 0) isHit = ((x * x + y * y) <= (r / 2 * r / 2));
    return isHit
}