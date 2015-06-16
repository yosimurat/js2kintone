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

def formHeader = """<form class="kintoneform" id="kintoneform">"""
def formFooter = """
  <div id="kintoneFormSubmitSuccess" style="display: none" class="alert alert-success" role="alert"></div>
  <div id="kintoneFormSubmitAlert" style="display: none" class="alert alert-danger" role="alert"></div>
  <button type="buton" id="kintoneFormSubmit" class="btn btn-primary">送信</button>
</form>
"""

def reqAlert = ' <span class="alert-danger">*</span>'
def required = 'required="required"'

println formHeader

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
        
        def input = ''
        switch (it.type) {
          case "MULTI_LINE_TEXT":
            input = """<textarea class="form-control" id="_kintoneform_${code}" name="_kintoneform_${code}" rows="3" ${required}></textarea>"""
            required = 'required'
            break
        
          case "DROP_DOWN":
            input = """<select class="form-control drop_down" id="_kintoneform_${code}" name="_kintoneform_${code}">"""
            it.options.each { optLabel ->
              input <<= """<option>${optLabel}</option>"""
            }
            input <<= '</select>'
            break
            
          default:
            input = """<input type="text" class="form-control" id="_kintoneform_${code}" name="${code}" value="" maxlength="256" ${required} />"""
            break
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

println formFooter
