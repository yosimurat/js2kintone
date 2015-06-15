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
        println template

    }
  }
}
