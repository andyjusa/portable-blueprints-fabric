package nimble.portable_blueprints.test;

import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import nimble.portable_blueprints.fabric.network.PayloadValidation;
import nimble.portable_blueprints.init.ModItems;
import nimble.portable_blueprints.network.CtoS_CreateStructureByBlueprint;
import nimble.portable_blueprints.network.SharedBlueprints.CtoS_AddShareWithOthers;
import nimble.portable_blueprints.portable_blueprints.OldVersionUtilities;

public final class NetworkGameTests implements FabricGameTest {
   @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
   public void authenticatedBlueprintIdentity(GameTestHelper helper) {
      var player = helper.makeMockServerPlayerInLevel();
      try {
         ItemStack held = new ItemStack(ModItems.BLUEPRINT.get());
         CompoundTag tag = new CompoundTag();
         tag.putString("owner", player.getUUID().toString());
         tag.putString("nome", "network-regression");
         tag.putBoolean("visualizeBuild", false);
         OldVersionUtilities.impostaItemStackNBT(held, tag);
         player.setItemInHand(InteractionHand.MAIN_HAND, held);

         // The GUI toggles preview locally, so full NBT equality would reject valid requests.
         ItemStack preview = held.copy();
         CompoundTag previewTag = OldVersionUtilities.getItemStackNBT(preview);
         previewTag.putBoolean("visualizeBuild", true);
         OldVersionUtilities.impostaItemStackNBT(preview, previewTag);
         BlockPos origin = helper.absolutePos(new BlockPos(1, 1, 1));
         helper.assertTrue(PayloadValidation.permits(
            new CtoS_CreateStructureByBlueprint(preview, false, false, origin), player),
            "A legitimate held blueprint must allow client-local preview state");

         ItemStack forged = preview.copy();
         CompoundTag forgedTag = OldVersionUtilities.getItemStackNBT(forged);
         UUID otherOwner = UUID.randomUUID();
         forgedTag.putString("owner", otherOwner.toString());
         forgedTag.putBoolean("free_build", true);
         OldVersionUtilities.impostaItemStackNBT(forged, forgedTag);
         helper.assertFalse(PayloadValidation.permits(
            new CtoS_CreateStructureByBlueprint(forged, false, false, origin), player),
            "A client must not replace the held blueprint owner or trusted item data");
         helper.assertFalse(PayloadValidation.permits(
            new CtoS_AddShareWithOthers("network-regression", otherOwner, player.getUUID()), player),
            "A player must not grant themselves access on another owner's behalf");
         helper.assertTrue(PayloadValidation.permits(
            new CtoS_AddShareWithOthers("network-regression", player.getUUID(), otherOwner), player),
            "An owner must retain permission to share their own blueprint");
         helper.succeed();
      } finally {
         player.discard();
         player.server.getPlayerList().remove(player);
      }
   }
}
