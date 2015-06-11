//$ = require('jquery');

(function(config) {
    
    config['mode'] = 'mode' in config ? config['mode'] : 'createTemplate';
    config['template'] = 'template' in config ? config['template'] : 'onTheFly';

    function ccc() {}
    ccc.prototype.foobar = function() {
        // foobar
    };


    var data = {
        name: 'ジャック'
    };
    $.get('./kintoneform.html', function(value) {
        console.log(value);
        var testTmp = $.templates(value);
        var html = testTmp.render(data);
        $('#result').append(html);
    });
    
})(js2kintoneConfig);
