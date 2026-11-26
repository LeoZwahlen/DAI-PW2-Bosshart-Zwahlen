# Network Polling Protocol

## 1. Overview

The Network Polling Application allows multiple users to create
polls, vote and view results from a command-line interface.

The application uses a client-server architecture. All polls
and votes are stored in memory on the server.

Users do not need an account and can vote multiple times.

The application supports two transport protocols:

- TCP to create polls, list polls, view options and vote.
- UDP to retrieve poll results without opening a TCP connection.

Both protocols use the same poll data.

## 2. Transport protocol

### TCP

TCP is used for poll management because it provides reliable
and ordered communication. This is important when creating
polls and sending votes.

The server listens on port 12345 by default.

The client starts the connection and sends commands to the
server. Multiple clients can be connected at the same time.

The server does not send a message when a client connects.

The client waits for a response before sending another command.

The connection stays open until the client sends QUIT,
disconnects or a network error occurs.

### UDP

UDP is used to retrieve poll results without establishing
a connection.

Unlike TCP, UDP does not guarantee that a message is received.

The server listens on port 12346 by default.

The UDP message format and timeout behavior will be defined
in the UDP section.

### Message format

Messages use UTF-8 encoding and contain plain text.

Each TCP message line ends with a newline character (\n).
The receiver also accepts Windows line endings (\r\n).

Some server responses contain multiple lines.
These responses end with a line containing END.

Error responses always contain a single line.

Command names are case-sensitive and written in uppercase.

Both default ports can be changed when starting the application.

## 3. Messages

### General rules

- Poll IDs are positive integers starting at 1.
- Option IDs are positive integers starting at 1 for each poll.
- A poll must contain between 2 and 10 options.
- Questions cannot be empty.
- Options cannot be empty or duplicated.
- Each option is limited to 64 bytes in UTF-8.
- Questions and options cannot contain double quotes
  or line breaks.
- Options cannot contain the characters :, ; or = because
  they are used as separators in responses.
- Each new option starts with zero votes.

Arguments are separated by spaces. Questions and options
must be placed between double quotes.

### CREATE

Creates a new poll with a question and multiple options.

Request:

`CREATE "<question>" "<option1>" "<option2>" [...]`

Response:

`OK <poll-id>`

Example:

```
CREATE "Favorite language?" "Java" "C++" "Python"
OK 1
```

Possible errors:

- Invalid arguments.

### LIST

Returns the list of available polls, ordered by poll ID.

Request:

`LIST`

Response:

```
<poll-id> | <question>
...
END
```

Example:

```
1 | Favorite language?
2 | Favorite sport?
END
```

If no polls exist, the server only returns END.

Possible errors:

- Invalid arguments.

### SHOW

Returns the options of a specific poll, ordered by option ID.

Request:

`SHOW <poll-id>`

Response:

```
<option-id> | <option>
...
END
```

Example:

```
1 | Java
2 | C++
3 | Python
END
```

Possible errors:

- Invalid arguments.
- Poll not found.

### VOTE

Adds one vote to an option of a specific poll.

Request:

`VOTE <poll-id> <option-id>`

Response:

`OK`

Example:

```
VOTE 1 2
OK
```

Possible errors:

- Invalid arguments.
- Poll not found.
- Option not found.

### RESULTS

Returns the number of votes for each option of a poll,
ordered by option ID.

Request:

`RESULTS <poll-id>`

Response:

```
<option>: <votes>
...
END
```

Example:

```
Java: 0
C++: 1
Python: 0
END
```

Possible errors:

- Invalid arguments.
- Poll not found.

### QUIT

Closes the TCP connection between the client and the server.

Request:

`QUIT`

Response:

`OK`

After sending the response, the server closes the connection.

Possible errors:

- Invalid arguments.

### Error messages

The server returns one of the following error messages
when a request cannot be processed.

| Response | Description |
| --- | --- |
| ERROR 1 Unknown command | The command does not exist. |
| ERROR 2 Invalid arguments | The syntax or arguments are invalid. |
| ERROR 3 Poll not found | The requested poll does not exist. |
| ERROR 4 Option not found | The requested option does not exist. |
| ERROR 5 Unsupported transport | The command is not supported by the transport protocol. |

Invalid requests do not close the TCP connection.

The client identifies an error by matching one of the
error responses listed above.

All TCP commands are available as soon as the client connects.
No login or other command is required before using them.

After a successful QUIT, no more messages can be exchanged
on the closed connection.

### UDP messages

To be completed in the UDP protocol issue.

## 4. Examples

### Successful communication

To be completed with a communication sequence diagram.

### Unsuccessful communication

To be completed with an error sequence diagram.

---

## Sources

- DAI course: https://heigvd-dai-26.github.io/
- DAI PW2: https://heigvd-dai-26.github.io/practical-work/pw2.html
- DAI application protocol course:
  https://github.com/heig-vd-dai-course/heig-vd-dai-course/tree/main/06.01-define-an-application-protocol

ChatGPT was used to help define, structure and write
the application protocol specification.