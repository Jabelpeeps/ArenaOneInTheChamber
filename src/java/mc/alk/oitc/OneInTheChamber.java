package mc.alk.oitc;

import mc.alk.arena.BattleArena;
import mc.alk.arena.util.Log;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Set;
public class OneInTheChamber extends JavaPlugin{

	@Override
	public void onEnable(){
		/// Registers this plugin with BattleArena
		/// this: our plugin
		/// "OneInTheChamber": The name of our competition
		/// "oic": the name of our command alias
		/// OITCArena.class: which arena should this competition use
		BattleArena.registerCompetition(this, "OneInTheChamber", "oic", OITCArena.class);

		/// Load our config options
		loadConfig();

		Log.info("[" + getName()+ "] v" + getDescription().getVersion()+ " enabled!");
	}

	@Override
	public void onDisable(){
		Log.info("[" + getName()+ "] v" + getDescription().getVersion()+ " stopping!");
	}

	@Override
	public void reloadConfig(){
		super.reloadConfig();
		loadConfig();
	}

	public void loadConfig(){
		/// create our default config if it doesn't exist
		saveDefaultConfig();

		FileConfiguration config = getConfig();
		ConfigurationSection cs = config.getConfigurationSection("items");
		Set<String> keys = cs.getKeys(false);
		HashMap<Material, Integer> damages = new HashMap<Material,Integer>();
		for (String key : keys){
			Material m = Material.valueOf(key.toUpperCase());
			if (m == null)
				continue;
			int dmg = cs.getInt(key+".damage",-1);
			if (dmg != -1)
				damages.put(m, dmg);
		}
		OITCArena.damages=damages;
        OITCArena.velocity = config.getDouble("items.arrow.velocity", OITCArena.velocity);
        OITCArena.breakOnHit = config.getBoolean("items.arrow.breakOnHit", OITCArena.breakOnHit);
		OITCArena.instantShot = config.getBoolean("bow.instantShoot", OITCArena.instantShot);
	}

}
