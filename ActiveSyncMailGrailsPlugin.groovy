import grails.plugin.eas.EASMailSender;

class ActiveSyncMailGrailsPlugin {
    // the plugin version
    def version = "0.1"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.3 > *"
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
        "grails-app/views/error.gsp"
    ]

    // TODO Fill in these fields
    def title = "Activesync Mail Plugin" // Headline display name of the plugin
    def author = "Your name"
    def authorEmail = ""
    def description = '''\
Brief summary/description of the plugin.
'''

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/grails-activesync-mail"
	
	ConfigObject easMailConfig

    def doWithSpring = {
        easMailConfig = application.config.grails.plugin.eas
		
		if (!easMailConfig.disabled || easMailConfig.disabled == 'false') {
			mailSender(EASMailSender) {
				easServer = easMailConfig.server ?: 'https://outlook.office365.com'
				domain = easMailConfig.userDomain ?: 'foo'
				username = easMailConfig.username ?: 'bar'
				password = easMailConfig.password ?: 'password'
			}
		}
    }
    
}
