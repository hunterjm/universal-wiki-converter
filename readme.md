# Universal Wiki Converter

### About this Fork

This fork was created 100% to convert a MindTouch wiki to Confluence.  All work that has been done has been done for MindTouch.  If you have a different Wiki, you may want to look somewhere else.  If you have MindTouch this will save you weeks of work.

#### Simple Setup

Below is the easiest way to get started and see if it works.  It's easier this way than to try digging through the additional documentation linked below.

* Modify conf/confluenceSettings.properties and add in your confluence settings
* Open conf/exporter.mindtouch.properties and be sure to set the following:
  * url.base: The URL to your MindTouch Wiki
  * user: Your MindTouch administrator username
  * pass: Your MindTouch administrator password
  * output.dir: You might want to set this to an absolute path
  * timeout: This is the HTTP connection timeout
  * socketTimeout: How long we will wait for a response
    * -1 uses HttpClient default, and skips the page on timeout

#### Socket Timeout & Retry functionality

Set this if you, like me, keep getting random 502's from the MindTouch API. Setting this to a positive number will also enable API retry functionality to pull down the page. The retry functionality will keep adding the time defined here to HttpClient socketTimeout whenever it recieves a SocketTimeoutException, and retry.

The sweet spot for me was 350ms to keep it fast.  Obviously some large pages took longer and thus two requests (second one at 700ms, etc.), but when I was getting 502's it took 1 minute to timeout when a quick retry would pull the page back down just fine.


## Original Documentation

To build the UWC use [ANT]:
* cd devel (the devel dir.)
* ant      (the default target will build the UWC under target/uwc/)

*Note: you do not need to build the UWC to run it; only if you're doing development work with it.*

To run the newly built UWC
* cd target/uwc/
* chmod a\+x \*sh
* ./run_uwc_devel.sh

More details and documentation is [here]

ABOUT THE UWC

This code is open source and is up to date with Atlassian's latest storage format of Atlassian Confluence
(introduced in Confluence 4). We successfully use/run the UWC for Confluence 5.X releases, however, there are *many* flavors and versions of MIGRATE_FROM wikis. 

As such, we feel it is accurate to say that this is "a tool", yet not always the end-to-end solution or silver bullet. Wiki formats are varied, and so please understand that the UWC will get you further along, but there may be post-processing, additional scripting, username database merging, or other things required to assist in the process. 

Please refer to the Wiki Migration Checklist (http://www.appfusions.com/display/Dashboard/Wiki+Migration+Checklist) to educate yourself on what is invoived in a migration. The checklist is not to suggest that all content elements are problematic. They aren't. But some are, and not always the same between different flavors of wikis that are being migrated from.

We do provide paid ongoing small and big support for migrations, depending on needs. Email us at info@appfusions.com and let us know what you are trying to do and we can see if we can help you!  We have many client references too.

[ANT]:http://ant.apache.org/
[here]:https://migrations.atlassian.net/wiki/display/UWC/Universal+Wiki+Converter