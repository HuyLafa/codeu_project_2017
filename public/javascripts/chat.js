/* Set up WebSocket */
var $messages = $("#chat-list"), connection;
var username;
var websocketURL;

function init(websocket, user) {
  username = user;
  websocketURL = websocket;
  console.log(websocketURL);
  setupWebSocket(websocketURL, username);

  closeKeyboard();

  // switch between normal text and math input
  $('#typeMath').change( function() {
    $('#input-area').html("");
    if (this.checked) {
      $("<div id='mathquill' tabindex='0'></div>").appendTo('#input-area');
      setupMathInput();
    }
    else {
      $("<textarea class='form-control' placeholder='type a message' id='input-box'></textarea>").appendTo('#input-area');
      closeKeyboard();
    }
    initInputBox();
  });

  // add conversation
  $('#new-conversation').on("click", function() {
    var roomName = prompt("Please enter the room name");
    var users = prompt("Enter the usernames of people who can enter your private chatroom: ");
    if (users != null) {
      $.post('/add-user-to-room', {'roomname': roomName, 'user' : users})
    }
    if (roomName != null) {
      $.post(`/new-conversation`, { 'roomName': roomName, 'owner' : username }, function(data) {
        $('.member-list ul').append(`
          <li class="left clearfix" id=${roomName}>
              <span class="chat-img pull-left">
                  <img
                    src="/assets/images/conversation.jpeg"
                    alt="User Avatar"
                    class="img-circle"
                  >
              </span>
              <div class="chat-body clearfix">
                  <div class="header_sec">
                      <strong class="primary-font">${data}</strong> <strong class="pull-right ">
                      09:45AM</strong>
                  </div>
                  <div class="contact_sec">
                      <strong class="primary-font">(123) 123-456</strong>
                      <span class="badge pull-right">3</span>
                  </div>
              </div>
          </li>
        `).on("click", function() {
          var url = `/chat/${roomName}`;
          window.location.replace(url);
        });
      });
    }
  });

  // pick conversation
  $('.member-list li').on("click", function() {
    var url = `/chat/${this.id}`;
    window.location.replace(url);
  });
}

function setupWebSocket(websocketURL, username) {
    connection = new WebSocket(websocketURL);

    connection.onopen = function () {
      initInputBox();
    };

    connection.onerror = function (error) {
      console.log('WebSocket Error ', error);
    };

    connection.onmessage = function (event) {
      console.log("event data: " + event.data);
      var msg = JSON.parse(event.data);
      addMessage(msg.author, $messages, msg.message, msg.date);
      $('.chat-area').scrollTop($('.chat-area')[0].scrollHeight);
    }
}

function initInputBox() {
  $text = $("#input-box");
  $text.keypress(function(event) {
    var keycode = (event.keyCode ? event.keyCode : event.which);
    if(keycode == '13'){
      event.preventDefault();
      var inputText = $text.val();
      if (inputText == null || inputText == "") {
        return;
      }
      var currentDate = Date.now().toString();
      var msg = {
        author : username,
        message : inputText,
        date : currentDate,
      }
      $text.val("");
      connection.send(JSON.stringify(msg));
    }
  });
}

var setupMathInput = function() {
	// math input configuration
	var isMobile = window.matchMedia("only screen and (max-width: 760px)").matches;
	var MQElement = document.getElementById("mathquill");

	var MQ = MathQuill.getInterface(2); // for backcompat

	var mathField = MQ.MathField(MQElement, {});

	$('#keyboard .key').click( function(event) {
		event.preventDefault();

		if (mathField)  {
			if ($(this).data('action') == 'write') {
						mathField.write($(this).data('content'));
			} else if($(this).data('action') == 'cmd') {
				mathField.cmd($(this).data('content'));
			} else if($(this).data('action') == 'keystroke') {
				mathField.keystroke($(this).data('content'));
			} else if($(this).data('action') == 'switch-keys') {
				$(this).parents('#keyboard').switchClass("trig", $(this).data("content"));
				$(this).parents('#keyboard').switchClass("std", $(this).data("content"));
			} else if($(this).data('action') == 'keyboard-hide'){
				closeKeyboard();
			}

			if (typeof $(this).data('stepback') !== 'undefined') {
				for (var i = 0; i < parseInt($(this).data('stepback')); i++) {
					mathField.keystroke('Left');
				}
			}

			if (typeof $(this).data('stepforward') !== 'undefined') {
				for (var i = 0; i < parseInt($(this).data('stepforward')); i++) {
					mathField.keystroke('Right');
				}
			}

			if(isMobile) {
				mathField.blur();
			} else {
				mathField.focus();
			}
		}
	});

	openKeyboard();

	$('#mathquill').click(function(e) {
	setTimeout(function() {
		openKeyboard();
	}, 100);
	});

	$('#mathquill').keypress(function(event) {
	  var keycode = (event.keyCode ? event.keyCode : event.which);
    if(keycode == '13'){
      event.preventDefault();
      var inputText = mathField.latex();
      if (inputText == null || inputText == "") {
        return;
      }
      inputText = "$$" + inputText + "$$";
      var currentDate = Date.now().toString();
      var msg = {
        author : username,
        message : inputText,
        date : currentDate,
      }
      connection.send(JSON.stringify(msg));
      mathField.latex("");
    }
	});

	$('#keyboard-mask').height($('#keyboard-wrapper').height());
}

function addMessage(author, container, message, date) {
  var d = new Date(+date);
  var timeString = d.toLocaleTimeString();
  if (author == username) {
    addLeftMessage(author, container, message, timeString);
  } else {
    addRightMessage(author, container, message, timeString);
  }
  MathJax.Hub.Queue(["Typeset", MathJax.Hub]);
}

function addLeftMessage(username, container, message, time) {
  var htmlCode =
  `<li class="left clearfix">
    <div class="chat_time pull-left"><strong>${username}</strong></div>
    <br>
    <span class="chat-img1 pull-left">
    <img src="/assets/images/sample-avatar.png" alt="User Avatar"
         class="img-circle">
    </span>
     <div class="chat-body1 clearfix">
         <p mathjax>${message}</p>
         <div class="chat_time pull-right">${time}</div>
     </div>
   </li>
  `;
   container.append(htmlCode);
}

function addRightMessage(username, container, message, time) {
  var htmlCode =
  `<li class="left clearfix other_chat">
      <div class="chat_time pull-right"><strong>${username}</strong></div>
      <br>
      <span class="chat-img1 pull-right">
      <img src="/assets/images/sample-avatar.png" alt="User Avatar"
           class="img-circle">
      </span>
       <div class="chat-body1 clearfix">
           <p mathjax>${message}</p>
           <div class="chat_time pull-left">${time}</div>
       </div>
     </li>
   `;
   container.append(htmlCode);
}

function closeKeyboard() {
	$('#keyboard-mask').slideUp();
	$('#keyboard-wrapper').slideUp();
}

function openKeyboard() {
	$('#keyboard-wrapper').slideDown();
	$('#keyboard-mask').slideDown("fast", function() {
		$(window).scrollTop($('#keyboard-mask').position().top + $('#keyboard-mask').outerHeight());
	});
}
