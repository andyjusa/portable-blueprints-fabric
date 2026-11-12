package nimble.portable_blueprints.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.netty.buffer.ByteBuf;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.arguments.EntityAnchorArgument.Anchor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;
import nimble.portable_blueprints.fabric.network.PayloadContext;
import nimble.portable_blueprints.altro.BlockBlueprintUsage;
import nimble.portable_blueprints.altro.Blueprint;
import nimble.portable_blueprints.altro.BlueprintUsageCooldown;
import nimble.portable_blueprints.altro.ExternalBlueprintContainerMetodi;
import nimble.portable_blueprints.altro.PlacingBlockManager;
import nimble.portable_blueprints.entity.custom.BusinessVillagerEntity;
import nimble.portable_blueprints.init.ModEntities;
import nimble.portable_blueprints.init.ModGameRules;
import nimble.portable_blueprints.init.ModItems;
import nimble.portable_blueprints.mod_compatibility.ModCompatibilityInit;
import nimble.portable_blueprints.mod_compatibility.projecte.ProjectEMetodi;
import nimble.portable_blueprints.network.custom_codecs.ModCustomCodecs;
import nimble.portable_blueprints.portable_blueprints.ModServerConfig;
import nimble.portable_blueprints.portable_blueprints.OldVersionUtilities;
import nimble.portable_blueprints.robautile.Operazioni;

