"use strict";

let x, y, r;
const resultTable = document.getElementById('result');
const resultTableBody = document.querySelector("#result tbody");
const deleteButton = document.getElementById('deleteButton');

async function validate() {
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

    if (success) {
        let jsonData = {
            "x": x.toString(),
            "y": y.toString(),
            "r": r.toString()
        };

        let startTime = Date.now();

        await fetch(`/fcgi-bin/server.jar?${new URLSearchParams(jsonData).toString()}`, {
            method: 'GET'
        })
            .then(response => response.json())
            .then(response => {
                let endTime = Date.now();
                let timeTaken = endTime - startTime;
                if (response.error != null) {
                    alert('Ответ от сервера не получен');
                    console.log(response.error);
                } else {
                    const newRow = resultTableBody.insertRow(-1);

                    const rowX = newRow.insertCell(0);
                    const rowY = newRow.insertCell(1);
                    const rowR = newRow.insertCell(2);
                    const rowTime = newRow.insertCell(3);
                    const rowDuration = newRow.insertCell(4);
                    const rowResult = newRow.insertCell(5);

                    const date = new Date();

                    rowX.textContent = x;
                    rowY.textContent = y;
                    rowR.textContent = r;
                    rowTime.textContent = date.getHours() + ":" + date.getMinutes().toString().padStart(2, '0') + ":" + date.getSeconds().toString().padStart(2, '0');
                    rowDuration.textContent = timeTaken + " мс";
                    rowResult.textContent = response.hit;
                }
            })

        resultTable.style.opacity = '1';
        deleteButton.style.opacity = '1';
        saveTableData();
        updateSVGDot();
        updateSVGRadius();
    }
}

function saveTableData() {
    const rows = [];
    const tableRows = resultTableBody.querySelectorAll('tr');

    tableRows.forEach(row => {
        const cells = row.querySelectorAll('td');
        const rowData = {
            x: cells[0].textContent,
            y: cells[1].textContent,
            r: cells[2].textContent,
            time: cells[3].textContent,
            duration: cells[4].textContent,
            result: cells[5].textContent
        };
        rows.push(rowData);
    });

    localStorage.setItem('tableData', JSON.stringify(rows));
}

function restoreTableData() {
    const savedData = localStorage.getItem('tableData');
    if (savedData) {
        const rows = JSON.parse(savedData);
        resultTable.style.opacity = '1';
        deleteButton.style.opacity = '1';

        rows.forEach(rowData => {
            const newRow = resultTableBody.insertRow(-1);

            const rowX = newRow.insertCell(0);
            const rowY = newRow.insertCell(1);
            const rowR = newRow.insertCell(2);
            const rowTime = newRow.insertCell(3);
            const rowDuration = newRow.insertCell(4);
            const rowResult = newRow.insertCell(5);

            rowX.textContent = rowData.x;
            rowY.textContent = rowData.y;
            rowR.textContent = rowData.r;
            rowTime.textContent = rowData.time;
            rowDuration.textContent = rowData.duration;
            rowResult.textContent = rowData.result;
        });
    }
}

function deleteHistory() {
    localStorage.removeItem('tableData');
    resultTableBody.innerHTML = '';
}

function validateX() {
    if (!isNaN(parseFloat(x)) && isFinite(x)) return true;
    else {
        showErrorMessage('Введите X', 'x');
        return false;
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

function showErrorMessage(text, coordinate) {
    let textPlace = document.getElementById(coordinate + 'Error');
    textPlace.textContent = text;
    textPlace.style.opacity = '1';
}

function hideErrorMessage(coordinate) {
    let textPlace = document.getElementById(coordinate + 'Error');
    textPlace.style.opacity = '0';
}

function updateSVGRadius() {
    document.getElementById('radiusX').textContent = r;
    document.getElementById('radiusY').textContent = r;
    document.getElementById('-radiusX').textContent = -r;
    document.getElementById('-radiusY').textContent = -r;
    document.getElementById('radius2X').textContent = r / 2;
    document.getElementById('radius2Y').textContent = r / 2;
    document.getElementById('-radius2X').textContent = -r / 2;
    document.getElementById('-radius2Y').textContent = -r / 2;
}

function updateSVGDot() {
    const dot = document.getElementById('dot');
    dot.setAttribute("cx", ((x / r) * 154) + 256);
    dot.setAttribute("cy", (256 - (y / r) * 154));
}

function setupButtons(className) {
    document.querySelectorAll('.' + className + ' input[type="button"]').forEach(button => {
        button.addEventListener('click', function () {
            document.querySelectorAll('.' + className + ' input[type="button"]').forEach(btn => {
                btn.classList.remove('active');
            });
            this.classList.add('active');
            updateValues();
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
deleteButton.addEventListener('click', deleteHistory);

function updateValues() {
    x = document.querySelector('.xButtons input.active')?.value;
    r = document.querySelector('.rButtons input.active')?.value;
}

document.addEventListener('DOMContentLoaded', restoreTableData);