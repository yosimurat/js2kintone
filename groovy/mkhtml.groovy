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

def reqAlert = ' <span class="alert-danger">*</span>'
def required = 'required="required"'

def http = new HTTPBuilder("https://${subDomain}.cybozu.com")

http.request(Method.GET, ContentType.TEXT) {
  uri.path = "/k/v1/form.json"
  uri.query = [ app: appId ]
  headers.'X-Cybozu-API-Token' = apiToken

  response.success = { resp, reader ->
    if (resp.statusLine.statusCode == 200) {
      def text = reader.getText()
      def json = new JsonSlurper().parseText(text)
      json.properties.each {
        def code = it.code
        def label = it.label
        if (it.required == 'true') {
          reqAlert = ' <span class="alert-danger">*</span>'
          required = 'required="required"'
        } else {
          reqAlert = ''
          required = ''
        }
        // type switch
        def input = """<input type="text" class="form-control" id="_kintoneform_${code}" name="${code}" value="" maxlength="256" ${required} />"""
        if (it.type == 'MULTI_LINE_TEXT'){
          input = """<textarea class="form-control" id="_kintoneform_${code}" name="_kintoneform_${code}" ${required}></textarea>"""
        }

        def template = """
  <div class="form-group">
    <label for="_kintoneform_${code}">${label}${reqAlert}</label>
    ${input}
  </div>
"""
        println template
      }

    }
  }
}
