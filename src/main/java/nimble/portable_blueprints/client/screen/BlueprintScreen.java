package nimble.portable_blueprints.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec2;
import nimble.portable_blueprints.fabric.network.PacketDistributor;
import nimble.portable_blueprints.Events.RenderBlueprintsPreview;
import nimble.portable_blueprints.altro.Blueprint;
import nimble.portable_blueprints.client.ClientMethods;
import nimble.portable_blueprints.client.VisualizeBlueprintDatas;
import nimble.portable_blueprints.client.screen.tablet.utils.TabletScrollBar;
import nimble.portable_blueprints.init.ModItems;
import nimble.portable_blueprints.mod_compatibility.ModCompatibilityInit;
import nimble.portable_blueprints.mod_compatibility.projecte.ProjectEBlueprintScreen;
import nimble.portable_blueprints.mod_compatibility.projecte.ProjectEMetodi;
import nimble.portable_blueprints.network.CtoS_CreateStructureByBlueprint;
import nimble.portable_blueprints.network.server_blacklist_blocks.CtoS_ServerBlacklistBlocks;
import nimble.portable_blueprints.network.server_blacklist_blocks.StoC_ServerBlacklistBlocks;
import nimble.portable_blueprints.network.update_item_blueprint_nbt.CtoS_UpdateItemBlueprintNBT;
import nimble.portable_blueprints.portable_blueprints.OldVersionUtilities;
import nimble.portable_blueprints.robautile.AdvancedButton;
import nimble.portable_blueprints.robautile.Operazioni;
import nimble.portable_blueprints.robautile.RenderingMethods;
import nimble.portable_blueprints.robautile.ScrollingText;
import org.joml.Vector2i;

public class BlueprintScreen extends Screen {
   private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("portable_blueprints", "textures/gui/blueprint_gui.png");
   private int CentroX;
   private int CentroY;
   private int W;
   private int H;
   public static boolean DEBUG_PROJECT_E = false;
   private RenderingMethods.GuiGraphicsElement Titolo;
   private AdvancedButton B_Reload;
   private AdvancedButton B_AUTOFILLMODE;
   private AdvancedButton B_ROTATE_LEFT;
   private AdvancedButton B_ROTATE_RIGHT;
   private AdvancedButton B_ROTATE_RESET;
   private AdvancedButton B_MIRROR_X;
   private AdvancedButton B_MIRROR_Y;
   private AdvancedButton B_MIRROR_Z;
   private AdvancedButton B_SKIP_OBSTRUCTION_BLOCK;
   private AdvancedButton B_BUILD_ANYWAY;
   private int ultimaGrandezza = 0;
   private Blueprint.BlueprintDatas BD = null;
   public Map<Item, Integer> ItemQuantity = new HashMap<>();
   private List<BlockPos> posContainer = new ArrayList<>();
   private ItemStack blueprintItem = null;
   private ProjectEBlueprintScreen projectEBlueprintScreen = null;
   private AdvancedButton B_BUILD;
   private boolean hasOrigin = false;
   private boolean hasFreeBuild = false;
   private int posYLista = 0;
   private int posYMaxLista = 0;
   private List<BlueprintScreen.ItemRequestWidget> itemRequestWidgets = new ArrayList<>();
   private float R = 1.0F;
   private float G = 1.0F;
   private float B = 1.0F;
   private long StartTimeAnim = 0L;
   private TabletScrollBar scrollBar;
   private double rotstep = 90.0;
   private double oldrotstep = -1.0;
   private double oldCurrentRot = 0.0;
   private short autofillmode = 0;

   public BlueprintScreen(ItemStack blueprintItem, List<BlockPos> posContainer, boolean openAnimation) {
      super(Component.empty());
      this.blueprintItem = blueprintItem;
      this.posContainer = posContainer;
      UUID id = UUID.randomUUID();
      StoC_ServerBlacklistBlocks.IDPACKET = id;
      CtoS_ServerBlacklistBlocks.send(id);
      CompoundTag nbt = OldVersionUtilities.getItemStackNBT(blueprintItem);
      this.hasOrigin = nbt.contains("originPos");
      this.hasFreeBuild = nbt.contains("free_build") && nbt.getBoolean("free_build");
      this.autofillmode = nbt.contains("autofillmode") ? nbt.getShort("autofillmode") : 5;
      if (DEBUG_PROJECT_E || ModCompatibilityInit.isProjectELoaded) {
         this.projectEBlueprintScreen = new ProjectEBlueprintScreen(this, blueprintItem);
      }

      if (blueprintItem != null) {
         if (blueprintItem.getItem().equals(ModItems.BLUEPRINT.get())) {
            this.R = 0.15294118F;
            this.G = 0.5372549F;
            this.B = 1.0F;
         } else if (blueprintItem.getItem().equals(ModItems.SERVER_BLUEPRINT.get())) {
            this.R = 0.8666667F;
            this.G = 0.93333334F;
            this.B = 0.21176471F;
         } else if (blueprintItem.getItem().equals(ModItems.WORN_BLUEPRINT.get())) {
            this.R = 0.31764707F;
            this.G = 0.88235295F;
            this.B = 0.9490196F;
         } else if (blueprintItem.getItem().equals(ModItems.GLOBAL_BLUEPRINT.get())) {
            this.R = 0.8980392F;
            this.G = 0.14509805F;
            this.B = 0.8862745F;
         }
      }

      if (openAnimation) {
         this.StartTimeAnim = System.currentTimeMillis() + 250L;
      }
   }

