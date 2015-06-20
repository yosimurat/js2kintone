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

def jsHeader = """
jQuery.noConflict();

(function(\$, config) {
    
    var errMsg = ' is required.';
    var requireConfigs = ['id', 'region', 'template', 'lambdaFunc', 'successMsg'];

    for (i = 0; i < requireConfigs.length; i++) {
        config[requireConfigs[i]] = requireConfigs[i] in config ? config[requireConfigs[i]] : alert(requireConfigs[i] + errMsg);
    }

    config.accessKey = base64.decode(config.id.split('%')[0]);
    config.secretKey = base64.decode(config.id.split('%')[1]);

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
        }
    })();
    if(_ua.ltIE9){
        console.log('non support');
    }

    var callLambda = function() {
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
                    successHandler();
                }
            }
        });
    };

    var successHandler = function() {
        \$("#kintoneFormSubmitSuccess").html(config['successMsg']);
        \$("#kintoneFormSubmitSuccess").css("display", "block");
        \$('#kintoneFormSubmit').hide();
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
            callLambda();
        } else {
            \$('#kintoneFormSubmit').attr("disabled", false);
        }

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
          records <<= ","
        }
        records <<= """ "${it.code}": encodeURIComponent(\$("#_kintoneform_${it.code}").val()) """
      }

      def template = """
        var json = {
            app: config['app'],
            record: {
                ${records}
            }
        };
"""
      println jsHeader
      println template
      println jsFooter
    }
  }
}
