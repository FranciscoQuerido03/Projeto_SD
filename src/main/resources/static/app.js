var stompClient = null;

function setConnected(connected) {
    if (connected) {
        document.getElementById("connect").setAttribute("disabled", "");
        document.getElementById("disconnect").removeAttribute("disabled");
        document.getElementById("conversation").style.display = "block";
    }else {
        document.getElementById("conversation").style.display = "hidden";
        document.getElementById("connect").removeAttribute("disabled");
        document.getElementById("disconnect").setAttribute("disabled", "");
    }
    document.getElementById("SV_ON").innerHTML = "";
    document.getElementById("Search").innerHTML = "";
    document.getElementById("Response").innerHTML = "";
}

function connect() {
    var socket = new SockJS("/my-websocket");
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        setConnected(true);
        console.log("Connected: " + frame);
        stompClient.subscribe("/topic/messages", function (message) {
            showMessage(message.toString());
        });
    });
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
        }
        setConnected(false);
        console.log("Disconnected");
}

function showMessage(message) {
    var lines = message.split('\n');

    for (var i = 0; i < lines.length; i++) {
        if (lines[i].trim().startsWith('{')) {
            try {
                var jsonObject = JSON.parse(lines[i]);
                console.log("Line " + (i + 1) + ": ", jsonObject);
            } catch (error) {
                console.error("Error parsing JSON on line " + (i + 1) + ": ", error.message);
            }
        }
    }

    document.getElementById("SV_ON").insertRow();
    document.getElementById("SV_ON").textContent = jsonObject.on_svs;
    document.getElementById("Search").insertRow();
    document.getElementById("Search").textContent = jsonObject.common_s;
    document.getElementById("Response").insertRow();
    document.getElementById("Response").textContent = jsonObject.resp_t;

    // document.getElementById("messages").insertRow();
    // document.getElementById("messages").append(message);
}

window.addEventListener('load',
    function () {
        document.getElementById("connect").addEventListener('click', (e) => {
            e.preventDefault();
            connect();
        });
        document.getElementById("disconnect").addEventListener('click', (e) => {
            e.preventDefault();
            disconnect();
        });
    }, false);
