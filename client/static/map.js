var mymap;
var points;

var countryDataList = {
    'Poland': [[51.919438, 19.145136], [6]],
    'Germany': [[51.165691, 10.451526], [6]],
    'France': [[46.227638, 2.213749], [6]],
    'Czech Republic': [[49.817492, 15.472962],[8]]
}

function addPoints(points) {
    var point = [];
    var arraySize = points.length;

    for (var i = 0; i < arraySize; i++) {
        point = points[i];
        console.log(points[i]);
        console.log(point);
        console.log(point[0]);
        console.log(point[1]);
        console.log(point[2]);
        addCircle(point[0], point[1], point[2]);
    }
}

function addLayer() {
    mymap = L.map('mapid').setView([52.25, 21.000000], 6);
    mymap.options.minZoom = 3;

    L.tileLayer('https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token=pk.eyJ1IjoibWFwYm94IiwiYSI6ImNpejY4NXVycTA2emYycXBndHRqcmZ3N3gifQ.rJcFIG214AriISLbB6B5aw', {
        maxZoom: 18,
        attribution: 'Map data &copy; <a href="https://www.openstreetmap.org/">OpenStreetMap</a> contributors, ' +
        '<a href="https://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, ' +
        'Imagery © <a href="https://www.mapbox.com/">Mapbox</a>',
        id: 'mapbox.streets'
    }).addTo(mymap);
}

function addCircle(latitude, longitude, radius) {
    var antennaRange =
        L.circle([latitude, longitude, 0], radius * 1000, {
            color: 'red',
            fillColor: '#f03',
            fillOpacity: 0.5
        });

    var antennaPoint =
        L.circle([latitude, longitude, 0], 500, {
            color: 'black',
            fillColor: 'black',
            fillOpacity: 1
        });

    antennaRange.addTo(mymap);
    antennaPoint.addTo(mymap);
}


function makeRequest() {
    var xmlHttpAntenna = new XMLHttpRequest();
    var xmlHttpTime = new XMLHttpRequest();
    var url = 'http://localhost:8080/';
    var countryElement = document.getElementById("country-select");
    var country = countryElement.options[countryElement.selectedIndex].text;
    var radius = document.getElementById("radius-input").value;

    if(!validateRequestData(radius)){
        return;
    }

    var urlAntenna = url + 'antenna?' + 'country=' + country + '&' + 'radius=' + radius;
    var urlTime = url + 'time?' + 'radius=' + radius + '&' + 'country=' + country;

    xmlHttpTime.open("GET",urlTime, true);
    xmlHttpTime.send(null);

    xmlHttpTime.onreadystatechange = function() {
        if (xmlHttpTime.readyState == 4 && xmlHttpTime.status == 200)
            var jsonResponse = JSON.parse(xmlHttpTime.responseText);

        if(jsonResponse !== '' && typeof jsonResponse !== 'undefined'){
            console.log(jsonResponse);
            var time = getTimeFromJson(jsonResponse);
            displayTime(time)
        }
    };

    xmlHttpAntenna.open("GET", urlAntenna, true);
    xmlHttpAntenna.send(null);

    xmlHttpAntenna.onreadystatechange = function() {
        if (xmlHttpAntenna.readyState == 4 && xmlHttpAntenna.status == 200)
            var jsonResponse = JSON.parse(xmlHttpAntenna.responseText);

        if(jsonResponse !== '' && typeof jsonResponse !== 'undefined'){
            points = getPointsFromJson(jsonResponse);
        }
    };
}

function getPointsFromJson(jsonData) {
    var points = jsonData['data']['points'];

    return points;
}

function getTimeFromJson(jsonData) {
    var time = jsonData['data']['time'];

    return time;
}

function addCountries()
{
    var countryList = Object.keys(countryDataList);
    var listSize = countryList.length;
    console.log(countryList);

    for(var i = 0; i < listSize; i++){
        var select = document.getElementById("country-select");
        var option = document.createElement("option");
        option.text = countryList[i];
        select.add(option);
    }
}

function showCountry()
{
    var elementId = "country-select";
    var element = document.getElementById(elementId);
    var country = null

    if (element.selectedIndex != -1)
    {
        country = element.options[element.selectedIndex].text;
    }

    mymap.setView(countryDataList[country][0], countryDataList[country][1]);
    console.log(country);
}

function validateRequestData(radius) {
    radius = radius.trim();

    if(radius == null || radius === ''){
        addWarning('Radius cannot be null');
        return false;
    } else if(radius <= 0){
        addWarning('Radius must be greater than 0');
        return false;
    } else if(Number.isNaN(radius/radius)) {
        addWarning('Radius must be a number')
        return false;
    } else {
        removeWarning();
        return true;
    }
}

function addWarning(message) {
    if(!(document.getElementById("radius-alert") === null)){
        removeWarning();
    }

    var divElement = document.createElement("div");
    var alertText = document.createTextNode(message);

    divElement.setAttribute("class", "alert alert-danger center-block");
    divElement.setAttribute("id", "radius-alert");
    divElement.setAttribute("role", "alert");
    divElement.appendChild(alertText);

    var element = document.getElementById("calculate-menu");
    var child = document.getElementById("p-radius-input");

    element.insertBefore(divElement, child);
}

function removeWarning() {
    if(!(document.getElementById("radius-alert") === null)){
        document.getElementById("radius-alert").remove();
    }
}

function displayProgress(timeInMiliseconds) {
    var progressBar = document.getElementById("progressbar");
    var id = setInterval(frame, timeInMiliseconds/100);
    var percentage = 0;

    function frame() {
        if (percentage > 100) {
            clearInterval(id);
        } else {
            progressBar.style.width = percentage + "%";
            progressBar.innerHTML = percentage + "%";
            percentage++;
        }
    }
}

function displayTime(timeInMilliseconds) {

    var id = setInterval(countdown, 1000);
    var time = timeInMilliseconds;
    var days;
    var hours;
    var minutes;
    var seconds;
    var overlayPlaceholder = document.getElementById("overlay-placeholder");

    var divOverlay = document.createElement("div");
    divOverlay.setAttribute("class", "overlay");
    divOverlay.setAttribute("id", "id-overlay");

    var overlay = '<div id="id-overlay" class="overlay"><div class="loader" style="margin: auto; margin-top: 10%"></div><p id="timer" style="text-align: center; font-size: 64px;"></p></div>';

    overlayPlaceholder.appendChild(divOverlay);
    divOverlay.innerHTML = overlay;
    var timer = document.getElementById("timer");

    function countdown() {
        if (time < 0) {
            overlayPlaceholder.removeChild(divOverlay);
            clearInterval(id);
            addPoints(points);
        } else {
            days = Math.floor(time / (1000 * 60 * 60 * 24));
            hours = Math.floor((time % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
            minutes = Math.floor((time % (1000 * 60 * 60)) / (1000 * 60));
            seconds = Math.floor((time % (1000 * 60)) / 1000);

            timer.innerHTML = 'Calculating in progress.' + '<br>' + 'Will be finished in:' + '<br>' + days + "d " + hours + "h " + minutes + "m " + seconds + "s ";
            time -= 1000;
        }
    }
}