   protected void init() {
      super.init();
      this.W = this.width;
      this.H = this.height;
      this.CentroX = this.W / 2;
      this.CentroY = this.H / 2;
      this.Titolo = new RenderingMethods.GuiGraphicsElement(
         new RenderingMethods.GuiGraphicsElement.TESTO_Parametri(
            this.CentroX - 81, this.CentroY - 68, "", 1.0F, Minecraft.getInstance().font, 0, false, RenderingMethods.PosizioneTesto.SINISTRA, true
         )
      );
      int px = this.CentroX - 62;
      int py = this.CentroY + 56;
      this.B_Reload = AdvancedButton.builder(Component.literal(""), pAdvanceButton -> this.Reload())
         .pos(px, py)
         .size(13, 13)
         .defaultImage(new AdvancedButton.Image(TEXTURE, px, py, 129, 226, 15, 15))
         .hoverImage(new AdvancedButton.Image(TEXTURE, px, py, 129, 242, 15, 15))
         .tooltip(Tooltip.create(Component.translatable("gui.portable_blueprints.blueprint.button_reload")))
         .build();
      this.addRenderableWidget(this.B_Reload);
      px = this.CentroX + 35;
      py = this.CentroY + 56 - 31;
      this.B_ROTATE_RIGHT = AdvancedButton.builder(Component.literal(""), pAdvanceButton -> this.RotateStructure(true, false))
         .pos(px, py)
         .size(13, 13)
         .defaultImage(new AdvancedButton.Image(TEXTURE, px, py, 1, 226, 13, 13))
         .hoverImage(new AdvancedButton.Image(TEXTURE, px, py, 1, 242, 13, 13))
         .tooltip(Tooltip.create(Component.translatable("gui.portable_blueprints.blueprint.button_rotate_right")))
         .build();
      this.addRenderableWidget(this.B_ROTATE_RIGHT);
      px = this.CentroX + 34;
      py = this.CentroY + 56;
      this.B_ROTATE_RESET = AdvancedButton.builder(Component.literal(""), pAdvanceButton -> this.RotateStructure(false, true))
         .pos(px, py)
         .size(13, 13)
         .defaultImage(new AdvancedButton.Image(TEXTURE, px, py, 33, 226, 13, 13))
         .hoverImage(new AdvancedButton.Image(TEXTURE, px, py, 33, 242, 13, 13))
         .tooltip(Tooltip.create(Component.translatable("gui.portable_blueprints.blueprint.button_rotate_reset")))
         .build();
      this.addRenderableWidget(this.B_ROTATE_RESET);
      px = this.CentroX + 34;
      py = this.CentroY + 56 - 15;
      this.B_ROTATE_LEFT = AdvancedButton.builder(Component.literal(""), pAdvanceButton -> this.RotateStructure(false, false))
         .pos(px, py)
         .size(13, 13)
         .defaultImage(new AdvancedButton.Image(TEXTURE, px, py, 17, 226, 13, 13))
         .hoverImage(new AdvancedButton.Image(TEXTURE, px, py, 17, 242, 13, 13))
         .tooltip(Tooltip.create(Component.translatable("gui.portable_blueprints.blueprint.button_rotate_left")))
         .build();
      this.addRenderableWidget(this.B_ROTATE_LEFT);
      px = this.CentroX + 50;
      py = this.CentroY + 55 - 31;
      this.B_MIRROR_X = AdvancedButton.builder(Component.literal(""), pAdvanceButton -> this.mirrowStructure((short)0))
         .pos(px, py)
         .size(12, 13)
         .defaultImage(new AdvancedButton.Image(TEXTURE, px, py, 50, 226, 15, 15))
         .hoverImage(new AdvancedButton.Image(TEXTURE, px, py, 50, 242, 15, 15))
         .pressedImageCondition(new AdvancedButton.Image(TEXTURE, px, py, 50, 242, 15, 15))
         .tooltip(Tooltip.create(Component.translatable("gui.portable_blueprints.blueprint.button_mirror_x")))
         .build();
      this.addRenderableWidget(this.B_MIRROR_X);
      px = this.CentroX + 49;
      py = this.CentroY + 56;
      this.B_MIRROR_Y = AdvancedButton.builder(Component.literal(""), pAdvanceButton -> this.mirrowStructure((short)1))
         .pos(px, py)
         .size(13, 14)
         .defaultImage(new AdvancedButton.Image(TEXTURE, px, py, 66, 226, 15, 15))
         .hoverImage(new AdvancedButton.Image(TEXTURE, px, py, 66, 242, 15, 15))
         .pressedImageCondition(new AdvancedButton.Image(TEXTURE, px, py, 66, 242, 15, 15))
         .tooltip(Tooltip.create(Component.translatable("gui.portable_blueprints.blueprint.button_mirror_y")))
         .build();
      this.addRenderableWidget(this.B_MIRROR_Y);
      px = this.CentroX + 50;
      py = this.CentroY + 55 - 15;
      this.B_MIRROR_Z = AdvancedButton.builder(Component.literal(""), pAdvanceButton -> this.mirrowStructure((short)2))
         .pos(px, py)
         .size(12, 13)
         .defaultImage(new AdvancedButton.Image(TEXTURE, px, py, 82, 226, 15, 15))
         .hoverImage(new AdvancedButton.Image(TEXTURE, px, py, 82, 242, 15, 15))
         .pressedImageCondition(new AdvancedButton.Image(TEXTURE, px, py, 82, 242, 15, 15))
         .tooltip(Tooltip.create(Component.translatable("gui.portable_blueprints.blueprint.button_mirror_z")))
         .build();
      this.addRenderableWidget(this.B_MIRROR_Z);
      px = this.CentroX - 61;
      py = this.CentroY + 42;
      this.B_SKIP_OBSTRUCTION_BLOCK = AdvancedButton.builder(Component.literal(""), pAdvanceButton -> this.skipObstructionBlock())
         .pos(px, py)
         .size(11, 12)
         .defaultImage(new AdvancedButton.Image(TEXTURE, px, py, 99, 226, 11, 12))
         .hoverImage(new AdvancedButton.Image(TEXTURE, px, py, 99, 242, 11, 12))
         .pressedImageCondition(new AdvancedButton.Image(TEXTURE, px, py, 99, 242, 11, 12))
         .tooltip(Tooltip.create(Component.translatable("gui.portable_blueprints.blueprint.button_skip_obstruction_block")))
         .build();
      this.addRenderableWidget(this.B_SKIP_OBSTRUCTION_BLOCK);
      px = this.CentroX - 47;
      py = this.CentroY + 56;
      this.B_BUILD_ANYWAY = AdvancedButton.builder(Component.literal(""), pAdvanceButton -> this.buildAnyway())
         .pos(px, py)
         .size(13, 13)
         .defaultImage(new AdvancedButton.Image(TEXTURE, px, py, 113, 226, 15, 15))
         .pressedImageCondition(new AdvancedButton.Image(TEXTURE, px, py, 113, 242, 15, 15))
         .tooltip(Tooltip.create(Component.translatable("gui.portable_blueprints.blueprint.button_build_anyway")))
         .build();
      this.addRenderableWidget(this.B_BUILD_ANYWAY);
      this.setAutofillmode(false);
      px = this.CentroX - 24;
      py = this.CentroY + 48;
      this.B_BUILD = AdvancedButton.builder(Component.literal(""), pAdvanceButton -> this.build())
         .pos(px, py)
         .size(48, 21)
         .defaultImage(new AdvancedButton.Image(TEXTURE, px, py, 208, 117, 48, 21))
         .pressedImageCondition(new AdvancedButton.Image(TEXTURE, px, py, 208, 96, 48, 21))
         .tooltip(
            Tooltip.create(
               this.hasOrigin
                  ? Component.translatable("gui.portable_blueprints.blueprint.tooltip.button_build")
                  : Component.translatable("gui.portable_blueprints.blueprint.tooltip.button_set_origin_first")
            )
         )
         .build();
      this.addRenderableWidget(this.B_BUILD);
      if (this.projectEBlueprintScreen != null) {
         this.projectEBlueprintScreen.init(this.CentroX, this.CentroY, this.W, this.H);

         for (AdvancedButton ab : this.projectEBlueprintScreen.getRenderable()) {
            this.addRenderableWidget(ab);
         }
      }

      this.richiediItemQuantityContainer();
      this.updateList();
   }

