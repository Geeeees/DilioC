package gsk.onetcore;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import java.util.Map;
import org.bukkit.entity.Player;

public class Cooldowns {
    private static Table<String, String, Long> cooldowns = HashBasedTable.create();

    public static long getCooldown(Player player, String key) {
        return Cooldowns.calculateRemainder((Long)cooldowns.get((Object)player.getName(), (Object)key));
    }

    public static long setCooldown(Player player, String key, long delay) {
        return Cooldowns.calculateRemainder((Long)cooldowns.put((Object)player.getName(), (Object)key, (Object)(System.currentTimeMillis() + delay)));
    }

    public static boolean tryCooldown(Player player, String key, long delay) {
        if (Cooldowns.getCooldown(player, key) <= 0) {
            Cooldowns.setCooldown(player, key, delay);
            return true;
        }
        return false;
    }

    public static void removeCooldowns(Player player) {
        cooldowns.row((Object)player.getName()).clear();
    }

    private static long calculateRemainder(Long expireTime) {
        return expireTime != null ? expireTime - System.currentTimeMillis() : Long.MIN_VALUE;
    }
}
