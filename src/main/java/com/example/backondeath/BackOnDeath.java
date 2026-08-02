package com.example.backondeath;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BackOnDeath extends JavaPlugin implements Listener, CommandExecutor {

    // Lưu thông tin chết của từng người chơi theo UUID
    private final Map<UUID, DeathData> deathLocations = new HashMap<>();

    @Override
    public void onEnable() {
        // Đăng ký Event & Command
        getServer().getPluginManager().registerEvents(this, this);
        if (getCommand("back") != null) {
            getCommand("back").setExecutor(this);
        }
        getLogger().info("Plugin BackOnDeath đã được kích hoạt!");
    }

    @Override
    public void onDisable() {
        deathLocations.clear();
    }

    // Sự kiện khi người chơi chết
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        // Lưu lại vị trí chính xác lúc chết
        deathLocations.put(victim.getUniqueId(), new DeathData(victim.getLocation()));
        victim.sendMessage(ChatColor.YELLOW + "Vị trí chết của bạn đã được lưu! Dùng lệnh " + ChatColor.GREEN + "/back" + ChatColor.YELLOW + " để quay lại.");
    }

    // Xử lý lệnh /back
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Lệnh này chỉ dành cho người chơi!");
            return true;
        }

        if (!player.hasPermission("backondeath.use")) {
            player.sendMessage(ChatColor.RED + "Bạn không có quyền dùng lệnh này!");
            return true;
        }

        DeathData data = deathLocations.get(player.getUniqueId());

        if (data == null || data.getDeathLocation() == null) {
            player.sendMessage(ChatColor.RED + "Bạn không có vị trí chết nào để quay lại!");
            return true;
        }

        Location deathLoc = data.getDeathLocation();

        // Kiểm tra xem có người chơi khác trong bán kính 5 block không
        boolean isOtherPlayerNearby = false;
        if (deathLoc.getWorld() != null) {
            for (Player nearbyPlayer : deathLoc.getWorld().getPlayers()) {
                // Bỏ qua chính bản thân người vừa chết
                if (nearbyPlayer.getUniqueId().equals(player.getUniqueId())) {
                    continue;
                }
                // Tính khoảng cách
                if (nearbyPlayer.getLocation().distance(deathLoc) <= 5.0) {
                    isOtherPlayerNearby = true;
                    break;
                }
            }
        }

        long currentTime = System.currentTimeMillis();

        if (isOtherPlayerNearby) {
            // Nếu đây là lần đầu phát hiện có người đứng gần
            if (data.getNearPlayerStartTime() == -1) {
                data.setNearPlayerStartTime(currentTime);
            }

            // Tính thời gian người đó đã đứng gần (tính bằng giây)
            long secondsPassed = (currentTime - data.getNearPlayerStartTime()) / 1000;

            if (secondsPassed >= 3) {
                player.sendMessage(ChatColor.RED + "Có người chơi khác ở gần vị trí chết của bạn hơn 3 giây! Bạn không thể /back nữa.");
                // Xóa vị trí chết vì đã bị hủy
                deathLocations.remove(player.getUniqueId());
                return true;
            } else {
                long remaining = 3 - secondsPassed;
                player.sendMessage(ChatColor.RED + "Có người chơi ở gần nơi bạn chết! Vị trí sẽ bị hủy sau " + remaining + " giây nữa nếu họ không rời đi!");
            }
        } else {
            // Nếu không còn ai ở gần, reset lại bộ đếm thời gian
            data.setNearPlayerStartTime(-1);
        }

        // Thực hiện dịch chuyển về vị trí chết
        player.teleport(deathLoc);
        player.sendMessage(ChatColor.GREEN + "Đã dịch chuyển về vị trí chết!");
        
        // Dịch chuyển xong thì xóa dữ liệu cũ
        deathLocations.remove(player.getUniqueId());

        return true;
    }
}
