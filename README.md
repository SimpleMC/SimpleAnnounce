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
    - `sound`(SoundConfig, optional):
      - `sound`([Sound]): Sound to send with announcement
      - `volume`(float, optional): Volume of sound to send between 0 and 1, default 1
      - `pitch`(float, optional): Pitch of sound to send between 0.5 and 2, default 1
    - `includesPermissions`**(String list, optional): Only send announcement to players with these permissions
    - `excludesPermissions`**(String list, optional): Exclude players with these permissions from receiving the announcement
    - ...additional options depending on announcement type
  - `Chat` type announcement:
    - `messages`(ChatMessage list): Message(s) to send
      - `message`([Chat Component]): message component (plain text, or json, see examples)
      - `sound`(SoundConfig, optional): Override announcement SoundConfig
  - `Boss` type announcement:
    - `hold`(duration*): Time for boss bar to be on screen
    - `color`([BarColor]): Color of bar, one of PINK, BLUE, RED, GREEN, YELLOW, PURPLE, WHITE
    - `style`([BarStyle]): Style of bar, one of SOLID, SEGMENTED_6, SEGMENTED_10, SEGMENTED_12, SEGMENTED_20
    - `animate`(boolean): if bar should animate over hold time
    - `reverseAnimation`(boolean): if animation should be reversed
    - `messages`(BossBarMessage list):
      - `message`([Chat Component]): message component (plain text, or json, see examples)
      - `sound`(SoundConfig, optional): Override announcement SoundConfig
      - ...boss bar config overrides per message eg hold, color, style, animate, etc...
  - `Title` type announcement:
    - `fadeIn`(duration*): Time it takes for title to fade in
    - `stay`(duration*): Time for title to stay on screen
    - `fadeOut`(duration*): Time it takes for title to fade out
    - `messages`(TitleMessage list):
      - `title`([Chat Component]): title component (plain text, or json, see examples)
      - `subtitle`([Chat Component]): subtitle component (plain text, or json, see examples), appears below title slightly smaller
      - `sound`(SoundConfig, optional): Override announcement SoundConfig
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
      - message: hello
      - message: world
  - type: Chat
    repeat: 1m 40s
    sound:
      sound: AMBIENT_CAVE
      volume: .5
      pitch: 2
    messages:
      - message: abc
        sound:
          sound: BLOCK_ENCHANTMENT_TABLE_USE
      - message: xyz
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

#### Chat Components

Message contents are parsed as [Chat Component]s. This means that in addition to the normal `message: some message`, you can use JSON or YAML-formatted components.

```yaml
  - type: Chat
    repeat: 30s
    messages:
      - message: {
        "extra": [
          {
            "color": "gold",
            "text": "This is a "
          },
          {
            "bold": true,
            "color": "gold",
            "clickEvent": {
              "action": "open_url",
              "value": "https://www.spigotmc.org/wiki/the-chat-component-api/"
            },
            "hoverEvent": {
              "action": "show_text",
              "contents": "Chat Component API"
            },
            "text": "TextComponent"
          },
          { "text": " announcement!" }
        ]
      }
```
or
```yaml
  - type: Chat
    repeat: 30s
    messages:
      - message:
          extra:
            - color: gold
              text: "This is a "
            - bold: true
              color: gold
              text: TextComponent
              clickEvent:
                action: open_url
                value: "https://www.spigotmc.org/wiki/the-chat-component-api/"
              hoverEvent:
                action: show_text
                "contents": "Chat Component API"
            - text: " announcement!"
```

[Sound]: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Sound.html
[BarColor]: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/boss/BarColor.html
[BarStyle]: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/boss/BarStyle.html
[Chat Component]: https://www.spigotmc.org/wiki/the-chat-component-api/
