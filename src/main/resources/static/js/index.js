
const MODAL = $("#modal");

window.addEventListener("load",function(event) {
    init()
    console.log('index finished initializing')
},false);

function init() {
    window.onclick = function(event) {
        if (event.target === MODAL.get(0)) {
            MODAL.css('display', 'none');
        }
    }
}

function openNamespace(namespace) {
    window.location.assign(BACKEND_URL + NAMESPACE_ENDPOINT + "?id=" + namespace);
}

function openStats() {
    window.location.assign(BACKEND_URL + STATS_ENDPOINT);
}

function refresh() {
    MODAL.css('display', 'block');
    sendHttpRequest('GET', REFRESH_ENDPOINT, function(data) {
        window.location.assign(BACKEND_URL);
    });
}

function deleteNamespace(namespace) {
    let answer = window.confirm('Delete Namespace ' + namespace + '.\nAre you sure?');
    if (answer) {
        MODAL.css('display', 'block');
        sendHttpRequest('DELETE', NAMESPACE_ENDPOINT + "/" + namespace, function(data) {
            updatePage();
        });
    }
}

function flush() {
    let answer = window.confirm('Delete entire cache.\nAre you sure?');
    if (answer) {
        sendHttpRequest('DELETE', KEYS_ENDPOINT, function(data) {
            updatePage();
        });
    }
}

function updatePage() {
    window.location.assign(BACKEND_URL);
}