   private void build() {
      CompoundTag nbt = OldVersionUtilities.getItemStackNBT(this.blueprintItem);
      if (nbt.contains("originPos")
         && (
            nimble.portable_blueprints.items.Blueprint.condiviso_con_player(nbt)
               || !nbt.getString("owner").equals("server")
                  && !nbt.getString("owner").equals("worn")
                  && !nbt.getString("owner").equals("")
                  && UUID.fromString(nbt.getString("owner")).equals(Minecraft.getInstance().player.getUUID())
         )) {
         if (nbt.getString("owner").equals("server")) {
            PacketDistributor.sendToServer(
               new CtoS_CreateStructureByBlueprint(this.blueprintItem, true, false, new BlockPos(Operazioni.getVec3iFromString(nbt.getString("originPos")))),
               new CustomPacketPayload[0]
            );
         } else if (nbt.getString("owner").equals("worn")) {
            PacketDistributor.sendToServer(
               new CtoS_CreateStructureByBlueprint(this.blueprintItem, false, true, new BlockPos(Operazioni.getVec3iFromString(nbt.getString("originPos")))),
               new CustomPacketPayload[0]
            );
         } else if (!nbt.getString("owner").equals("")) {
            PacketDistributor.sendToServer(
               new CtoS_CreateStructureByBlueprint(this.blueprintItem, false, false, new BlockPos(Operazioni.getVec3iFromString(nbt.getString("originPos")))),
               new CustomPacketPayload[0]
            );
         }

         nbt.putBoolean("visualizeBuild", false);
         OldVersionUtilities.impostaItemStackNBT(this.blueprintItem, nbt);
         Minecraft.getInstance().setScreen(null);
      }
   }

