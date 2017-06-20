 $(document).ready(function() {

    $('.validation-summary-message a').on('click', function(e){
        e.preventDefault();
            var focusId = $(this).attr('data-focuses');
            thingToFocus = $("#"+focusId.replace(/\./g, '\\\.'));
        $('html, body').animate({
            scrollTop: thingToFocus.parent().offset().top
        }, 500);
        thingToFocus.parent().find('.block-label').first().focus();
        thingToFocus.parent().find('.form-control').first().focus();
    });

    $('#errors').focus();

	$(".skiplink").click(function(event){
		// strip the leading hash and declare
		// the content we're skipping to
		var skipTo="#"+this.href.split('#')[1];
		// Setting 'tabindex' to -1 takes an element out of normal
		// tab flow but allows it to be focused via javascript
		$(skipTo).attr('tabindex', -1).on('blur focusout', function () {
			// when focus leaves this element,
			// remove the tabindex attribute
			$(this).removeAttr('tabindex');
		}).focus(); // focus on the content container
	});


//	$('#due_diligence_link').click(function() {
//		if (this.getAttribute('data-page') == "no-results") {
//			ga('send', 'event', "what-to-do-no-results-page", 'click');
//		}
//		if (this.getAttribute('data-page') == "unapproved") {
//			ga('send', 'event', "what-to-do-unapproved-page", 'click');
//		}
//	});


	// ----------------------------------------------------------
	// If you're not in IE (or IE version is less than 5) then:
	// ie === undefined
	// If you're in IE (>=5) then you can determine which version:
	// ie === 7; // IE7
	// Thus, to detect IE:
	// if (ie) {}
	// And to detect the version:
	// ie === 6 // IE6
	// ie > 7 // IE8, IE9, IE10 ...
	// ie < 9 // Anything less than IE9
	// ----------------------------------------------------------
	var ie = (function(){
	    var undef,rv = -1; // Return value assumes failure.
	    var ua = window.navigator.userAgent;
	    var msie = ua.indexOf('MSIE ');
	    var trident = ua.indexOf('Trident/');
	    var edge = ua.indexOf('Edge/');
	    if (msie > 0) {
	        // IE 10 or older => return version number
	        rv = parseInt(ua.substring(msie + 5, ua.indexOf('.', msie)), 10);
	    } else if (trident > 0) {
	        // IE 11 (or newer) => return version number
	        var rvNum = ua.indexOf('rv:');
	        rv = parseInt(ua.substring(rvNum + 3, ua.indexOf('.', rvNum)), 10);
	    } else if (edge > 0) {
			// Edge
			rv = 13
        }
	    return ((rv > -1) ? rv : undef);
	}());

    $("summary").keypress(
        function(event) {
            event = event || window.event
            if (event.preventDefault && ( event.which != 13 || ie) ) {
                event.preventDefault();
            } else { // IE<9 variant:
                event.returnValue = false;
            }
        }
	);

});


