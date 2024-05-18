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
    var jsonObject;

    for (var i = 0; i < lines.length; i++) {
        if (lines[i].trim().startsWith('{')) {
            try {
                jsonObject = JSON.parse(lines[i]);
                console.log("Line " + (i + 1) + ": ", jsonObject);
            } catch (error) {
                console.error("Error parsing JSON on line " + (i + 1) + ": ", error.message);
            }
        }
    }

    if (jsonObject) {
        var svOnTable = document.getElementById("SV_ON");
        var searchTable = document.getElementById("Search");
        var responseTable = document.getElementById("Response");

        // Clear existing rows
        svOnTable.innerHTML = "";
        searchTable.innerHTML = "";
        responseTable.innerHTML = "";

        // Insert new rows
        var svOnRow = svOnTable.insertRow();
        var svOnCell = svOnRow.insertCell(0);
        svOnCell.innerHTML = jsonObject.on_svs;

        var searchRow = searchTable.insertRow();
        var searchCell = searchRow.insertCell(0);
        searchCell.innerHTML = "Top Pesquisas<br>" + jsonObject.common_s;

        var responseRow = responseTable.insertRow();
        var responseCell = responseRow.insertCell(0);
        responseCell.innerHTML = "Tempo de Resposta dos Servidores<br>" + jsonObject.resp_t;
    }
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
