This program implements basic functions of IMAP4rev1 server.

The application was successfully tested with TheBat! mail client.

By default, the app works in a signle-thread. To start multi-threading version, use:
``` Server server = new Server(port); ```

The app uses MySQL database to store information about users, mailboxes, and messages.
It contains following tables:

```
User
Mailox
Message
```

The messages (its content) store in directory *Messages* as *.eml files.
