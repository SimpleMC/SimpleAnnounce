package org.simplemc.simpleannounce.config

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonUnwrapped
import org.bukkit.Sound
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.simplemc.simpleannounce.config.SimpleAnnounceConfig.AnnouncementConfig.Chat.ChatMessage
import org.simplemc.simpleannounce.inTicks
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

data class SimpleAnnounceConfig(
    val autoReload: Duration? = null,
    val announcements: List<AnnouncementConfig<*>>,
) {
    companion object {
        private fun Duration?.requireNullZeroOrPositive(name: String) {
            require(this == null || this == Duration.Companion.ZERO || this.isPositive()) {
                "When set, $name must be >= 0s"
            }
        }
    }

    @JsonIgnore
    val autoReloadTicks = autoReload?.inTicks

    init {
        require(autoReload == null || autoReload == Duration.Companion.ZERO || autoReload.inWholeMinutes >= 1) {
            "When set, Auto Reload Duration must be > 1 minute"
        }
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.SIMPLE_NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
    sealed class AnnouncementConfig<T : AnnouncementConfig.Message> {
        abstract val random: Boolean
        abstract val delay: Duration
        abstract val repeat: Duration?
        abstract val sound: SoundConfig?
        abstract val includesPermissions: List<String>
        abstract val excludesPermissions: List<String>
        abstract val messages: List<T>

        interface Message {
            val sound: SoundConfig?
        }

        @JsonIgnore
        val delayTicks = delay.inTicks.toInt()

        @JsonIgnore
        val repeatTicks = repeat?.inTicks?.toInt()

        init {
            delay.requireNullZeroOrPositive("delay")
            repeat.requireNullZeroOrPositive("repeat")
        }

        data class Chat(
            override val random: Boolean = false,
            override val delay: Duration = Duration.Companion.ZERO,
            override val repeat: Duration? = null,
            override val sound: SoundConfig? = null,
            override val includesPermissions: List<String> = emptyList(),
            override val excludesPermissions: List<String> = emptyList(),
            @field:JsonAlias("message") override val messages: List<ChatMessage>,
        ) : AnnouncementConfig<ChatMessage>() {
            data class ChatMessage(val message: String, override val sound: SoundConfig? = null) : Message
        }

        data class Boss(
            override val random: Boolean = false,
            override val delay: Duration = Duration.Companion.ZERO,
            override val repeat: Duration? = null,
            override val sound: SoundConfig? = null,
            override val includesPermissions: List<String> = emptyList(),
            override val excludesPermissions: List<String> = emptyList(),
            @field:JsonAlias("message") override val messages: List<BossBarMessage>,
            @field:JsonUnwrapped val barConfig: BarConfig = BarConfig(),
        ) : AnnouncementConfig<Boss.BossBarMessage>() {
            data class BossBarMessage(
                val message: String,
                override val sound: SoundConfig? = null,
                @field:JsonUnwrapped val barConfig: BarConfig? = null,
            ) : Message {
                init {
                    require(message.length <= 64) { "Boss Bar text must be <= 64 characters" }
                }
            }

            data class BarConfig(
                val hold: Duration = 5.seconds,
                val color: BarColor = BarColor.PURPLE,
                val style: BarStyle = BarStyle.SOLID,
                val animate: Boolean = true,
                val reverseAnimation: Boolean = false,
            ) {
                @JsonIgnore
                val holdTicks = hold.inTicks

                init {
                    hold.requireNullZeroOrPositive("hold")
                }
            }
        }

        data class Title(
            override val random: Boolean = false,
            override val delay: Duration = Duration.Companion.ZERO,
            override val repeat: Duration? = null,
            override val sound: SoundConfig? = null,
            override val includesPermissions: List<String> = emptyList(),
            override val excludesPermissions: List<String> = emptyList(),
            @field:JsonAlias("message") override val messages: List<TitleMessage>,
            @field:JsonUnwrapped val titleConfig: TitleConfig = TitleConfig(),
        ) : AnnouncementConfig<Title.TitleMessage>() {
            data class TitleMessage(
                val title: String,
                val subtitle: String? = null,
                override val sound: SoundConfig? = null,
                @field:JsonUnwrapped val titleConfig: TitleConfig? = null,
            ) : Message

            data class TitleConfig(
                val fadeIn: Duration = 500.milliseconds,
                val stay: Duration = 5.seconds,
                val fadeOut: Duration = 500.milliseconds,
            ) {
                @JsonIgnore
                val fadeInTicks = fadeIn.inTicks.toInt()

                @JsonIgnore
                val stayTicks = stay.inTicks.toInt()

                @JsonIgnore
                val fadeOutTicks = fadeOut.inTicks.toInt()

                init {
                    fadeIn.requireNullZeroOrPositive("fadeIn")
                    stay.requireNullZeroOrPositive("stay")
                    fadeOut.requireNullZeroOrPositive("fadeOut")
                }
            }
        }

        data class SoundConfig(val sound: Sound, val volume: Float = 1F, val pitch: Float = 1F) {
            init {
                require(volume >= 0 && volume <= 1) { "Sound volume must be between 0 and 1" }
                require(pitch >= 0.5 && pitch <= 2) { "Sound pitch must be between 0.5 and 2" }
            }
        }
    }
}
