/**
* JS code for handling math formula
* Source: https://github.com/HuyLafa/MathTutor
*/

$(document).ready( function() {
	setupMathInput();
	// switch between normal text and math input
	$('#typeMath').change( function() {
		$('#input-area').html("");
		if (this.checked) {
			$textarea = $("<div id='mathquill'></div>").appendTo('#input-area');
			setupMathInput();
		}
		else {
			$textarea = $("<textarea class='form-control' placeholder='type a message'></textarea>").appendTo('#input-area');
			closeKeyboard();
		}
	});
});

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

	closeKeyboard();

	$('#mathquill').click(function(e) {
	setTimeout(function() {
		openKeyboard();
	}, 100);
	});

	$('#keyboard-mask').height($('#keyboard-wrapper').height());
}


var closeKeyboard = function() {
	$('#keyboard-mask').slideUp();
	$('#keyboard-wrapper').slideUp();
}

var openKeyboard = function() {
	$('#keyboard-wrapper').slideDown();
	$('#keyboard-mask').slideDown("fast", function() {
		$(window).scrollTop($('#keyboard-mask').position().top + $('#keyboard-mask').outerHeight());
	});
}




