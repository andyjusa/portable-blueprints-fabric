package nimble.portable_blueprints.network;

import io.netty.buffer.ByteBuf;
import java.util.function.BiConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import nimble.portable_blueprints.fabric.network.PacketDistributor;
import nimble.portable_blueprints.fabric.network.PayloadContext;
import nimble.portable_blueprints.network.ServerBlueprints.CtoS_ServerBlueprintsData;
import nimble.portable_blueprints.network.ServerBlueprints.StoC_ServerBlueprintsData;
import nimble.portable_blueprints.network.SharedBlueprints.CtoS_AddShareWithOthers;
import nimble.portable_blueprints.network.SharedBlueprints.CtoS_RemoveShareWithOthers;
import nimble.portable_blueprints.network.blueprint_tags.CtoS_GetBlueprintTags;
import nimble.portable_blueprints.network.blueprint_tags.CtoS_SetBlueprintTag;
import nimble.portable_blueprints.network.blueprint_tags.StoC_GetBlueprintTags;
import nimble.portable_blueprints.network.contenuto_container_selezionati.CtoS_ContenutoContainerSelezionati;
import nimble.portable_blueprints.network.contenuto_container_selezionati.StoC_ContenutoContainerSelezionati;
import nimble.portable_blueprints.network.controllo_esiste_blueprint.CtoS_BlueprintExist;
import nimble.portable_blueprints.network.controllo_esiste_blueprint.StoC_BlueprintExist;
import nimble.portable_blueprints.network.global_blueprint_net.CtoS_SaveGlobalBlueprintInServer;
import nimble.portable_blueprints.network.ottieni_nome_x_blueprint.CtoS_BlueprintXName;
import nimble.portable_blueprints.network.ottieni_nome_x_blueprint.StoC_BlueprintXName;
import nimble.portable_blueprints.network.ottieni_players_uuid_mondo.CtoS_GetWorldPlayersUUID;
import nimble.portable_blueprints.network.ottieni_players_uuid_mondo.StoC_GetWorldPlayersUUID;
import nimble.portable_blueprints.network.ottieni_shared_blueprint_data.CtoS_SharedBlueprintData;
import nimble.portable_blueprints.network.ottieni_shared_blueprint_data.StoC_SharedBlueprintData;
import nimble.portable_blueprints.network.progress_bar_player.StoC_ProgressBarPlayer;
import nimble.portable_blueprints.network.request_ids_blueprints.CtoS_RequestIDsBlueprints;
import nimble.portable_blueprints.network.request_ids_blueprints.StoC_RequestIDsBlueprints;
import nimble.portable_blueprints.network.request_server_tags.CtoS_RequestServerTags;
import nimble.portable_blueprints.network.request_server_tags.StoC_RequestServerTags;
import nimble.portable_blueprints.network.request_specific_data.CtoS_RequestSpecificData;
import nimble.portable_blueprints.network.request_specific_data.StoC_RequestSpecificData;
import nimble.portable_blueprints.network.rimuovi_blueprint.CtoS_BlueprintRemove;
import nimble.portable_blueprints.network.save_global_blueprint.CtoS_SaveGlobalBlueprint;
import nimble.portable_blueprints.network.save_global_blueprint.StoC_SaveGlobalBlueprint;
import nimble.portable_blueprints.network.server_blacklist_blocks.CtoS_ServerBlacklistBlocks;
import nimble.portable_blueprints.network.server_blacklist_blocks.StoC_ServerBlacklistBlocks;
import nimble.portable_blueprints.network.update_item_blueprint_nbt.CtoS_UpdateItemBlueprintNBT;
import nimble.portable_blueprints.network.update_item_blueprint_nbt.StoC_UpdateItemBlueprintNBT;
import nimble.portable_blueprints.network.update_item_scanner_nbt.CtoS_UpdateItemScannerNBT;
import nimble.portable_blueprints.network.visualize_blueprint_data.CtoS_VisualizeBlueprintData;
import nimble.portable_blueprints.network.visualize_blueprint_data.StoC_VisualizeBlueprintData;

