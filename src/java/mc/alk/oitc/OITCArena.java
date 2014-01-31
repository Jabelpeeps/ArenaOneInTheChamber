package mc.alk.oitc;

import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.events.ArenaEventHandler;
import mc.alk.arena.util.DmgDeathUtil;
import mc.alk.arena.util.InventoryUtil;
import mc.alk.arena.util.Log;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class OITCArena extends Arena{
	static double velocity = 3;
	static Map<Material,Integer> damages = new HashMap<Material,Integer>();
	static boolean instantShot = false;
    public static boolean breakOnHit = false;

    static class EnchantAdapter{
		Integer power, knockback;
		public EnchantAdapter(Integer power, Integer knockback){
			this.power = power;
			this.knockback = knockback;
		}
	}

//	Map<Integer, EnchantAdapter> arrowIds = new ConcurrentHashMap<Integer, EnchantAdapter>();
	Set<Integer> arrowIds = Collections.synchronizedSet(new HashSet<Integer>());

	@ArenaEventHandler
	public void onEntityDamage(EntityDamageByEntityEvent event) {
		if (event.isCancelled())
			return;
		Integer dmg;
		switch (event.getDamager().getType()){
		case ARROW:
			dmg = damages.get(Material.ARROW);
//			EnchantAdapter ea = event.getDamager().getEntityId();
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

	@ArenaEventHandler(entityMethod="getEntity")
	public void onEntityShootBowEvent(EntityShootBowEvent event){
		Entity proj = event.getProjectile();
		if (proj == null || proj.getType() != EntityType.ARROW)
			return;

        shotArrow((Arrow) proj, null, true);
	}

	@ArenaEventHandler
	public void onPlayerDeathEvent(PlayerDeathEvent event){
		ArenaPlayer killer = DmgDeathUtil.getPlayerCause(event);
		if (killer == null)
			return;
		InventoryUtil.addItemToInventory(killer.getPlayer(),
				new ItemStack(Material.ARROW,1));
	}

    @ArenaEventHandler(needsPlayer=false)
	public void onProjectileHitEvent(ProjectileHitEvent event){
        Log.debug("### checking " + event.getEntity().getEntityId());
		if (arrowIds.remove(event.getEntity().getEntityId())){
			Entity e = event.getEntity();
//			event.getEntity().t
            Log.debug("### removing " + event.getEntity().getEntityId());
			Location l = e.getLocation().clone();
			l.setY(l.getY()-256); /// into the void with you
			e.teleport(l);
			e.remove();
		}
	}
    private void shotArrow(Arrow arrow, Player p, boolean shotFromBow){
        Log.debug("onEntityShootBowEvent  " + arrow.getEntityId());
        arrow.setVelocity(arrow.getVelocity().multiply(velocity));
        arrowIds.add(arrow.getEntityId());
        if (!shotFromBow){ /// They are using the classic instashot arrow from a click
            boolean inf = false;
            ItemStack is = p.getItemInHand();
            Map<Enchantment,Integer> encs = is.getEnchantments();
            if (encs != null){
                if (encs.containsKey(Enchantment.ARROW_FIRE)){
                    arrow.setFireTicks(100);}
                inf = encs.containsKey(Enchantment.ARROW_INFINITE);
//			if (encs.containsKey(Enchantment.ARROW_DAMAGE) ||
//					encs.containsKey(Enchantment.ARROW_KNOCKBACK)){
//				arrowIds.put(arrow.getEntityId(),
//						new EnchantAdapter(encs.get(Enchantment.ARROW_DAMAGE),
//								encs.get(Enchantment.ARROW_DAMAGE)));
//			}
//			/// Power kludge
//			if (encs.containsKey(Enchantment.ARROW_DAMAGE)){
//				int power = encs.get(Enchantment.ARROW_DAMAGE);
//				if (power > 0){
//					arrow.setVelocity(arrow.getVelocity().multiply(1+ 0.5*power));
//				}
//			}
            }
            if (!inf){
                InventoryUtil.removeItems(p.getInventory(), new ItemStack(Material.ARROW,1));
            }

        }
    }

	@ArenaEventHandler
	public void onPlayerInteract(PlayerInteractEvent event){
        Log.debug(" onPlayerInteract FRIIGN  " + event);
		if (!instantShot ||
				!(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) ||
				event.getPlayer().getItemInHand() == null || event.getPlayer().getItemInHand().getType() != Material.BOW ||
				!event.getPlayer().getInventory().contains(Material.ARROW)){
			return;}
		Player p = event.getPlayer();
		Arrow arrow = p.launchProjectile(Arrow.class);
        shotArrow(arrow, p, false);
		event.setCancelled(true);
	}


}