   private void setAutofillmode(boolean toSet) {
      CompoundTag nbt = OldVersionUtilities.getItemStackNBT(this.blueprintItem);
      if (toSet) {
         this.autofillmode++;
         if (this.autofillmode > 5) {
            this.autofillmode = 1;
         }

         CtoS_UpdateItemBlueprintNBT.send(this.blueprintItem, "autofillmode", this.autofillmode);
         nbt.putShort("autofillmode", this.autofillmode);
         OldVersionUtilities.impostaItemStackNBT(this.blueprintItem, nbt);
      }

      if (this.B_AUTOFILLMODE != null) {
         this.removeWidget(this.B_AUTOFILLMODE);
      }

      int px = this.CentroX - 47;
      int py = this.CentroY + 42;
      MutableComponent tooltip = Component.translatable("gui.portable_blueprints.blueprint.button_autofillmode").withStyle(ChatFormatting.WHITE);
      tooltip.append("\n")
         .append(Component.translatable("gui.portable_blueprints.blueprint.button_autofillmode_2").withStyle(ChatFormatting.GRAY))
         .append(Component.literal(this.autofillmode == 5 ? "OFF" : String.valueOf(this.autofillmode)).withStyle(ChatFormatting.GOLD));
      this.B_AUTOFILLMODE = AdvancedButton.builder(Component.literal(""), pAdvanceButton -> this.setAutofillmode(true))
         .pos(px, py)
         .size(13, 13)
         .defaultImage(new AdvancedButton.Image(TEXTURE, px, py, 161, 241 - (this.autofillmode < 5 ? 16 * this.autofillmode : 0), 13, 13))
         .tooltip(Tooltip.create(tooltip))
         .build();
      this.addRenderableWidget(this.B_AUTOFILLMODE);
      if (toSet) {
         ClientMethods.openBlueprintItemGUI(this.blueprintItem, this.posContainer, false);
      }
   }

   private void raccoltaBlocchiBlueprint() {
      if (this.BD != null && VisualizeBlueprintDatas.VisualizeBlueprint != null) {
         if (this.autofillmode == 5) {
            this.BD = VisualizeBlueprintDatas.VisualizeBlueprint;
         }

         if (this.oldCurrentRot != 0.0 && this.oldCurrentRot != 90.0 && this.oldCurrentRot != 180.0 && this.oldCurrentRot != 270.0) {
            this.BD = this.BD.rotate360(this.oldCurrentRot, this.autofillmode, null);
         } else {
            this.BD = VisualizeBlueprintDatas.VisualizeBlueprint;
         }

         this.ultimaGrandezza = this.BD.getAllBlocksCount();
         this.ItemQuantity.clear();

         for (int v = 0; v < 2; v++) {
            for (Integer altezza : v == 0 ? this.BD.BlocksData.keySet() : this.BD.NotSolidBlocksData.keySet()) {
               for (Entry<String, String> dati_blocco : v == 0
                  ? this.BD.BlocksData.get(altezza).entrySet()
                  : this.BD.NotSolidBlocksData.get(altezza).entrySet()) {
                  String blockdata = dati_blocco.getValue();
                  String bloccoId = blockdata.substring(blockdata.indexOf("<bid>") + 5, blockdata.indexOf("</bid>"));
                  blockdata = blockdata.replaceFirst("<bid>" + bloccoId + "</bid>", "");
                  BlockState state = blockdata.equals("null")
                     ? Operazioni.getBlockFromString(bloccoId).defaultBlockState()
                     : Operazioni.getPropertiesStringToBlock(Operazioni.getBlockFromString(bloccoId), blockdata);
                  if ((
                        !state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)
                           || !((DoubleBlockHalf)state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF)).equals(DoubleBlockHalf.UPPER)
                     )
                     && (!state.hasProperty(BlockStateProperties.BED_PART) || !((BedPart)state.getValue(BlockStateProperties.BED_PART)).equals(BedPart.HEAD))) {
                     ItemStack itemStack = Operazioni.getCorrectItemStackFromId(Operazioni.getBlockFromString(bloccoId));
                     if (!itemStack.is(Items.AIR)) {
                        if (this.ItemQuantity.containsKey(itemStack.getItem())) {
                           this.ItemQuantity.replace(itemStack.getItem(), this.ItemQuantity.get(itemStack.getItem()) + 1);
                        } else {
                           this.ItemQuantity.put(itemStack.getItem(), 1);
                        }
                     }
                  }
               }
            }
         }

