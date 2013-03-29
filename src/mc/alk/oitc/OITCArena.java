package mc.alk.oitc;

import java.util.HashMap;
import java.util.Map;

import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.events.MatchEventHandler;
import mc.alk.arena.util.DmgDeathUtil;
import mc.alk.arena.util.InventoryUtil;

import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

public class OITCArena extends Arena{
	static double velocity = 3;
	static Map<Material,Integer> damages = new HashMap<Material,Integer>();

	@MatchEventHandler
	public void onEntityDamage(EntityDamageByEntityEvent event) {
		if (event.isCancelled())
			return;
		Integer dmg = null;
		switch (event.getDamager().getType()){
		case ARROW:
			dmg = damages.get(Material.ARROW);
			break;
		case PLAYER:
			ArenaPlayer ap = DmgDeathUtil.getPlayerCause(event);
			if (ap == null)
				return;
			ItemStack is = ap.getInventory().getItemInHand();
			if (is == null){
				dmg = damages.get(Material.AIR);
			} else {
				dmg = damages.get(is.getType());
			}
			break;
		default:
			return;
		}
		if (dmg != null)
			event.setDamage(dmg);
	}

	@MatchEventHandler(entityMethod="getEntity")
	public void onEntityShootBowEvent(EntityShootBowEvent event){
		Entity proj = event.getProjectile();
		if (proj == null || proj.getType() != EntityType.ARROW)
			return;

		Arrow arrow = (Arrow) proj;
		arrow.setVelocity(arrow.getVelocity().multiply(velocity));
	}

	@MatchEventHandler
	public void onPlayerDeathEvent(PlayerDeathEvent event){
		ArenaPlayer killer = DmgDeathUtil.getPlayerCause(event);
		if (killer == null)
			return;
		InventoryUtil.addItemToInventory(killer.getPlayer(),
				new ItemStack(Material.ARROW,1));
	}
}