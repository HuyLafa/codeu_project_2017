function init(websocketURL) {
  var $messages = $("#chat-list"),
      $text = $("#input-box"),
      connection = new WebSocket(websocketURL);

    var send = function () {
        var text = $text.val();
        connection.send(text);
    };

    connection.onopen = function () {
        $text.keypress(function(event) {
            var keycode = (event.keyCode ? event.keyCode : event.which);
            if(keycode == '13'){
                send();
            }
        });
    };
    connection.onerror = function (error) {
        console.log('WebSocket Error ', error);
    };
    connection.onmessage = function (event) {
        addLeftMessage($messages, event.data);
        $text.val("");
    }
}

function addLeftMessage(container, message) {
  var htmlCode =
  `<li class="left clearfix">
    <span class="chat-img1 pull-left">
    <img src="https://pldh.net/media/dreamworld/054.png" alt="User Avatar"
         class="img-circle">
    </span>
     <div class="chat-body1 clearfix">
         <p>${message}</p>
         <div class="chat_time pull-right">09:40PM</div>
     </div>
   </li>
`;
   container.append(htmlCode);
}

function addRightMessage(container, message) {
  var htmlCode =
  `<li class="right clearfix">
        <span class="chat-img pull-right">
           <img src="https://pldh.net/media/dreamworld/054.png" alt="User Avatar"
             class="img-circle">
        </span>
        <div class="chat-body clearfix">
           <div class="header-sec">
               <strong class="primary-font">Huy Nguyen</strong> <strong class="pull-left">
               09:45AM</strong>
           </div>
           <div class="contact-sec">
               <strong class="primary-font">${message}</strong>
           </div>
        </div>
   </li>`;
   container.append(htmlCode);
}