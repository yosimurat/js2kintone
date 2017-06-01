@Grab('org.codehaus.groovy.modules.http-builder:http-builder:0.7' )
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.ContentType
import groovyx.net.http.Method
import groovyx.net.http.RESTClient
import groovy.json.*

def config = new ConfigSlurper().parse(new File('kintoneCredential.groovy').toURL())

def subDomain = config.account.subDomain
def appId = config.account.appId
def apiToken = config.account.apiToken

def dateField = {id -> """
        jQuery("#${id}").datepicker({
            dateFormat: 'yy-mm-dd',
            minDate: 0, maxDate: "+3M",
            showButtonPanel: true
        });
"""
}
def dateFieldScript = ''

def jsHeader = """
(function(\$, config) {
    Js2kintone = {
        checkEnv: function() {
            var _ua = (function(){
                return {
                    ltIE6:typeof window.addEventListener == "undefined" && typeof document.documentElement.style.maxHeight == "undefined",
                    ltIE7:typeof window.addEventListener == "undefined" && typeof document.querySelectorAll == "undefined",
                    ltIE8:typeof window.addEventListener == "undefined" && typeof document.getElementsByClassName == "undefined",
                    ltIE9:document.uniqueID && typeof window.matchMedia == "undefined",
                    gtIE10:document.uniqueID && window.matchMedia,
                    Trident:document.uniqueID,
                    Gecko:'MozAppearance' in document.documentElement.style,
                    Presto:window.opera,
                    Blink:window.chrome,
                    Webkit:typeof window.chrome == "undefined" && 'WebkitAppearance' in document.documentElement.style,
                    Touch:typeof document.ontouchstart != "undefined",
                    Mobile:(typeof window.orientation != "undefined") || (navigator.userAgent.indexOf("Windows Phone") != -1),
                    ltAd4_4:typeof window.orientation != "undefined" && typeof(EventSource) == "undefined",
                    Pointer:window.navigator.pointerEnabled,
                    MSPoniter:window.navigator.msPointerEnabled
                };
            })();

            if(_ua.ltIE9){
                \$("#js2kintoneNonsupport").css("display", "block");
                \$('#js2kintoneRender').css("display", "none");
                return false;
            } else {
                return true;
            }
        }
        ,
        checkParams: function() {
            var errMsg = ' is required.';
            var requireConfigs = ['id', 'region', 'template', 'lambdaFunc', 'successMsg'];

            for (i = 0; i < requireConfigs.length; i++) {
                config[requireConfigs[i]] = requireConfigs[i] in config ? config[requireConfigs[i]] : alert(requireConfigs[i] + errMsg);
            }

            config.accessKey = base64.decode(config.id.split('%')[0]);
            config.secretKey = base64.decode(config.id.split('%')[1]);
        }
        ,
        callLambda : function() {
"""

def jsFooter = """
            AWS.config.update({
                accessKeyId: config.accessKey,
                secretAccessKey: config.secretKey,
                region: config.region
            });

            var context = base64.encode(JSON.stringify(json));
            console.log(JSON.stringify(json));

            var lambda = new AWS.Lambda();

            var params = {
                FunctionName: config.lambdaFunc,
                InvocationType: "RequestResponse",
                ClientContext: context
            };

            lambda.invoke( params,function(err,data) {
                if(err) {
                    console.log(err,err.stack);
                } else {
                    console.log(data);
                    if (data.StatusCode == 200) {
                        Js2kintone.successHandler();
                    }
                }
            });
        }
        ,
        successHandler: function() {
            \$("#kintoneFormSubmitSuccess").html(config['successMsg']);
            \$("#kintoneFormSubmitSuccess").css("display", "block");
            \$('#kintoneFormSubmit').hide();
        }
        

    };

    \$.ajax({
        url: config.template,
        dataType: 'text',
        success: function(contents) {
            var testTmp = \$.templates(contents);
            var html = testTmp.render();
            \$('#js2kintoneRender').append(html);
        }
    });
    
    \$(document).on("click", '#kintoneFormSubmit', function () {
        \$('#kintoneFormSubmit').attr("disabled", true);

        \$("#kintoneform").validate({
            errorPlacement: function (error, element) {
                \$(element).addClass('alert');
                \$(element).addClass('alert-danger');
                \$(element).removeClass('alert-success');
            },
            success: function (label, element) {
                \$(element).removeClass('alert');
                \$(element).removeClass('alert-danger');
                \$(element).addClass('alert');
                \$(element).addClass('alert-success');
            }
        });


        if (\$("#kintoneform").valid()) {
            Js2kintone.callLambda();
        } else {
            \$('#kintoneFormSubmit').attr("disabled", false);
        }

        return false;
    });

})(jQuery, js2kintoneConfig);
"""

def http = new HTTPBuilder("https://${subDomain}.cybozu.com")

http.request(Method.GET, ContentType.TEXT) {
  uri.path = "/k/v1/form.json"
  uri.query = [ app: appId ]
  headers.'X-Cybozu-API-Token' = apiToken

  response.success = { resp, reader ->
    if (resp.statusLine.statusCode == 200) {
      def text = reader.getText()
      def json = new JsonSlurper().parseText(text)
      def records = ""
      
      json.properties.each {
        if (records.length() > 0) {
          records <<= ",\n                 "
        }
        records <<= """ "${it.code}": encodeURIComponent(\$("#_kintoneform_${it.code}").val()) """

        switch (it.type) {
          case "DATE":
            dateFieldScript <<= dateField("_kintoneform_${it.code}")
            break
            
          default:
            break
        }

      }

      def template = """
        var json = {
            app: config['app'],
            record: {
                ${records}
            }
        };
"""
      def jsInit = """
jQuery.noConflict();

jQuery(window).load(function () {
    if (Js2kintone.checkEnv()) {
        Js2kintone.checkParams();
        ${dateFieldScript}
    } else {
//
    }
});
"""
      println jsInit
      println jsHeader
      println template
      println jsFooter
    }
  }
}