         this.updateList();
      }
   }

   private void buildAnyway() {
      CompoundTag nbt = OldVersionUtilities.getItemStackNBT(this.blueprintItem);
      if (nbt.contains("buildAnyway")) {
         CtoS_UpdateItemBlueprintNBT.send(this.blueprintItem, "buildAnyway", !nbt.getBoolean("buildAnyway"));
         nbt.putBoolean("buildAnyway", !nbt.getBoolean("buildAnyway"));
         OldVersionUtilities.impostaItemStackNBT(this.blueprintItem, nbt);
      } else {
         CtoS_UpdateItemBlueprintNBT.send(this.blueprintItem, "buildAnyway", true);
      }
   }

   private void skipObstructionBlock() {
      CompoundTag nbt = OldVersionUtilities.getItemStackNBT(this.blueprintItem);
      if (nbt.contains("skipObstructionBlock")) {
         CtoS_UpdateItemBlueprintNBT.send(this.blueprintItem, "skipObstructionBlock", !nbt.getBoolean("skipObstructionBlock"));
         nbt.putBoolean("skipObstructionBlock", !nbt.getBoolean("skipObstructionBlock"));
         OldVersionUtilities.impostaItemStackNBT(this.blueprintItem, nbt);
      } else {
         CtoS_UpdateItemBlueprintNBT.send(this.blueprintItem, "skipObstructionBlock", true);
      }
   }

   private void RotateStructure(boolean right, boolean reset) {
      CompoundTag nbt = OldVersionUtilities.getItemStackNBT(this.blueprintItem);
      if (nbt.contains("rotateValue")) {
         double val = nbt.getDouble("rotateValue");
         if (reset) {
            val = 0.0;
         } else {
            val += this.rotstep * (right ? 1 : -1);
            if (val >= 360.0) {
               val -= 360.0;
            } else if (val < 0.0) {
               val += 360.0;
            }
         }

         this.oldCurrentRot = val;
         this.oldrotstep = -1.0;
         CtoS_UpdateItemBlueprintNBT.send(this.blueprintItem, "rotateValue", val);
         nbt.putDouble("rotateValue", val);
         OldVersionUtilities.impostaItemStackNBT(this.blueprintItem, nbt);
      }

      RenderBlueprintsPreview.doCleanupOnlyBlueprint();
      this.raccoltaBlocchiBlueprint();
   }

   private void mirrowStructure(short xMir) {
      CompoundTag nbt = OldVersionUtilities.getItemStackNBT(this.blueprintItem);
      switch (xMir) {
         case 0:
            CtoS_UpdateItemBlueprintNBT.send(this.blueprintItem, "mirrorX", !nbt.getBoolean("mirrorX"));
            nbt.putBoolean("mirrorX", !nbt.getBoolean("mirrorX"));
            break;
         case 1:
            CtoS_UpdateItemBlueprintNBT.send(this.blueprintItem, "mirrorY", !nbt.getBoolean("mirrorY"));
            nbt.putBoolean("mirrorY", !nbt.getBoolean("mirrorY"));
            break;
         case 2:
            CtoS_UpdateItemBlueprintNBT.send(this.blueprintItem, "mirrorZ", !nbt.getBoolean("mirrorZ"));
            nbt.putBoolean("mirrorZ", !nbt.getBoolean("mirrorZ"));
      }

      RenderBlueprintsPreview.doCleanupOnlyBlueprint();
   }

   private void Reload() {
      CompoundTag nbt = OldVersionUtilities.getItemStackNBT(this.blueprintItem);
      RenderBlueprintsPreview.doCleanupOnlyBlueprint();
      if (nbt.getString("owner").equals("server")) {
         System.out.println("server");
         VisualizeBlueprintDatas.newVisualizeBlueprint(true, nbt.getString("nome"), true, true);
      } else if (nbt.getString("owner").equals("worn")) {
         System.out.println("worn");
         VisualizeBlueprintDatas.newVisualizeBlueprint(false, true, nbt.getString("nome"), true, true);
      } else if (!nbt.getString("owner").equals("")) {
         System.out.println(nbt.getString("owner"));
         VisualizeBlueprintDatas.newVisualizeBlueprint(UUID.fromString(nbt.getString("owner")), nbt.getString("nome"), true, true);
      }

      List<BlueprintScreen.ItemRequestWidget> toRemove = new ArrayList<>();

      for (int x = 0; x < this.itemRequestWidgets.size(); x++) {
         if (this.itemRequestWidgets.get(x) != null) {
            this.itemRequestWidgets.get(x).remove();
         }

         toRemove.add(this.itemRequestWidgets.get(x));
      }

      for (BlueprintScreen.ItemRequestWidget r : toRemove) {
         this.itemRequestWidgets.remove(r);
      }

      this.BD = null;
      this.richiediItemQuantityContainer();
      if (this.projectEBlueprintScreen != null) {
         this.projectEBlueprintScreen.Reload();
      }

      nbt.putBoolean("visualizeBuild", true);
   }

   private void renderBG(GuiGraphics gg) {
      float t = 1.0F - (float)(this.StartTimeAnim - System.currentTimeMillis()) / 250.0F;
      if (t > 1.0F) {
         t = 1.0F;
      }

      int posXMeno = (int)Operazioni.lerp(64.0F, 0.0F, t);
      gg.blit(TEXTURE, this.CentroX - 80 + posXMeno, this.CentroY - 80, 0, 0, 16, 160);
      posXMeno -= this.projectEBlueprintScreen != null && this.projectEBlueprintScreen.EMC_Attivo ? (int)Operazioni.lerp(0.0F, 64.0F, t) : 0;
      gg.blit(TEXTURE, this.CentroX + 64 - posXMeno, this.CentroY - 80, 16, 0, 16, 160);
   }

   public void render(GuiGraphics gg, int mouseX, int mouseY, float delta) {
      this.rotstep = 90.0;
      if (hasShiftDown() && hasControlDown() && hasAltDown()) {
         this.rotstep = 0.5;
      } else if (hasShiftDown() && hasControlDown()) {
         this.rotstep = 1.0;
      } else if (hasControlDown() && hasAltDown()) {
         this.rotstep = 2.0;
      } else if (hasShiftDown() && hasAltDown()) {
         this.rotstep = 5.0;
      } else if (hasShiftDown()) {
         this.rotstep = 10.0;
      } else if (hasControlDown()) {
         this.rotstep = 22.5;
      } else if (hasAltDown()) {
         this.rotstep = 45.0;
      }

      if (this.B_ROTATE_RIGHT != null && this.B_ROTATE_LEFT != null && this.oldrotstep != this.rotstep) {
         this.oldrotstep = this.rotstep;
         CompoundTag nbt = OldVersionUtilities.getItemStackNBT(this.blueprintItem);
         if (nbt.contains("rotateValue")) {
            this.oldCurrentRot = nbt.getDouble("rotateValue");
         }

         MutableComponent tooltip = Component.translatable("gui.portable_blueprints.blueprint.button_rotate_right").withStyle(ChatFormatting.WHITE);
         tooltip.append("\n")
            .append(Component.translatable("gui.portable_blueprints.blueprint.rotation_step").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(String.format(" %.1f°", this.rotstep)).withStyle(ChatFormatting.AQUA));
         tooltip.append("\n")
            .append(Component.translatable("gui.portable_blueprints.blueprint.current_rotation").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(String.format(" %.1f°", this.oldCurrentRot)).withStyle(ChatFormatting.GOLD));
         this.B_ROTATE_RIGHT.setTooltip(Tooltip.create(tooltip));
         MutableComponent tooltip2 = Component.translatable("gui.portable_blueprints.blueprint.button_rotate_left").withStyle(ChatFormatting.WHITE);
         tooltip2.append("\n")
            .append(Component.translatable("gui.portable_blueprints.blueprint.rotation_step").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(String.format(" %.1f°", -this.rotstep)).withStyle(ChatFormatting.AQUA));
         tooltip2.append("\n")
            .append(Component.translatable("gui.portable_blueprints.blueprint.current_rotation").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(String.format(" %.1f°", this.oldCurrentRot)).withStyle(ChatFormatting.GOLD));
         this.B_ROTATE_LEFT.setTooltip(Tooltip.create(tooltip2));
      }

      if (this.B_ROTATE_RESET != null && this.B_ROTATE_RESET.isHoveredOrFocused()) {
         MutableComponent tooltip = Component.translatable("gui.portable_blueprints.blueprint.button_rotate_reset").withStyle(ChatFormatting.WHITE);
         tooltip.append("\n")
            .append(Component.translatable("gui.portable_blueprints.blueprint.current_rotation").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(String.format(" %.1f°", this.oldCurrentRot)).withStyle(ChatFormatting.GOLD));
         this.B_ROTATE_RESET.setTooltip(Tooltip.create(tooltip));
      }

      if (VisualizeBlueprintDatas.VisualizeBlueprint != null
         && (this.BD == null || this.BD.getAllBlocksCount() != this.ultimaGrandezza || !ClientMethods.ServerBlackListBlocksCompleta)) {
         this.BD = VisualizeBlueprintDatas.VisualizeBlueprint;
         this.raccoltaBlocchiBlueprint();
      }

      float t = 1.0F - (float)(this.StartTimeAnim - System.currentTimeMillis()) / 250.0F;
      if (t > 1.0F) {
         t = 1.0F;
      }

      this.renderBG(gg);
      int posXEspansione = this.projectEBlueprintScreen != null && this.projectEBlueprintScreen.EMC_Attivo ? 64 : 0;
      RenderingMethods.enableScissorFromGui(
         Minecraft.getInstance(),
         this.CentroX - 2 - (int)Operazioni.lerp(0.0F, 62.0F, t),
         this.CentroY - 72,
         3 + (int)Operazioni.lerp(0.0F, 125 + posXEspansione, t),
         144
      );
      RenderSystem.setShaderColor(this.R, this.G, this.B, 1.0F);
      gg.fill(this.CentroX - 64, this.CentroY - 72, this.CentroX - 56, this.CentroY + 72, -1140850689);
      gg.fill(this.CentroX - 56, this.CentroY - 64, this.CentroX + 56 + posXEspansione, this.CentroY + 64, -1141705998);
      gg.fill(this.CentroX - 56, this.CentroY - 72, this.CentroX + 56 + posXEspansione, this.CentroY - 64, -1140850689);
      gg.fill(this.CentroX + 64 + posXEspansione, this.CentroY - 72, this.CentroX + 56 + posXEspansione, this.CentroY + 72, -1144864062);
      gg.fill(this.CentroX - 56, this.CentroY + 72, this.CentroX + 56 + posXEspansione, this.CentroY + 64, -1144864062);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      if (this.scrollBar != null) {
         this.posYLista = (int)this.scrollBar.getValore(this.posYMaxLista);
      }

      if (this.BD != null) {
         float var10004 = this.CentroX - 60;
         float var10005 = this.CentroY - 71;
         RenderingMethods.GUITextAutoResize(gg, this.BD.nome, Minecraft.getInstance().font, 16777215, var10004, var10005, 120, 14, false);
      }

      gg.blit(TEXTURE, this.CentroX - 63, this.CentroY - 60, 32, 0, 126, 86);
      RenderSystem.setShaderColor(this.R, this.G, this.B, 1.0F);
      gg.fill(this.CentroX - 58, this.CentroY - 56, this.CentroX + 59, this.CentroY + 21, -5658199);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

      try {
         super.render(gg, mouseX, mouseY, delta);
      } catch (Exception var10) {
      }

      Map<Item, Integer> itemQuantityFromInventory = ClientMethods.ItemDentroInventari;
      if (t == 1.0F) {
         RenderingMethods.enableScissorFromGui(Minecraft.getInstance(), this.CentroX - 58, this.CentroY - 56, 117, 77);

         for (BlueprintScreen.ItemRequestWidget bw : this.itemRequestWidgets) {
            if (bw != null) {
               bw.render(gg);
               if (itemQuantityFromInventory.containsKey(bw.item)) {
                  bw.setQuantitaInInventory(itemQuantityFromInventory.get(bw.item));
               } else {
                  bw.setQuantitaInInventory(0);
               }
            }
         }

         RenderingMethods.disableScissor();
      }

      if (this.blueprintItem != null) {
         CompoundTag nbt = OldVersionUtilities.getItemStackNBT(this.blueprintItem);
         if (this.B_MIRROR_X != null) {
            this.B_MIRROR_X.PressedImageCondition = nbt.contains("mirrorX") ? nbt.getBoolean("mirrorX") : false;
         }

         if (this.B_MIRROR_Y != null) {
            this.B_MIRROR_Y.PressedImageCondition = nbt.contains("mirrorY") ? nbt.getBoolean("mirrorY") : false;
         }

         if (this.B_MIRROR_Z != null) {
            this.B_MIRROR_Z.PressedImageCondition = nbt.contains("mirrorZ") ? nbt.getBoolean("mirrorZ") : false;
         }

         if (this.B_SKIP_OBSTRUCTION_BLOCK != null) {
            this.B_SKIP_OBSTRUCTION_BLOCK.PressedImageCondition = nbt.contains("skipObstructionBlock") ? nbt.getBoolean("skipObstructionBlock") : false;
         }

         if (this.B_BUILD_ANYWAY != null) {
            this.B_BUILD_ANYWAY.PressedImageCondition = nbt.contains("buildAnyway") ? nbt.getBoolean("buildAnyway") : false;
         }

         if (this.B_BUILD != null) {
            this.B_BUILD.PressedImageCondition = this.hasOrigin;
         }
      }

      RenderingMethods.GuiGraphicsTestoScalato(
         gg,
         Component.translatable("gui.portable_blueprints.blueprint.button_build").getString(),
         1.5F,
         Minecraft.getInstance().font,
         new Vec2(this.CentroX, this.CentroY + 58),
         this.hasOrigin ? 16777215 : 9605778,
         false,
         RenderingMethods.PosizioneTesto.CENTRO,
         true
      );
      if (this.projectEBlueprintScreen != null) {
         this.projectEBlueprintScreen.render(gg, mouseX, mouseY, delta);
      }

      RenderingMethods.disableScissor();
   }

   private void updateList() {
      for (int x = 0; x < this.itemRequestWidgets.size(); x++) {
         if (this.itemRequestWidgets.get(x) != null) {
            this.itemRequestWidgets.get(x).remove();
         }
      }

      this.itemRequestWidgets.clear();
      int count = 0;

      for (Item key : this.ItemQuantity.keySet()) {
         BlueprintScreen.ItemRequestWidget irw = new BlueprintScreen.ItemRequestWidget(this.CentroX - 58, this.CentroY - 56 + 19 * count, key);
         irw.setItemNeeded(this.ItemQuantity.get(key));
         this.itemRequestWidgets.add(irw);
         count++;
      }

      this.posYMaxLista = (this.itemRequestWidgets.size() - 4) * 19;
      if (this.posYMaxLista < 0) {
         this.posYMaxLista = 0;
      }

      if (this.itemRequestWidgets.size() > 4) {
         this.addRenderableWidget(
            this.scrollBar = new TabletScrollBar(
               this.CentroX + 57,
               this.CentroY - 55,
               1,
               75,
               -12696497,
               -4406063,
               new TabletScrollBar.ScrollingArea(this.CentroX - 70, this.CentroY - 68, 140, 157, 3)
            )
         );
      } else {
         this.scrollBar = null;
      }
   }

   public void richiediItemQuantityContainer() {
      ClientMethods.mandaRichiestaItemQuantityContainer(this.posContainer);
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      return super.mouseClicked(mouseX, mouseY, button);
   }

   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      return super.mouseReleased(mouseX, mouseY, button);
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double delta, double delta2) {
      return this.scrollBar != null && this.scrollBar.mouseScrolled(mouseX, mouseY, delta2, delta) ? true : super.mouseScrolled(mouseX, mouseY, delta2, delta);
   }

   public void onClose() {
      for (BlueprintScreen.ItemRequestWidget irw : this.itemRequestWidgets) {
         if (irw != null) {
            irw.remove();
         }
      }

      super.onClose();
   }

   public boolean isPauseScreen() {
      return false;
   }

   protected void renderBlurredBackground(float partialTick) {
   }

   public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
   }

   public class ItemRequestWidget {
      public Item item;
      public int ItemNeeded = 0;
      public ScrollingText NomeScrolling;
      public ScrollingText QuantityScrolling;
      public RenderingMethods.GuiGraphicsElement Nome;
      public RenderingMethods.GuiGraphicsElement QuantityNeeded;
      public RenderingMethods.GuiGraphicsElement Sfondo;
      public int x;
      public int y;
      public boolean isRunning = true;
      public boolean completato = false;
      public int QuantitaInInventory = 0;
      public boolean blacklistItem = false;

      public ItemRequestWidget(int _x, int _y, Item item) {
         this.x = _x;
         this.y = _y;
         this.item = item;
         String id = BuiltInRegistries.ITEM.getKey(item).toString();
         String name = new ItemStack(item).getHoverName().getString();
         if (ClientMethods.ServerBlackListBlocks.contains(id)) {
            name = name + Component.translatable("gui.portable_blueprints.blueprint.blacklist_block").getString();
            this.blacklistItem = true;
         }

         this.NomeScrolling = new ScrollingText(name, 16);
         this.QuantityScrolling = new ScrollingText("", 16);
         this.Nome = new RenderingMethods.GuiGraphicsElement(
            new RenderingMethods.GuiGraphicsElement.TESTO_Parametri(
               this.x + 22,
               this.y + 5,
               this.NomeScrolling.TestoVisualizzabile,
               1.0F,
               Minecraft.getInstance().font,
               16777215,
               false,
               RenderingMethods.PosizioneTesto.SINISTRA,
               true
            )
         );
         this.QuantityNeeded = new RenderingMethods.GuiGraphicsElement(
            new RenderingMethods.GuiGraphicsElement.TESTO_Parametri(
               this.x + 22,
               this.y + 14,
               String.valueOf(this.ItemNeeded),
               1.0F,
               Minecraft.getInstance().font,
               16777215,
               false,
               RenderingMethods.PosizioneTesto.SINISTRA,
               true
            )
         );
         this.Sfondo = new RenderingMethods.GuiGraphicsElement(
            new RenderingMethods.GuiGraphicsElement.BLIT_Parametri(this.x + 1, this.y + 1, 147, 142, 109, 18, BlueprintScreen.TEXTURE)
         );
         Thread tickerThread = new Thread(() -> {
            short tickCount = 1;

            try {
               for (; this.isRunning; Thread.sleep(50L)) {
                  this.tick(tickCount);
                  if (++tickCount > 20) {
                     tickCount = 1;
                  }
               }
            } catch (InterruptedException e) {
               Thread.currentThread().interrupt();
            }
         });
         tickerThread.setDaemon(true);
         tickerThread.start();
      }

      public void render(GuiGraphics gg) {
         gg.pose().pushPose();
         gg.pose().translate(0.0F, -BlueprintScreen.this.posYLista, 0.0F);
         this.Nome
            .testoParametri
            .setTesto(
               this.blacklistItem
                  ? this.NomeScrolling.TestoVisualizzabile
                  : (this.completato ? "§m" + this.NomeScrolling.TestoVisualizzabile : this.NomeScrolling.TestoVisualizzabile)
            );
         this.Nome.testoParametri.color = this.blacklistItem ? 16711680 : (this.completato ? 65280 : 16777215);
         this.Nome.render(gg);
         String QNT = this.getStringItemNeeded();
         if (!this.QuantityScrolling.TestoVisualizzabile.equals(QNT)) {
            this.QuantityScrolling.changeText(QNT);
         }

         this.QuantityNeeded.testoParametri.setTesto(this.QuantityScrolling.TestoVisualizzabile);
         this.QuantityNeeded.render(gg);
         this.Sfondo.render(gg);
         RenderingMethods.RenderItemInGui(gg, new Vector2i(this.x + 2, this.y + 2), Vec2.ZERO, 1.0F, new ItemStack(this.item));
         gg.pose().popPose();
      }

      public void setItemNeeded(int quantity) {
         this.ItemNeeded = quantity;
      }

      private String getStringItemNeeded() {
         if (BlueprintScreen.this.hasFreeBuild) {
            this.completato = true;
            return "";
         }

         String v = "";
         int itemAttuali = this.ItemNeeded - this.QuantitaInInventory;
         if (BlueprintScreen.this.projectEBlueprintScreen != null
            && BlueprintScreen.this.projectEBlueprintScreen.EMC_Attivo
            && ProjectEMetodi.getEMCForItem(this.item) != 0L) {
            itemAttuali -= (int)(
               (itemAttuali * ProjectEMetodi.getEMCForItem(this.item) - BlueprintScreen.this.projectEBlueprintScreen.getItemEMCValue(this.item))
                  / ProjectEMetodi.getEMCForItem(this.item)
            );
            if (itemAttuali <= 0) {
               itemAttuali = 0;
            }
         }

         this.completato = itemAttuali <= 0;
         int maxStack = new ItemStack(this.item).getMaxStackSize();
         int quantitaMaxStack = (int)Math.floor(itemAttuali / maxStack);
         int restanti = itemAttuali - quantitaMaxStack * maxStack;
         if (restanti < 0) {
            restanti = 0;
         }

         if (quantitaMaxStack > 0) {
            v = v + maxStack + "x" + quantitaMaxStack;
            if (restanti > 0) {
               v = v + " + " + restanti;
            }
         } else {
            v = v + itemAttuali;
         }

         if (BlueprintScreen.this.projectEBlueprintScreen != null && BlueprintScreen.this.projectEBlueprintScreen.EMC_Attivo) {
            long lv = BlueprintScreen.this.projectEBlueprintScreen.getItemEMCValue(this.item);
            if (lv != -1L) {
               v = v + "  |  EMC: " + lv;
               if (lv == 0L) {
                  this.completato = true;
               }
            }
         }

         return this.completato ? "" : v;
      }

      private void tick(int TickCount) {
         if (TickCount % 5 == 0 && this.NomeScrolling != null) {
            this.NomeScrolling.update();
            this.QuantityScrolling.update();
         }
      }

      public void remove() {
         this.isRunning = false;
      }

      public void setQuantitaInInventory(int x) {
         this.QuantitaInInventory = x;
      }
   }
}
