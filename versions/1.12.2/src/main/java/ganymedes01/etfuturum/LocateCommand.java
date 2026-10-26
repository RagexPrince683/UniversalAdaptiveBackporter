package ganymedes01.etfuturum;

import java.util.*;
import net.minecraft.command.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

final class LocateCommand extends CommandBase {
    @Override public String getName() { return "uab_locate"; }
    @Override public List<String> getAliases() { return Collections.singletonList("efr_locate"); }
    @Override public String getUsage(ICommandSender sender) { return "/uab_locate <shipwreck|ocean_ruins|buried_treasure>"; }
    @Override public int getRequiredPermissionLevel() { return 2; }
    @Override public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 1 || !Arrays.asList("shipwreck", "ocean_ruins", "buried_treasure").contains(args[0])) throw new WrongUsageException(getUsage(sender));
        BlockPos found = args[0].equals("buried_treasure") ? OceanTreasure.nearest(sender.getEntityWorld(), sender.getPosition()) : OceanStructures.nearest(sender.getEntityWorld(), sender.getPosition(), args[0].equals("ocean_ruins") ? "underwater_ruin" : "shipwreck");
        if (found == null) throw new CommandException("No saved " + args[0] + " found in this dimension.");
        sender.sendMessage(new TextComponentString(args[0] + ": " + found.getX() + ", " + found.getY() + ", " + found.getZ()));
    }
    @Override public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos target) { return args.length == 1 ? getListOfStringsMatchingLastWord(args, "shipwreck", "ocean_ruins", "buried_treasure") : Collections.emptyList(); }
}
