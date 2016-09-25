package com.originalsbytoto.bukkit.sqlperms;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;

public class MainClass extends JavaPlugin implements Listener{

	public HashMap<UUID, PermissionAttachment> attachs = new HashMap<UUID, PermissionAttachment>();
	public HashMap<UUID, String[]> groups = new HashMap<UUID, String[]>();
	SQLManager sqlManager;
	
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
		saveDefaultConfig();
		String user = getConfig().getString("database.user");
		String pass = getConfig().getString("database.password");
		String host = getConfig().getString("database.host");
		int port = getConfig().getInt("database.port");
		String bdd = getConfig().getString("database.db_name");
		sqlManager = new SQLManager(user, pass, host, bdd, port);
		if(sqlManager.Init() == false) {
			getLogger().severe("Error during connecting to Database! Disabling ...");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		getLogger().info("SQLPerms loaded!");
	}
	
	public void onDisabled() {
		getLogger().info("SQLPerms disabled!");
	}
	
	public void setPlayersPerms(UUID uuid) {
		
		String[] groups = sqlManager.getGroups(uuid);
		for(int i = 0; i < groups.length; i++) {
			
			if(groups[i].isEmpty() == false) {
				
				addPermFromGroup(groups[i], uuid);
				
			}
			
		}
		
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String args[]) {

		if (cmd.getName().equalsIgnoreCase("perms")) {

			if(args.length == 0) sender.sendMessage(attachs.get(((Entity) sender).getUniqueId()).getPermissions().toString());
			else sender.sendMessage("State : " + sender.hasPermission(args[0]));
			
		}
		
		return true;
		
	}
	
	public void addPermFromGroup(String group, UUID uuid) {
		
		if(getConfig().contains("groups." + group)) {

			if(getConfig().contains("groups." + group + ".inherit")) {
				
				List<String> inhG = getConfig().getStringList("groups." + group + ".inherit");
				for(int z = 0; z < inhG.size(); z++) {
					
					if(getConfig().contains("groups." + inhG.get(z))) { addPermFromGroup(inhG.get(z), uuid); }
					
				}
				
			}
			
			List<String> perms = getConfig().getStringList("groups." + group + ".perms");
			for(int z = 0; z < perms.size(); z++) {
				
				if(perms.get(z).startsWith("-")) {
					
					String p = perms.get(z).split("-")[1];
					attachs.get(uuid).setPermission(p, false);
					
				} else {
					
					attachs.get(uuid).setPermission(perms.get(z), true);
					
				}
				
			}
			
		}
		
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent ev) {
		
		UUID uuid = ev.getPlayer().getUniqueId();
		
		if(attachs.containsKey(uuid)) {
			
			getLogger().severe("Player exists on join!");
			
		} else {
			
			attachs.put(uuid, ev.getPlayer().addAttachment(this));
			sqlManager.SetDefaultGroup(getConfig().getString("default_group"), ev.getPlayer());
			setPlayersPerms(uuid);
			
		}
		
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent ev) {
		
		UUID uuid = ev.getPlayer().getUniqueId();
		
		if(attachs.containsKey(uuid)) {
			
			attachs.remove(uuid);
			
		} else {
			
			getLogger().severe("Player not found on quit!");
			
		}
		
	}
	
}
