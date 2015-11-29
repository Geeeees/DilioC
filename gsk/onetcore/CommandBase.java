package gsk.onetcore;

import gsk.onetcore.Category;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class CommandBase
implements CommandExecutor {
    private String command;
    private String description;
    private String permission;
    private Category category;
    private boolean isPlayerOnly;

    public CommandBase(String command, String description, Category category, boolean playerOnly) {
        this.command = command;
        this.description = description;
        this.category = category;
        this.permission = "command." + (Object)((Object)this.getCategory());
        this.isPlayerOnly = playerOnly;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equals(this.getCommand())) {
            if (this.isPlayerOnly() && !(sender instanceof Player)) {
                sender.sendMessage((Object)ChatColor.RED + "Only players can execute the command '" + label + "'.");
                return true;
            }
            if (sender.hasPermission("command." + (Object)((Object)Category.OP))) {
                this.execute(sender, cmd, label, args);
                return true;
            }
            if (!sender.hasPermission("command." + (Object)((Object)this.getCategory()))) {
                sender.sendMessage((Object)ChatColor.RED + "You do not have permission to execute this command.");
                return true;
            }
            this.execute(sender, cmd, label, args);
        }
        return false;
    }

    public abstract void execute(CommandSender var1, Command var2, String var3, String[] var4);

    public void message(CommandSender sender, String msg) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)msg));
    }

    public String getCommand() {
        return this.command;
    }

    public String getDescription() {
        return this.description;
    }

    public String getPermission() {
        return this.permission;
    }

    public Category getCategory() {
        return this.category;
    }

    public boolean isPlayerOnly() {
        return this.isPlayerOnly;
    }
}
