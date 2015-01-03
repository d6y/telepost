Telepost
========

Reads an IMAP email account and writes JPEG attachments to the file system and creates a Markdown file for Telegr.am or Jekyll.

How use use
-----------

    sbt "run /path/to/my/blog me@example.org mypassw0rd"

This will connect to the me@example.org google email account, process emails, and write images to /path/to/my/blog/media and blog posts to /path/to/my/blog/_posts

The subject is used as the title of the blog post and the filename.

It will then delete the email (archive it).


Serving suggestion
------------------

Run via cron, and wrapper in a script that either commits and pushes to your GitHub hosted telegr.am blog, or move to a Dropbox folder.


Known issues
------------

* Images must be written to `/media/` on your blog
* Only does JPEGs
* Doesn't do video or sound files

Prerequisites
-------------

* SBT 0.11
* Java (should work with 1.5; tested with 1.7)
* Probably needs to be a Sun JVM


License
=======

Apache 2.0

Contains code from https://github.com/hoisted/hoisted
