let month = ["января", "февраля", "марта", "апреля", "мая", "июня", "июля", "августа", "сентября", "октября", "ноября", "декабря"]

// update();
// setInterval(update, 6000);
//
// function update() {
//     let date_full = new Date();
//
//     let time = date_full.getHours()
//         + ":" + ((date_full.getMinutes() < 10) ? ("0" + date_full.getMinutes()) : date_full.getMinutes())
//         + ":" + ((date_full.getSeconds() < 10) ? ("0" + date_full.getSeconds()) : date_full.getSeconds());
//
//     let date = ((date_full.getDay() < 10) ? ("0" + date_full.getDay()) : date_full.getDay())
//         + " " + month[date_full.getMonth()] + " " + date_full.getFullYear() + " г.";
//
//     document.getElementById("clock-time").innerText = time;
//     document.getElementById("clock-date").innerText = date;
// }

function updateClock() {
    const now = new Date();
    const milliseconds = now.getMilliseconds();
    const seconds = now.getSeconds();
    const minutes = now.getMinutes();
    const hours = now.getHours() % 12; // Приводим к 12-часовому формату

    const secondDeg = ((seconds / 60) * 2*Math.PI) + Math.PI/2 + ((milliseconds/1000)*Math.PI/30);
    const minuteDeg = ((minutes / 60) * 2*Math.PI)+Math.PI/2;
    const hourDeg = ((hours / 12) * 2*Math.PI)+Math.PI/2;


    document.getElementById('second').setAttribute('x2', (100-Math.cos(secondDeg)*60)+"");
    document.getElementById('second').setAttribute('y2', (100-Math.sin(secondDeg)*60)+"");
    document.getElementById('minute').setAttribute('x2', (100-Math.cos(minuteDeg)*60)+"");
    document.getElementById('minute').setAttribute('y2', (100-Math.sin(minuteDeg)*60)+"");
    document.getElementById('hour').setAttribute('x2', (100-Math.cos(hourDeg)*50)+"");
    document.getElementById('hour').setAttribute('y2', (100-Math.sin(hourDeg)*50)+"");

    let date = ((now.getDay() < 10) ? ("0" + now.getDay()) : now.getDay())
    + " " + month[now.getMonth()] + " " + now.getFullYear() + " г.";
    document.getElementById("clock-date").innerText = date;
}

setInterval(updateClock, 100);
updateClock(); // Обновить сразу при загрузке