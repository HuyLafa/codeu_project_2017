$(function() {

  $('#login-form-link').click(function(e) {
		$("#login-form").delay(100).fadeIn(100);
 		$("#register-form").fadeOut(100);
		$('#register-form-link').removeClass('active');
		$(this).addClass('active');
		e.preventDefault();
	});

	$('#register-form-link').click(function(e) {
		$("#register-form").delay(100).fadeIn(100);
 		$("#login-form").fadeOut(100);
		$('#login-form-link').removeClass('active');
		$(this).addClass('active');
		e.preventDefault();
	});


  $('#register-form').on('submit', function(e) {
    e.preventDefault();
    var password = $('#register-form #password').val();
    var confirm = $('#register-form #confirm-password').val();
    if (password === confirm) {
      this.submit();
    }
    else {
      alert("Passwords do not match.");
    }
  });
});