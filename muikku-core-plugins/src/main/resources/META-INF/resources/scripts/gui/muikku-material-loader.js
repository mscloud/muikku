(function() {

  'use strict';
  
  $.widget("custom.muikkuMaterialLoader", {
    options : {
      baseUrl: null,
      workspaceEntityId: -1,
      loadAnswers: false,
      readOnlyFields: false,
      dustTemplate: 'workspace/materials-page.dust',
      prependTitle : true,
      renderMode: {
        "html": "raw"
      },
      defaultRenderMode: 'dust'
    },
    
    _create : function() {
    },    
    
    _getRenderMode: function(type) {
      return this.options.renderMode[type]||this.options.defaultRenderMode;
    },

    _loadHtmlMaterial: function(pageElement, fieldAnswers) {
      var workspaceMaterialId = $(pageElement).attr('data-workspace-material-id');
      var materialId = $(pageElement).attr('data-material-id');
      var parentIds = []; // TODO needed anymore?

      try {
        var material = {
          title: $(pageElement).attr('data-material-title'),
          html: $(pageElement).attr('data-material-content'),
          currentRevision: $(pageElement).attr('data-material-current-revision'),
          publishedRevision: $(pageElement).attr('data-material-published-revision'),
          path: $(pageElement).attr('data-path')
        };
        
        $(pageElement).removeAttr('data-material-content');

        var parsed = $('<div>');
        if (material.title && this.options.prependTitle) {
          parsed.append('<h2>' + material.title + '</h2>');
        }
        
        if (material.html) {
          parsed.append(material.html);
        }
        
        // Convert relative urls to point to correct url
        
        parsed.find('a,div.lazy-pdf,img,embed,iframe,object,source').each($.proxy(function (index, element) {
          var urlAttribute = '';
          
          switch (element.tagName.toLowerCase()) {
            case 'a':
              var isAnchorSource = $(element).attr('href') && $(element).attr('href').indexOf('#') == 0;
              var isAnchorTarget = !$(element).attr('href') && $(element).attr('name');
              if (isAnchorSource) {
                $(element).attr('href', '#' + workspaceMaterialId + '-' + $(element).attr('href').substring(1));
              }
              else if (isAnchorTarget) {
                var name = $(element).attr('name'); 
                var hasId = $(element).attr('id') == name;  
                $(element).attr('name', workspaceMaterialId + '-' + name);
                if (hasId) {
                  $(element).attr('id', workspaceMaterialId + '-' + name);
                }
              }
              else {
                urlAttribute = 'href';
              }
            break;
            case 'div':
              urlAttribute = $(element).hasClass('lazy-pdf') ? 'data-url' : null;
            break;
            case 'img':
              urlAttribute = $(element).hasClass('lazy') ? 'data-original' : 'src';
            break;
            case 'embed':
            case 'iframe':
            case 'source':
              urlAttribute = 'src';
            break;
            case 'object':
              urlAttribute = 'data';
            break;
          }
          
          var src = urlAttribute ? $(element).attr(urlAttribute) : null;
          if (src) {
            var absolute = (src.indexOf('/') == 0) || (src.indexOf('data:') == 0) || (src.match("^(?:[a-zA-Z]+:)?\/\/"));
            if (!absolute) {
              if (src.lastIndexOf('/') == src.length - 1) {
                $(element).attr(urlAttribute, this.options.baseUrl + '/' + material.path + src);
              } else {
                $(element).attr(urlAttribute, this.options.baseUrl + '/' + material.path + '/' + src);
              }
            }
          }
        }, this));
        
        $(document).trigger('beforeHtmlMaterialRender', {
          pageElement: pageElement,
          parentIds: parentIds,
          workspaceMaterialId: workspaceMaterialId,
          materialId: materialId,
          element: parsed,
          fieldAnswers: fieldAnswers,
          readOnlyFields: this.options.readOnlyFields
        });
        
        if (this._getRenderMode('html') == 'dust') {
          material.html = parsed.html();
          renderDustTemplate(this.options.dustTemplate, { id: materialId, materialId: materialId, workspaceMaterialId: workspaceMaterialId, type: 'html', data: material, hidden: pageElement.hasClass('item-hidden') }, $.proxy(function (text) {
            $(pageElement).append(text);
            $(document).trigger('afterHtmlMaterialRender', {
              pageElement: pageElement,
              parentIds: parentIds,
              workspaceMaterialId: workspaceMaterialId,
              materialId: materialId,
              fieldAnswers: fieldAnswers,
              readOnlyFields: this.options.readOnlyFields
            });
            $.waypoints('refresh');
          }, this));
        }
        else {
          $(pageElement).append(parsed);
          $(document).trigger('afterHtmlMaterialRender', {
            pageElement: pageElement,
            parentIds: parentIds,
            workspaceMaterialId: workspaceMaterialId,
            materialId: materialId,
            fieldAnswers: fieldAnswers,
            readOnlyFields: this.options.readOnlyFields
          });
        }

      } catch (e) {
        $('.notification-queue').notificationQueue('notification', 'error', getLocaleText("plugin.workspace.materialsLoader.htmlMaterialReadingFailed", e));
      }
    },
    
    loadMaterial: function(page, fieldAnswers) {
      var workspaceMaterialId = $(page).attr('data-workspace-material-id');
      var materialId = $(page).attr('data-material-id');
      var materialType = $(page).attr('data-material-type');
      var materialTitle = $(page).attr('data-material-title');
      switch (materialType) {
        case 'html':
          this._loadHtmlMaterial($(page), fieldAnswers);
        break;
        case 'folder':
          renderDustTemplate(this.options.dustTemplate, { id: workspaceMaterialId, workspaceMaterialId: workspaceMaterialId, type: materialType, hidden: $(page).hasClass('item-hidden'), data: { title: $(page).attr('data-material-title') } }, $.proxy(function (text) {
            $(this).html(text);
            $.waypoints('refresh');
          }, page));
        break;
        default:
          var typeEndpoint = mApi({async: false}).materials[materialType];
          if (typeEndpoint != null) {
            typeEndpoint.read(materialId).callback($.proxy(function (err, result) {
              var binaryType = 'unknown';
              if (materialType == 'binary') {
                if (result.contentType.indexOf('image/') != -1) {
                  binaryType = 'image';  
                } else {
                  switch (result.contentType) {
                    case "application/pdf":
                    case "application/x-pdf":
                    case "application/vnd.pdf":
                    case "text/pdf":
                      binaryType = 'pdf';  
                    break;
                    case "application/x-shockwave-flash":
                      binaryType = 'flash';  
                    break;
                  }
                }
              }
              $(page).attr('data-binary-type', binaryType);

              renderDustTemplate(this.options.dustTemplate, { 
                workspaceMaterialId: workspaceMaterialId,
                materialId: materialId,
                id: materialId,
                title: materialTitle,
                type: materialType,
                binaryType: binaryType,
                data: result
              }, $.proxy(function (text) {
                $(this).html(text);
                // PDF and SWF lazy loading 
                $(this).find('.lazyPdf').lazyPdf();
                $(this).find('.lazySwf').lazySwf();
                $.waypoints('refresh');
              }, page));
            }, this));
          }
        break;
      }
    },
    
    loadMaterials: function(pageElements, fieldAnswers) {
      if (this.options.loadAnswers === true) {
        mApi({async: false}).workspace.workspaces.compositeReplies.read(this.options.workspaceEntityId).callback($.proxy(function (err, replies) {
          if (err) {
            $('.notification-queue').notificationQueue('notification', 'error', getLocaleText("plugin.workspace.materialsLoader.answerLoadingFailed", err));
          } else {
            // answers array
            var fieldAnswers = {};
            
            $.each(replies||[], $.proxy(function (index, reply) {
              if (reply && reply.answers.length) {
                for (var i = 0, l = reply.answers.length; i < l; i++) {
                  var answer = reply.answers[i];
                  var answerKey = [answer.materialId, answer.embedId, answer.fieldName].join('.');
                  fieldAnswers[answerKey] = answer.value;
                }
                
                if (reply.state || reply.workspaceMaterialReplyId) {
                  var page = $(pageElements).filter('*[data-workspace-material-id="' + reply.workspaceMaterialId + '"]');
                  
                  if (reply.state) { 
                    page.attr('data-workspace-material-state', reply.state);
                  }

                  if (reply.workspaceMaterialReplyId) { 
                    page.attr('data-workspace-reply-id', reply.workspaceMaterialReplyId);
                  }
                }
              }
            }, this));

            // actual loading of pages
            $(pageElements).each($.proxy(function (index, page) {
              this.loadMaterial(page, fieldAnswers);
              $(page).muikkuMaterialPage({
                workspaceEntityId: this.options.workspaceEntityId
              });
            }, this));
          }       
        }, this));
      }
      else {
        $(pageElements).each($.proxy(function (index, page) {
          this.loadMaterial(page, fieldAnswers||{});
        }, this));
      }
    }
  }); // material loader widget
  
  $(document).on('taskFieldDiscovered', function (event, data) {
    var object = data.object;
    if ($(object).attr('type') == 'application/vnd.muikku.field.text') {
      var taskfieldWrapper = $('<span>')
        .addClass('textfield-wrapper');
      var input = $('<input>')
        .addClass('muikku-text-field')
        .attr({
          'type': "text",
          'size':data.meta.columns,
          'placeholder': data.meta.hint,
          'name': data.name
        })
        .val(data.value)
        .muikkuField({
          fieldName: data.name,
          materialId: data.materialId,
          embedId: data.embedId,
          meta: data.meta,
          readonly: data.readOnlyFields||false,
          trackChange: false,
          isReadonly: function () {
            return $(this.element).attr('disabled') == 'disabled' || $(this.element).attr('readonly') == 'readonly';
          },
          setReadonly: function (readonly) {
            if (readonly) {
              $(this.element).attr('readonly', 'readonly')
            } else {
              $(this.element).removeAttr('readonly');
            } 
          },
          hasExamples: function () {
            var meta = this.options.meta;
            if (meta.rightAnswers && meta.rightAnswers.length > 0) {
              for (var i = 0, l = meta.rightAnswers.length; i < l; i++) {
                if (meta.rightAnswers[i].correct === true) {
                  return false;
                }
              }
              
              return true;
            }
            
            return false;
          },
          getCorrectAnswers: function() {
            var result = [];
            var meta = this.options.meta;
            if (meta.rightAnswers && meta.rightAnswers.length > 0) {
              for (var i = 0, l = meta.rightAnswers.length; i < l; i++) {
                if (meta.rightAnswers[i].correct) {
                  result.push(meta.rightAnswers[i].text);
                }
              }
            }
            return result;
          },
          getExamples: function () {
            var result = [];
            
            var meta = this.options.meta;
            if (meta.rightAnswers && meta.rightAnswers.length > 0) {
              for (var i = 0, l = meta.rightAnswers.length; i < l; i++) {
                result.push(meta.rightAnswers[i].text);
              }
            }
            
            return result;
          },
          hasDisplayableAnswers: function() {
            return this.options.meta.rightAnswers && this.options.meta.rightAnswers.length > 0; 
          },
          canCheckAnswer: function() {
            var meta = this.options.meta;
            if (meta.rightAnswers) {
              for (var i = 0, l = meta.rightAnswers.length; i < l; i++) {
                if (meta.rightAnswers[i].correct === true) {
                  return true;
                }
              }
            }
            
            return false;
          },
          isCorrectAnswer: function() {
            var meta = this.options.meta;
            
            var meta = this.options.meta;
            if (meta.rightAnswers) {
              for (var i = 0, l = meta.rightAnswers.length; i < l; i++) {
                if (meta.rightAnswers[i].correct === true) {
                  var answer = this.answer()||'';
                  var text = meta.rightAnswers[i].text||'';
                  
                  if (!meta.rightAnswers[i].caseSensitive) {
                    answer = answer.toLowerCase();
                    text = text.toLowerCase();
                  }
                  
                  if (meta.rightAnswers[i].normalizeWhitespace) {
                    answer = answer.trim();
                    text = text.trim();
                  }
                  
                  if (text == answer) {
                    return true;
                  }
                }
              }
            }
            
            return false; 
          }
        });  
      
      taskfieldWrapper.append(input);
      
      if (data.meta.hint != '') {
        taskfieldWrapper.append($('<span class="muikku-text-field-hint">' + data.meta.hint + '</span>'));  
        
        $(input).on("focus", function() {
          $(taskfieldWrapper).children('.muikku-text-field-hint')
            .css({
              visibility:"visible",
              top: input.outerHeight() + 4 + 'px'
            })
            .animate({
              opacity:1
              
            }, 300, function() {
            
            })
        });
        
        $(input).on("blur", function() {
          $(taskfieldWrapper).children('.muikku-text-field-hint')
            .css({
              visibility:"hidden"  
            })
            .animate({
              opacity:0
              
            }, 150, function() {
            
            })
        });
      }
      $(object).replaceWith(taskfieldWrapper);      
    }
  });
  
  $(document).on('taskFieldDiscovered', function (event, data) {
    var object = data.object;
    if ($(object).attr('type') == 'application/vnd.muikku.field.memo') {
      var field = $('<textarea>')
        .addClass('muikku-memo-field')
        .attr({
          'cols':data.meta.columns,
          'rows':data.meta.rows,
          'placeholder': data.meta.help,
          'title': data.meta.hint,
          'name': data.name
        })
        .val(data.value)
        .muikkuField({
          meta: data.meta,
          fieldName: data.name,
          materialId: data.materialId,
          embedId: data.embedId,
          readonly: data.readOnlyFields||false,
          trackChange: data.meta.richedit,
          trackKeyUp: !!!data.meta.richedit,
          isReadonly: function () {
            return $(this.element).attr('disabled') == 'disabled' || $(this.element).attr('readonly') == 'readonly';
          },
          setReadonly: function (readonly) {
            if (readonly) {
              $(this.element).attr('readonly', 'readonly')
            } else {
              $(this.element).removeAttr('readonly');
            } 
          },
          canCheckAnswer: function() {
            return false;
          },
          hasDisplayableAnswers: function() {
            return this.hasExamples();
          },
          hasExamples: function () {
            var meta = this.options.meta;
            return meta.example && meta.example != '';
          },
          getCorrectAnswers: function() {
            return [];
          },
          getExamples: function () {
            var meta = this.options.meta;
            if (meta.example) {
              return [meta.example];
            } else {
              return [];
            }
          }
        });
      if (data.meta.richedit == true) {
        field.addClass('ckeditor-field');
      }
      $(object).replaceWith(field);
    }
  });
  
  $(document).on('taskFieldDiscovered', function (event, data) {
    
    function concatText(text, length){
      if(text.length > length){
        return text.substring(0, length)+'...';
      }else{
        return text;
      }
    }
    
    function shuffleArray(array) {
      for (var i = array.length - 1; i > 0; i--) {
          var j = Math.floor(Math.random() * (i + 1));
          var temp = array[i];
          array[i] = array[j];
          array[j] = temp;
      }
      return array;
    }
    
    function organizeFieldsByAnswers(terms, counterparts, answers){
      var organizedTerms = [];
      var organizedCounterparts = [];
      for (var answer in answers) {
        if (answers.hasOwnProperty(answer)) {
          for(var i = 0, j = terms.length; i < j;i++){
            if(terms[i].name == answer){
              organizedTerms.push(terms[i]);
              terms.splice(i, 1);
              break;
            }
          }
          
          for(var n = 0, l = counterparts.length; n < l;n++){
            if(counterparts[n].name == answers[answer]){
              organizedCounterparts.push(counterparts[n]);
              counterparts.splice(n, 1);
              break;
            }
          }
        }
      }
      
      terms = shuffleArray(terms);
      counterparts = shuffleArray(counterparts);
      
      return {
        terms: organizedTerms.concat(terms),
        counterparts: organizedCounterparts.concat(counterparts)
      };
      
    };

    var object = data.object;
    if ($(object).attr('type') == 'application/vnd.muikku.field.connect') {
      var meta = data.meta;
      var values = data.value ? $.parseJSON(data.value) : {};
      if(!$.isEmptyObject(values)){
        var organizedFields = organizeFieldsByAnswers(meta.fields, meta.counterparts, values);
        meta.fields = organizedFields.terms;
        meta.counterparts = organizedFields.counterparts
      }else{
        meta.fields = shuffleArray(meta.fields);
        meta.counterparts = shuffleArray(meta.counterparts);
      }   
      
      var tBody = $('<tbody>');
      
      var field = $('<table>')
        .addClass('muikku-connect-field-table')
        .attr({
          'data-field-name': meta.name,
          'data-material-id': data.materialId,
          'data-embed-id': data.embedId,
          'data-meta': JSON.stringify(data.meta)
        });
      
      var fieldsSize = meta.fields.length;
      var counterpartsSize = meta.counterparts.length;
      var rowCount = Math.max(fieldsSize, counterpartsSize);
      
      for (var rowIndex = 0; rowIndex < rowCount; rowIndex++) {
        var tdTermElement = $("<td>").addClass('muikku-connect-field-term-cell');
        var tdValueElement = $("<td>").addClass('muikku-connect-field-value-cell');
        var tdCounterpartElement = $("<td>").addClass("muikku-connect-field-counterpart-cell");
        var inputElement = $("<input>") 
          .addClass('muikku-connect-field-value') 
          .attr({
            'type': 'text'
          });
        
        var connectFieldTermMeta = rowIndex < fieldsSize ? meta.fields[rowIndex] : null;
        var connectFieldCounterpartMeta = rowIndex < counterpartsSize ? meta.counterparts[rowIndex] : null;
        
        if (connectFieldTermMeta != null) {
          inputElement
            .attr({
              'name': connectFieldTermMeta.name
            })
            .val(values[connectFieldTermMeta.name]);
          
          tdTermElement.text(concatText(connectFieldTermMeta.text, 50));
          tdTermElement.attr('title', connectFieldTermMeta.text);
          tdTermElement.attr('data-muikku-connect-field-option-name', connectFieldTermMeta.name);
          tdValueElement.append(inputElement);
        }
        
        if (connectFieldCounterpartMeta != null) {
          tdCounterpartElement.text(concatText(connectFieldCounterpartMeta.text, 50));
          tdCounterpartElement.attr('title', connectFieldCounterpartMeta.text);
          tdCounterpartElement.attr('data-muikku-connect-field-option-name', connectFieldCounterpartMeta.name);
        }
      
        tBody
          .append($('<tr>')
            .append(tdTermElement)
            .append(tdValueElement)
            .append(tdCounterpartElement));
      }
      
      field.append(tBody);
      
      $(object).replaceWith(field);
      
      // TODO: data.meta.help, data.meta.hint
    }
  });
  
  $(document).on('taskFieldDiscovered', function (event, data) {
    var object = data.object;
    if ($(object).attr('type') == 'application/vnd.muikku.field.select') {
      var meta = data.meta;
      switch (meta.listType) {
        case 'list':
        case 'dropdown':
          var input = $('<select>')
            .addClass('muikku-select-field')
            .attr({
              name: data.name
            });
          
          // Empty option to be able to clear an answer (unless such exists in options already)
          if (meta.listType == 'dropdown') {
            var hasEmpty = false;
            for (var i = 0, l = meta.options.length; i < l; i++) {
              hasEmpty = (meta.options[i].text == '' || meta.options[i].text == '-');
              if (hasEmpty)
                break;
            }
            if (!hasEmpty)
              input.append($('<option>'));
          }
          else {
            input.attr('size', meta.options.length);
          }
          
          for(var i = 0, l = meta.options.length; i < l; i++){
            var option = $('<option>')
            .attr({
              'value': meta.options[i].name
            });
            option.text(meta.options[i].text);
            input.append(option);
          }      
          
          input
            .val(data.value)
            .muikkuField({
              fieldName: data.name,
              materialId: data.materialId,
              embedId: data.embedId,
              meta: meta,
              readonly: data.readOnlyFields||false,
              hasDisplayableAnswers: function() {
                return this.canCheckAnswer(); 
              },
              canCheckAnswer: function() {
                for (var i = 0, l = meta.options.length; i < l; i++) {
                  if (meta.options[i].correct == true) {
                    return true;
                  }
                }
                return false;
              },
              isCorrectAnswer: function() {
                var answer = this.answer();
                for (var i = 0, l = meta.options.length; i < l; i++) {
                  if (meta.options[i].correct == true && meta.options[i].name == answer) {
                    return true;
                  }
                }
                return false; 
              },
              getCorrectAnswers: function() {
                var result = [];
                for (var i = 0, l = meta.options.length; i < l; i++) {
                  if (meta.options[i].correct == true) {
                    result.push(meta.options[i].text);
                  }
                }
                return result;
              }
            });
          
          $(object).replaceWith(input);
        break;
        case 'radio-horizontal':
        case 'radio-vertical':
          var idPrefix = [data.materialId, data.embedId, data.name].join(':');
          var container = $('<span>').addClass('muikku-select-field');
          if (meta.listType == 'radio-horizontal') {
            container.addClass('radiobutton-horizontal');
          } else {
            container.addClass('radiobutton-vertical');
          }
          for (var i = 0, l = meta.options.length; i < l; i++){
            var label = $('<label>')
              .attr({
                'for': [idPrefix, meta.options[i].name].join(':')
              });
            label.text(meta.options[i].text);
            var radio = $('<input>')
              .attr({
                id: [idPrefix, meta.options[i].name].join(':'),
                name: data.name,
                type: 'radio',
                value: meta.options[i].name
            });
            if (data.value == meta.options[i].name) {
              radio.attr({
                checked: 'checked'
              });
            }
            var optionContainer = $('<span>');
            container.append(optionContainer);
            optionContainer.append(radio);
            optionContainer.append(label);
          }      
          container.muikkuField({
            fieldName: data.name,
            materialId: data.materialId,
            embedId: data.embedId,
            meta: meta,
            readonly: data.readOnlyFields||false,
            isReadonly: function () {
              return $(this.element).attr('data-disabled') == 'true';
            },
            setReadonly: function (readonly) {
              if (readonly) {
                $(this.element)
                  .attr('data-disabled', 'true') 
                  .find('input[type="radio"]').attr('disabled', 'disabled');
              } else {
                $(this.element)
                  .removeAttr('data-disabled') 
                  .find('input[type="radio"]').removeAttr('disabled');
              }
            },
            answer: function(val) {
              if (val) {
                $(this.element).find('input').prop('checked', false);
                $(this.element).find('input[value="' + val + '"]').prop('checked', true);
              } else {
                return $(this.element).find('input:checked').val();
              }
            },
            hasDisplayableAnswers: function() {
              return this.canCheckAnswer(); 
            },
            canCheckAnswer: function() {
              for (var i = 0, l = meta.options.length; i < l; i++) {
                if (meta.options[i].correct == true) {
                  return true;
                }
              }
              return false;
            },
            isCorrectAnswer: function() {
              var answer = this.answer();
              for (var i = 0, l = meta.options.length; i < l; i++) {
                if (meta.options[i].correct == true && meta.options[i].name == answer) {
                  return true;
                }
              }
              return false; 
            },
            getCorrectAnswers: function() {
              var result = [];
              for (var i = 0, l = meta.options.length; i < l; i++) {
                if (meta.options[i].correct == true) {
                  result.push(meta.options[i].text);
                }
              }
              return result;
            }
          });
          $(object).replaceWith(container);
        break;
      }
    }
  });

  $(document).on('taskFieldDiscovered', function (event, data) {
    var object = data.object;
    if ($(object).attr('type') == 'application/vnd.muikku.field.multiselect') {

      var meta = data.meta;
      var idPrefix = [data.materialId, data.embedId, data.name].join(':');
      var container = $('<span>').addClass('muikku-checkbox-field');
      if (meta.listType == 'checkbox-horizontal') {
        container.addClass('checkbox-horizontal');
      } else {
        container.addClass('checkbox-vertical');
      }
      for (var i = 0, l = meta.options.length; i < l; i++){
        var label = $('<label>')
          .attr({
            'for': [idPrefix, meta.options[i].name].join(':')
          });
        label.text(meta.options[i].text);
        var values = data.value ? $.parseJSON(data.value) : [];
        var checkbox = $('<input>')
          .attr({
            id: [idPrefix, meta.options[i].name].join(':'),
            name: data.name,
            type: 'checkbox',
            value: meta.options[i].name
        });
        if ($.inArray(meta.options[i].name, values) > -1) {
          checkbox.attr({
            checked: 'checked'
          });
        }
        var optionContainer = $('<span>');
        container.append(optionContainer);
        optionContainer.append(checkbox);
        optionContainer.append(label);
      }      
      container.muikkuField({
        fieldName: data.name,
        materialId: data.materialId,
        embedId: data.embedId,
        meta: meta,
        readonly: data.readOnlyFields||false,
        isReadonly: function () {
          return $(this.element).attr('data-disabled') == 'true';
        },
        setReadonly: function (readonly) {
          if (readonly) {
            $(this.element)
              .attr('data-disabled', 'true') 
              .find('input[type="checkbox"]').attr('disabled', 'disabled');
          } else {
            $(this.element)
              .removeAttr('data-disabled') 
              .find('input[type="checkbox"]').removeAttr('disabled');
          }
        },
        answer: function(val) {
          if (val) {
            $(this.element).find('input').prop('checked', false);
            $.each($.isArray(val) ? val : $.parseJSON(val), $.proxy(function (index, value) {
              $(this.element).find('input[value="' + value + '"]').prop('checked', true);
            }, this));
            
          } else {
            var values = [];
            $(this.element).find('input:checked').each(function() {
              values.push($(this).val());
            });

            return JSON.stringify(values); 
          }
        },
        hasDisplayableAnswers: function() {
          return this.canCheckAnswer(); 
        },
        canCheckAnswer: function() {
          for (var i = 0, l = meta.options.length; i < l; i++) {
            if (meta.options[i].correct == true) {
              return true;
            }
          }
          return false;
        },
        isCorrectAnswer: function() {
          var selectedValues = [];
          $(this.element).find('input:checked').each(function() {
            selectedValues.push($(this).val());
          });
          var correctValues = [];
          for (var i = 0, l = meta.options.length; i < l; i++) {
            if (meta.options[i].correct == true) {
              correctValues.push(meta.options[i].name);
            }
          }
          if (selectedValues.length != correctValues.length) {
            return false;
          }
          for (var i = 0; i < selectedValues.length; i++) {
            if (correctValues.indexOf(selectedValues[i]) == -1)
              return false;
          }
          return true;
        },
        getCorrectAnswers: function() {
          var result = [];
          for (var i = 0, l = meta.options.length; i < l; i++) {
            if (meta.options[i].correct == true) {
              result.push(meta.options[i].text);
            }
          }
          return result;
        }
      });
      $(object).replaceWith(container);
    }
  });
  
  $(document).on('taskFieldDiscovered', function (event, data) {
    var object = data.object;
    if ($(object).attr('type') == 'application/vnd.muikku.field.file') {
      
      var input = $('<input>')
        .addClass('muikku-file-field')
        .attr({
          'type': "file",
          'placeholder': data.meta.help,
          'title': data.meta.hint,
          'name': data.name,
          'data-readonly': $(object).attr('data-readonly')
        })
        .data({
          'field-name': data.name,
          'material-id': data.materialId,
          'embed-id': data.embedId
        });   
      
      var files = data.value ? $.parseJSON(data.value) : [];
      for (var i = 0, l = files.length; i < l; i++) {
        var file = files[i];
        $(input).data('file-' + i + '.file-id', file.originalId);
        $(input).data('file-' + i + '.filename', file.name);
        $(input).data('file-' + i + '.content-type', file.contentType);
      }
      
      $(input).data({
        'file-count': files.length
      });
      
      $(object).replaceWith(input);
    }
  });
  
  function createEmbedId(parentIds) {
    return (parentIds.length ? parentIds.join(':') : null);
  }
  
  $(document).on('beforeHtmlMaterialRender', function (event, data) {
    $(data.element).find('object[type*="vnd.muikku.field"]').each(function (index, object) {
      var meta = $.parseJSON($(object).find('param[name="content"]').attr('value'));
      var embedId = createEmbedId(data.parentIds);
      var materialId = data.materialId;
      var valueKey = [materialId, embedId, meta.name].join('.');
      var value = data.fieldAnswers ? data.fieldAnswers[valueKey] : '';
      
      $(document).trigger('taskFieldDiscovered', {
        pageElement: data.pageElement,
        object: object,
        meta: meta,
        name: meta.name,
        embedId: embedId,
        materialId: materialId,
        value: value,
        readOnlyFields: data.readOnlyFields
      });
    });
  });
  
  $(document).on('afterHtmlMaterialRender', function (event, data) {
    
    /* If last element inside article is floating this prevents mentioned element from overlapping its parent container */
    $(data.pageElement)
      .append($('<div>').addClass('clear'));
    
    $(data.pageElement).find('.muikku-connect-field-table').each(function (index, field) {
      var meta = $.parseJSON($(field).attr('data-meta'));
      $(field).muikkuConnectField({
        fieldName: $(field).data('field-name'),
        embedId: $(field).data('embed-id'),
        materialId: $(field).data('material-id'),
        meta: meta,
        readonly: data.readOnlyFields||false
      });
    });
    
    if (jQuery().magnificPopup) {
      // Lazy loading with magnific popup
      $(data.pageElement).find('img.lazy').each(function (index, img) {
        if ($(img).closest('a').length == 0) {
          var src = $(img).attr('data-original');
          var a = $('<a>')
            .attr('href', src)
            .magnificPopup({
              type: 'image'
            })
            .insertBefore(img);
          
          $(img)
            .appendTo(a)
            .lazyload();
        }
        else {
          $(img).lazyload();
        }
      });        
    } else {
      // Lazy loading
     $(data.pageElement).find('img.lazy').lazyload();
    }
    
    if (jQuery().imageDetails) {
      $(data.pageElement).find('img')
        .imageDetails();
    }
        
    $(data.pageElement).find('.js-lazyyt').lazyYT();
    
    $(data.pageElement).find('.ckeditor-field').muikkuRichMemoField();
    
    /* Add autoGrow to textfield */
    if (jQuery().autoGrowInput) {
      $(data.pageElement)
        .find('.muikku-text-field')
        .each(function() {
          $(this)
            .autoGrowInput({
              minWidth: $(this).width(),
              maxWidth: function() { 
                 return $(data.pageElement).width()-40; 
              },
              comfortZone:0
            });
        });
    }

    /* Add autosize to textarea */
    if ((typeof autosize) == 'function') {
      autosize($('textarea'));
    }
    
    if ((typeof MathJax) != 'undefined') {
      MathJax.Hub.Queue(["Typeset",MathJax.Hub,$(data.pageElement)[0]]);
    }

    var maxFileSize = null;
    if ($("input[name='max-file-size']").length) {
      maxFileSize = Number($("input[name='max-file-size']").val());
    }
    
    renderDustTemplate('workspace/materials-assignment-attachement-remove-confirm.dust', { }, $.proxy(function (text) {
      // File field support
      $(data.pageElement).find('.muikku-file-field').each(function (index, field) {
        var readonlyData = $(field).attr('data-readonly');
        var readonly = data.readOnlyFields || ('true' === readonlyData);
        $(field)
          .muikkuFileField({
            maxFileSize: maxFileSize,
            confirmRemove: true,
            confirmRemoveHtml: text,
            supportRestore: false,
            confirmRemoveDialogClass: "workspace-materials-assigment-attachment-dialog",
          })
          .muikkuField({
            fieldName: $(field).data('field-name'),
            embedId: $(field).data('embed-id'),
            materialId: $(field).data('material-id'),
            readonly: readonly,
            answer: function (val) {
              // TODO: Support setter for files
              return JSON.stringify($(this.element).muikkuFileField('files'));
            },
            isReadonly: function () {
              return $(this.element).muikkuFileField("isReadonly");
            },
            setReadonly: function (readonly) {
              $(this.element).muikkuFileField("setReadonly", readonly);
            }
          });
      });
      
    }, this));
    
  });

}).call(this);