/** Payload schemas are common; client handlers are installed only by the client entrypoint. */
public final class PacketHandler {
   private PacketHandler() {}

   public static void register() {
      PacketDistributor.initialize();
      toServer(CtoS_RemoveItem.TYPE, CtoS_RemoveItem.STREAM_CODEC, CtoS_RemoveItem::handle);
      toServer(CtoS_CreateBlueprint.TYPE, CtoS_CreateBlueprint.STREAM_CODEC, CtoS_CreateBlueprint::handle);
      toServer(CtoS_BlueprintRemove.TYPE, CtoS_BlueprintRemove.STREAM_CODEC, CtoS_BlueprintRemove::handle);
      toServer(CtoS_CreaItemBlueprint.TYPE, CtoS_CreaItemBlueprint.STREAM_CODEC, CtoS_CreaItemBlueprint::handle);
      toServer(CtoS_RemoveItemIfBlueprintNotExist.TYPE, CtoS_RemoveItemIfBlueprintNotExist.STREAM_CODEC, CtoS_RemoveItemIfBlueprintNotExist::handle);
      toServer(CtoS_AddShareWithOthers.TYPE, CtoS_AddShareWithOthers.STREAM_CODEC, CtoS_AddShareWithOthers::handle);
      toServer(CtoS_RemoveShareWithOthers.TYPE, CtoS_RemoveShareWithOthers.STREAM_CODEC, CtoS_RemoveShareWithOthers::handle);
      toServer(CtoS_UpdateItemScannerNBT.TYPE, CtoS_UpdateItemScannerNBT.STREAM_CODEC, CtoS_UpdateItemScannerNBT::handle);
      toServer(CtoS_CreateStructureByBlueprint.TYPE, CtoS_CreateStructureByBlueprint.STREAM_CODEC, CtoS_CreateStructureByBlueprint::handle);
      toServer(CtoS_BlueprintExist.TYPE, CtoS_BlueprintExist.STREAM_CODEC, CtoS_BlueprintExist::handle);
      toServer(CtoS_SaveGlobalBlueprint.TYPE, CtoS_SaveGlobalBlueprint.STREAM_CODEC, CtoS_SaveGlobalBlueprint::handle);
      toServer(CtoS_BlueprintXName.TYPE, CtoS_BlueprintXName.STREAM_CODEC, CtoS_BlueprintXName::handle);
      toServer(CtoS_VisualizeBlueprintData.TYPE, CtoS_VisualizeBlueprintData.STREAM_CODEC, CtoS_VisualizeBlueprintData::handle);
      toServer(CtoS_ContenutoContainerSelezionati.TYPE, CtoS_ContenutoContainerSelezionati.STREAM_CODEC, CtoS_ContenutoContainerSelezionati::handle);
      toServer(CtoS_UpdateItemBlueprintNBT.TYPE, CtoS_UpdateItemBlueprintNBT.STREAM_CODEC, CtoS_UpdateItemBlueprintNBT::handle);
      toServer(CtoS_SharedBlueprintData.TYPE, CtoS_SharedBlueprintData.STREAM_CODEC, CtoS_SharedBlueprintData::handle);
      toServer(CtoS_ServerBlueprintsData.TYPE, CtoS_ServerBlueprintsData.STREAM_CODEC, CtoS_ServerBlueprintsData::handle);
      toServer(CtoS_ServerBlacklistBlocks.TYPE, CtoS_ServerBlacklistBlocks.STREAM_CODEC, CtoS_ServerBlacklistBlocks::handle);
      toServer(CtoS_GetBlueprintTags.TYPE, CtoS_GetBlueprintTags.STREAM_CODEC, CtoS_GetBlueprintTags::handle);
      toServer(CtoS_SetBlueprintTag.TYPE, CtoS_SetBlueprintTag.STREAM_CODEC, CtoS_SetBlueprintTag::handle);
      toServer(CtoS_GetWorldPlayersUUID.TYPE, CtoS_GetWorldPlayersUUID.STREAM_CODEC, CtoS_GetWorldPlayersUUID::handle);
      toServer(CtoS_RequestIDsBlueprints.TYPE, CtoS_RequestIDsBlueprints.STREAM_CODEC, CtoS_RequestIDsBlueprints::handle);
      toServer(CtoS_RequestServerTags.TYPE, CtoS_RequestServerTags.STREAM_CODEC, CtoS_RequestServerTags::handle);
      toServer(CtoS_RequestSpecificData.TYPE, CtoS_RequestSpecificData.STREAM_CODEC, CtoS_RequestSpecificData::handle);
      toServer(CtoS_SaveGlobalBlueprintInServer.TYPE, CtoS_SaveGlobalBlueprintInServer.STREAM_CODEC, CtoS_SaveGlobalBlueprintInServer::handle);
      toServer(CtoS_CreateRemoveBlockWorkRequest.TYPE, CtoS_CreateRemoveBlockWorkRequest.STREAM_CODEC, CtoS_CreateRemoveBlockWorkRequest::handle);
      toServer(CtoS_UpdateServerConfigurationValue.TYPE, CtoS_UpdateServerConfigurationValue.STREAM_CODEC, CtoS_UpdateServerConfigurationValue::handle);
      PayloadTypeRegistry.playS2C().register(StoC_OpenScannerScreen.TYPE, StoC_OpenScannerScreen.STREAM_CODEC);
      PayloadTypeRegistry.playS2C().register(StoC_AnimazionePosizionamentoBlocco.TYPE, StoC_AnimazionePosizionamentoBlocco.STREAM_CODEC);
      PayloadTypeRegistry.playS2C().register(StoC_ScannerAnimation.TYPE, StoC_ScannerAnimation.STREAM_CODEC);
      PayloadTypeRegistry.playS2C().register(StoC_ObstructionBlock.TYPE, StoC_ObstructionBlock.STREAM_CODEC);
      PayloadTypeRegistry.playS2C().register(StoC_BlueprintExist.TYPE, StoC_BlueprintExist.STREAM_CODEC);
      PayloadTypeRegistry.playS2C().register(StoC_SaveGlobalBlueprint.TYPE, StoC_SaveGlobalBlueprint.STREAM_CODEC);
      PayloadTypeRegistry.playS2C().register(StoC_BlueprintXName.TYPE, StoC_BlueprintXName.STREAM_CODEC);
      PayloadTypeRegistry.playS2C().register(StoC_VisualizeBlueprintData.TYPE, StoC_VisualizeBlueprintData.STREAM_CODEC);
      PayloadTypeRegistry.playS2C().register(StoC_ContenutoContainerSelezionati.TYPE, StoC_ContenutoContainerSelezionati.STREAM_CODEC);
      PayloadTypeRegistry.playS2C().register(StoC_UpdateItemBlueprintNBT.TYPE, StoC_UpdateItemBlueprintNBT.STREAM_CODEC);
      PayloadTypeRegistry.playS2C().register(StoC_SharedBlueprintData.TYPE, StoC_SharedBlueprintData.STREAM_CODEC);
      PayloadTypeRegistry.playS2C().register(StoC_ServerBlueprintsData.TYPE, StoC_ServerBlueprintsData.STREAM_CODEC);
      PayloadTypeRegistry.playS2C().register(StoC_ServerBlacklistBlocks.TYPE, StoC_ServerBlacklistBlocks.STREAM_CODEC);
      PayloadTypeRegistry.playS2C().register(StoC_GetBlueprintTags.TYPE, StoC_GetBlueprintTags.STREAM_CODEC);
      PayloadTypeRegistry.playS2C().register(StoC_GetWorldPlayersUUID.TYPE, StoC_GetWorldPlayersUUID.STREAM_CODEC);
      PayloadTypeRegistry.playS2C().register(StoC_ProgressBarPlayer.TYPE, StoC_ProgressBarPlayer.STREAM_CODEC);
      PayloadTypeRegistry.playS2C().register(StoC_RequestServerTags.TYPE, StoC_RequestServerTags.STREAM_CODEC);
      PayloadTypeRegistry.playS2C().register(StoC_RequestSpecificData.TYPE, StoC_RequestSpecificData.STREAM_CODEC);
      PayloadTypeRegistry.playS2C().register(StoC_RequestIDsBlueprints.TYPE, StoC_RequestIDsBlueprints.STREAM_CODEC);
      PayloadTypeRegistry.playS2C().register(StoC_SendLinkedContainer.TYPE, StoC_SendLinkedContainer.STREAM_CODEC);
   }

