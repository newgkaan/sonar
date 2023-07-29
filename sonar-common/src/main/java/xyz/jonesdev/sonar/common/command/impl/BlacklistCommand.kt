/*
 * Copyright (C) 2023 Sonar Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package xyz.jonesdev.sonar.common.command.impl

import xyz.jonesdev.sonar.api.Sonar
import xyz.jonesdev.sonar.api.command.CommandInvocation
import xyz.jonesdev.sonar.api.command.argument.Argument
import xyz.jonesdev.sonar.api.command.subcommand.Subcommand
import xyz.jonesdev.sonar.api.command.subcommand.SubcommandInfo
import java.net.InetAddress

@SubcommandInfo(
  name = "blacklist",
  description = "Manage blacklisted IP addresses",
  arguments = [
    Argument("add"),
    Argument("remove"),
    Argument("clear"),
    Argument("size"),
  ],
)
class BlacklistCommand : Subcommand() {
  companion object {
    private val IP_REGEX =
      Regex("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])([.])){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\$")
  }

  override fun execute(invocation: CommandInvocation) {
    when (invocation.arguments[1]!!.lowercase()) {
      "add" -> {
        if (invocation.arguments.size <= 2) {
          invocation.invocationSender.sendMessage(
            sonar.config.INCORRECT_COMMAND_USAGE
              .replace("%usage%", "blacklist add <IP address>")
          )
          return
        }

        val rawInetAddress = invocation.arguments[2]

        if (!rawInetAddress.matches(IP_REGEX)) {
          invocation.invocationSender.sendMessage(sonar.config.INCORRECT_IP_ADDRESS)
          return
        }

        val inetAddress = InetAddress.getByName(rawInetAddress)

        if (inetAddress.isAnyLocalAddress || inetAddress.isLoopbackAddress) {
          invocation.invocationSender.sendMessage(sonar.config.ILLEGAL_IP_ADDRESS)
          return
        }

        synchronized(sonar.fallback.blacklisted) {
          if (sonar.fallback.blacklisted.has(inetAddress.toString())) {
            invocation.invocationSender.sendMessage(sonar.config.BLACKLIST_DUPLICATE)
            return
          }

          sonar.fallback.blacklisted.put(inetAddress.toString())
          invocation.invocationSender.sendMessage(
            sonar.config.BLACKLIST_ADD
              .replace("%ip%", rawInetAddress)
          )
        }
      }

      "remove" -> {
        if (invocation.arguments.size <= 2) {
          invocation.invocationSender.sendMessage(
            sonar.config.INCORRECT_COMMAND_USAGE
              .replace("%usage%", "blacklist remove <IP address>")
          )
          return
        }

        val rawInetAddress = invocation.arguments[2]

        if (!rawInetAddress.matches(IP_REGEX)) {
          invocation.invocationSender.sendMessage(sonar.config.INCORRECT_IP_ADDRESS)
          return
        }

        val inetAddress = InetAddress.getByName(rawInetAddress)

        if (inetAddress.isAnyLocalAddress || inetAddress.isLoopbackAddress) {
          invocation.invocationSender.sendMessage(sonar.config.ILLEGAL_IP_ADDRESS)
          return
        }

        synchronized(sonar.fallback.blacklisted) {
          if (!sonar.fallback.blacklisted.has(inetAddress.toString())) {
            invocation.invocationSender.sendMessage(sonar.config.BLACKLIST_NOT_FOUND)
            return
          }

          sonar.fallback.blacklisted.invalidate(inetAddress.toString())
          invocation.invocationSender.sendMessage(
            sonar.config.BLACKLIST_REMOVE
              .replace("%ip%", rawInetAddress)
          )
        }
      }

      "clear" -> {
        synchronized(sonar.fallback.blacklisted) {
          val blacklisted = sonar.fallback.blacklisted.estimatedSize()

          if (blacklisted == 0) {
            invocation.invocationSender.sendMessage(sonar.config.BLACKLIST_EMPTY)
            return
          }

          sonar.fallback.blacklisted.invalidateAll()

          invocation.invocationSender.sendMessage(
            sonar.config.BLACKLIST_CLEARED
              .replace("%removed%", Sonar.DECIMAL_FORMAT.format(blacklisted))
          )
        }
      }

      "size" -> {
        invocation.invocationSender.sendMessage(
          sonar.config.BLACKLIST_SIZE
            .replace("%amount%", Sonar.DECIMAL_FORMAT.format(sonar.fallback.blacklisted.estimatedSize()))
        )
      }

      else -> incorrectUsage(invocation.invocationSender)
    }
  }
}
