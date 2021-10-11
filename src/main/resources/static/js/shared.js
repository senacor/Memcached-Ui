
// Backend URL
const BACKEND_URL = window.location.origin
// Endpoints
const NAMESPACE_ENDPOINT = '/namespace'
const REFRESH_ENDPOINT = '/refresh';
const VALUE_ENDPOINT = '/value';
const STATS_ENDPOINT = '/stats';
const KEYS_ENDPOINT = '/keys';
const KEY_ENDPOINT = '/key';

window.addEventListener("load",function(event) {
    console.log('Finished initializing')
},false);

function sendHttpRequest(method, endpoint, callback, responseType = 'json') {
    let request = new XMLHttpRequest();
    request.open(method, endpoint);
    request.responseType = responseType;
    request.onload = function() {
        console.log('Received response from ' + method + ' ' + endpoint);
        callback(request.response);
    };
    console.log('Sending ' + method + ' request to ' + endpoint);
    request.send();
}