public record CtoS_CreateStructureByBlueprint(ItemStack blueprint_item, boolean isServerBlueprint, boolean isWornBlueprint, BlockPos origin)
   implements CustomPacketPayload {
   private static final Gson GSON = nimble.portable_blueprints.fabric.BlueprintJson.create();
   public static final Type<CtoS_CreateStructureByBlueprint> TYPE = new Type(
      ResourceLocation.fromNamespaceAndPath("portable_blueprints", "ctos_create_structure_by_blueprint")
   );
   public static final StreamCodec<ByteBuf, CtoS_CreateStructureByBlueprint> STREAM_CODEC = StreamCodec.composite(
      ModCustomCodecs.ITEM_STACK,
      CtoS_CreateStructureByBlueprint::blueprint_item,
      ByteBufCodecs.BOOL,
      CtoS_CreateStructureByBlueprint::isServerBlueprint,
      ByteBufCodecs.BOOL,
      CtoS_CreateStructureByBlueprint::isWornBlueprint,
      ModCustomCodecs.BLOCKPOS,
      CtoS_CreateStructureByBlueprint::origin,
      CtoS_CreateStructureByBlueprint::new
   );

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(CtoS_CreateStructureByBlueprint packet, PayloadContext context) {
      context.enqueueWork(
         () -> {
            ServerPlayer player = (ServerPlayer)context.player();
            ServerLevel level = player.serverLevel();
            ItemStack blueprintItem = player.getMainHandItem().copy();
            if (!blueprintItem.equals(ItemStack.EMPTY)) {
               if (BlockBlueprintUsage.PlayersBlock.contains(player.getName().getString())) {
                  player.displayClientMessage(Component.translatable("chat_message.portable_blueprints.block").withStyle(ChatFormatting.RED), false);
               } else if (BlueprintUsageCooldown.hasCooldown(player.getUUID()) != 0) {
                  player.displayClientMessage(
                     Component.literal(
                           Component.translatable("chat_message.portable_blueprints.blueprint_cooldown")
                              .getString()
                              .replace("%v%", String.valueOf(BlueprintUsageCooldown.hasCooldown(player.getUUID())))
                        )
                        .withStyle(ChatFormatting.RED),
                     false
                  );
               } else {
                  CompoundTag nbt = OldVersionUtilities.getItemStackNBT(blueprintItem);
                  BlockPos origin_pos = packet.origin;
                  boolean mirrowX = nbt.getBoolean("mirrorX");
                  boolean mirrowY = nbt.getBoolean("mirrorY");
                  boolean mirrowZ = nbt.getBoolean("mirrorZ");
                  boolean skipObstructionBlock = nbt.getBoolean("skipObstructionBlock");
                  double rotation = nbt.getDouble("rotateValue");
                  short autofillMode = nbt.getShort("autofillmode");
                  String nome = nbt.getString("nome");
                  List<BlockPos> posInventari = ExternalBlueprintContainerMetodi.getContainersBlockPosFromBlueprint(blueprintItem);

                  UUID owner_blueprint;
                  try {
                     owner_blueprint = !nbt.getString("owner").equals("") && !packet.isServerBlueprint && !packet.isWornBlueprint
                        ? UUID.fromString(nbt.getString("owner"))
                        : null;
                  } catch (Exception e) {
                     owner_blueprint = null;
                  }

                  if (!nimble.portable_blueprints.fabric.network.PayloadValidation.validName(nome)
                     || (!packet.isServerBlueprint && !packet.isWornBlueprint
                        && (owner_blueprint == null
                           || !nimble.portable_blueprints.fabric.network.PayloadValidation.canRead(player, owner_blueprint, nome)))) return;

                  boolean buildAniway = nbt.getBoolean("buildAnyway");
                  if (player.getMainHandItem() != null && player.getMainHandItem().getItem().equals(ModItems.BLUEPRINT.get())) {
                     nbt.putBoolean("visualizeBuild", false);
                     OldVersionUtilities.impostaItemStackNBT(blueprintItem, nbt);
                  }

                  File blueprintFile = Arrays.stream(
                        packet.isServerBlueprint
                           ? Blueprint.getServerBlueprintFile(level).listFiles()
                           : (
                              packet.isWornBlueprint
                                 ? Blueprint.getWornBlueprintFile(level).listFiles()
                                 : Blueprint.getFileForBlueprints(level, owner_blueprint).listFiles()
                           )
                     )
                     .filter(f -> f.getName().equals(nome + ".bp"))
                     .findFirst()
                     .orElse(null);
                  List<String> BlocksDataToRemove = new ArrayList<>();
                  List<String> NotSolidBlocksDataToRemove = new ArrayList<>();
                  if (blueprintFile != null) {
                     Map<Item, Integer> ItemDisponibili = Blueprint.getAllItemQuantityFromInventory(player, level, posInventari);
                     context.enqueueWork(
                           () -> {
                              try {
                                 Blueprint.BlueprintDatas bd = null;

                                 try (Reader reader = new FileReader(blueprintFile)) {
                                    bd = (Blueprint.BlueprintDatas)GSON.fromJson(reader, Blueprint.BlueprintDatas.class);
                                 } catch (Exception e) {
                                    e.printStackTrace();
                                 }

                                 if (rotation != 0.0) {
                                    bd = bd.rotate360(rotation, autofillMode, level);
                                 }

                                 if (mirrowX || mirrowY || mirrowZ) {
                                    bd = bd.Mirror(mirrowX, mirrowY, mirrowZ);
                                 }

                                 bd.offset = Operazioni.SommaBlockPos(bd.offset == null ? BlockPos.ZERO : bd.offset, origin_pos);
                                 boolean free_build = nbt.contains("free_build") && nbt.getBoolean("free_build");
                                 boolean emc_active = nbt.contains("projecte_active_emc") && nbt.getBoolean("projecte_active_emc");
                                 Map<Item, Integer> QuantitaItemRichiesti = new HashMap<>();
                                 long PlayerEMC = ProjectEMetodi.getEMCPlayer(player.getUUID());
                                 boolean AreaLibera = true;
                                 boolean erroreBloccoOstruzione = false;
                                 List<BlockPos> posBlocchiCheOstruiscono = new ArrayList<>();

                                 for (int alt = 0; alt <= bd.altezza && (AreaLibera || erroreBloccoOstruzione); alt++) {
                                    for (int x = 0; x < 2; x++) {
                                       boolean isSolidBlocks = x == 0;
                                       if (isSolidBlocks ? bd.BlocksData.containsKey(alt) : bd.NotSolidBlocksData.containsKey(alt)) {
                                          int counting = 0;

                                          for (String pos : isSolidBlocks ? bd.BlocksData.get(alt).keySet() : bd.NotSolidBlocksData.get(alt).keySet()) {
                                             if (!AreaLibera && !erroreBloccoOstruzione) {
                                                break;
                                             }

                                             BlockPos blockPos = new BlockPos(Operazioni.getVec3iFromString(pos));
                                             String datiBlocco = isSolidBlocks
                                                ? bd.BlocksData
                                                   .get(alt)
                                                   .get(Operazioni.getVec3iString(new Vec3i(blockPos.getX(), blockPos.getY(), blockPos.getZ())))
                                                : bd.NotSolidBlocksData
                                                   .get(alt)
                                                   .get(Operazioni.getVec3iString(new Vec3i(blockPos.getX(), blockPos.getY(), blockPos.getZ())));
                                             String bloccoId = datiBlocco.substring(datiBlocco.indexOf("<bid>") + 5, datiBlocco.indexOf("</bid>"));
                                             datiBlocco = datiBlocco.replaceFirst("<bid>" + bloccoId + "</bid>", "");
                                             if (ModServerConfig.blacklistBlock.contains(bloccoId)) {
                                                if (isSolidBlocks) {
                                                   BlocksDataToRemove.add(alt + "<daRimuovere>" + pos);
                                                } else {
                                                   NotSolidBlocksDataToRemove.add(alt + "<daRimuovere>" + pos);
                                                }
                                             } else {
                                                BlockState blockState = datiBlocco.equals("null")
                                                   ? Operazioni.getBlockFromString(bloccoId).defaultBlockState()
                                                   : Operazioni.getPropertiesStringToBlock(Operazioni.getBlockFromString(bloccoId), datiBlocco);
                                                Block blocco = Operazioni.getBlockFromString(bloccoId);
                                                BlockState PlacedBlockBlockState = level.getBlockState(Operazioni.SommaBlockPos(bd.offset, blockPos));
                                                Item bi = Operazioni.getCorrectItemStackFromId(blocco).getItem();
                                                if (QuantitaItemRichiesti.containsKey(bi)) {
                                                   QuantitaItemRichiesti.replace(bi, QuantitaItemRichiesti.get(bi) + 1);
                                                } else {
                                                   QuantitaItemRichiesti.put(bi, 1);
                                                }

                                                if (!skipObstructionBlock
                                                   && !PlacedBlockBlockState.is(Blocks.AIR)
                                                   && !PlacedBlockBlockState.is(blocco)
                                                   && !PlacingBlockManager.isRimpiazzabile(PlacedBlockBlockState)) {
                                                   AreaLibera = false;
                                                   erroreBloccoOstruzione = true;
                                                   posBlocchiCheOstruiscono.add(Operazioni.SommaBlockPos(bd.offset, blockPos));
                                                }

                                                if (!free_build
                                                   && !buildAniway
                                                   && !player.isCreative()
                                                   && !player.isSpectator()
                                                   && (
                                                      !blockState.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)
                                                         || ((DoubleBlockHalf)blockState.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF))
                                                            .equals(DoubleBlockHalf.LOWER)
                                                   )
                                                   && (
                                                      !blockState.hasProperty(BlockStateProperties.BED_PART)
                                                         || ((BedPart)blockState.getValue(BlockStateProperties.BED_PART)).equals(BedPart.FOOT)
                                                   )) {
                                                   if (ItemDisponibili.containsKey(blocco.asItem())) {
                                                      int newQuantity = ItemDisponibili.get(blocco.asItem()) - 1;
                                                      if (newQuantity <= 0) {
                                                         ItemDisponibili.remove(blocco.asItem());
                                                      } else {
                                                         ItemDisponibili.replace(blocco.asItem(), newQuantity);
                                                      }
                                                   } else if (!blocco.asItem().equals(Items.AIR)) {
                                                      if (ModCompatibilityInit.isProjectELoaded && emc_active) {
                                                         long vEMC = ProjectEMetodi.getEMCForItem(Operazioni.getCorrectItemStackFromId(blocco).getItem());
                                                         if (vEMC == 0L) {
                                                            AreaLibera = false;
                                                            player.displayClientMessage(
                                                               Component.literal(
                                                                     Component.translatable("hotbar_message.portable_blueprints.item_missing")
                                                                        .getString()
                                                                        .replace(
                                                                           "%v%",
                                                                           Operazioni.getCorrectItemStackFromId(Operazioni.getBlockFromString(bloccoId))
                                                                              .getItem()
                                                                              .toString()
                                                                        )
                                                                  )
                                                                  .withStyle(ChatFormatting.RED),
                                                               false
                                                            );
                                                         } else if (PlayerEMC >= vEMC) {
                                                            PlayerEMC -= vEMC;
                                                         } else {
                                                            AreaLibera = false;
                                                            player.displayClientMessage(
                                                               Component.literal(
                                                                     Component.translatable("gui.portable_blueprints.compatibility.projecte.emc_missing")
                                                                        .getString()
                                                                        .replace(
                                                                           "%v%",
                                                                           Operazioni.getCorrectItemStackFromId(Operazioni.getBlockFromString(bloccoId))
                                                                              .getItem()
                                                                              .toString()
                                                                        )
                                                                  )
                                                                  .withStyle(ChatFormatting.RED),
                                                               false
                                                            );
                                                         }
                                                      } else {
                                                         AreaLibera = false;
                                                         player.displayClientMessage(
                                                            Component.literal(
                                                                  Component.translatable("hotbar_message.portable_blueprints.item_missing")
                                                                     .getString()
                                                                     .replace(
                                                                        "%v%",
                                                                        Operazioni.getCorrectItemStackFromId(Operazioni.getBlockFromString(bloccoId))
                                                                           .getItem()
                                                                           .toString()
                                                                     )
                                                               )
                                                               .withStyle(ChatFormatting.RED),
                                                            false
                                                         );
                                                      }
                                                   }

                                                   if (++counting % 50 == 0) {
                                                      // World checks stay on the server thread.
                                                   }
                                                }
                                             }
                                          }
                                       }
                                    }


                                 }

                                 if (AreaLibera) {
                                    if (packet.isWornBlueprint) {
                                       for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                                          ItemStack stack = player.getInventory().getItem(i);
                                          if (!stack.isEmpty()
                                             && ItemStack.isSameItem(stack, blueprintItem)
                                             && OldVersionUtilities.getItemStackNBT(stack)
                                                .getString("blueprint_name")
                                                .equals(OldVersionUtilities.getItemStackNBT(blueprintItem).getString("blueprint_name"))) {
                                             CompoundTag _nbt = OldVersionUtilities.getItemStackNBT(stack);
                                             if (_nbt.contains("remaining_uses")) {
                                                int uses = _nbt.getInt("remaining_uses") - 1;
                                                _nbt.putInt("remaining_uses", uses);
                                                OldVersionUtilities.impostaItemStackNBT(stack, _nbt);
                                                if (uses <= 0) {
                                                   level.playSound(null, player.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.AMBIENT, 1.0F, 2.0F);
                                                   player.getInventory().removeItem(stack);
                                                }
                                             }
                                             break;
                                          }
                                       }
                                    }

                                    for (String elemento : BlocksDataToRemove) {
                                       String[] split = elemento.split("<daRimuovere>");
                                       bd.BlocksData.get(Integer.parseInt(split[0])).remove(split[1]);
                                    }

                                    for (String elemento : NotSolidBlocksDataToRemove) {
                                       String[] split = elemento.split("<daRimuovere>");
                                       bd.NotSolidBlocksData.get(Integer.parseInt(split[0])).remove(split[1]);
                                    }

                                    if (!player.hasPermissions(1)) {
                                       BlueprintUsageCooldown.setInCooldown(level, player.getUUID());
                                    }

                                    Blueprint.AggiungiStructureAllaCoda(
                                       level,
                                       bd,
                                       player,
                                       posInventari,
                                       free_build,
                                       (short)(nbt.contains("allow_nbt") ? (nbt.getBoolean("allow_nbt") ? 1 : 0) : -1),
                                       emc_active
                                    );
                                 } else if (erroreBloccoOstruzione) {
                                    player.displayClientMessage(
                                       Component.literal(
                                             Component.translatable("chat_message.portable_blueprints.block_obstruction")
                                                .getString()
                                                .replace("%v%", String.valueOf(posBlocchiCheOstruiscono.size()))
                                          )
                                          .withStyle(ChatFormatting.RED),
                                       false
                                    );
                                    if (posBlocchiCheOstruiscono.size() > 20 && level.getGameRules().getBoolean(ModGameRules.BusinessVillagerEnabled)) {
                                       BusinessVillagerEntity bve = (BusinessVillagerEntity)((EntityType)ModEntities.BUSINESS_VILLAGER.get()).create(level);
                                       if (bve != null) {
                                          bve.setTargetBP(player.getUUID());
                                          Vec3 look = player.getLookAngle().normalize();
                                          double x = player.getX() + look.x * 2.0;
                                          double y = player.getY();
                                          double z = player.getZ() + look.z * 2.0;
                                          bve.moveTo(x, y, z, player.getYRot(), 10.0F);
                                          bve.lookAt(Anchor.EYES, player.position());
                                          level.addFreshEntity(bve);
                                       }
                                    }

                                    StoC_ObstructionBlock.send(player);
                                    int count = 0;

                                    for (BlockPos pos : posBlocchiCheOstruiscono) {
                                       StoC_ObstructionBlock.send(pos, player);
                                    }
                                 }
                              } catch (Exception e) {
                                 e.printStackTrace();
                              }
                           }
                        );
                  }
               }
            }
         }
      );
   }
}
