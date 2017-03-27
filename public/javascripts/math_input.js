/**
* JS code for handling math formula
* Source: https://github.com/HuyLafa/MathTutor
*/

$(document).ready(function() {
  var isMobile = window.matchMedia("only screen and (max-width: 760px)");
  var MQElement = document.getElementById("mathquill");

  var MQ = MathQuill.getInterface(2); // for backcompat

  var mathField = MQ.MathField(MQElement, {});

  $('#keyboard .keypad:not(.default)').hide();
  $('#keyboard .key').click( function(event) {
    event.preventDefault();

    if ( $(this).data('action') == 'write') {
      mathField.write($(this).data('content'));
    } else if($(this).data('action') == 'cmd') {
      mathField.cmd($(this).data('content'));
    } else if($(this).data('action') == 'keystroke') {
      mathField.keystroke($(this).data('content'));
    } else if($(this).data('action') == 'switch-keys') {
      console.log("'#keyboard .keypad:not(.' + $(this).data('content') + ')'");
      $('#keyboard .keypad:not(.' + $(this).data('content') + ')').hide();
      $('#keyboard .keypad.' + $(this).data('content')).show();
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
  });

  closeKeyboard = function() {
    $('#keyboard-mask').slideUp();
    $('#keyboard-wrapper').slideUp();
  }

  openKeyboard = function() {
    $('#keyboard-wrapper').slideDown();
    $('#keyboard-mask').slideDown("fast", function() {
      $(window).scrollTop($('#keyboard-mask').position().top + $('#keyboard-mask').outerHeight());
    });
  }

  closeKeyboard();

  $('#mathquill').click(function(e) {
    //$('.mq-textarea textarea').attr('readonly', 'readonly'); // Force keyboard to hide on input field.
    //$('.mq-textarea textarea').attr('disabled', 'true'); // Force keyboard to hide on textarea field.
    setTimeout(function() {
        // $('.mq-textarea textarea').blur();  //actually close the keyboard
        // $('.mq-textarea textarea').removeAttr('readonly');
        // $('.mq-textarea textarea').removeAttr('disabled');
        openKeyboard();
    }, 100);
  });

  $('#keyboard-mask').height($('#keyboard-wrapper').height());

});
