# SimpleAnnounce

SimpleAnnounce is a simple and easy to use, yet powerful automated announcement plugin for the Bukkit Minecraft API.

Messages can be sent server-wide or controlled by permissions after a delay and optionally repeated after an interval.

## Features

- Timed server-wide announcements/news messages
- Easily configurable
- Configure one-time or repeating messages
- Extensive Permissions support - configure messages to only send to users including or excluding certain permission nodes
- Optional debug mode to track down who is/isn't receiving messages and why
- Automatic config reloading to get new messages
- `/simpleannouncereload` or `/sar` to reload on command!
  - Permissions: `simpleannounce.reload`

## Configuration

Format:
```yaml
config-version(int): **DO NOT EDIT**, used to migrate configurations as the format changes
auto-reloadconfig(int): <Time in minutes to check/reload config for message updates(0 for off)>
  NOTE: When config is reloaded, will reset delays for messages and cause one-time messages to resend
debug-mode(boolean): <Should we pring debug to server.log(true/false)?>
  NOTE: Look for fine and finer level log messages in server.log
announcements: Add announcements below to this section
  <message label>(String, must be unique):
    message(String, required) OR messages(String list, see sample config): <Message to send>
    random-order(boolean, optional): <if list of messages should be sent in random order>
    sender(String, optional): <Message Sender(chat or boss, default: chat)>
    bar(section, optional):
      hold(int, optional): <Time in sections for bar to be displayed on announce>
      color(String, optional): <bar color(https://hub.spigotmc.org/javadocs/spigot/org/bukkit/boss/BarColor.html)>
      style(String, optional): <bar style(https://hub.spigotmc.org/javadocs/spigot/org/bukkit/boss/BarStyle.html)>
      animate(section, optional):
        enable(boolean, optional): <if bar should animate hold time>
        reverse(boolean, optional): <if animation should be reversed>
    delay(int, optional - default 0): <Delay to send message on in seconds>
    repeat(int, optional): <time between repeat sendings of the message in seconds>
    includesperms(String list, optional):
      - <only send to those with this perm>
      - <and this one>
    excludesperms(String list, optional):
      - <don't send to those with this perm>
      - <and this one>
```
**Note**: For permissions, includes are applied first, then excludes are taken out

### Example

```yaml
config-version: 1
auto-reloadconfig: 20
debug-mode: false
announcements:
  repeating-example:
    messages:
      - This is an automatically generated repeating message!
    delay: 15
    repeat: 60
  permissions-example:
    messages:
      - This is another automatically generated repeating message for people with build permission!
    delay: 30
    repeat: 60
    includesperms:
    - permissions.build
  one-time-example:
    messages:
      - This is an automatically generated one-time message!
    delay: 45
  message-group-example:
    messages:
      - This is an automatically generated sequential message group (1 of 3)!
      - This is an automatically generated sequential message group (2 of 3)!
      - This is an automatically generated sequential message group (3 of 3)!
    delay: 30
    repeat: 30
    sender: bossbar
    random-order: false
```
