
const MODAL = $("#modal");

var args = {};
window.location.search.substr(1).split('&').forEach(arg => {
    let splitArg = arg.split('=');
    args[splitArg[0]] = splitArg[1];
})

const NAMESPACE = args['id'];
const PAGE = args['page'] || 1;

window.addEventListener("load",function(event) {
    init()
    console.log('index finished initializing')
},false);

function init() {
    $("#close-span").click(function() {
        MODAL.css('display', 'none');
    });
    window.onclick = function(event) {
        if (event.target === MODAL.get(0)) {
            MODAL.css('display', 'none');
        }
    }
}

function searchTerm() {
    let term = $("#search-term").val();
    window.location.assign(BACKEND_URL + NAMESPACE_ENDPOINT + "?id=" + NAMESPACE + "&term=" + term);
}

function getValue(key) {
    console.log('Key: ' + key)
    sendHttpRequest(
        'GET',
        VALUE_ENDPOINT + "?namespace=" + NAMESPACE + "&key=" + encodeURI(key),
        function(data) {
            showValue(key, data)
        },
        type='text');
}

function showValue(key, value) {
    let valueNamespace = $("#value-namespace");
    let valueKey = $("#value-key");
    let valueText = $("#value-text");
    valueNamespace.html(NAMESPACE);
    valueKey.html(key);
    valueText.html(value);
    MODAL.css('display', 'block');
}


function deleteNamespace() {
    let answer = window.confirm('Delete Namespace ' + NAMESPACE + '.\nAre you sure?');
    if (answer) {
        sendHttpRequest('DELETE', NAMESPACE_ENDPOINT + "/" + NAMESPACE, function(data) {
            openRoot();
        });
    }
}

function deleteKey(key) {
    let answer = window.confirm('Delete Key ' + key + '.\nAre you sure?');
    if (answer) {
        sendHttpRequest('DELETE', KEY_ENDPOINT + "/" + NAMESPACE + "/" + key, function(data) {
            updatePage();
        });
    }
}

function openRoot() {
    window.location.assign(BACKEND_URL);
}

function updatePage() {
    let url = BACKEND_URL + "/" + NAMESPACE_ENDPOINT + '?' +
        'id=' + NAMESPACE +
        '&page=' + PAGE;
    window.location.assign(url);
}