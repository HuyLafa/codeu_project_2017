/**
    JS code for handling math formula
*/

$(function() {

    var mathFieldSpan = document.getElementById("math-field");
    var latexSpan = document.getElementById("latex");

    var MQ = MathQuill.getInterface(2); // for backcompat
    var mathField = MQ.MathField(mathFieldSpan, {
      spaceBehavesLikeTab: true, // configurable
      handlers: {
        edit: function() { // useful event handlers
          latexSpan.textContent = mathField.latex(); // simple API
        }
      }
    });

});