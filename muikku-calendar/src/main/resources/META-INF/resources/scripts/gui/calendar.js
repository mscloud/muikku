(function() {
  
  var createDateField = function (id, name, placeholder) {
    return $('<div>') 
      .addClass('ca-field-date')
      .append($('<input>').attr({
        'placeholder': placeholder,
        'type': 'date',
        'id': id,
        'name': placeholder, 
        'required': 'required'
      }));
  };
  
  var createTimeField = function (id, name, placeholder) {
    return $('<div>') 
      .addClass('ca-field-time')
      .append($('<input>').attr({
        'placeholder': placeholder,
        'type': 'time',
        'id': id,
        'name': placeholder, 
        'required': 'required'
      }));
  };
  
  function createModalContent() {
    var repeat = $('<div>')
      .append($('<input>').attr({"type": "checkbox", "name": "repeat"}))
      .append($('<span>').text('Repeat:'))
      .append($('<div>').attr({'id': 'eventRecurrence'}));
    
    return $('<div>')
      .addClass('ca-event-new')
      .append($('<div>')
        .append(createDateField('startDate', 'eventStartDate', 'alkaa'))
        .append(createTimeField('startTime', 'eventStartTime', 'alkaa'))
        .append(createDateField('endDate', 'eventEndDate', 'loppuu'))
        .append(createTimeField('endTime', 'eventEndTime', 'loppuu'))
        .append(repeat)
        .append($('<div>').append($('<input>').attr({ 'placeholder': 'Tapahtuman nimi', 'name': 'eventSubject', 'required': 'required' })))
        .append($('<div>').append($('<select>').attr({'id': 'eventCalendar', 'required': 'required'}))))
      .append($('<div>').append($('<textarea>').attr({ 'placeholder': 'Tapahtuman kuvaus', 'name': 'eventContent', 'required': 'required' })));
  }
  
  function getISODateTime(date, time) {
    date.setMinutes(date.getMinutes() - date.getTimezoneOffset());
    return date.toISOString().split('T')[0] + 'T' + time.toISOString().split('T')[1];
  }
  
  $(document).ready(function(){
    var locale = getLocale().toLowerCase();
    var dpRegional = $.datepicker.regional[locale] || $.datepicker.regional[''];
    $.datepicker.setDefaults(dpRegional);

    $(".bt-mainFunction").m3modal({
  	  title : "Uusi tapahtuma! ",
  	  description : "Uuden tapahtuman luonti",
  	  content: createModalContent(),
  	  modalgrid : 24,
  	  contentgrid : 24,
  	  onBeforeOpen: function(modal){
  	    $('#eventRecurrence.recurrence-input').recurrenceInput('destroy');
  	    $('#eventRecurrence').recurrenceInput({
  	      texts: {
  	        label: getLocaleText('plugin.calendar.recurrenceInput.label'),
  	        freq: {
  	          "SECONDLY": getLocaleText('plugin.calendar.recurrenceInput.freqSecondly'),
  	          "MINUTELY": getLocaleText('plugin.calendar.recurrenceInput.freqMinutely'),
  	          "DAILY": getLocaleText('plugin.calendar.recurrenceInput.freqDaily'),
  	          "WEEKLY": getLocaleText('plugin.calendar.recurrenceInput.freqWeekly'),
  	          "MONTHLY": getLocaleText('plugin.calendar.recurrenceInput.freqMonthly'),
  	          "YEARLY": getLocaleText('plugin.calendar.recurrenceInput.freqYearly')
  	        },
  	        notRepeating: getLocaleText('plugin.calendar.recurrenceInput.notRepeating'),
  	        intervalLabel: getLocaleText('plugin.calendar.recurrenceInput.intervalLabel'),
  	        intervalDays: getLocaleText('plugin.calendar.recurrenceInput.intervalDays'),
  	        weekdaysLabel: getLocaleText('plugin.calendar.recurrenceInput.weekdaysLabel'),
  	        intervalWeeks: getLocaleText('plugin.calendar.recurrenceInput.intervalWeeks'),
  	        intervalMonths: getLocaleText('plugin.calendar.recurrenceInput.intervalMonths'),
  	        yearLabel: getLocaleText('plugin.calendar.recurrenceInput.yearLabel'),
  	        intervalYears: getLocaleText('plugin.calendar.recurrenceInput.intervalYears'),
  	        recurrenceLabel: getLocaleText('plugin.calendar.recurrenceInput.recurrenceLabel'),
  	        recurrenceNever: getLocaleText('plugin.calendar.recurrenceInput.recurrenceNever'),
  	        recurrenceOccurrencesLabel: getLocaleText('plugin.calendar.recurrenceInput.recurrenceOccurrencesLabel'),
  	        recurrenceOccurrences: getLocaleText('plugin.calendar.recurrenceInput.recurrenceOccurrences'),
            recurrenceUntilLabel: getLocaleText('plugin.calendar.recurrenceInput.recurrenceUntilLabel'),
            recurrenceUntil: getLocaleText('plugin.calendar.recurrenceInput.recurrenceUntil'),
  	        dayNamesShort: dpRegional.dayNamesShort
  	      }
  	    });
  	    
  	    
        mApi().calendar.calendars.read().callback($.proxy(function (err, calendars) {
  	      if (err) {
  	     	  $('.notification-queue').notificationQueue('notification', 'error', err);
  	     	} else {
  	     	 $('#eventCalendar option').remove();
  	        for (var i = 0, l = calendars.length; i < l; i++) {
  	          var calendar = calendars[i];
  	          $('#eventCalendar').append($('<option>').attr('value', calendar.id).text(calendar.summary));
  	        }
  	     	}
  	    }));
        
        var start = new Date();
        start.setMinutes(0);
        start.setHours(start.getHours() + 1);
        var end = new Date(start.getTime());
        end.setHours(end.getHours() + 1);
        
        $('#startDate')
          .datepicker()
          .datepicker('setDate', start);
        $('#startTime')
          .timepicker()
          .timepicker({'setTime': start,
                       'timeFormat' : 'H:i',
                       'useSelect': true});
        
        $('#endDate')
          .datepicker()
          .datepicker('setDate', end);
        $('#endTime').timepicker()
          .timepicker()
          .timepicker({'setTime': end,
                       'timeFormat' : 'H:i',
                       'useSelect': true});
        
  	    $('.ca-event-new').on('focus', 'input', function(){
  		    var dval = this.defaultValue;
  		    var cval = $(this).val();
  		    if (dval === cval){
  		      $(this).val('');
  		    } 
  	    });
  		  
  	    $('.ca-event-new').on('blur', 'input', function(){
  		    var dval = this.defaultValue;
  		    var cval = $(this).val();
  		    if (!cval){ 
  		      $(this).val(dval);	 
  		    }        	
  	    });	 	
  
  	    $('input[name="repeat"]').click(function () {
  	      if ($(this).is(':checked')){
            $('#eventRecurrence').recurrenceInput('show');
  	      } else {
  	        $('#eventRecurrence').recurrenceInput('rrule', null);  
            $('#eventRecurrence').recurrenceInput('hide');  
  	      }
  	    });
  	    
  	    $('#eventRecurrence').on("show", function () {
          $('input[name="repeat"]')
            .attr("checked", "checked")
            .prop("checked", "checked");
        });
  	    
  	    $('#eventRecurrence').on("hide", function () {
  	      if ($(this).recurrenceInput('rrule')) {
            $('input[name="repeat"]')
              .attr("checked", "checked")
              .prop("checked", "checked");
  	      } else {
            $('input[name="repeat"]')
              .removeAttr("checked")
              .prop("checked", false);
  	      }
  	    });
  	  },
  
  	  options: [
  //						{
  //						    caption: "Koko päivän tapahtuma",
  //							name : "whole day",
  //							type : "checkbox",
  //							action: function (e) {			
  //											}
  //						},
  //								
      ],
  	  buttons: [{
  	    caption: "Luo tapahtuma",
  	    name : "sendEvent",
  	    action: function (e) {
  	      var startDate = $('#startDate').datepicker('getDate');
  	      
  	      var startISO = getISODateTime(startDate, $('#startTime').timepicker('getTime'));
          var endISO = getISODateTime($('#endDate').datepicker('getDate'), $('#endTime').timepicker('getTime'));
          var recurrence = $('#eventRecurrence').recurrenceInput("rrule");
          
  	      mApi().calendar.calendars.events.create($('#eventCalendar').val(), {
  	        summary: $('input[name="eventSubject"]').val(),
  	        description: $('input[name="eventContent"]').val(),
  	        recurrence: recurrence,
  //	      location: calendarEvent.location,
  //	      latitude:calendarEvent.latitude,
  //	      longitude: calendarEvent.longitude,
  //	      url: calendarEvent.url,
  	        status: 'CONFIRMED',
  	        start: startISO,
  	        startTimeZone: 'GMT+0300',
  	        end: endISO ,
  	        endTimeZone: 'GMT+0300',
  	        allDay: false
  //	      attendees: attendees,
  //	      reminders: calendarEvent.reminders
          }).callback(function (err, result) {
            if (err) {
              $('.notification-queue').notificationQueue('notification', 'error', err);
              $('#startDate,#endDate').datepicker('destroy');
              $('#startTime,#endTime').timepicker('destroy');
              $('.md-background').fadeOut().remove();
            } else {
              window.location.reload(true);
            }
          });
  	    }
  	  }]
    });
  
    function getThisMoment() {
      var now = new Date();
      var weekdays;
      
      // TODO: IF-rakenne kullekin kielivaihtoehdolle
      weekdays = ["Sunnuntai", "Maanantai", "Tiistai", "Keskiviikko", "Torstai", "Perjantai", "Lauantai"];
      // weekdays = ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"];

      $(".ca-date-primary span").text(now.getDate() + "." + (now.getMonth()+1) + ".");
      $(".ca-date-secondary span").text(weekdays[now.getDay()]);
      // $(".ca-date-primary span").text(formatDate(now));
      
      $(".ca-date-day-time span").text(formatTime(now));
      setTimeout(getThisMoment, 1000);
    }
    
    $('#smallMonthCalendar').fullCalendar({
      firstDay: 1,
      header:{
        left: 'prev',
        center: 'title',
        right: 'next'
      },  
      titleFormat:{
        month: 'MMMM'
      },
      dayClick: function(date, turha1, turha2) {
        $("#smallMonthCalendar").fullCalendar("gotoDate", date);
        $("#fpWeekView").fullCalendar("gotoDate", date);
      }
    });    
    
      getThisMoment();
	});
  
  window.loadFullCalendarEvents = function (element) {
    var view = $(element).fullCalendar('getView');
    var calendarIds = $(element).attr('data-user-calendar-ids').split(',');
    var batchCalls = {};
    var viewStart = view.start;
    var viewEnd = view.end;
      
    var calls = $.map(calendarIds, function (calendarId) {
      return mApi().calendar.calendars.events.read(parseInt(calendarId), {
        timeMin: viewStart.toISOString(),
        timeMax: viewEnd.toISOString()
      });
    });
    
    mApi().batch(calls)
      .callback(function (err, results) {
        if (err) {
          $('.notification-queue').notificationQueue('notification', 'error', err);
        } else {
          var events = [];

          $.each(results, function (index, result) {
            events = $.merge(events, $.map(result, function (event) {
              if (event.recurrence) {
                var rule = new RRule($.extend(RRule.fromString(event.recurrence).origOptions, { dtstart: new Date(event.start) }));
                return $.map(rule.between(viewStart, viewEnd), function (date) {
                  return {
                    id: event.calendarId + ":" + event.id,
                    title: event.summary,
                    start: date,
                    end: new Date(date.getTime() + (event.end - event.start)),
                    allDay: event.allDay,
                    editable: false
                  };
                });
              } else {
                return {
                  id: event.calendarId + ":" + event.id,
                  title: event.summary,
                  start: new Date(event.start),
                  end: new Date(event.end),
                  allDay: event.allDay,
                  editable: false
                };
              }
            }));
          });
          
          $(element).fullCalendar('removeEvents');
          $(element).fullCalendar("addEventSource", {
            events: events
          });          
        }
      });
  };
  
})(this);