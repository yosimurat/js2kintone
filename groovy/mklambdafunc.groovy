def config = new ConfigSlurper().parse(new File('kintoneCredential.groovy').toURL())

def subDomain = config.account.subDomain
def appId = config.account.appId
def apiToken = config.account.apiToken


def indexjs = """
var https = require('https');
 
exports.handler = function(event, context) {
    var record = {};
    for(var key in context.clientContext["record"]){
        record[key] = {"value": decodeURIComponent(context.clientContext["record"][key])};
    }
    
    var payload = {"app": ${appId}};
    payload["record"] = record;
    var json = JSON.stringify(payload);
    console.log(record);
    console.log(json);
    
    var options = {
        hostname: '${subDomain}.cybozu.com',
        port: 443,
        path: '/k/v1/record.json',
        method: 'POST',
        secureProtocol: 'SSLv3_method',
        headers: {
            'User-Agent': 'lambda2kintone/0.0.1',
            'Content-Type': 'application/json',
            'X-Cybozu-API-Token': '${apiToken}'
        }
    };

    var req = https.request(options, function(res) {
        console.log('STATUS: ' + res.statusCode);
        console.log('HEADERS: ' + JSON.stringify(res.headers));
        res.setEncoding('utf8');
        res.on('data', function (chunk) {
            console.log('BODY: ' + chunk);
            if (res.statusCode === 200) {
                context.succeed(chunk);
            }
        });
        
    });

    req.on('error', function(e) {
        console.log('problem with request: ' + e.message);
        context.fail(e.message);
    });

    req.write(json);
    req.end();
};
"""

println indexjs
