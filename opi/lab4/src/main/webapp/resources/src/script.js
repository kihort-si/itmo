const center = 150;


let pointsToString;


function getR(){
    return document.getElementById('coordinates:r-options').selectedIndex + 1.0
}

document.getElementById('click_catcherR').addEventListener('click', () => {

    redraw(getR(), pointsToString);

    document.querySelectorAll('circle').forEach((point) => {
        point.remove();
    })

    const points = pointsToString.split(';');
    points.forEach((point) => {
        const parameters = point.split(',');
        let x = parameters[0];
        let y = parameters[1];
        let r = parameters[2];
        let isHit = (parameters[3] === "true");

        drawPoint(x, y, r, isHit);
    })
});


function redraw(r, arr) {
    pointsToString = arr;
    baseR = 25 * r;

    let triangle = document.getElementById("triangle");
    triangle.setAttribute('points',
        "150,150 " + (150 + baseR) + ",150 150," + (150 - baseR / 2)
    );

    let square = document.getElementById("square");
    square.setAttribute('x', (150 - baseR) + "");
    square.setAttribute('y', (150 - baseR) + "");
    square.setAttribute('width', (baseR) + "");
    square.setAttribute('height', (baseR) + "");

    let arc = document.getElementById("arc");
    arc.setAttribute('d',
        "M 150 150 L 150 "
        + (150 + baseR) + " A" + (baseR) + ", " + (baseR) + " 0 0, 1 " + (150 - baseR) + ",150 Z"
    );

    // document.getElementById("R-1").setAttribute('x', (150 + baseR - 5) + "");
    // document.getElementById("R-2").setAttribute('x', (150 - baseR - 8) + "");
    // document.getElementById("R-3").setAttribute('y', (150 - baseR + 5) + "");
    // document.getElementById("R-4").setAttribute('y', (150 + baseR + 3) + "");

}


