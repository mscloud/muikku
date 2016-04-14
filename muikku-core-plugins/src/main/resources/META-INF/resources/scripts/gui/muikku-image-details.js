(function() {
  'use strict';
  
  $.widget("custom.imageDetails", {
    
    _create : function() {
      this._details = null;
      
      $.each(['source', 'license', 'author'], $.proxy(function (index, type) {
        this._appendDetails(type, this.element.attr('data-' + type), this.element.attr('data-' + type + '-url'));
      }, this));
    },
    
    _appendDetails: function (type, text, url) {
      if (text || url) {
        if (!this._details) {
          var figure = this.element.closest('figure');
          if (!figure.length) {
            var figure = $('<figure>')
              .insertBefore(this.element);
            this.element.appendTo(figure);
          }
          
          figure.css('max-width', this.element.width())
          
          this._details = $('<details>')
            .insertAfter(this.element);
        }
        
        if (url) {
          $('<a>')
            .attr('src', url)
            .text(text||url)
            .appendTo(this._details);
        } else {
          $('<span>')
            .text(text||url)
            .appendTo(this._details);
        }
      }
    },
    
    _destroy: function () {
      
    }
  });
  
  
  
}).call(this);