   private static <T extends CustomPacketPayload> void toServer(CustomPacketPayload.Type<T> type,
         StreamCodec<? super net.minecraft.network.RegistryFriendlyByteBuf, T> codec,
         BiConsumer<T, PayloadContext> handler) {
      PayloadTypeRegistry.playC2S().register(type, codec);
      ServerPlayNetworking.registerGlobalReceiver(type, (payload, context) ->
         context.server().execute(() -> {
            if (nimble.portable_blueprints.fabric.network.PayloadValidation.permits(payload, context.player()))
               handler.accept(payload, new PayloadContext(context.player(), context.server()));
         }));
   }

   @Environment(EnvType.CLIENT)
   public static void registerClient() {
      ClientReceivers.register();
   }

   /** Isolate client API linkage from dedicated-server class loading. */
   @Environment(EnvType.CLIENT)
   private static final class ClientReceivers {
      private static void register() {
         register(StoC_OpenScannerScreen.TYPE, StoC_OpenScannerScreen::handle);
         register(StoC_AnimazionePosizionamentoBlocco.TYPE, StoC_AnimazionePosizionamentoBlocco::handle);
         register(StoC_ScannerAnimation.TYPE, StoC_ScannerAnimation::handle);
         register(StoC_ObstructionBlock.TYPE, StoC_ObstructionBlock::handle);
         register(StoC_BlueprintExist.TYPE, StoC_BlueprintExist::handle);
         register(StoC_SaveGlobalBlueprint.TYPE, StoC_SaveGlobalBlueprint::handle);
         register(StoC_BlueprintXName.TYPE, StoC_BlueprintXName::handle);
         register(StoC_VisualizeBlueprintData.TYPE, StoC_VisualizeBlueprintData::handle);
         register(StoC_ContenutoContainerSelezionati.TYPE, StoC_ContenutoContainerSelezionati::handle);
         register(StoC_UpdateItemBlueprintNBT.TYPE, StoC_UpdateItemBlueprintNBT::handle);
         register(StoC_SharedBlueprintData.TYPE, StoC_SharedBlueprintData::handle);
         register(StoC_ServerBlueprintsData.TYPE, StoC_ServerBlueprintsData::handle);
         register(StoC_ServerBlacklistBlocks.TYPE, StoC_ServerBlacklistBlocks::handle);
         register(StoC_GetBlueprintTags.TYPE, StoC_GetBlueprintTags::handle);
         register(StoC_GetWorldPlayersUUID.TYPE, StoC_GetWorldPlayersUUID::handle);
         register(StoC_ProgressBarPlayer.TYPE, StoC_ProgressBarPlayer::handle);
         register(StoC_RequestServerTags.TYPE, StoC_RequestServerTags::handle);
         register(StoC_RequestSpecificData.TYPE, StoC_RequestSpecificData::handle);
         register(StoC_RequestIDsBlueprints.TYPE, StoC_RequestIDsBlueprints::handle);
         register(StoC_SendLinkedContainer.TYPE, StoC_SendLinkedContainer::handle);
      }

      private static <T extends CustomPacketPayload> void register(CustomPacketPayload.Type<T> type,
            BiConsumer<T, PayloadContext> handler) {
         net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(type,
            (payload, context) -> handler.accept(payload, new PayloadContext(context.player(), context.client())));
      }
   }
}
