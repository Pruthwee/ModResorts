// index-init.js
// Scripts that used to be inline in index.html, moved to a same-origin file so the page
// runs under a Content-Security-Policy without 'unsafe-inline' or a nonce.
// Loaded at the end of <body>, after main.js and lib/pikaday.js.

function showCurrentDate() {
    var dateObj = new Date();
    var locale = "en-us";
    var month = dateObj.toLocaleString(locale, { month: "short" }); //months from 1-12
    var day = dateObj.getUTCDate();
    var year = dateObj.getUTCFullYear();
    return day + ' ' + month + ' ' + year;
}

function displayDate(dateObj, elem) {
    var locale = "en-us";
    var month = dateObj.toLocaleString(locale, { month: "short" }); //months from 1-12
    var day = dateObj.getUTCDate();
    var year = dateObj.getUTCFullYear();
    elem.innerHTML = day + ' ' + month + ' ' + year;
}

// Show today's date in both date fields (was an inline <script> in each field).
document.getElementById('datepickerfrom').innerHTML = showCurrentDate();
document.getElementById('datepickerto').innerHTML = showCurrentDate();

// Event handlers that used to be inline onchange / onclick attributes.
document.getElementById('city-select').addEventListener('change', function () {
    callRESTAPI();
});
document.getElementById('reserve-btn').addEventListener('click', function () {
    confirmReservcation();
});

var pickerFrom = new Pikaday(
{
    field: document.getElementById('datepickerfrom'),
    firstDay: 1,
    minDate: new Date(),
    maxDate: new Date(2020, 12, 31),
    yearRange: [2000, 2020],
    onSelect: function checkAvailability(selectedDate) {
        callAvailabilityChecker(selectedDate, document.getElementById('datepickerfrom'));
    }
});
var pickerTo = new Pikaday(
{
    field: document.getElementById('datepickerto'),
    firstDay: 1,
    minDate: new Date(),
    maxDate: new Date(2020, 12, 31),
    yearRange: [2000, 2020],
    onSelect: function checkAvailability(selectedDate) {
        callAvailabilityChecker(selectedDate, document.getElementById('datepickerto'));
    }
});
