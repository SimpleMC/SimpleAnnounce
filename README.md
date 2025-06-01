# SimpleAnnounce

SimpleAnnounce is a simple and easy to use, yet powerful automated announcement plugin for the Bukkit Minecraft API.

Messages can be sent server-wide or controlled by permissions after a delay and optionally repeated after an interval.

## Features

- Timed server-wide announcements/news messages sent as either a chat message, boss bar, or title/subtitle
- Easily configurable
- Configure one-time (per reload) or repeating/rotating messages
- Extensive Permissions support - configure messages to only send to users including or excluding certain permission nodes
- Optional debug mode to track down who is/isn't receiving messages and why
- Automatic config reloading to get new messages
- `/simpleannouncereload` or `/sar` to reload on command!
  - Permissions: `simpleannounce.reload`

## Configuration

- `autoReload`(duration*, optional): Time between automatically reloading plugin configuration, unset or `0m` for off
- `announcements`(Announcement list): list of announcement configurations, see below for detail
  - General announcement configuration:
    - `type`(Announcement type): Type of announcement: `Chat`, `Title`, or `Boss`
    - `messages` OR `message`(Message list): Message(s) to send, depends on type (see sample and description below)
    - `random`(boolean, optional): if list of messages should be sent in random order
    - `delay`(duration*, optional - default 0): Delay after loading to send message
    - `repeat`(duration*, optional): time between sending/repeating each message
    - `includesPermissions`**(String list, optional): Only send announcement to players with these permissions
    - `excludesPermissions`**(String list, optional): Exclude players with these permissions from receiving the announcement
    - `<additional options depending on announcement type>`
  - `Chat` type announcement:
    - `messages`(String list): Message(s) to send
  - `Boss` type announcement:
    - `hold`(duration*): Time for boss bar to be on screen
    - `color`(BarColor): Color of bar, one of PINK, BLUE, RED, GREEN, YELLOW, PURPLE, WHITE
    - `style`(BarStyle): Style of bar, one of SOLID, SEGMENTED_6, SEGMENTED_10, SEGMENTED_12, SEGMENTED_20
    - `animate`(boolean): if bar should animate over hold time
    - `reverseAnimation`(boolean): if animation should be reversed
    - `messages`(BossBarMessage list):
      - `message`(string): message string
      - ...boss bar config overrides per message eg hold, color, style, animate, etc...
  - `Title` type announcement:
    - `fadeIn`(duration*): Time it takes for title to fade in
    - `stay`(duration*): Time for title to stay on screen
    - `fadeOut`(duration*): Time it takes for title to fade out
    - `messages`(TitleMessage list):
      - `title`(string): title string
      - `subtitle`(string): subtitle string, appears below title slightly smaller
      - ...title config overrides eg fadeIn, stay, fadeOut...
- `config-version`: **Internal use for configuration migrations, do not edit**

<sub>
*Durations are in the form '10s', '5m', '1h', or ISO-8601 duration. See [parse](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.time/-duration/-companion/parse.html).

**For permissions, includes are applied first, then excludes are taken out
</sub>


### Example

```yaml
autoReload: 10m
announcements:
    - type: Chat
      delay: 30s
      repeat: 2m
      includesPermissions:
          - permissions.build
          - another.permission
      excludesPermissions:
          - permissions.admin
      messages:
          - hello
          - world
    - type: Chat
      repeat: 1m 40s
      messages:
          - abc
          - xyz
    - type: Title
      repeat: 30s
      messages:
          - title: Title!
            subtitle: Subtitle!
          - title: Title only custom durations
            fadeIn: 100ms
            stay: 10s
            fadeOut: 1s
      fadeIn: 500ms
      stay: 5s
      fadeOut: 500ms
    - type: Boss
      random: true
      repeat: 15s
      messages:
          - message: eyy
          - message: custom bar config
            hold: 10s
            color: GREEN
            style: SEGMENTED_20
            reverseAnimation: true
      hold: 5s
      color: PURPLE
      style: SOLID
      animate: true
config-version: 1
```
