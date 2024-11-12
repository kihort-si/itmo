function updateClock() {
    const now = new Date();
    const hours = now.getHours();
    const minutes = now.getMinutes();
    const seconds = now.getSeconds();
    const milliseconds = now.getMilliseconds();

    const hourHand = document.getElementById('hourHand');
    const minuteHand = document.getElementById('minuteHand');
    const secondHand = document.getElementById('secondHand');

    const hourDegrees = ((hours % 12) + minutes / 60) * 30;
    hourHand.style.transform = `translateX(-50%) rotate(${hourDegrees}deg)`;

    const minuteDegrees = (minutes + seconds / 60) * 6;
    minuteHand.style.transform = `translateX(-50%) rotate(${minuteDegrees}deg)`;

    const secondDegrees = (seconds + milliseconds / 1000) * 6;
    secondHand.style.transform = `translateX(-50%) rotate(${secondDegrees}deg)`;

    const ruDate = new Intl.DateTimeFormat("ru", { day: "numeric", month: "long", year: "numeric", weekday: "long" }).format(now);
    document.getElementById('date').innerHTML = ruDate.replace(ruDate[0], ruDate[0].toUpperCase());
}

setInterval(updateClock, 50);
updateClock();
