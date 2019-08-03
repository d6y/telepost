Telepost
========

Reads an IMAP email account and writes JPEG attachments to the file system and creates a Markdown file for Telegr.am or Jekyll.

How use use
-----------

The main method expects the following arguments:

- path to write the markdown blog post to
- Google email adrress
- Address password
- S3 bucket name
- S3 key
- S3 secret

For example:

    sbt "runMain Main blog/_posts me@example.org mypassw0rd images.bucket xxx yyy"

or:

    $ sbt assembly
    $ java -jar target/scala-2.11/telepost-assembly-1.0.1.jar blog/_post email pass bucket key secret

The subject is used as the title of the blog post and the filename.

It will then delete the email (archive it).


Serving suggestion
------------------

Run via cron, and wrapper in a script that either commits and pushes to your GitHub hosted telegr.am blog, or move to a Dropbox folder.


License
=======

Apache 2.0

Contains code from https://github.com/hoisted/hoisted
