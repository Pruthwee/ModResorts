// index-init.js
// External replacement for the inline scripts and inline event handlers of index.html,
// so that the page satisfies a Content-Security-Policy without 'unsafe-inline' scripts.

function displayDate(dateObj, elem) {
    var locale = "en-us";
    var month = dateObj.toLocaleString(locale, { month: "short" }); //months from 1-12
    var day = dateObj.getUTCDate();
    var year = dateObj.getUTCFullYear();
    elem.innerHTML = day + ' ' + month + ' ' + year;
}

function showCurrentDate(elem) {
    displayDate(new Date(), elem);
}

showCurrentDate(document.getElementById('datepickerfrom'));
showCurrentDate(document.getElementById('datepickerto'));

// Replaces the inline onchange / onclick attributes
var citySelect = document.querySelector('.js-select');
if (citySelect) {
    citySelect.addEventListener('change', function () {
        callRESTAPI();
    });
}
var reserveButton = document.getElementById('reserve-btn');
if (reserveButton) {
    reserveButton.addEventListener('click', function () {
        confirmReservcation();
    });
}

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
