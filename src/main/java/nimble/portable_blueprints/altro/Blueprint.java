package nimble.portable_blueprints.altro;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;
import org.jetbrains.annotations.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.Vec3;
import nimble.portable_blueprints.fabric.inventory.ItemHandlers;
import net.minecraft.server.MinecraftServer;
import nimble.portable_blueprints.fabric.inventory.ItemHandler;
import nimble.portable_blueprints.fabric.network.PacketDistributor;
import nimble.portable_blueprints.client.ClientMethods;
import nimble.portable_blueprints.client.ServerToClientDatasShared;
import nimble.portable_blueprints.init.ModItems;
import nimble.portable_blueprints.init.ModToast;
import nimble.portable_blueprints.mod_compatibility.ModCompatibilityInit;
import nimble.portable_blueprints.mod_compatibility.projecte.ProjectEMetodi;
import nimble.portable_blueprints.mod_compatibility.sophisticatedbackpacks.SophisticatedBackpacksMetodi;
import nimble.portable_blueprints.network.StoC_AnimazionePosizionamentoBlocco;
import nimble.portable_blueprints.network.StoC_ScannerAnimation;
import nimble.portable_blueprints.network.global_blueprint_net.CtoS_SaveGlobalBlueprintInServer;
import nimble.portable_blueprints.network.progress_bar_player.StoC_ProgressBarPlayer;
import nimble.portable_blueprints.portable_blueprints.MOD;
import nimble.portable_blueprints.portable_blueprints.ModServerConfig;
import nimble.portable_blueprints.portable_blueprints.OldVersionUtilities;
import nimble.portable_blueprints.robautile.ModifiableItemInventory;
import nimble.portable_blueprints.robautile.Operazioni;

public class Blueprint {
   private static final Gson GSON = nimble.portable_blueprints.fabric.BlueprintJson.create();
   public static final String extension = ".bp";
   public static final String extension_swm = ".swm";
   private static final int MAX_BLOCKS_FOR_TICK = 500;
   public static List<Block> ILLEGAL_BLOCK_IN_SURVIVAL = new ArrayList<>();
   private static List<Blueprint.BlueprintStructure> structureDaFare = new ArrayList<>();
   private static Map<Blueprint.BlueprintStartingData, Blueprint.BlueprintDatas> creazioneDati = new HashMap<>();

   public static void resetQueues() {
      structureDaFare.clear();
      creazioneDati.clear();
   }

   public static void setup() {
      ILLEGAL_BLOCK_IN_SURVIVAL.add(Blocks.BEDROCK);
      ILLEGAL_BLOCK_IN_SURVIVAL.add(Blocks.END_PORTAL);
      ILLEGAL_BLOCK_IN_SURVIVAL.add(Blocks.END_PORTAL_FRAME);
      ILLEGAL_BLOCK_IN_SURVIVAL.add(Blocks.NETHER_PORTAL);
      ILLEGAL_BLOCK_IN_SURVIVAL.add(Blocks.COMMAND_BLOCK);
      ILLEGAL_BLOCK_IN_SURVIVAL.add(Blocks.REPEATING_COMMAND_BLOCK);
      ILLEGAL_BLOCK_IN_SURVIVAL.add(Blocks.CHAIN_COMMAND_BLOCK);
      ILLEGAL_BLOCK_IN_SURVIVAL.add(Blocks.BARRIER);
      ILLEGAL_BLOCK_IN_SURVIVAL.add(Blocks.STRUCTURE_BLOCK);
      ILLEGAL_BLOCK_IN_SURVIVAL.add(Blocks.STRUCTURE_VOID);
      ILLEGAL_BLOCK_IN_SURVIVAL.add(Blocks.JIGSAW);
      ILLEGAL_BLOCK_IN_SURVIVAL.add(Blocks.LIGHT);
      ILLEGAL_BLOCK_IN_SURVIVAL.add(Blocks.MOVING_PISTON);
      ILLEGAL_BLOCK_IN_SURVIVAL.add(Blocks.FROSTED_ICE);
   }

   public static void CreateBlueprint(
      ServerLevel level, BlockPos angolo1, BlockPos angolo2, String nome, UUID richiedente, boolean consideraCreativeBlocks, List<String> ScannerIgnoredBlocks
   ) {
      BlockPos angoloIniziale = new BlockPos(
         Math.min(angolo1.getX(), angolo2.getX()), Math.min(angolo1.getY(), angolo2.getY()), Math.min(angolo1.getZ(), angolo2.getZ())
      );
      BlockPos angoloFinale = new BlockPos(
         Math.max(angolo1.getX(), angolo2.getX()), Math.max(angolo1.getY(), angolo2.getY()), Math.max(angolo1.getZ(), angolo2.getZ())
      );
      UUID uuidAnimazioneScanner = UUID.randomUUID();
      if (level.getPlayerByUUID(richiedente) != null) {
         StoC_ScannerAnimation.send(uuidAnimazioneScanner, true, angoloIniziale, angoloFinale, (ServerPlayer)level.getPlayerByUUID(richiedente));
      }

      Blueprint.BlueprintStartingData bsd = new Blueprint.BlueprintStartingData(
         level, nome, angoloIniziale, angoloFinale, richiedente, consideraCreativeBlocks, ScannerIgnoredBlocks, uuidAnimazioneScanner
      );
      if (!creazioneDati.containsKey(bsd)) {
         Blueprint.BlueprintDatas bd = new Blueprint.BlueprintDatas(nome);
         bd.setX(angoloFinale.getX() - angoloIniziale.getX() + 1);
         bd.setZ(angoloFinale.getZ() - angoloIniziale.getZ() + 1);
         bd.altezza = angoloFinale.getY() - angoloIniziale.getY() + 1;
         creazioneDati.put(bsd, bd);
         PacketDistributor.sendToPlayer(
            (ServerPlayer)level.getPlayerByUUID(richiedente),
            new StoC_ProgressBarPlayer(4, bd.lunghezzaX * bd.altezza * bd.lunghezzaZ, true),
            new CustomPacketPayload[0]
         );
      }
   }

