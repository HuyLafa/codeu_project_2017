/* Set up WebSocket */
var $messages = $("#chat-list"), connection;


function init(websocketURL, username) {
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

  $('.member-list li').on("click", function() {
    var url = "/chat/" + this.id;
    window.location.replace(url);
  });
}

function setupWebSocket(websocketURL, username) {
    connection = new WebSocket(websocketURL);
    var initInputBox = function() {
      $text = $("#input-box");
      $text.keypress(function(event) {
        var keycode = (event.keyCode ? event.keyCode : event.which);
        if(keycode == '13'){
          var inputText = $text.val();
          $text.val("");
          connection.send(inputText);
        }
      });
    }

    connection.onopen = function () {
        initInputBox();
    };

    connection.onerror = function (error) {
        console.log('WebSocket Error ', error);
    };
    connection.onmessage = function (event) {
        addLeftMessage(username, $messages, event.data);
    }
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
      connection.send("$$" + mathField.latex() + "$$");
      mathField.latex("");
    }
	});

	$('#keyboard-mask').height($('#keyboard-wrapper').height());
}

function addLeftMessage(username, container, message) {
  var htmlCode =
  `<li class="left clearfix">
    <div class="chat_time pull-left"><strong>${username}</strong></div>
    <br>
    <span class="chat-img1 pull-left">
    <img src="https://pldh.net/media/dreamworld/054.png" alt="User Avatar"
         class="img-circle">
    </span>
     <div class="chat-body1 clearfix">
         <p mathjax>${message}</p>
     </div>
   </li>
  `;
   container.append(htmlCode);
   MathJax.Hub.Queue(["Typeset", MathJax.Hub]);
}

function addRightMessage(username, container, message) {
  var htmlCode =
  `<li class="right clearfix">
      <div class="chat_time pull-right"><strong>${username}</strong></div>
      <br>
      <span class="chat-img1 pull-right">
      <img src="https://pldh.net/media/dreamworld/054.png" alt="User Avatar"
           class="img-circle">
      </span>
       <div class="chat-body1 clearfix">
           <p mathjax>${message}</p>
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