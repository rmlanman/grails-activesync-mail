Grails ActiveSync Mail Plugin
======================

Grails plugin for sending mail to an Exchange ActiveSync provider.

This is an incredibly simple (read: not robust) grails plugin that was hacked together to support email for a customer who only exposed ActiveSync for e-mail (i.e. - no SMTP for Exchange). It is based heavily on pieces of the [Android Exchange code](https://android.googlesource.com/platform/packages/apps/Exchange/). All copyrights and attributions have been maintained with modifications noted.

Build
--------
Once you've checked out the plugin, install it to your local Maven repo

```
> grails maven-install
```

Use
--------
To use it, simply include it in the plugins section of BuildConfig.groovy as such:

```groovy
plugins {
  runtime ":grails-activesync-mail:0.2"
}
```

Before you can begin using it, you need to configure the client with connection information

Configure
---------

```groovy
grails.plugin.eas.server = 'https://outlook.office365.com'  //the server on which your target ActiveSync service is hosted
grails.plugin.eas.userDomain = 'FOO' //the Windows domain that the email user you will be using belongs to
grails.plugin.eas.username = 'bar' //the username of the email user you intend to send emails as
grails.plugin.eas.password = 'password' //the password of the email user you intend to send emails as
```

Limitations
----------
As stated before, this is a hack-job with not a whole lot of time invested in it. That being said, there are some pretty large limitations of this plugin:

- It only supports sending email
- As you can probably surmise from the above configuration, it only supports a single user as the sender at this point
- It has only been tested with Grails 2.4.3. I didn't do anything that _shouldn't_ be compatible with 2.3.x, but use at your own risk
- It only supports Exchange Active Sync version >= 14.0