   public static void onServerTick(MinecraftServer server) {
      if (!creazioneDati.isEmpty()) {
         Blueprint.BlueprintStartingData key = null;
         Iterator nBlocchiArrivati = creazioneDati.keySet().iterator();
         if (nBlocchiArrivati.hasNext()) {
            Blueprint.BlueprintStartingData k = (Blueprint.BlueprintStartingData)nBlocchiArrivati.next();
            key = k;
         }

         if (key.serverLevel.getPlayerByUUID(key.uuid) == null) {
            creazioneDati.remove(key);
            PacketDistributor.sendToPlayer(
               (ServerPlayer)key.serverLevel.getPlayerByUUID(key.uuid), new StoC_ProgressBarPlayer(4, 0, true), new CustomPacketPayload[0]
            );
            key = null;
         }

         if (key != null) {
            int controllati = 0;
            ServerPlayer player = (ServerPlayer)key.serverLevel.getPlayerByUUID(key.uuid);

            while (key.posYControllata != key.angolo2.getY() + 1) {
               while (key.posXControllata != key.angolo2.getX() + 1) {
                  while (key.posZControllata != key.angolo2.getZ() + 1 && controllati < 500) {
                     BlockPos position = new BlockPos(key.posXControllata, key.posYControllata, key.posZControllata);
                     BlockState blockState = key.serverLevel.getBlockState(position);
                     if (blockState.getBlock().equals(Blocks.BUBBLE_COLUMN)) {
                        blockState = Blocks.WATER.defaultBlockState();
                     }

                     if (!blockState.is(Blocks.AIR)
                        && (key.consideraCreativeBlocks || !key.consideraCreativeBlocks && !ILLEGAL_BLOCK_IN_SURVIVAL.contains(blockState.getBlock()))
                        && !key.scannerIgnoredBlocks.contains(BuiltInRegistries.BLOCK.getKey(blockState.getBlock()).toString())) {
                        if (blockState.hasProperty(BlockStateProperties.LEVEL)) {
                           if (blockState.getBlock() instanceof LiquidBlock && (Integer)blockState.getValue(BlockStateProperties.LEVEL) != 0) {
                              key.posZControllata++;
                              controllati++;
                              continue;
                           }

                           blockState = (BlockState)blockState.setValue(BlockStateProperties.LEVEL, 0);
                        }

                        if (blockState.hasProperty(BlockStateProperties.LEVEL_CAULDRON)) {
                           if (!blockState.getBlock().equals(Blocks.WATER_CAULDRON) && !blockState.getBlock().equals(Blocks.LAVA_CAULDRON)) {
                              blockState = (BlockState)blockState.setValue(BlockStateProperties.LEVEL_CAULDRON, 0);
                           } else {
                              blockState = Blocks.CAULDRON.defaultBlockState();
                           }
                        }

                        if (blockState.hasProperty(BlockStateProperties.LEVEL_COMPOSTER)) {
                           blockState = (BlockState)blockState.setValue(BlockStateProperties.LEVEL_COMPOSTER, 0);
                        }

                        if (blockState.hasProperty(BlockStateProperties.LEVEL_FLOWING)) {
                           blockState = (BlockState)blockState.setValue(BlockStateProperties.LEVEL_FLOWING, 0);
                        }

                        if (blockState.hasProperty(BlockStateProperties.LEVEL_HONEY)) {
                           blockState = (BlockState)blockState.setValue(BlockStateProperties.LEVEL_HONEY, 0);
                        }

                        if (blockState.hasProperty(BlockStateProperties.AGE_1)) {
                           blockState = (BlockState)blockState.setValue(BlockStateProperties.AGE_1, 0);
                        }

                        if (blockState.hasProperty(BlockStateProperties.AGE_2)) {
                           blockState = (BlockState)blockState.setValue(BlockStateProperties.AGE_2, 0);
                        }

                        if (blockState.hasProperty(BlockStateProperties.AGE_3)) {
                           blockState = (BlockState)blockState.setValue(BlockStateProperties.AGE_3, 0);
                        }

                        if (blockState.hasProperty(BlockStateProperties.AGE_4)) {
                           blockState = (BlockState)blockState.setValue(BlockStateProperties.AGE_4, 0);
                        }

                        if (blockState.hasProperty(BlockStateProperties.AGE_5)) {
                           blockState = (BlockState)blockState.setValue(BlockStateProperties.AGE_5, 0);
                        }

                        if (blockState.hasProperty(BlockStateProperties.AGE_7)) {
                           blockState = (BlockState)blockState.setValue(BlockStateProperties.AGE_7, 0);
                        }

                        if (blockState.hasProperty(BlockStateProperties.AGE_15)) {
                           blockState = (BlockState)blockState.setValue(BlockStateProperties.AGE_15, 0);
                        }

                        if (blockState.hasProperty(BlockStateProperties.AGE_25)) {
                           blockState = (BlockState)blockState.setValue(BlockStateProperties.AGE_25, 0);
                        }

                        String nbt = "";
                        BlockEntity blockEntity = key.serverLevel.getBlockEntity(position);
                        if (blockEntity != null && ItemHandlers.find(key.serverLevel, position, Direction.NORTH) == null) {
                           nbt = OldVersionUtilities.getAllComponentasAndNBTInJson(blockEntity);
                        }

                        if (!MOD.ListForcedNotSolidBlock.contains(BuiltInRegistries.BLOCK.getKey(blockState.getBlock()).toString())
                           && !blockState.getPistonPushReaction().name().equals("DESTROY")
                           && !blockState.getCollisionShape(key.serverLevel, position).isEmpty()) {
                           creazioneDati.get(key)
                              .addBlock(
                                 key.altezzaArrivato,
                                 Operazioni.getVec3iString(
                                    new Vec3i(
                                       key.posXControllata - key.angolo1.getX(),
                                       key.posYControllata - key.angolo1.getY(),
                                       key.posZControllata - key.angolo1.getZ()
                                    )
                                 ),
                                 Operazioni.getBlockStateString(blockState),
                                 nbt
                              );
                        } else {
                           creazioneDati.get(key)
                              .addNonSolidBlock(
                                 key.altezzaArrivato,
                                 Operazioni.getVec3iString(
                                    new Vec3i(
                                       key.posXControllata - key.angolo1.getX(),
                                       key.posYControllata - key.angolo1.getY(),
                                       key.posZControllata - key.angolo1.getZ()
                                    )
                                 ),
                                 Operazioni.getBlockStateString(blockState),
                                 nbt
                              );
                        }
                     }

                     if (key.serverLevel.getPlayerByUUID(key.uuid) != null) {
                        if (!key.annullaAnimazioneScanner) {
                           StoC_ScannerAnimation.send(key.uuidAnimazioneScanner, (ServerPlayer)key.serverLevel.getPlayerByUUID(key.uuid));
                        }
                     } else {
                        key.annullaAnimazioneScanner = true;
                     }

                     PacketDistributor.sendToPlayer(player, new StoC_ProgressBarPlayer(4, 0, false), new CustomPacketPayload[0]);
                     key.posZControllata++;
                     controllati++;
                  }

                  if (controllati >= 500) {
                     break;
                  }

                  key.posXControllata++;
                  key.posZControllata = key.angolo1.getZ();
               }

               if (controllati >= 500) {
                  break;
               }

               key.posYControllata++;
               key.altezzaArrivato++;
               key.posXControllata = key.angolo1.getX();
            }

            if (controllati < 500) {
               File jsonFile = new File(getFileForBlueprints(key.serverLevel, key.uuid), key.nome + ".bp");

               try (FileWriter writer = new FileWriter(jsonFile)) {
                  GSON.toJson(creazioneDati.get(key), writer);
               } catch (IOException e) {
                  e.printStackTrace();
               }

               if (key.serverLevel.getPlayerByUUID(key.uuid) != null) {
                  PacketDistributor.sendToPlayer(player, new StoC_ProgressBarPlayer(4, 0, true), new CustomPacketPayload[0]);
                  StoC_ScannerAnimation.send(
                     key.uuidAnimazioneScanner, false, BlockPos.ZERO, BlockPos.ZERO, (ServerPlayer)key.serverLevel.getPlayerByUUID(key.uuid)
                  );
               }

               creazioneDati.remove(key);
            }
         }
      } else if (structureDaFare.size() > 0) {
         List<Blueprint.BlueprintStructure> toRemove = new ArrayList<>();
         int nBlocchiArrivati = 0;
         boolean skip = false;

         for (Blueprint.BlueprintStructure bs : structureDaFare) {
            if (skip) {
               break;
            }

            if (!playerIsOnline(bs.owner)) {
               toRemove.add(bs);
            } else {
               List<BlockPos> blocchiDaScartareDaEspansione = new ArrayList<>();

               for (BlockPos posBlocco : new ArrayList<>(bs.posDiEspansione)) {
                  if (skip) {
                     break;
                  }

                  BlockState b = bs.serverLevel.getBlockState(Operazioni.SommaBlockPos(bs.bd.offset, posBlocco));
                  if (!b.is(Blocks.AIR) && !PlacingBlockManager.isRimpiazzabile(b)) {
                     if (playerIsOnline(bs.owner)
                        && (
                           bs.solidi
                                 && bs.bd.BlocksData.containsKey(posBlocco.getY())
                                 && bs.bd
                                    .BlocksData
                                    .get(posBlocco.getY())
                                    .containsKey(Operazioni.getVec3iString(new Vec3i(posBlocco.getX(), posBlocco.getY(), posBlocco.getZ())))
                              || !bs.solidi
                                 && bs.bd.NotSolidBlocksData.containsKey(posBlocco.getY())
                                 && bs.bd
                                    .NotSolidBlocksData
                                    .get(posBlocco.getY())
                                    .containsKey(Operazioni.getVec3iString(new Vec3i(posBlocco.getX(), posBlocco.getY(), posBlocco.getZ())))
                        )) {
                        PacketDistributor.sendToPlayer((ServerPlayer)bs.owner, new StoC_ProgressBarPlayer(0, 0, false), new CustomPacketPayload[0]);
                     }
                  } else if (bs.solidi
                     && bs.bd.BlocksData.containsKey(posBlocco.getY())
                     && bs.bd
                        .BlocksData
                        .get(posBlocco.getY())
                        .containsKey(Operazioni.getVec3iString(new Vec3i(posBlocco.getX(), posBlocco.getY(), posBlocco.getZ())))) {
                     String datiBlocco = bs.bd
                        .BlocksData
                        .get(posBlocco.getY())
                        .get(Operazioni.getVec3iString(new Vec3i(posBlocco.getX(), posBlocco.getY(), posBlocco.getZ())));
                     String bloccoId = datiBlocco.substring(datiBlocco.indexOf("<bid>") + 5, datiBlocco.indexOf("</bid>"));
                     datiBlocco = datiBlocco.replaceFirst("<bid>" + bloccoId + "</bid>", "");
                     BlockState blockState = datiBlocco.equals("null")
                        ? Operazioni.getBlockFromString(bloccoId).defaultBlockState()
                        : Operazioni.getPropertiesStringToBlock(Operazioni.getBlockFromString(bloccoId), datiBlocco);
                     Vec3 posizioneAnim = Vec3.ZERO;
                     boolean item_trovato = false;
                     if (!playerIsOnline(bs.owner)) {
                        toRemove.add(bs);
                        continue;
                     }

                     posizioneAnim = bs.owner.position().add(0.0, 1.0, 0.0);
                     if (bs.free_build || ILLEGAL_BLOCK_IN_SURVIVAL.contains(blockState.getBlock()) && bs.owner.isCreative() || bs.owner.isSpectator()) {
                        item_trovato = true;
                     } else if (ILLEGAL_BLOCK_IN_SURVIVAL.contains(blockState.getBlock())) {
                        item_trovato = false;
                     } else if (!bs.owner.isCreative()
                        && !bs.owner.isSpectator()
                        && (
                           !blockState.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)
                              || !((DoubleBlockHalf)blockState.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF)).equals(DoubleBlockHalf.UPPER)
                        )
                        && (
                           !blockState.hasProperty(BlockStateProperties.BED_PART)
                              || !((BedPart)blockState.getValue(BlockStateProperties.BED_PART)).equals(BedPart.HEAD)
                        )) {
                        Tuple<Boolean, Vec3> v = removeItemFromTheContainers(
                           (ServerPlayer)bs.owner,
                           bs.serverLevel,
                           bs.posizione_container,
                           Operazioni.getCorrectItemStackFromId(Operazioni.getBlockFromString(bloccoId)).getItem()
                        );
                        if (!(Boolean)v.getA()
                           && bs.emc_active
                           && ProjectEMetodi.removeEMCForItem(
                              (ServerPlayer)bs.owner, Operazioni.getCorrectItemStackFromId(Operazioni.getBlockFromString(bloccoId)).getItem()
                           )) {
                           item_trovato = true;
                        } else {
                           item_trovato = (Boolean)v.getA();
                           posizioneAnim = (Vec3)v.getB();
                        }
                     } else {
                        item_trovato = true;
                     }

                     if (item_trovato) {
                        if (!blockState.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)
                           || blockState.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER) {
                           BlockPos p = Operazioni.SommaBlockPos(bs.bd.offset, posBlocco);
                           PacketDistributor.sendToAllPlayers(
                              new StoC_AnimazionePosizionamentoBlocco(
                                 bloccoId, System.currentTimeMillis() + 1000L, posizioneAnim, new Vec3(p.getX() + 0.5F, p.getY() + 0.5F, p.getZ() + 0.5F)
                              ),
                              new CustomPacketPayload[0]
                           );
                        }

                        PacketDistributor.sendToPlayer((ServerPlayer)bs.owner, new StoC_ProgressBarPlayer(0, 0, false), new CustomPacketPayload[0]);
                        String nbt = bs.bd.BlocksNBT != null
                              && bs.bd.BlocksNBT.containsKey(posBlocco.getY())
                              && bs.bd
                                 .BlocksNBT
                                 .get(posBlocco.getY())
                                 .containsKey(Operazioni.getVec3iString(new Vec3i(posBlocco.getX(), posBlocco.getY(), posBlocco.getZ())))
                           ? bs.bd
                              .BlocksNBT
                              .get(posBlocco.getY())
                              .get(Operazioni.getVec3iString(new Vec3i(posBlocco.getX(), posBlocco.getY(), posBlocco.getZ())))
                           : "";
                        PlacingBlockManager.addNewPlaceBlock(
                           (ServerPlayer)bs.owner, bs.serverLevel, Operazioni.SommaBlockPos(bs.bd.offset, posBlocco), blockState, nbt, bs.allow_nbt, 1000L
                        );
                     }
                  } else if (!bs.solidi
                     && bs.bd.NotSolidBlocksData.containsKey(posBlocco.getY())
                     && bs.bd
                        .NotSolidBlocksData
                        .get(posBlocco.getY())
                        .containsKey(Operazioni.getVec3iString(new Vec3i(posBlocco.getX(), posBlocco.getY(), posBlocco.getZ())))) {
                     String datiBlocco = bs.bd
                        .NotSolidBlocksData
                        .get(posBlocco.getY())
                        .get(Operazioni.getVec3iString(new Vec3i(posBlocco.getX(), posBlocco.getY(), posBlocco.getZ())));
                     String bloccoId = datiBlocco.substring(datiBlocco.indexOf("<bid>") + 5, datiBlocco.indexOf("</bid>"));
                     datiBlocco = datiBlocco.replaceFirst("<bid>" + bloccoId + "</bid>", "");
                     BlockState blockState = datiBlocco.equals("null")
                        ? Operazioni.getBlockFromString(bloccoId).defaultBlockState()
                        : Operazioni.getPropertiesStringToBlock(Operazioni.getBlockFromString(bloccoId), datiBlocco);
                     Vec3 posizioneAnim = Vec3.ZERO;
                     boolean item_trovato = false;
                     if (!playerIsOnline(bs.owner)) {
                        toRemove.add(bs);
                        continue;
                     }

                     posizioneAnim = bs.owner.position().add(0.0, 1.0, 0.0);
                     if (bs.free_build || ILLEGAL_BLOCK_IN_SURVIVAL.contains(blockState.getBlock()) && bs.owner.isCreative() || bs.owner.isSpectator()) {
                        item_trovato = true;
                     } else if (ILLEGAL_BLOCK_IN_SURVIVAL.contains(blockState.getBlock())) {
                        item_trovato = false;
                     } else if (!bs.owner.isCreative()
                        && !bs.owner.isSpectator()
                        && (
                           !blockState.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)
                              || !((DoubleBlockHalf)blockState.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF)).equals(DoubleBlockHalf.UPPER)
                        )
                        && (
                           !blockState.hasProperty(BlockStateProperties.BED_PART)
                              || !((BedPart)blockState.getValue(BlockStateProperties.BED_PART)).equals(BedPart.HEAD)
                        )) {
                        Tuple<Boolean, Vec3> v = removeItemFromTheContainers(
                           (ServerPlayer)bs.owner,
                           bs.serverLevel,
                           bs.posizione_container,
                           Operazioni.getCorrectItemStackFromId(Operazioni.getBlockFromString(bloccoId)).getItem()
                        );
                        if (!(Boolean)v.getA()
                           && bs.emc_active
                           && ProjectEMetodi.removeEMCForItem(
                              (ServerPlayer)bs.owner, Operazioni.getCorrectItemStackFromId(Operazioni.getBlockFromString(bloccoId)).getItem()
                           )) {
                           item_trovato = true;
                        } else {
                           item_trovato = (Boolean)v.getA();
                           posizioneAnim = (Vec3)v.getB();
                        }
                     } else {
                        item_trovato = true;
                     }

                     if (item_trovato) {
                        if (!blockState.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)
                           || blockState.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER) {
                           BlockPos p = Operazioni.SommaBlockPos(bs.bd.offset, posBlocco);
                           PacketDistributor.sendToAllPlayers(
                              new StoC_AnimazionePosizionamentoBlocco(
                                 bloccoId, System.currentTimeMillis() + 1000L, posizioneAnim, new Vec3(p.getX() + 0.5F, p.getY() + 0.5F, p.getZ() + 0.5F)
                              ),
                              new CustomPacketPayload[0]
                           );
                        }

                        PacketDistributor.sendToPlayer((ServerPlayer)bs.owner, new StoC_ProgressBarPlayer(0, 0, false), new CustomPacketPayload[0]);
                        String nbt = bs.bd.BlocksNBT != null
                              && bs.bd.BlocksNBT.containsKey(posBlocco.getY())
                              && bs.bd
                                 .BlocksNBT
                                 .get(posBlocco.getY())
                                 .containsKey(Operazioni.getVec3iString(new Vec3i(posBlocco.getX(), posBlocco.getY(), posBlocco.getZ())))
                           ? bs.bd
                              .BlocksNBT
                              .get(posBlocco.getY())
                              .get(Operazioni.getVec3iString(new Vec3i(posBlocco.getX(), posBlocco.getY(), posBlocco.getZ())))
                           : "";
                        PlacingBlockManager.addNewPlaceBlock(
                           (ServerPlayer)bs.owner, bs.serverLevel, Operazioni.SommaBlockPos(bs.bd.offset, posBlocco), blockState, nbt, bs.allow_nbt, 1000L
                        );
                     }
                  }

                  if (posBlocco.getX() - 1 >= 0) {
                     BlockPos newPos = Operazioni.SommaBlockPos(posBlocco, new BlockPos(-1, 0, 0));
                     if (!bs.posDiEspansione.contains(newPos)) {
                        bs.posDiEspansione.add(newPos);
                     }
                  }

                  if (posBlocco.getZ() - 1 >= 0) {
                     BlockPos newPos = Operazioni.SommaBlockPos(posBlocco, new BlockPos(0, 0, -1));
                     if (!bs.posDiEspansione.contains(newPos)) {
                        bs.posDiEspansione.add(newPos);
                     }
                  }

                  if (posBlocco.getY() + 1 < bs.bd.altezza) {
                     BlockPos newPos = Operazioni.SommaBlockPos(posBlocco, new BlockPos(0, 1, 0));
                     if (!bs.posDiEspansione.contains(newPos)) {
                        bs.posDiEspansione.add(newPos);
                     }
                  }

                  if (posBlocco.getX() - 1 >= 0 && posBlocco.getZ() - 1 >= 0) {
                     BlockPos newPos = Operazioni.SommaBlockPos(posBlocco, new BlockPos(-1, 0, -1));
                     if (!bs.posDiEspansione.contains(newPos)) {
                        bs.posDiEspansione.add(newPos);
                     }
                  }

                  blocchiDaScartareDaEspansione.add(posBlocco);
                  if (++nBlocchiArrivati >= 500) {
                     skip = true;
                  }
               }

               for (BlockPos posDelet : blocchiDaScartareDaEspansione) {
                  bs.posDiEspansione.remove(posDelet);
               }

               if (bs.solidi && bs.posDiEspansione.size() == 0) {
                  bs.solidi = false;
                  bs.posDiEspansione.add(new BlockPos(bs.bd.lunghezzaX - 1, 0, bs.bd.lunghezzaZ - 1));
               }

               if (bs.posDiEspansione == null || bs.posDiEspansione.size() == 0) {
                  toRemove.add(bs);
               }
            }
         }

         for (Blueprint.BlueprintStructure b : toRemove) {
            PacketDistributor.sendToPlayer((ServerPlayer)b.owner, new StoC_ProgressBarPlayer(0, 0, true), new CustomPacketPayload[0]);
            structureDaFare.remove(b);
         }
      }
   }

   private static boolean playerIsOnline(Player player) {
      return player instanceof ServerPlayer serverPlayer ? serverPlayer.getServer().getPlayerList().getPlayer(serverPlayer.getUUID()) != null : false;
   }

   public static File getFileForBlueprints(ServerLevel level, UUID uuid) {
      File CartellaMod = new File(level.getServer().getWorldPath(LevelResource.ROOT).toFile(), "portable_blueprints");
      if (!CartellaMod.exists()) {
         CartellaMod.mkdir();
      }

      CartellaMod = new File(CartellaMod, "blueprints");
      if (!CartellaMod.exists()) {
         CartellaMod.mkdir();
      }

      CartellaMod = new File(CartellaMod, uuid.toString());
      if (!CartellaMod.exists()) {
         CartellaMod.mkdir();
      }

      return CartellaMod;
   }

   public static boolean BlueprintExist(ServerLevel level, String nome, UUID richiedente) {
      File cartella = getFileForBlueprints(level, richiedente);

      for (File child : cartella.listFiles()) {
         if (child.getName().equals(nome + ".bp")) {
            return true;
         }
      }

      return false;
   }

   public static void RimuoviBlueprint(ServerLevel level, ServerPlayer player, String nome) {
      Blueprint.SharedWithMe swm = getSharedBlueprints(level, player.getUUID());

      for (UUID shared_with : swm.getAllSpecificSWOBPlayersUUID(nome)) {
         removeSharedBlueprint(level, nome, player.getUUID(), shared_with);
      }

      for (File f : getFileForBlueprints(level, player.getUUID()).listFiles()) {
         if (f.getName().equals(nome + ".bp")) {
            f.delete();
            break;
         }
      }
   }

   public static void CreaItemBlueprintEDaiAGiocatore(ServerPlayer player, UUID owner, String nome, int altezza, int lunghezzaX, int lunghezzaZ) {
      ItemStack blueprint = nome.contains("[Global]")
         ? new ItemStack((ItemLike)ModItems.GLOBAL_BLUEPRINT.get(), 1)
         : new ItemStack((ItemLike)ModItems.BLUEPRINT.get(), 1);
      if (blueprint.getItem() instanceof nimble.portable_blueprints.items.Blueprint blueprintItem) {
         blueprintItem.setBlueprint(blueprint, player, owner, nome, altezza, lunghezzaX, lunghezzaZ);
      }

      boolean added = player.getInventory().add(blueprint);
      if (!added) {
         player.sendSystemMessage(Component.translatable("system.portable_blueprints.inv_full"), true);
      }
   }

   public static void CreaItemServerBlueprintEDaiAGiocatore(ServerPlayer player, String nome, int altezza, int lunghezzaX, int lunghezzaZ) {
      ItemStack blueprint = new ItemStack((ItemLike)ModItems.SERVER_BLUEPRINT.get(), 1);
      if (blueprint.getItem() instanceof nimble.portable_blueprints.items.Blueprint blueprintItem) {
         blueprintItem.setServerBlueprint(blueprint, nome, altezza, lunghezzaX, lunghezzaZ);
      }

      boolean added = player.getInventory().add(blueprint);
      if (!added) {
         player.sendSystemMessage(Component.translatable("system.portable_blueprints.inv_full"), true);
      }
   }

   public static void AggiungiStructureAllaCoda(
      ServerLevel level,
      Blueprint.BlueprintDatas bd,
      Player player,
      List<BlockPos> posizione_container,
      boolean free_build,
      short allow_nbt,
      boolean emc_active
   ) {
      if (!structureDaFare.contains(new Blueprint.BlueprintStructure(level, bd, player, posizione_container, free_build, allow_nbt, emc_active))) {
         structureDaFare.add(new Blueprint.BlueprintStructure(level, bd, player, posizione_container, free_build, allow_nbt, emc_active));
         PacketDistributor.sendToPlayer((ServerPlayer)player, new StoC_ProgressBarPlayer(0, bd.getAllBlocksCount() + 1, true), new CustomPacketPayload[0]);
      }
   }

   public static Map<Item, Integer> getAllItemQuantityFromInventory(ServerPlayer player, ServerLevel level, List<BlockPos> posizioniContainer) {
      Map<Item, Integer> ItemQuantity = new HashMap<>();

      for (ItemStack item : player.getInventory().items) {
         if (ItemQuantity.containsKey(item.getItem())) {
            ItemQuantity.replace(item.getItem(), ItemQuantity.get(item.getItem()) + item.getCount());
         } else {
            ItemQuantity.put(item.getItem(), item.getCount());
         }

         if (SophisticatedBackpacksMetodi.isABackpack(item)) {
            SophisticatedBackpacksMetodi.getAllItemFromBackpack(item, ItemQuantity);
         } else if (!MOD.ListaModContainerNonConsentiti.contains(BuiltInRegistries.ITEM.getKey(item.getItem()).getNamespace())) {
            ModifiableItemInventory.of(level, item).ifPresent(inv -> {
               Container container = inv.getContainer();

               for (int xx = 0; xx < container.getContainerSize(); xx++) {
                  ItemStack itemStack = container.getItem(xx);
                  if (ItemQuantity.containsKey(itemStack.getItem())) {
                     ItemQuantity.replace(itemStack.getItem(), ItemQuantity.get(itemStack.getItem()) + itemStack.getCount() * item.getCount());
                  } else {
                     ItemQuantity.put(itemStack.getItem(), itemStack.getCount() * item.getCount());
                  }
               }
            });
         }
      }

      for (ItemStack armorItem : player.getInventory().armor) {
         if (ItemQuantity.containsKey(armorItem.getItem())) {
            ItemQuantity.replace(armorItem.getItem(), ItemQuantity.get(armorItem.getItem()) + armorItem.getCount());
         } else {
            ItemQuantity.put(armorItem.getItem(), armorItem.getCount());
         }

         if (SophisticatedBackpacksMetodi.isABackpack(armorItem)) {
            SophisticatedBackpacksMetodi.getAllItemFromBackpack(armorItem, ItemQuantity);
         } else if (!MOD.ListaModContainerNonConsentiti.contains(BuiltInRegistries.ITEM.getKey(armorItem.getItem()).getNamespace())) {
            ModifiableItemInventory.of(level, armorItem).ifPresent(inv -> {
               Container container = inv.getContainer();

               for (int xx = 0; xx < container.getContainerSize(); xx++) {
                  ItemStack itemStack = container.getItem(xx);
                  if (ItemQuantity.containsKey(itemStack.getItem())) {
                     ItemQuantity.replace(itemStack.getItem(), ItemQuantity.get(itemStack.getItem()) + itemStack.getCount() * armorItem.getCount());
                  } else {
                     ItemQuantity.put(itemStack.getItem(), itemStack.getCount() * armorItem.getCount());
                  }
               }
            });
         }
      }

      for (BlockPos posizione : posizioniContainer) {
         BlockEntity be = level.getBlockEntity(posizione);
         if (be == null) {
            System.err.println("Il container non è una entity");
         } else if (SophisticatedBackpacksMetodi.isABackpack(be)) {
            SophisticatedBackpacksMetodi.getAllItemFromBackpack(be, ItemQuantity);
         } else {
            ItemHandler handler = (ItemHandler)ItemHandlers.find(level, posizione, Direction.DOWN);
            if (handler != null) {
               for (int x = 0; x < handler.getSlots(); x++) {
                  ItemStack item = handler.getStackInSlot(x);
                  if (ItemQuantity.containsKey(item.getItem())) {
                     ItemQuantity.replace(item.getItem(), ItemQuantity.get(item.getItem()) + item.getCount());
                  } else {
                     ItemQuantity.put(item.getItem(), item.getCount());
                  }

                  if (!MOD.ListaModContainerNonConsentiti.contains(BuiltInRegistries.ITEM.getKey(item.getItem()).getNamespace())) {
                     ModifiableItemInventory.of(level, item).ifPresent(inv -> {
                        Container container_item = inv.getContainer();

                        for (int xItem = 0; xItem < container_item.getContainerSize(); xItem++) {
                           ItemStack itemStack = container_item.getItem(xItem);
                           if (ItemQuantity.containsKey(itemStack.getItem())) {
                              ItemQuantity.replace(itemStack.getItem(), ItemQuantity.get(itemStack.getItem()) + itemStack.getCount() * item.getCount());
                           } else {
                              ItemQuantity.put(itemStack.getItem(), itemStack.getCount() * item.getCount());
                           }
                        }
                     });
                  }
               }
            }
         }
      }

      return ItemQuantity;
   }

   public static Tuple<Boolean, Vec3> removeItemFromTheContainers(ServerPlayer player, ServerLevel level, List<BlockPos> posizioniContainer, Item Target) {
      for (ItemStack item : player.getInventory().items) {
         if (item.getItem().equals(Target)) {
            if (!item.getItem().equals(Items.WATER_BUCKET) && !item.getItem().equals(Items.LAVA_BUCKET)) {
               item.setCount(item.getCount() - 1);
            } else {
               item.setCount(item.getCount() - 1);
               int item_rimasti = Operazioni.addItemToInventory(player.getInventory(), new ItemStack(Items.BUCKET), 1);
               if (item_rimasti > 0) {
                  Operazioni.dropItem(level, player.position(), new ItemStack(Items.BUCKET));
               }
            }

            return new Tuple(true, player.position().add(0.0, 1.0, 0.0));
         }

         if (SophisticatedBackpacksMetodi.isABackpack(item)) {
            Tuple<Boolean, Vec3> v = SophisticatedBackpacksMetodi.removeItemFromBackpack(player, level, item, Target);
            if (v != null) {
               return v;
            }
         } else if (!MOD.ListaModContainerNonConsentiti.contains(BuiltInRegistries.ITEM.getKey(item.getItem()).getNamespace())) {
            AtomicReference<Tuple<Boolean, Vec3>> v = new AtomicReference<>();
            ModifiableItemInventory.of(level, item).ifPresent(inv -> {
               Container container = inv.getContainer();

               for (int xx = 0; xx < container.getContainerSize(); xx++) {
                  ItemStack itemStack = container.getItem(xx);
                  if (itemStack.getItem().equals(Target)) {
                     if (!itemStack.getItem().equals(Items.WATER_BUCKET) && !itemStack.getItem().equals(Items.LAVA_BUCKET)) {
                        itemStack.setCount(itemStack.getCount() - 1);
                     } else {
                        itemStack.setCount(itemStack.getCount() - 1);
                        int item_rimastix = Operazioni.addItemToInventory(player.getInventory(), new ItemStack(Items.BUCKET), 1);
                        if (item_rimastix > 0) {
                           Operazioni.dropItem(level, player.position(), new ItemStack(Items.BUCKET));
                        }
                     }

                     v.set(new Tuple(true, player.position().add(0.0, 1.0, 0.0)));
                     break;
                  }
               }

               inv.save();
            });
            if (v.get() != null) {
               return v.get();
            }
         }
      }

      for (ItemStack armorItem : player.getInventory().armor) {
         if (armorItem.getItem().equals(Target)) {
            if (!armorItem.getItem().equals(Items.WATER_BUCKET) && !armorItem.getItem().equals(Items.LAVA_BUCKET)) {
               armorItem.setCount(armorItem.getCount() - 1);
            } else {
               armorItem.setCount(armorItem.getCount() - 1);
               int item_rimasti = Operazioni.addItemToInventory(player.getInventory(), new ItemStack(Items.BUCKET), 1);
               if (item_rimasti > 0) {
                  Operazioni.dropItem(level, player.position(), new ItemStack(Items.BUCKET));
               }
            }

            return new Tuple(true, player.position().add(0.0, 1.0, 0.0));
         }

         if (SophisticatedBackpacksMetodi.isABackpack(armorItem)) {
            Tuple<Boolean, Vec3> v = SophisticatedBackpacksMetodi.removeItemFromBackpack(player, level, armorItem, Target);
            if (v != null) {
               return v;
            }
         } else if (!MOD.ListaModContainerNonConsentiti.contains(BuiltInRegistries.ITEM.getKey(armorItem.getItem()).getNamespace())) {
            AtomicReference<Tuple<Boolean, Vec3>> v = new AtomicReference<>();
            ModifiableItemInventory.of(level, armorItem).ifPresent(inv -> {
               Container container = inv.getContainer();

               for (int xx = 0; xx < container.getContainerSize(); xx++) {
                  ItemStack itemStack = container.getItem(xx);
                  if (itemStack.getItem().equals(Target)) {
                     if (!itemStack.getItem().equals(Items.WATER_BUCKET) && !itemStack.getItem().equals(Items.LAVA_BUCKET)) {
                        itemStack.setCount(itemStack.getCount() - 1);
                     } else {
                        itemStack.setCount(itemStack.getCount() - 1);
                        int item_rimastix = Operazioni.addItemToInventory(player.getInventory(), new ItemStack(Items.BUCKET), 1);
                        if (item_rimastix > 0) {
                           Operazioni.dropItem(level, player.position(), new ItemStack(Items.BUCKET));
                        }
                     }

                     v.set(new Tuple(true, player.position().add(0.0, 1.0, 0.0)));
                     break;
                  }
               }

               inv.save();
            });
            if (v.get() != null) {
               return v.get();
            }
         }
      }

      for (BlockPos posizione : posizioniContainer) {
         BlockEntity be = level.getBlockEntity(posizione);
         if (be == null || !level.mayInteract(player, posizione)) continue;
         ItemHandler handler = ItemHandlers.find(level, posizione, Direction.DOWN);
         if (handler == null) continue;
         for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (stack.is(Target)) {
               ItemStack removed = handler.extractItem(slot, 1, false);
               if (removed.isEmpty()) continue;
               if (Target == Items.WATER_BUCKET || Target == Items.LAVA_BUCKET) {
                  ItemStack bucket = new ItemStack(Items.BUCKET);
                  if (!player.getInventory().add(bucket)) Operazioni.dropItem(level, player.position(), bucket);
               }
               return new Tuple<>(true, posizione.getCenter());
            }
            if (stack.getCount() != 1 || MOD.ListaModContainerNonConsentiti.contains(BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace())) continue;
            // The handler exposes snapshots: take the container out transactionally, then persist its changed component.
            java.util.Optional<ModifiableItemInventory> optional = ModifiableItemInventory.of(level, stack);
            if (optional.isEmpty()) continue;
            Container contents = optional.get().getContainer();
            int targetSlot = -1;
            for (int inner = 0; inner < contents.getContainerSize(); inner++) if (contents.getItem(inner).is(Target) && !contents.getItem(inner).isEmpty()) { targetSlot = inner; break; }
            if (targetSlot < 0) continue;
            ItemStack extractedContainer = handler.extractItem(slot, 1, false);
            if (extractedContainer.isEmpty()) continue;
            ModifiableItemInventory inventory = ModifiableItemInventory.of(level, extractedContainer).orElseThrow();
            inventory.getContainer().removeItem(targetSlot, 1);
            inventory.save();
            ItemStack remainder = handler.insertItem(slot, extractedContainer, false);
            if (!remainder.isEmpty() && !player.getInventory().add(remainder)) Operazioni.dropItem(level, posizione.getCenter(), remainder);
            if (Target == Items.WATER_BUCKET || Target == Items.LAVA_BUCKET) {
               ItemStack bucket = new ItemStack(Items.BUCKET);
               if (!player.getInventory().add(bucket)) Operazioni.dropItem(level, player.position(), bucket);
            }
            return new Tuple<>(true, posizione.getCenter());
         }
      }

      return new Tuple(false, Vec3.ZERO);
   }

   public static Blueprint.SharedWithMe getSharedBlueprints(ServerLevel level, UUID uuid) {
      File FileShared = getSharedFile(level, uuid);
      Blueprint.SharedWithMe swm = new Blueprint.SharedWithMe();

      try (Reader reader = new FileReader(FileShared)) {
         swm = getAllSharedGlobalBlueprintAsSWM(level, uuid, (Blueprint.SharedWithMe)GSON.fromJson(reader, Blueprint.SharedWithMe.class));
      } catch (Exception e) {
         e.printStackTrace();
      }

      return swm;
   }

   public static void addSharedBlueprint(ServerLevel serverLevel, String nome, UUID creatoreRichiesta, UUID ricevente) {
      if (BlueprintExist(serverLevel, nome, creatoreRichiesta)) {
         Blueprint.SharedWithMe swm = getSharedBlueprints(serverLevel, creatoreRichiesta);
         swm.addSharedWithOthersBlueprint(ricevente, nome);

         try (FileWriter writer = new FileWriter(getSharedFile(serverLevel, creatoreRichiesta))) {
            GSON.toJson(swm, writer);
         } catch (IOException e) {
            e.printStackTrace();
         }

         swm = getSharedBlueprints(serverLevel, ricevente);
         swm.addSharedWithMeBlueprint(creatoreRichiesta, nome);

         try (FileWriter writer = new FileWriter(getSharedFile(serverLevel, ricevente))) {
            GSON.toJson(swm, writer);
         } catch (IOException e) {
            e.printStackTrace();
         }
      }
   }

   public static void removeSharedBlueprint(ServerLevel serverLevel, String nome, UUID creatoreRichiesta, UUID ricevente) {
      if (BlueprintExist(serverLevel, nome, creatoreRichiesta)) {
         Blueprint.SharedWithMe swm = getSharedBlueprints(serverLevel, creatoreRichiesta);
         swm.removeSharedWithOthersBlueprint(ricevente, nome);

         try (FileWriter writer = new FileWriter(getSharedFile(serverLevel, creatoreRichiesta))) {
            GSON.toJson(swm, writer);
         } catch (IOException e) {
            e.printStackTrace();
         }

         swm = getSharedBlueprints(serverLevel, ricevente);
         swm.removeSharedWithMeBlueprint(creatoreRichiesta, nome);

         try (FileWriter writer = new FileWriter(getSharedFile(serverLevel, ricevente))) {
            GSON.toJson(swm, writer);
         } catch (IOException e) {
            e.printStackTrace();
         }
      }
   }

   public static void setSharedBlueprintAsGlobal(ServerLevel serverLevel, UUID owner, String blueprint_name) {
      File file = getSharedGlobalFile(serverLevel);
      if (file != null) {
         Map<UUID, List<String>> MapGlobal = new HashMap<>();

         try {
            Type type = (new TypeToken<Map<UUID, List<String>>>() {}).getType();

            try (Reader reader = new FileReader(file)) {
               MapGlobal = (Map<UUID, List<String>>)GSON.fromJson(reader, type);
            } catch (Exception e) {
               e.printStackTrace();
            }
         } catch (Exception var15) {
         }

         if (MapGlobal.containsKey(owner) && !MapGlobal.get(owner).contains(blueprint_name)) {
            MapGlobal.get(owner).add(blueprint_name);
         } else {
            MapGlobal.put(owner, List.of(blueprint_name));
         }

         try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(MapGlobal, writer);
         } catch (IOException e) {
            e.printStackTrace();
         }
      }
   }

   public static void removeSharedBlueprintAsGlobal(ServerLevel serverLevel, UUID owner, String blueprint_name) {
      File file = getSharedGlobalFile(serverLevel);
      if (file != null) {
         Map<UUID, List<String>> MapGlobal = new HashMap<>();

         try {
            Type type = (new TypeToken<Map<UUID, List<String>>>() {}).getType();

            try (Reader reader = new FileReader(file)) {
               MapGlobal = (Map<UUID, List<String>>)GSON.fromJson(reader, type);
            } catch (Exception e) {
               e.printStackTrace();
            }
         } catch (Exception e) {
            e.printStackTrace();
         }

         for (UUID uuid : MapGlobal.keySet()) {
            System.out.println("uudi nella map: " + uuid + " | uuid owner: " + owner);
         }

         if (MapGlobal.containsKey(owner) && MapGlobal.get(owner).contains(blueprint_name)) {
            MapGlobal.get(owner).remove(blueprint_name);
         }

         try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(MapGlobal, writer);
         } catch (IOException e) {
            e.printStackTrace();
         }
      }
   }

   public static Blueprint.SharedWithMe getAllSharedGlobalBlueprintAsSWM(ServerLevel serverLevel, UUID Richiedente, Blueprint.SharedWithMe swm) {
      File file = getSharedGlobalFile(serverLevel);
      if (file != null) {
         Map<UUID, List<String>> MapGlobal = new HashMap<>();

         try {
            Type type = (new TypeToken<Map<UUID, List<String>>>() {}).getType();

            try (Reader reader = new FileReader(file)) {
               MapGlobal = (Map<UUID, List<String>>)GSON.fromJson(reader, type);
            } catch (Exception e) {
               e.printStackTrace();
            }

            for (UUID owner : MapGlobal.keySet()) {
               for (String name : MapGlobal.get(owner)) {
                  if (owner.equals(Richiedente)) {
                     swm.addSharedWithOthersBlueprint(owner, name);
                  } else {
                     swm.addSharedWithMeBlueprint(owner, name);
                  }
               }
            }
         } catch (Exception var13) {
         }
      }

      return swm;
   }

   public static File getSharedGlobalFile(ServerLevel serverLevel) {
      File CartellaMod = new File(serverLevel.getServer().getWorldPath(LevelResource.ROOT).toFile(), "portable_blueprints");
      if (!CartellaMod.exists()) {
         CartellaMod.mkdir();
      }

      CartellaMod = new File(CartellaMod, "shared");
      if (!CartellaMod.exists()) {
         CartellaMod.mkdir();
      }

      String file = "global.sg";
      if (!new File(CartellaMod, file).exists()) {
         File jsonFile = new File(CartellaMod, file);

         try (FileWriter writer = new FileWriter(jsonFile)) {
            GSON.toJson(new HashMap(), writer);
         } catch (IOException e) {
            e.printStackTrace();
         }
      }

      return new File(CartellaMod, file);
   }

   public static File getSharedFile(ServerLevel serverLevel, UUID target) {
      File CartellaMod = new File(serverLevel.getServer().getWorldPath(LevelResource.ROOT).toFile(), "portable_blueprints");
      if (!CartellaMod.exists()) {
         CartellaMod.mkdir();
      }

      CartellaMod = new File(CartellaMod, "shared");
      if (!CartellaMod.exists()) {
         CartellaMod.mkdir();
      }

      String file = target.toString() + ".swm";
      if (!new File(CartellaMod, file).exists()) {
         Blueprint.SharedWithMe swm = new Blueprint.SharedWithMe();
         File jsonFile = new File(CartellaMod, file);

         try (FileWriter writer = new FileWriter(jsonFile)) {
            GSON.toJson(swm, writer);
         } catch (IOException e) {
            e.printStackTrace();
         }
      }

      return new File(CartellaMod, file);
   }

   public static File getGlobalBlueprintDir() {
      File CartellaMod = new File(Minecraft.getInstance().gameDirectory, "portable_blueprints");
      if (!CartellaMod.exists()) {
         CartellaMod.mkdir();
      }

      CartellaMod = new File(CartellaMod, "blueprints");
      if (!CartellaMod.exists()) {
         CartellaMod.mkdir();
      }

      return CartellaMod;
   }

   public static boolean globalBlueprintExist(String nome) {
      return new File(getGlobalBlueprintDir(), nome + ".bp").exists();
   }

   public static int getGlobalBlueprintsQuantity() {
      int x = 0;

      for (File file : getGlobalBlueprintDir().listFiles()) {
         if (file.getName().contains(".bp")) {
            x++;
         }
      }

      return x;
   }

   public static List<String> getAllGlobalBlueprintsName() {
      List<String> nomi = new ArrayList<>();
      List<File> files = List.of(getGlobalBlueprintDir().listFiles());

      for (File file : files) {
         if (file.getName().contains(".bp")) {
            String blueprint_name = file.getName().replace(".bp", "");
            StringBuilder nomeCorretto = new StringBuilder();

            for (char c : blueprint_name.toCharArray()) {
               if ("QWERTYUIOPASDFGHJKL;ZXCVBNM .,;'[]{}<>:\"/\\|?*\u0000\u0001\u0002\u0003\u0004\u0005\u0006\u0007\b\t\n\u000b\f\r\u000e\u000f\u0010\u0011\u0012\u0013\u0014\u0015\u0016\u0017\u0018\u0019\u001a\u001b\u001c\u001d\u001e\u001f"
                     .indexOf(c)
                  == -1) {
                  nomeCorretto.append(c);
               }
            }

            String nomeFinale = nomeCorretto.toString();
            boolean inUso = true;

            for (int count = 0; inUso; count++) {
               inUso = false;

               for (File f : files) {
                  String ne = f.getName().replace(".bp", "");
                  if (nomeFinale.equals(ne) && !ne.equals(blueprint_name) || nomeFinale.isEmpty()) {
                     nomeFinale = nomeCorretto + "_(" + count + ")";
                     inUso = true;
                     break;
                  }
               }
            }

            if (!blueprint_name.equals(nomeFinale)) {
               Path dir = getGlobalBlueprintDir().toPath();

               try {
                  Files.move(dir.resolve(blueprint_name + ".bp"), dir.resolve(nomeFinale + ".bp"), StandardCopyOption.REPLACE_EXISTING);

                  try (Reader reader = new FileReader(dir.resolve(nomeFinale + ".bp").toFile())) {
                     Blueprint.BlueprintDatas bd = (Blueprint.BlueprintDatas)GSON.fromJson(reader, Blueprint.BlueprintDatas.class);
                     bd.nome = nomeFinale;

                     try (FileWriter writer = new FileWriter(dir.resolve(nomeFinale + ".bp").toFile())) {
                        GSON.toJson(bd, writer);
                     } catch (IOException e) {
                        e.printStackTrace();
                     }
                  } catch (Exception e) {
                     e.printStackTrace();
                  }
               } catch (Exception e) {
                  e.printStackTrace();
               }
            }

            nomi.add(nomeFinale);
         }
      }

      return nomi;
   }

   public static void removeGlobalBlueprint(String nome) {
      File f = new File(getGlobalBlueprintDir(), nome + ".bp");

      for (int count = 10; count > 0; count--) {
         if (f.exists()) {
            System.err.println("remove global blueprint: " + f.getName());
            if (f.delete()) {
               break;
            }
         }
      }
   }

   public static Blueprint.BlueprintDatas getGlobalBlueprintClass(String nome) {
      File f = new File(getGlobalBlueprintDir(), nome + ".bp");
      if (f.exists()) {
         try (Reader reader = new FileReader(f)) {
            return (Blueprint.BlueprintDatas)GSON.fromJson(reader, Blueprint.BlueprintDatas.class);
         } catch (Exception e) {
            e.printStackTrace();
         }
      }

      return null;
   }

   public static void sendToServerGlobalBlueprint(String nome) {
      if (!ClientMethods.SendingGlobalBlueprintToServer) {
         Thread tickerThread = new Thread(
            () -> {
               try {
                  for (boolean allSend = false;
                     !allSend;
                     allSend = ServerToClientDatasShared.RichiesteInteger.containsKey(ServerToClientDatasShared.ID_PLAYER_BLUEPRINTS_QUANTITY)
                        && ServerToClientDatasShared.RichiesteInteger.containsKey(507)
                        && ServerToClientDatasShared.RichiesteInteger.containsKey(508)
                  ) {
                     Thread.sleep(50L);
                  }

                  int QuantityBlocksMax = ServerToClientDatasShared.getRichiestaInteger(507);
                  Blueprint.BlueprintDatas bdc = getGlobalBlueprintClass(nome);
                  boolean puoMandarlo = false;
                  if (bdc != null) {
                     if (bdc.getAllBlocksCount() <= QuantityBlocksMax) {
                        if (ServerToClientDatasShared.RichiesteInteger.get(ServerToClientDatasShared.ID_PLAYER_BLUEPRINTS_QUANTITY)
                           < ServerToClientDatasShared.getRichiestaInteger(508)) {
                           puoMandarlo = true;
                        } else {
                           Minecraft.getInstance()
                              .player
                              .displayClientMessage(
                                 Component.translatable("chat_message.portable_blueprints.global_bp_quantity_limit").withStyle(ChatFormatting.RED), false
                              );
                        }
                     } else {
                        Minecraft.getInstance()
                           .player
                           .displayClientMessage(
                              Component.translatable("chat_message.portable_blueprints.global_bp_blocks_limit").withStyle(ChatFormatting.RED), false
                           );
                     }
                  }

                  if (puoMandarlo) {
                     ClientMethods.SendingGlobalBlueprintToServer = true;
                     File bd = new File(getGlobalBlueprintDir(), nome + ".bp");
                     if (bd.exists()) {
                        String bd_string_datas = new String(Files.readAllBytes(bd.toPath()));
                        String id_richiesta = UUID.randomUUID().toString();
                        ModToast.showToast(
                           Component.translatable("portable_blueprints.toast.global_blueprints.import.title"),
                           Component.translatable("portable_blueprints.toast.global_blueprints.import.subtitle"),
                           new ItemStack((ItemLike)ModItems.GLOBAL_BLUEPRINT.get()),
                           -1453289,
                           -1
                        );
                        PacketDistributor.sendToServer(
                           new CtoS_SaveGlobalBlueprintInServer("[INIZIO DATI BLUEPRINT <id>" + id_richiesta + "</id><nome>" + nome + "</nome>"),
                           new CustomPacketPayload[0]
                        );
                        PacketDistributor.sendToServer(new CtoS_SaveGlobalBlueprintInServer("[NOME DEL FILE:" + nome), new CustomPacketPayload[0]);
                        List<String> dati = Operazioni.splitStringByLength(bd_string_datas, 10000);
                        ProgressBarBlueprintPlayer.aggiungi(3, dati.size() + 1);

                        for (String val : dati) {
                           ProgressBarBlueprintPlayer.progressoPlus(3);
                           PacketDistributor.sendToServer(new CtoS_SaveGlobalBlueprintInServer("[" + id_richiesta + "]" + val), new CustomPacketPayload[0]);
                           Thread.sleep(10L);
                        }

                        ModToast.showToast(
                           Component.translatable("portable_blueprints.toast.global_blueprints.import.title"),
                           Component.translatable("portable_blueprints.toast.global_blueprints.import.subtitle2"),
                           new ItemStack((ItemLike)ModItems.GLOBAL_BLUEPRINT.get()),
                           -13447886,
                           -1
                        );
                        ProgressBarBlueprintPlayer.impostaValore(3, dati.size() + 1);
                        PacketDistributor.sendToServer(
                           new CtoS_SaveGlobalBlueprintInServer("[FINE DATI BLUEPRINT DI ID: " + id_richiesta + "]"), new CustomPacketPayload[0]
                        );
                        ClientMethods.SendingGlobalBlueprintToServer = false;
                     }
                  }
               } catch (Exception e) {
                  e.printStackTrace();
                  ClientMethods.SendingGlobalBlueprintToServer = false;
               }
            }
         );
         tickerThread.setDaemon(true);
         tickerThread.start();
      }
   }

   public static File getServerBlueprintFile(ServerLevel serverLevel) {
      File CartellaMod = new File(serverLevel.getServer().getWorldPath(LevelResource.ROOT).toFile(), "portable_blueprints");
      if (!CartellaMod.exists()) {
         CartellaMod.mkdir();
      }

      CartellaMod = new File(CartellaMod, "server");
      if (!CartellaMod.exists()) {
         CartellaMod.mkdir();
      }

      CartellaMod = new File(CartellaMod, "blueprints");
      if (!CartellaMod.exists()) {
         CartellaMod.mkdir();
      }

      return CartellaMod;
   }

   public static File getWornBlueprintFile(ServerLevel serverLevel) {
      File CartellaMod = new File(serverLevel.getServer().getWorldPath(LevelResource.ROOT).toFile(), "portable_blueprints");
      if (!CartellaMod.exists()) {
         CartellaMod.mkdir();
      }

      CartellaMod = new File(CartellaMod, "worn blueprints");
      if (!CartellaMod.exists()) {
         CartellaMod.mkdir();
      }

      return CartellaMod;
   }

   public static File getServerPremissionFile(ServerLevel serverLevel) {
      File CartellaMod = new File(serverLevel.getServer().getWorldPath(LevelResource.ROOT).toFile(), "portable_blueprints");
      if (!CartellaMod.exists()) {
         CartellaMod.mkdir();
      }

      CartellaMod = new File(CartellaMod, "server");
      if (!CartellaMod.exists()) {
         CartellaMod.mkdir();
      }

      CartellaMod = new File(CartellaMod, "permission");
      if (!CartellaMod.exists()) {
         CartellaMod.mkdir();
      }

      String file = "permissions.settings";
      if (!new File(CartellaMod, file).exists()) {
         Blueprint.BlueprintPermission bp = new Blueprint.BlueprintPermission();
         File jsonFile = new File(CartellaMod, file);

         try (FileWriter writer = new FileWriter(jsonFile)) {
            GSON.toJson(bp, writer);
         } catch (IOException e) {
            e.printStackTrace();
         }
      }

      return new File(CartellaMod, file);
   }

   public static String getIDBlueprint(String namespace, Blueprint.BlueprintDatas blueprintDatas) {
      if (blueprintDatas != null) {
         String nome = "";

         for (char c : blueprintDatas.nome.toCharArray()) {
            if (Character.isUpperCase(c)) {
               nome = nome + "<" + String.valueOf(c).toLowerCase() + ">";
            } else {
               nome = nome + c;
            }
         }

         return namespace.toLowerCase()
            + "|"
            + nome
            + "?"
            + blueprintDatas.lunghezzaX
            + blueprintDatas.altezza
            + blueprintDatas.lunghezzaZ
            + blueprintDatas.getAllBlocksCount()
            + (blueprintDatas.BlocksData != null ? blueprintDatas.BlocksData.size() : 0)
            + (blueprintDatas.NotSolidBlocksData != null ? blueprintDatas.NotSolidBlocksData.size() : 0)
            + (blueprintDatas.BlocksNBT != null ? blueprintDatas.BlocksNBT.size() : 0);
      } else {
         return "";
      }
   }

   public static String sanitizePathUnique(String input) {
      String base = input.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9/._-]", "_");

      try {
         MessageDigest md = MessageDigest.getInstance("SHA-1");
         byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
         StringBuilder hex = new StringBuilder();

         for (byte b : digest) {
            hex.append(String.format("%02x", b));
         }

         return base + "_" + hex.substring(0, 8);
      } catch (NoSuchAlgorithmException e) {
         return base + "_" + Integer.toHexString(input.hashCode());
      }
   }

   public static String getIDBlueprint(ServerLevel level, String namespace, UUID BPOwner, String BPName) {
      File blueprintFile = Arrays.stream(BPOwner == null ? getServerBlueprintFile(level).listFiles() : getFileForBlueprints(level, BPOwner).listFiles())
         .filter(f -> f.getName().equals(BPName + ".bp"))
         .findFirst()
         .orElse(null);
      if (blueprintFile != null) {
         try {
            Blueprint.BlueprintDatas bd = (Blueprint.BlueprintDatas)GSON.fromJson(new FileReader(blueprintFile), Blueprint.BlueprintDatas.class);
            if (bd != null) {
               return getIDBlueprint(namespace, bd);
            }
         } catch (Exception var6) {
         }
      }

      return "";
   }

   public static String getNomeByBlueprintID(String blueprint_Id) {
      String nome = blueprint_Id;

      while (true) {
         int open = nome.indexOf("<");
         int close = nome.indexOf(">");
         if (open == -1 || close == -1 || open >= close) {
            open = nome.indexOf("|");
            close = nome.lastIndexOf("?");
            return open != -1 && close != -1 && open + 1 < close ? nome.substring(open + 1, close) : nome;
         }

         String v = nome.substring(open + 1, close);
         nome = nome.replace("<" + v + ">", v.toUpperCase());
      }
   }

   public static class BlueprintDatas {
      public String nome = "";
      public int altezza = 0;
      public int lunghezzaX = 0;
      public int lunghezzaZ = 0;
      public BlockPos offset = new BlockPos(0, 0, 0);
      public Map<Integer, Map<String, String>> BlocksData = new HashMap<>();
      public Map<Integer, Map<String, String>> NotSolidBlocksData = new HashMap<>();
      public Map<Integer, Map<String, String>> BlocksNBT = new HashMap<>();

      public BlueprintDatas(String NOME) {
         this.nome = NOME;
      }

      public void addBlock(int altezza, String posDalOrigine, String blocco, String nbt) {
         if (!ServerBlackListBlock(blocco) && !controlloProprietaBlocchi(blocco)) {
            if (this.BlocksData.containsKey(altezza)) {
               this.BlocksData.get(altezza).put(posDalOrigine, blocco);
            } else {
               this.BlocksData.put(altezza, new HashMap<>());
               this.BlocksData.get(altezza).put(posDalOrigine, blocco);
            }

            if (!nbt.isEmpty()) {
               if (this.BlocksNBT != null && this.BlocksNBT.containsKey(altezza)) {
                  this.BlocksNBT.get(altezza).put(posDalOrigine, nbt);
               } else {
                  this.BlocksNBT.put(altezza, new HashMap<>());
                  this.BlocksNBT.get(altezza).put(posDalOrigine, nbt);
               }
            }
         }
      }

      public void addNonSolidBlock(int altezza, String posDalOrigine, String blocco, String nbt) {
         if (!ServerBlackListBlock(blocco) && !controlloProprietaBlocchi(blocco)) {
            if (this.NotSolidBlocksData.containsKey(altezza)) {
               this.NotSolidBlocksData.get(altezza).put(posDalOrigine, blocco);
            } else {
               this.NotSolidBlocksData.put(altezza, new HashMap<>());
               this.NotSolidBlocksData.get(altezza).put(posDalOrigine, blocco);
            }

            if (!nbt.isEmpty()) {
               if (this.BlocksNBT != null && this.BlocksNBT.containsKey(altezza)) {
                  this.BlocksNBT.get(altezza).put(posDalOrigine, nbt);
               } else {
                  this.BlocksNBT.put(altezza, new HashMap<>());
                  this.BlocksNBT.get(altezza).put(posDalOrigine, nbt);
               }
            }
         }
      }

      public void setX(int x) {
         this.lunghezzaX = x;
      }

      public void setZ(int z) {
         this.lunghezzaZ = z;
      }

      public List<String> getBlocksDataByAltezza(int altezza) {
         List<String> bs = new ArrayList<>();

         for (String pos : this.BlocksData.get(altezza).keySet()) {
            String valore = this.BlocksData.get(altezza).get(pos);
            bs.add(valore);
         }

         return bs;
      }

      public List<String> getBlocksPosByAltezza(int altezza) {
         List<String> bs = new ArrayList<>();

         for (String pos : this.BlocksData.get(altezza).keySet()) {
            bs.add(pos);
         }

         return bs;
      }

      public int getAllBlocksCount() {
         int count = 0;

         for (int v = 0; v < 2; v++) {
            if ((v != 0 || this.BlocksData != null) && (v != 1 || this.NotSolidBlocksData != null)) {
               for (Integer altezza : v == 0 ? this.BlocksData.keySet() : this.NotSolidBlocksData.keySet()) {
                  count += v == 0 ? this.BlocksData.get(altezza).size() : this.NotSolidBlocksData.get(altezza).size();
               }
            }
         }

         return count;
      }

      public Blueprint.BlueprintDatas rotate360(double angleDegrees, short autofillMode, @Nullable ServerLevel serverLevel) {
         Blueprint.BlueprintDatas ruotato = new Blueprint.BlueprintDatas(this.nome);
         ruotato.altezza = this.altezza;
         double normalizedAngle = angleDegrees % 360.0;
         if (normalizedAngle < 0.0) {
            normalizedAngle += 360.0;
         }

         short rot90 = (short)(Math.round(normalizedAngle / 90.0) % 4L);
         double rad = Math.toRadians(angleDegrees);
         double[] cornersX = new double[]{0.0, this.lunghezzaX - 1, 0.0, this.lunghezzaX - 1};
         double[] cornersZ = new double[]{0.0, 0.0, this.lunghezzaZ - 1, this.lunghezzaZ - 1};
         double minX = Double.MAX_VALUE;
         double maxX = -Double.MAX_VALUE;
         double minZ = Double.MAX_VALUE;
         double maxZ = -Double.MAX_VALUE;

         for (int i = 0; i < 4; i++) {
            double nx = cornersX[i] * Math.cos(rad) - cornersZ[i] * Math.sin(rad);
            double nz = cornersX[i] * Math.sin(rad) + cornersZ[i] * Math.cos(rad);
            if (nx < minX) {
               minX = nx;
            }

            if (nx > maxX) {
               maxX = nx;
            }

            if (nz < minZ) {
               minZ = nz;
            }

            if (nz > maxZ) {
               maxZ = nz;
            }
         }

         ruotato.lunghezzaX = (int)Math.round(maxX - minX + 1.0);
         ruotato.lunghezzaZ = (int)Math.round(maxZ - minZ + 1.0);

         for (int v = 0; v < 2; v++) {
            Map<Integer, Map<String, String>> dataMap = v == 0 ? this.BlocksData : this.NotSolidBlocksData;

            for (Entry<Integer, Map<String, String>> entry : dataMap.entrySet()) {
               Map<String, String> innerMap = entry.getValue();

               for (Entry<String, String> blockEntry : innerMap.entrySet()) {
                  BlockPos pos = new BlockPos(Operazioni.getVec3iFromString(blockEntry.getKey()));
                  String blockData = blockEntry.getValue();
                  double dx = pos.getX();
                  double dz = pos.getZ();
                  double nx = dx * Math.cos(rad) - dz * Math.sin(rad);
                  double nz = dx * Math.sin(rad) + dz * Math.cos(rad);
                  int newX = (int)Math.round(nx - minX);
                  int newZ = (int)Math.round(nz - minZ);
                  BlockPos newPos = new BlockPos(newX, pos.getY(), newZ);
                  String nbt = this.BlocksNBT != null
                        && this.BlocksNBT.containsKey(pos.getY())
                        && this.BlocksNBT.get(pos.getY()).containsKey(blockEntry.getKey())
                     ? this.BlocksNBT.get(pos.getY()).get(blockEntry.getKey())
                     : "";
                  String blockDataRuotato = blockData;
                  if (rot90 != 0) {
                     blockDataRuotato = this.ruotaProprietaBlocco(blockData, rot90);
                  }

                  if (v == 0) {
                     ruotato.addBlock(entry.getKey(), Operazioni.getVec3iString(newPos), blockDataRuotato, nbt);
                  } else {
                     ruotato.addNonSolidBlock(entry.getKey(), Operazioni.getVec3iString(newPos), blockDataRuotato, nbt);
                  }
               }
            }
         }

         if (autofillMode != 5 && autofillMode != 0) {
            List<BlockPos> listaBuchi = this.getRotationHolesPattern(angleDegrees, this.lunghezzaX, this.lunghezzaZ, this.altezza);
            Map<Integer, Map<String, String>> lookupMap = new HashMap<>();
            ruotato.BlocksData.forEach((y, map) -> lookupMap.put(y, new HashMap<>((Map<? extends String, ? extends String>)map)));
            ruotato.NotSolidBlocksData
               .forEach((y, map) -> lookupMap.computeIfAbsent(y, k -> new HashMap<>()).putAll((Map<? extends String, ? extends String>)map));

            for (BlockPos pos : listaBuchi) {
               String currentPosStr = Operazioni.getVec3iString(pos);
               int y = pos.getY();
               if (!lookupMap.containsKey(y) || !lookupMap.get(y).containsKey(currentPosStr)) {
                  Map<String, String> levelMap = lookupMap.getOrDefault(y, new HashMap<>());
                  BlockPos[] neighbors = new BlockPos[]{pos.north(), pos.east(), pos.south(), pos.west()};
                  String foundBlockData = null;
                  BlockPos neighborFound = null;
                  short viciniConteggio = 0;

                  for (BlockPos nPos : neighbors) {
                     String nStr = Operazioni.getVec3iString(nPos);
                     if (levelMap.containsKey(nStr)) {
                        viciniConteggio++;
                        if (foundBlockData == null) {
                           foundBlockData = levelMap.get(nStr);
                           neighborFound = nPos;
                        }
                     }
                  }

                  if (viciniConteggio >= autofillMode && foundBlockData != null) {
                     String nStrFound = Operazioni.getVec3iString(neighborFound);
                     String nbt = "";
                     if (ruotato.BlocksNBT.containsKey(y) && ruotato.BlocksNBT.get(y).containsKey(nStrFound)) {
                        nbt = ruotato.BlocksNBT.get(y).get(nStrFound);
                     }

                     String bloccoId = foundBlockData.substring(foundBlockData.indexOf("<bid>") + 5, foundBlockData.indexOf("</bid>"));
                     String properties = foundBlockData.replaceFirst("<bid>" + bloccoId + "</bid>", "");
                     BlockState bs = properties.equals("null")
                        ? Operazioni.getBlockFromString(bloccoId).defaultBlockState()
                        : Operazioni.getPropertiesStringToBlock(Operazioni.getBlockFromString(bloccoId), properties);
                     boolean isSolid = !MOD.ListForcedNotSolidBlock.contains(BuiltInRegistries.BLOCK.getKey(bs.getBlock()).toString())
                        && !bs.getPistonPushReaction().name().equals("DESTROY")
                        && (serverLevel == null || !bs.getCollisionShape(serverLevel, pos).isEmpty());
                     if (isSolid) {
                        ruotato.addBlock(y, currentPosStr, foundBlockData, nbt);
                     } else {
                        ruotato.addNonSolidBlock(y, currentPosStr, foundBlockData, nbt);
                     }
                  }
               }
            }
         }

         return ruotato;
      }

      public List<BlockPos> getRotationHolesPattern(double angleDegrees, int lunghezzaX, int lunghezzaZ, int altezza) {
         List<BlockPos> holePositions = new ArrayList<>();
         double rad = Math.toRadians(angleDegrees);
         double[] cornersX = new double[]{0.0, lunghezzaX - 1, 0.0, lunghezzaX - 1};
         double[] cornersZ = new double[]{0.0, 0.0, lunghezzaZ - 1, lunghezzaZ - 1};
         double minX = Double.MAX_VALUE;
         double minZ = Double.MAX_VALUE;

         for (int i = 0; i < 4; i++) {
            double nx = cornersX[i] * Math.cos(rad) - cornersZ[i] * Math.sin(rad);
            double nz = cornersX[i] * Math.sin(rad) + cornersZ[i] * Math.cos(rad);
            if (nx < minX) {
               minX = nx;
            }

            if (nz < minZ) {
               minZ = nz;
            }
         }

         Set<Long> occupiedXZ = new HashSet<>();

         for (int x = 0; x < lunghezzaX; x++) {
            for (int z = 0; z < lunghezzaZ; z++) {
               double nx = x * Math.cos(rad) - z * Math.sin(rad);
               double nz = x * Math.sin(rad) + z * Math.cos(rad);
               int newX = (int)Math.round(nx - minX);
               int newZ = (int)Math.round(nz - minZ);
               occupiedXZ.add((long)newX << 32 | newZ & 4294967295L);
            }
         }

         int maxX = 0;
         int maxZ = 0;

         for (Long pos : occupiedXZ) {
            maxX = Math.max(maxX, (int)(pos >> 32));
            maxZ = Math.max(maxZ, pos.intValue());
         }

         for (int x = 0; x <= maxX; x++) {
            for (int z = 0; z <= maxZ; z++) {
               long currentPos = (long)x << 32 | z & 4294967295L;
               if (!occupiedXZ.contains(currentPos) && this.isAdjacentToOccupied(x, z, occupiedXZ)) {
                  for (int y = 0; y < altezza; y++) {
                     holePositions.add(new BlockPos(x, y, z));
                  }
               }
            }
         }

         return holePositions;
      }

      private boolean isAdjacentToOccupied(int x, int z, Set<Long> occupied) {
         int[] dx = new int[]{1, -1, 0, 0};
         int[] dz = new int[]{0, 0, 1, -1};

         for (int i = 0; i < 4; i++) {
            if (occupied.contains((long)(x + dx[i]) << 32 | z + dz[i] & 4294967295L)) {
               return true;
            }
         }

         return false;
      }

      private String ruotaProprietaBlocco(String _blockdata, short rot) {
         String blockdata = _blockdata;
         String bloccoId = blockdata.substring(blockdata.indexOf("<bid>") + 5, blockdata.indexOf("</bid>"));
         blockdata = blockdata.replaceFirst("<bid>" + bloccoId + "</bid>", "");
         if (!blockdata.equalsIgnoreCase("null")) {
            BlockState state = Operazioni.getPropertiesStringToBlock(Operazioni.getBlockFromString(bloccoId), blockdata);
            Tuple<Boolean, BlockState> val = setCustomBlockProprietyRotate(bloccoId, state, rot);
            if ((Boolean)val.getA()) {
               state = (BlockState)val.getB();
            } else {
               if (state.hasProperty(BlockStateProperties.FACING)) {
                  state = (BlockState)state.setValue(
                     BlockStateProperties.FACING, Operazioni.rotateFacing((Direction)state.getValue(BlockStateProperties.FACING), rot)
                  );
               } else if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                  state = (BlockState)state.setValue(
                     BlockStateProperties.HORIZONTAL_FACING, Operazioni.rotateFacing((Direction)state.getValue(BlockStateProperties.HORIZONTAL_FACING), rot)
                  );
               } else if (state.hasProperty(BlockStateProperties.FACING_HOPPER)) {
                  state = (BlockState)state.setValue(
                     BlockStateProperties.FACING_HOPPER, Operazioni.rotateFacing((Direction)state.getValue(BlockStateProperties.FACING_HOPPER), rot)
                  );
               }

               if ((rot == 1 || rot == 3) && state.hasProperty(BlockStateProperties.AXIS)) {
                  if (((Axis)state.getValue(BlockStateProperties.AXIS)).equals(Axis.X)) {
                     state = (BlockState)state.setValue(BlockStateProperties.AXIS, Axis.Z);
                  } else if (((Axis)state.getValue(BlockStateProperties.AXIS)).equals(Axis.Z)) {
                     state = (BlockState)state.setValue(BlockStateProperties.AXIS, Axis.X);
                  }
               }

               if (state.hasProperty(BlockStateProperties.RAIL_SHAPE) || state.hasProperty(BlockStateProperties.RAIL_SHAPE_STRAIGHT)) {
                  RailShape shape = state.hasProperty(BlockStateProperties.RAIL_SHAPE)
                     ? (RailShape)state.getValue(BlockStateProperties.RAIL_SHAPE)
                     : (RailShape)state.getValue(BlockStateProperties.RAIL_SHAPE_STRAIGHT);
                  List<RailShape> railShapesAlto = List.of(
                     RailShape.ASCENDING_NORTH, RailShape.ASCENDING_EAST, RailShape.ASCENDING_SOUTH, RailShape.ASCENDING_WEST
                  );
                  List<RailShape> railShapesCurva = List.of(RailShape.NORTH_WEST, RailShape.NORTH_EAST, RailShape.SOUTH_EAST, RailShape.SOUTH_WEST);
                  if (railShapesAlto.contains(shape)) {
                     short rotval = (short)(
                        shape.equals(RailShape.ASCENDING_NORTH)
                           ? 0
                           : (shape.equals(RailShape.ASCENDING_EAST) ? 1 : (shape.equals(RailShape.ASCENDING_SOUTH) ? 2 : 3))
                     );
                     rotval = (short)(rotval + rot);
                     if (rotval > 3) {
                        rotval = (short)(rotval - 4);
                     }

                     state = (BlockState)state.setValue(
                        state.hasProperty(BlockStateProperties.RAIL_SHAPE) ? BlockStateProperties.RAIL_SHAPE : BlockStateProperties.RAIL_SHAPE_STRAIGHT,
                        railShapesAlto.get(rotval)
                     );
                  } else if (railShapesCurva.contains(shape)) {
                     short rotval = (short)(
                        shape.equals(RailShape.NORTH_WEST) ? 0 : (shape.equals(RailShape.NORTH_EAST) ? 1 : (shape.equals(RailShape.SOUTH_EAST) ? 2 : 3))
                     );
                     rotval = (short)(rotval + rot);
                     if (rotval > 3) {
                        rotval = (short)(rotval - 4);
                     }

                     state = (BlockState)state.setValue(
                        state.hasProperty(BlockStateProperties.RAIL_SHAPE) ? BlockStateProperties.RAIL_SHAPE : BlockStateProperties.RAIL_SHAPE_STRAIGHT,
                        railShapesCurva.get(rotval)
                     );
                  } else {
                     short rotval = (short)(shape.equals(RailShape.EAST_WEST) ? 0 : 1);
                     rotval = (short)(rotval + rot);

                     while (rotval > 1) {
                        rotval = (short)(rotval - 2);
                     }

                     state = (BlockState)state.setValue(
                        state.hasProperty(BlockStateProperties.RAIL_SHAPE) ? BlockStateProperties.RAIL_SHAPE : BlockStateProperties.RAIL_SHAPE_STRAIGHT,
                        rotval == 1 ? RailShape.NORTH_SOUTH : RailShape.EAST_WEST
                     );
                  }
               }

               boolean n = state.hasProperty(BlockStateProperties.NORTH) ? (Boolean)state.getValue(BlockStateProperties.NORTH) : false;
               boolean s = state.hasProperty(BlockStateProperties.SOUTH) ? (Boolean)state.getValue(BlockStateProperties.SOUTH) : false;
               boolean e = state.hasProperty(BlockStateProperties.EAST) ? (Boolean)state.getValue(BlockStateProperties.EAST) : false;
               boolean w = state.hasProperty(BlockStateProperties.WEST) ? (Boolean)state.getValue(BlockStateProperties.WEST) : false;
               boolean vfN = false;
               boolean vfS = false;
               boolean vfE = false;
               boolean vfW = false;
               if (n) {
                  if (rot != 0) {
                     n = false;
                  }

                  switch (rot) {
                     case 1:
                        vfE = true;
                        break;
                     case 2:
                        vfS = true;
                        break;
                     case 3:
                        vfW = true;
                  }
               }

               if (s) {
                  if (rot != 0) {
                     s = false;
                  }

                  switch (rot) {
                     case 1:
                        vfW = true;
                        break;
                     case 2:
                        vfN = true;
                        break;
                     case 3:
                        vfE = true;
                  }
               }

               if (e) {
                  if (rot != 0) {
                     e = false;
                  }

                  switch (rot) {
                     case 1:
                        vfS = true;
                        break;
                     case 2:
                        vfW = true;
                        break;
                     case 3:
                        vfN = true;
                  }
               }

               if (w) {
                  if (rot != 0) {
                     w = false;
                  }

                  switch (rot) {
                     case 1:
                        vfN = true;
                        break;
                     case 2:
                        vfE = true;
                        break;
                     case 3:
                        vfS = true;
                  }
               }

               if (state.hasProperty(BlockStateProperties.NORTH)) {
                  state = (BlockState)state.setValue(BlockStateProperties.NORTH, vfN || n);
               }

               if (state.hasProperty(BlockStateProperties.SOUTH)) {
                  state = (BlockState)state.setValue(BlockStateProperties.SOUTH, vfS || s);
               }

               if (state.hasProperty(BlockStateProperties.EAST)) {
                  state = (BlockState)state.setValue(BlockStateProperties.EAST, vfE || e);
               }

               if (state.hasProperty(BlockStateProperties.WEST)) {
                  state = (BlockState)state.setValue(BlockStateProperties.WEST, vfW || w);
               }

               if (state.hasProperty(BlockStateProperties.ROTATION_16)) {
                  int rotazione_attuale_cartello = (Integer)state.getValue(BlockStateProperties.ROTATION_16) + rot * 4;
                  if (rotazione_attuale_cartello >= 16) {
                     rotazione_attuale_cartello -= 16;
                  }

                  state = (BlockState)state.setValue(BlockStateProperties.ROTATION_16, rotazione_attuale_cartello);
               }

               state = doModdedBlockPropriety(state, true, rot, false, false, false);
            }

            blockdata = Operazioni.getBlockStateString(state);
         } else {
            blockdata = _blockdata;
         }

         return blockdata;
      }

      public Blueprint.BlueprintDatas Mirror(boolean Mx, boolean My, boolean Mz) {
         Blueprint.BlueprintDatas mirror = new Blueprint.BlueprintDatas(this.nome);
         mirror.altezza = this.altezza;
         mirror.lunghezzaZ = this.lunghezzaZ;
         mirror.lunghezzaX = this.lunghezzaX;

         for (int v = 0; v < 2; v++) {
            for (Entry<Integer, Map<String, String>> posizione_e_blocco : v == 0 ? this.BlocksData.entrySet() : this.NotSolidBlocksData.entrySet()) {
               Map<String, String> innerMap = posizione_e_blocco.getValue();

               for (Entry<String, String> dati_blocco : innerMap.entrySet()) {
                  BlockPos pos = new BlockPos(Operazioni.getVec3iFromString(dati_blocco.getKey()));
                  String blockdata = dati_blocco.getValue();
                  String bloccoId = blockdata.substring(blockdata.indexOf("<bid>") + 5, blockdata.indexOf("</bid>"));
                  blockdata = blockdata.replaceFirst("<bid>" + bloccoId + "</bid>", "");
                  if (Mx) {
                     pos = new BlockPos(this.lunghezzaX - 1 - pos.getX(), pos.getY(), pos.getZ());
                  }

                  if (Mz) {
                     pos = new BlockPos(pos.getX(), pos.getY(), this.lunghezzaZ - 1 - pos.getZ());
                  }

                  if (My) {
                     pos = new BlockPos(pos.getX(), this.altezza - 1 - pos.getY(), pos.getZ());
                  }

                  if (blockdata.equalsIgnoreCase("null")) {
                     blockdata = dati_blocco.getValue();
                  } else {
                     BlockState state = Operazioni.getPropertiesStringToBlock(Operazioni.getBlockFromString(bloccoId), blockdata);
                     Tuple<Boolean, BlockState> val = setCustomBlockProprietyMirrow(bloccoId, state, Mx, My, Mz);
                     if ((Boolean)val.getA()) {
                        state = (BlockState)val.getB();
                     } else {
                        boolean suXFatta = false;
                        boolean suYFatta = false;
                        boolean suZFatta = false;

                        for (int statesTime = 0; statesTime < 3; statesTime++) {
                           if ((statesTime != 0 || Mx) && (statesTime != 1 || My) && (statesTime != 2 || Mz)) {
                              if (statesTime == 0) {
                                 suXFatta = true;
                                 suYFatta = false;
                                 suZFatta = false;
                              } else if (statesTime == 1) {
                                 suXFatta = false;
                                 suYFatta = true;
                                 suZFatta = false;
                              } else if (statesTime == 2) {
                                 suXFatta = false;
                                 suYFatta = false;
                                 suZFatta = true;
                              }

                              if (suYFatta && state.hasProperty(BlockStateProperties.ATTACH_FACE)) {
                                 switch ((AttachFace)state.getValue(BlockStateProperties.ATTACH_FACE)) {
                                    case FLOOR:
                                       state = (BlockState)state.setValue(BlockStateProperties.ATTACH_FACE, AttachFace.CEILING);
                                       break;
                                    case CEILING:
                                       state = (BlockState)state.setValue(BlockStateProperties.ATTACH_FACE, AttachFace.FLOOR);
                                 }
                              }

                              if (state.hasProperty(BlockStateProperties.FACING)) {
                                 Direction var31 = (Direction)state.getValue(BlockStateProperties.FACING);
                              } else if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                                 Direction dir = (Direction)state.getValue(BlockStateProperties.HORIZONTAL_FACING);
                                 if (dir.equals(Direction.NORTH) && suZFatta) {
                                    state = (BlockState)state.setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH);
                                 } else if (dir.equals(Direction.SOUTH) && suZFatta) {
                                    state = (BlockState)state.setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH);
                                 } else if (dir.equals(Direction.EAST) && suXFatta) {
                                    state = (BlockState)state.setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.WEST);
                                 } else if (dir.equals(Direction.WEST) && suXFatta) {
                                    state = (BlockState)state.setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST);
                                 } else if (dir.equals(Direction.UP) && suYFatta) {
                                    state = (BlockState)state.setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.DOWN);
                                 } else if (dir.equals(Direction.DOWN) && suYFatta) {
                                    state = (BlockState)state.setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.UP);
                                 }
                              }

                              if (state.hasProperty(BlockStateProperties.STAIRS_SHAPE) && !suYFatta) {
                                 StairsShape shape = (StairsShape)state.getValue(BlockStateProperties.STAIRS_SHAPE);
                                 if (shape.equals(StairsShape.INNER_LEFT)) {
                                    state = (BlockState)state.setValue(BlockStateProperties.STAIRS_SHAPE, StairsShape.INNER_RIGHT);
                                 } else if (shape.equals(StairsShape.INNER_RIGHT)) {
                                    state = (BlockState)state.setValue(BlockStateProperties.STAIRS_SHAPE, StairsShape.INNER_LEFT);
                                 } else if (shape.equals(StairsShape.OUTER_LEFT)) {
                                    state = (BlockState)state.setValue(BlockStateProperties.STAIRS_SHAPE, StairsShape.OUTER_RIGHT);
                                 } else if (shape.equals(StairsShape.OUTER_RIGHT)) {
                                    state = (BlockState)state.setValue(BlockStateProperties.STAIRS_SHAPE, StairsShape.OUTER_LEFT);
                                 }
                              }

                              if (state.hasProperty(BlockStateProperties.RAIL_SHAPE) && !suYFatta) {
                                 RailShape shape = (RailShape)state.getValue(BlockStateProperties.RAIL_SHAPE);
                                 if (suXFatta) {
                                    if (shape.equals(RailShape.SOUTH_WEST)) {
                                       state = (BlockState)state.setValue(BlockStateProperties.RAIL_SHAPE, RailShape.SOUTH_EAST);
                                    } else if (shape.equals(RailShape.SOUTH_EAST)) {
                                       state = (BlockState)state.setValue(BlockStateProperties.RAIL_SHAPE, RailShape.SOUTH_WEST);
                                    } else if (shape.equals(RailShape.NORTH_WEST)) {
                                       state = (BlockState)state.setValue(BlockStateProperties.RAIL_SHAPE, RailShape.NORTH_EAST);
                                    } else if (shape.equals(RailShape.NORTH_EAST)) {
                                       state = (BlockState)state.setValue(BlockStateProperties.RAIL_SHAPE, RailShape.NORTH_WEST);
                                    } else if (shape.equals(RailShape.ASCENDING_EAST)) {
                                       state = (BlockState)state.setValue(BlockStateProperties.RAIL_SHAPE, RailShape.ASCENDING_WEST);
                                    } else if (shape.equals(RailShape.ASCENDING_WEST)) {
                                       state = (BlockState)state.setValue(BlockStateProperties.RAIL_SHAPE, RailShape.ASCENDING_EAST);
                                    }
                                 } else if (shape.equals(RailShape.NORTH_EAST)) {
                                    state = (BlockState)state.setValue(BlockStateProperties.RAIL_SHAPE, RailShape.SOUTH_EAST);
                                 } else if (shape.equals(RailShape.SOUTH_EAST)) {
                                    state = (BlockState)state.setValue(BlockStateProperties.RAIL_SHAPE, RailShape.NORTH_EAST);
                                 } else if (shape.equals(RailShape.NORTH_WEST)) {
                                    state = (BlockState)state.setValue(BlockStateProperties.RAIL_SHAPE, RailShape.SOUTH_WEST);
                                 } else if (shape.equals(RailShape.SOUTH_WEST)) {
                                    state = (BlockState)state.setValue(BlockStateProperties.RAIL_SHAPE, RailShape.NORTH_WEST);
                                 } else if (shape.equals(RailShape.ASCENDING_NORTH)) {
                                    state = (BlockState)state.setValue(BlockStateProperties.RAIL_SHAPE, RailShape.ASCENDING_SOUTH);
                                 } else if (shape.equals(RailShape.ASCENDING_SOUTH)) {
                                    state = (BlockState)state.setValue(BlockStateProperties.RAIL_SHAPE, RailShape.ASCENDING_NORTH);
                                 }
                              }

                              if (state.hasProperty(BlockStateProperties.RAIL_SHAPE_STRAIGHT) && !suYFatta) {
                                 RailShape shape = (RailShape)state.getValue(BlockStateProperties.RAIL_SHAPE_STRAIGHT);
                                 if (suXFatta) {
                                    if (shape.equals(RailShape.ASCENDING_EAST)) {
                                       state = (BlockState)state.setValue(BlockStateProperties.RAIL_SHAPE_STRAIGHT, RailShape.ASCENDING_WEST);
                                    } else if (shape.equals(RailShape.ASCENDING_WEST)) {
                                       state = (BlockState)state.setValue(BlockStateProperties.RAIL_SHAPE_STRAIGHT, RailShape.ASCENDING_EAST);
                                    }
                                 } else if (shape.equals(RailShape.ASCENDING_NORTH)) {
                                    state = (BlockState)state.setValue(BlockStateProperties.RAIL_SHAPE_STRAIGHT, RailShape.ASCENDING_SOUTH);
                                 } else if (shape.equals(RailShape.ASCENDING_SOUTH)) {
                                    state = (BlockState)state.setValue(BlockStateProperties.RAIL_SHAPE_STRAIGHT, RailShape.ASCENDING_NORTH);
                                 }
                              }

                              if (!suYFatta) {
                                 boolean n = state.hasProperty(BlockStateProperties.NORTH) ? (Boolean)state.getValue(BlockStateProperties.NORTH) : false;
                                 boolean s = state.hasProperty(BlockStateProperties.SOUTH) ? (Boolean)state.getValue(BlockStateProperties.SOUTH) : false;
                                 boolean e = state.hasProperty(BlockStateProperties.EAST) ? (Boolean)state.getValue(BlockStateProperties.EAST) : false;
                                 boolean w = state.hasProperty(BlockStateProperties.WEST) ? (Boolean)state.getValue(BlockStateProperties.WEST) : false;
                                 boolean fvN = n;
                                 boolean fvS = s;
                                 boolean fvE = e;
                                 boolean fvW = w;
                                 if (suZFatta) {
                                    if (n && !s) {
                                       fvS = true;
                                       fvN = false;
                                    } else if (s && !n) {
                                       fvN = true;
                                       fvS = false;
                                    }
                                 } else if (w && !e) {
                                    fvE = true;
                                    fvW = false;
                                 } else if (e && !w) {
                                    fvW = true;
                                    fvE = false;
                                 }

                                 if (state.hasProperty(BlockStateProperties.NORTH)) {
                                    state = (BlockState)state.setValue(BlockStateProperties.NORTH, fvN);
                                 }

                                 if (state.hasProperty(BlockStateProperties.SOUTH)) {
                                    state = (BlockState)state.setValue(BlockStateProperties.SOUTH, fvS);
                                 }

                                 if (state.hasProperty(BlockStateProperties.EAST)) {
                                    state = (BlockState)state.setValue(BlockStateProperties.EAST, fvE);
                                 }

                                 if (state.hasProperty(BlockStateProperties.WEST)) {
                                    state = (BlockState)state.setValue(BlockStateProperties.WEST, fvW);
                                 }
                              } else {
                                 if (state.hasProperty(BlockStateProperties.HALF)) {
                                    state = (BlockState)state.setValue(
                                       BlockStateProperties.HALF,
                                       ((Half)state.getValue(BlockStateProperties.HALF)).equals(Half.BOTTOM) ? Half.TOP : Half.BOTTOM
                                    );
                                 }

                                 if (state.hasProperty(BlockStateProperties.SLAB_TYPE)) {
                                    state = (BlockState)state.setValue(
                                       BlockStateProperties.SLAB_TYPE,
                                       ((SlabType)state.getValue(BlockStateProperties.SLAB_TYPE)).equals(SlabType.BOTTOM) ? SlabType.TOP : SlabType.BOTTOM
                                    );
                                 }
                              }

                              state = doModdedBlockPropriety(state, false, (short)0, suXFatta, suYFatta, suZFatta);
                           }
                        }
                     }

                     blockdata = Operazioni.getBlockStateString(state);
                  }

                  String nbt = this.BlocksNBT != null
                        && this.BlocksNBT.containsKey(pos.getY())
                        && this.BlocksNBT.get(pos.getY()).containsKey(dati_blocco.getKey())
                     ? this.BlocksNBT.get(pos.getY()).get(dati_blocco.getKey())
                     : "";
                  if (v == 0) {
                     mirror.addBlock(pos.getY(), Operazioni.getVec3iString(new Vec3i(pos.getX(), pos.getY(), pos.getZ())), blockdata, nbt);
                  } else {
                     mirror.addNonSolidBlock(pos.getY(), Operazioni.getVec3iString(new Vec3i(pos.getX(), pos.getY(), pos.getZ())), blockdata, nbt);
                  }
               }
            }
         }

         return mirror;
      }

      public static boolean controlloProprietaBlocchi(String blockdata) {
         String bloccoId = blockdata.substring(blockdata.indexOf("<bid>") + 5, blockdata.indexOf("</bid>"));
         String idMod = bloccoId.split(":")[0];
         blockdata = blockdata.replaceFirst("<bid>" + bloccoId + "</bid>", "");
         String proprieta = blockdata;
         if (proprieta.equals("null")) {
            return false;
         }

         proprieta = proprieta.replace("<properties>", "").replace("</properties>", "");

         for (String v : proprieta.split(";!v!;")) {
            if (!v.equals("")) {
               String nome = v.substring(v.indexOf("<name>") + 6, v.indexOf("</name>"));
               String valore = v.substring(v.indexOf("<value>") + 7, v.indexOf("</value>"));
               if (idMod.equals("dramaticdoors") && nome.equals("third") && !valore.equals("lower")) {
                  return true;
               }
            }
         }

         return false;
      }

      public static boolean ServerBlackListBlock(String blockdata) {
         String bloccoId = blockdata.substring(blockdata.indexOf("<bid>") + 5, blockdata.indexOf("</bid>"));
         return ModServerConfig.blacklistBlock.contains(bloccoId);
      }

      private static boolean respectCondition(Map<String, Tuple<String, Property>> proprietaBlocco, String condition) {
         String[] condizioni = condition.split(",");

         for (String condizione : condizioni) {
            String[] data = condizione.split("=");
            if (!proprietaBlocco.containsKey(data[0]) || !((String)proprietaBlocco.get(data[0]).getA()).equalsIgnoreCase(data[1])) {
               return false;
            }
         }

         return true;
      }

      public static Tuple<Boolean, BlockState> setCustomBlockProprietyRotate(String bloccoID, BlockState blockState, short rot) {
         boolean contieneBloccoID = false;

         try {
            if (CustomBlockProprietyHandle.Rotate.RULES.containsKey(bloccoID)) {
               contieneBloccoID = true;
               List<CustomBlockProprietyHandle.Rotate.Rule> rules = CustomBlockProprietyHandle.Rotate.RULES.get(bloccoID);
               Map<String, Tuple<String, Property>> proprietaBlocco = new HashMap<>();

               for (Property p : blockState.getProperties()) {
                  proprietaBlocco.put(p.getName(), new Tuple(blockState.getValue(p).toString(), p));
               }

               for (CustomBlockProprietyHandle.Rotate.Rule rule : rules) {
                  if (rule.getRot() == rot) {
                     for (CustomBlockProprietyHandle.Rotate.PropertyRule pr : rule.properties) {
                        if (respectCondition(proprietaBlocco, pr.condition)) {
                           for (CustomBlockProprietyHandle.Rotate.PropertyValue propertyValue : pr.result) {
                              blockState = Operazioni.setDynamicProperty(
                                 blockState, (Property)proprietaBlocco.get(propertyValue.name).getB(), propertyValue.value
                              );
                           }
                        }
                     }
                  }
               }
            }
         } catch (Exception e) {
            MOD.LOGGER.error("[Portable Blueprints] -> Custom block rotation propriety error!");
            MOD.LOGGER.error(e.getMessage());
         }

         return new Tuple(contieneBloccoID, blockState);
      }

      public static Tuple<Boolean, BlockState> setCustomBlockProprietyMirrow(
         String bloccoID, BlockState blockState, boolean mirrowX, boolean mirrowY, boolean mirrowZ
      ) {
         boolean contieneBloccoID = false;

         try {
            if (CustomBlockProprietyHandle.Mirror.RULES.containsKey(bloccoID)) {
               contieneBloccoID = true;

               for (CustomBlockProprietyHandle.Mirror.Rule rule : CustomBlockProprietyHandle.Mirror.RULES.get(bloccoID)) {
                  if (rule.mirror.equals(Axis.X) && mirrowX || rule.mirror.equals(Axis.Y) && mirrowY || rule.mirror.equals(Axis.Z) && mirrowZ) {
                     Map<String, Tuple<String, Property>> proprietaBlocco = new HashMap<>();

                     for (Property p : blockState.getProperties()) {
                        proprietaBlocco.put(p.getName(), new Tuple(blockState.getValue(p).toString(), p));
                     }

                     for (CustomBlockProprietyHandle.Mirror.PropertyRule pr : rule.properties) {
                        if (respectCondition(proprietaBlocco, pr.condition)) {
                           for (CustomBlockProprietyHandle.Mirror.PropertyValue propertyValue : pr.result) {
                              blockState = Operazioni.setDynamicProperty(
                                 blockState, (Property)proprietaBlocco.get(propertyValue.name).getB(), propertyValue.value
                              );
                           }
                        }
                     }
                  }
               }
            }
         } catch (Exception e) {
            MOD.printLoggerError("Custom block mirror propriety error!");
            MOD.printLoggerError(e.getMessage());
         }

         return new Tuple(contieneBloccoID, blockState);
      }

      public static BlockState doModdedBlockPropriety(BlockState state, boolean rotation, short rot, boolean MX, boolean MY, boolean MZ) {
         String blockModId = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getNamespace();
         Map<String, Tuple<String, Property>> proprietaBlocco = new HashMap<>();

         for (Property p : state.getProperties()) {
            proprietaBlocco.put(p.getName(), new Tuple(state.getValue(p).toString(), p));
         }

         if (rotation) {
            if (blockModId.equals("diagonalwalls") || blockModId.equals("diagonalfences")) {
               boolean nw = proprietaBlocco.containsKey("north_west") ? Boolean.parseBoolean((String)proprietaBlocco.get("north_west").getA()) : false;
               boolean ne = proprietaBlocco.containsKey("north_east") ? Boolean.parseBoolean((String)proprietaBlocco.get("north_east").getA()) : false;
               boolean sw = proprietaBlocco.containsKey("south_west") ? Boolean.parseBoolean((String)proprietaBlocco.get("south_west").getA()) : false;
               boolean se = proprietaBlocco.containsKey("south_east") ? Boolean.parseBoolean((String)proprietaBlocco.get("south_east").getA()) : false;
               boolean vfNW = false;
               boolean vfNE = false;
               boolean vfSW = false;
               boolean vfSE = false;
               if (nw) {
                  if (rot != 0) {
                     nw = false;
                  }

                  switch (rot) {
                     case 1:
                        vfNE = true;
                        break;
                     case 2:
                        vfSE = true;
                        break;
                     case 3:
                        vfSW = true;
                  }
               }

               if (ne) {
                  if (rot != 0) {
                     ne = false;
                  }

                  switch (rot) {
                     case 1:
                        vfSE = true;
                        break;
                     case 2:
                        vfSW = true;
                        break;
                     case 3:
                        vfNW = true;
                  }
               }

               if (se) {
                  if (rot != 0) {
                     se = false;
                  }

                  switch (rot) {
                     case 1:
                        vfSW = true;
                        break;
                     case 2:
                        vfNW = true;
                        break;
                     case 3:
                        vfNE = true;
                  }
               }

               if (sw) {
                  if (rot != 0) {
                     sw = false;
                  }

                  switch (rot) {
                     case 1:
                        vfNW = true;
                        break;
                     case 2:
                        vfNE = true;
                        break;
                     case 3:
                        vfSE = true;
                  }
               }

               if (proprietaBlocco.containsKey("north_west")) {
                  state = (BlockState)state.setValue((Property)proprietaBlocco.get("north_west").getB(), vfNW || nw);
               }

               if (proprietaBlocco.containsKey("north_east")) {
                  state = (BlockState)state.setValue((Property)proprietaBlocco.get("north_east").getB(), vfNE || ne);
               }

               if (proprietaBlocco.containsKey("south_west")) {
                  state = (BlockState)state.setValue((Property)proprietaBlocco.get("south_west").getB(), vfSW || sw);
               }

               if (proprietaBlocco.containsKey("south_east")) {
                  state = (BlockState)state.setValue((Property)proprietaBlocco.get("south_east").getB(), vfSE || se);
               }
            }
         } else if ((blockModId.equals("diagonalwalls") || blockModId.equals("diagonalfences")) && !MY) {
            boolean nw = proprietaBlocco.containsKey("north_west") ? Boolean.parseBoolean((String)proprietaBlocco.get("north_west").getA()) : false;
            boolean ne = proprietaBlocco.containsKey("north_east") ? Boolean.parseBoolean((String)proprietaBlocco.get("north_east").getA()) : false;
            boolean sw = proprietaBlocco.containsKey("south_west") ? Boolean.parseBoolean((String)proprietaBlocco.get("south_west").getA()) : false;
            boolean se = proprietaBlocco.containsKey("south_east") ? Boolean.parseBoolean((String)proprietaBlocco.get("south_east").getA()) : false;
            boolean vfNW = nw;
            boolean vfNE = ne;
            boolean vfSW = sw;
            boolean vfSE = se;
            if (MZ) {
               if (nw && !sw) {
                  vfSW = true;
                  vfNW = false;
               } else if (sw && !nw) {
                  vfNW = true;
                  vfSW = false;
               }

               if (ne && !se) {
                  vfSE = true;
                  vfNE = false;
               } else if (se && !ne) {
                  vfNE = true;
                  vfSE = false;
               }
            } else {
               if (sw && !se) {
                  vfSE = true;
                  vfSW = false;
               } else if (se && !sw) {
                  vfSW = true;
                  vfSE = false;
               }

               if (nw && !ne) {
                  vfNE = true;
                  vfNW = false;
               } else if (ne && !nw) {
                  vfNW = true;
                  vfNE = false;
               }
            }

            if (proprietaBlocco.containsKey("north_west")) {
               state = (BlockState)state.setValue((Property)proprietaBlocco.get("north_west").getB(), vfNW);
            }

            if (proprietaBlocco.containsKey("north_east")) {
               state = (BlockState)state.setValue((Property)proprietaBlocco.get("north_east").getB(), vfNE);
            }

            if (proprietaBlocco.containsKey("south_west")) {
               state = (BlockState)state.setValue((Property)proprietaBlocco.get("south_west").getB(), vfSW);
            }

            if (proprietaBlocco.containsKey("south_east")) {
               state = (BlockState)state.setValue((Property)proprietaBlocco.get("south_east").getB(), vfSE);
            }
         }

         return state;
      }
   }

   public static class BlueprintPermission {
      public Map<String, Boolean> BlueprintsPermissionSetup = new HashMap<>();
      public Map<UUID, List<String>> PlayerPermission = new HashMap<>();

      public void addServerBlueprintPermission(String nome, boolean isFree) {
         this.BlueprintsPermissionSetup.put(nome, isFree);
      }

      public void removeServerBlueprintPermission(String nome) {
         this.BlueprintsPermissionSetup.remove(nome);

         for (UUID uuid : this.PlayerPermission.keySet()) {
            List<String> remValues = new ArrayList<>();

            for (String permission : this.PlayerPermission.get(uuid)) {
               if (permission.contains("<name>" + nome + "</name>")) {
                  remValues.add(permission);
               }
            }

            for (String remove : remValues) {
               this.PlayerPermission.get(uuid).remove(remove);
            }
         }
      }

      public void setPermissionForPlayer(ServerPlayer player, String nome, boolean hasPermission) {
         if (this.BlueprintsPermissionSetup.containsKey(nome)) {
            if (!this.PlayerPermission.containsKey(player.getUUID())) {
               this.PlayerPermission.put(player.getUUID(), new ArrayList<>());
            }

            if (this.PlayerPermission.get(player.getUUID()).contains("<name>" + nome + "</name><permission>false</permission>")) {
               this.PlayerPermission.get(player.getUUID()).remove("<name>" + nome + "</name><permission>false</permission>");
            }

            if (this.PlayerPermission.get(player.getUUID()).contains("<name>" + nome + "</name><permission>true</permission>")) {
               this.PlayerPermission.get(player.getUUID()).remove("<name>" + nome + "</name><permission>true</permission>");
            }

            this.PlayerPermission.get(player.getUUID()).add("<name>" + nome + "</name><permission>" + hasPermission + "</permission>");
         }
      }

      public List<String> getPlayerAllAllowedBlueprints(UUID player) {
         List<String> bps = new ArrayList<>();

         for (String nome : this.BlueprintsPermissionSetup.keySet()) {
            if (this.BlueprintsPermissionSetup.get(nome)
               || this.PlayerPermission.containsKey(player)
                  && this.PlayerPermission.get(player).contains("<name>" + nome + "</name><permission>true</permission>")) {
               bps.add(nome);
            }
         }

         return bps;
      }
   }

   private static class BlueprintStartingData {
      public BlockPos angolo1;
      public BlockPos angolo2;
      public String nome;
      public ServerLevel serverLevel;
      public UUID uuid;
      public int posXControllata;
      public int posYControllata;
      public int posZControllata;
      public int altezzaArrivato = 0;
      public boolean consideraCreativeBlocks = false;
      public List<String> scannerIgnoredBlocks;
      public boolean annullaAnimazioneScanner = false;
      public UUID uuidAnimazioneScanner;

      public BlueprintStartingData(
         ServerLevel serverLevel,
         String nome,
         BlockPos angolo1,
         BlockPos angolo2,
         UUID uuid,
         boolean consideraCreativeBlocks,
         List<String> scannerIgnoredBlocks,
         UUID uuidAnimazioneScanner
      ) {
         this.angolo1 = angolo1;
         this.angolo2 = angolo2;
         this.nome = nome;
         this.serverLevel = serverLevel;
         this.uuid = uuid;
         this.posXControllata = angolo1.getX();
         this.posYControllata = angolo1.getY();
         this.posZControllata = angolo1.getZ();
         this.consideraCreativeBlocks = consideraCreativeBlocks;
         this.scannerIgnoredBlocks = scannerIgnoredBlocks;
         this.uuidAnimazioneScanner = uuidAnimazioneScanner;
      }
   }

   private static class BlueprintStructure {
      public List<BlockPos> posDiEspansione = new ArrayList<>();
      public boolean solidi = true;
      public ServerLevel serverLevel;
      public Blueprint.BlueprintDatas bd;
      public List<BlockPos> posizione_container;
      public Player owner;
      public boolean free_build;
      public short allow_nbt = -1;
      public boolean emc_active = false;

      public BlueprintStructure(
         ServerLevel serverLevel,
         Blueprint.BlueprintDatas bd,
         Player player,
         List<BlockPos> posizione_container,
         boolean free_build,
         short allow_nbt,
         boolean emc_active
      ) {
         this.serverLevel = serverLevel;
         this.owner = player;
         this.bd = bd;
         this.posizione_container = posizione_container;
         this.free_build = free_build;
         this.allow_nbt = allow_nbt;
         if (ModCompatibilityInit.isProjectELoaded) {
            this.emc_active = emc_active;
         }

         this.posDiEspansione.add(new BlockPos(bd.lunghezzaX - 1, 0, bd.lunghezzaZ - 1));
      }
   }

   public static class SharedWithMe {
      public List<String> SharedWithOthersBlueprints = new ArrayList<>();
      public List<String> SharedWithMeBlueprints = new ArrayList<>();

      public void addSharedWithMeBlueprint(UUID PlayerCheCondivideIlSuoBlueprint, String NomeBlueprint) {
         String dato = "<owner>" + PlayerCheCondivideIlSuoBlueprint.toString() + "</owner><name>" + NomeBlueprint + "</name>";
         if (!this.SharedWithMeBlueprints.contains(dato)) {
            this.SharedWithMeBlueprints.add(dato);
         }
      }

      public void removeSharedWithMeBlueprint(UUID PlayerCheCondivideIlSuoBlueprint, String NomeBlueprint) {
         String dato = "<owner>" + PlayerCheCondivideIlSuoBlueprint.toString() + "</owner><name>" + NomeBlueprint + "</name>";
         if (this.SharedWithMeBlueprints.contains(dato)) {
            this.SharedWithMeBlueprints.remove(dato);
         }
      }

      public void addSharedWithOthersBlueprint(UUID PlayerAQuiVaIlBlueprint, String NomeBlueprint) {
         String dato = "<shared_with>" + PlayerAQuiVaIlBlueprint.toString() + "</shared_with><name>" + NomeBlueprint + "</name>";
         if (!this.SharedWithOthersBlueprints.contains(dato)) {
            this.SharedWithOthersBlueprints.add(dato);
         }
      }

      public void removeSharedWithOthersBlueprint(UUID PlayerAQuiVaIlBlueprint, String NomeBlueprint) {
         String dato = "<shared_with>" + PlayerAQuiVaIlBlueprint.toString() + "</shared_with><name>" + NomeBlueprint + "</name>";
         if (this.SharedWithOthersBlueprints.contains(dato)) {
            this.SharedWithOthersBlueprints.remove(dato);
         }
      }

      public boolean ContieneSWMBlueprint(UUID PlayerCheCondivideIlSuoBlueprint, String NomeBlueprint) {
         return this.SharedWithMeBlueprints.contains("<owner>" + PlayerCheCondivideIlSuoBlueprint.toString() + "</owner><name>" + NomeBlueprint + "</name>");
      }

      public boolean ContieneSWOBlueprint(UUID PlayerConQuiCondivideBlueprint, String NomeBlueprint) {
         return this.SharedWithOthersBlueprints
            .contains("<shared_with>" + PlayerConQuiCondivideBlueprint.toString() + "</shared_with><name>" + NomeBlueprint + "</name>");
      }

      public List<UUID> getAllSpecificSWOBPlayersUUID(String nome_blueprint) {
         List<UUID> trovati = new ArrayList<>();

         for (String v : this.SharedWithOthersBlueprints) {
            if (Operazioni.getValueInsideTag(v, "name").equals(nome_blueprint)) {
               trovati.add(UUID.fromString(Operazioni.getValueInsideTag(v, "shared_with")));
            }
         }

         return trovati;
      }
   }
}
