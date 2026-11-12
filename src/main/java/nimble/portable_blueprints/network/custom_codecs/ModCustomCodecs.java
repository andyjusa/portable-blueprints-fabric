package nimble.portable_blueprints.network.custom_codecs;

import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.phys.Vec3;
import nimble.portable_blueprints.init.ModTabletTags;
import nimble.portable_blueprints.network.CtoS_UpdateServerConfigurationValue;
import nimble.portable_blueprints.network.request_ids_blueprints.CtoS_RequestIDsBlueprints;
import nimble.portable_blueprints.network.request_specific_data.CtoS_RequestSpecificData;
import nimble.portable_blueprints.network.request_specific_data.StoC_RequestSpecificData;
import nimble.portable_blueprints.portable_blueprints.OldVersionUtilities;
import nimble.portable_blueprints.robautile.Operazioni;

public class ModCustomCodecs {
   public static final StreamCodec<ByteBuf, ItemStack> ITEM_STACK = StreamCodec.of((buf, stack) -> {
      FriendlyByteBuf fb = new FriendlyByteBuf(buf);
      ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
      fb.writeUtf(id.toString());
      fb.writeInt(stack.getCount());
      fb.writeNbt(OldVersionUtilities.getItemStackNBT(stack));
   }, buf -> {
      FriendlyByteBuf fb = new FriendlyByteBuf(buf);
      Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(fb.readUtf()));
      int count = fb.readInt();
      if (count < 0 || count > 99) throw new io.netty.handler.codec.DecoderException("Invalid item count");
      ItemStack itemStack = new ItemStack(item, count);
      var tag = fb.readNbt();
      if (tag != null) OldVersionUtilities.impostaItemStackNBT(itemStack, tag);
      return itemStack;
   });
   public static final StreamCodec<ByteBuf, BlockPos> BLOCKPOS = StreamCodec.of((buf, pos) -> {
      FriendlyByteBuf fb = new FriendlyByteBuf(buf);
      fb.writeBlockPos(pos);
   }, buf -> {
      FriendlyByteBuf fb = new FriendlyByteBuf(buf);
      return fb.readBlockPos();
   });
   public static final StreamCodec<ByteBuf, List<BlockPos>> LIST_BLOCKPOS = StreamCodec.of((buf, poss) -> {
      FriendlyByteBuf fb = new FriendlyByteBuf(buf);
      fb.writeInt(poss.size());

      for (BlockPos pos : poss) {
         fb.writeBlockPos(pos);
      }
   }, buf -> {
      FriendlyByteBuf fb = new FriendlyByteBuf(buf);
      List<BlockPos> poss = new ArrayList<>();
      int size = fb.readInt();
      if (size < 0 || size > 1024) throw new io.netty.handler.codec.DecoderException("Too many block positions");

      for (int i = 0; i < size; i++) {
         poss.add(fb.readBlockPos());
      }

      return poss;
   });
   public static final StreamCodec<ByteBuf, UUID> UUID = StreamCodec.of((buf, uuid) -> {
      FriendlyByteBuf fb = new FriendlyByteBuf(buf);
      fb.writeBoolean(uuid != null);
      if (uuid != null) {
         fb.writeUUID(uuid);
      }
   }, buf -> {
      FriendlyByteBuf fb = new FriendlyByteBuf(buf);
      return fb.readBoolean() ? fb.readUUID() : null;
   });
   public static final StreamCodec<ByteBuf, Item> ITEM = StreamCodec.of((buf, item) -> {
      FriendlyByteBuf fb = new FriendlyByteBuf(buf);
      ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
      fb.writeUtf(id.toString());
   }, buf -> {
      FriendlyByteBuf fb = new FriendlyByteBuf(buf);
      return (Item)BuiltInRegistries.ITEM.get(ResourceLocation.parse(fb.readUtf()));
   });
   public static final StreamCodec<ByteBuf, Vec3> VEC3 = StreamCodec.of((buf, vec3) -> {
      FriendlyByteBuf fb = new FriendlyByteBuf(buf);
      fb.writeUtf(Operazioni.getVec3String(vec3));
   }, buf -> {
      FriendlyByteBuf fb = new FriendlyByteBuf(buf);
      return Operazioni.getVec3FromString(fb.readUtf());
   });
   public static final StreamCodec<ByteBuf, CtoS_RequestIDsBlueprints.Tipo> ENUM_TIPOIDsBlueprints = StreamCodec.of((buf, TipoRichiesta) -> {
      FriendlyByteBuf fb = new FriendlyByteBuf(buf);
      fb.writeEnum(TipoRichiesta);
   }, buf -> {
      FriendlyByteBuf fb = new FriendlyByteBuf(buf);
      return (CtoS_RequestIDsBlueprints.Tipo)fb.readEnum(CtoS_RequestIDsBlueprints.Tipo.class);
   });
   public static final StreamCodec<ByteBuf, ModTabletTags.TagData> TAGDATA = StreamCodec.of((buf, tagData) -> {
      FriendlyByteBuf fb = new FriendlyByteBuf(buf);
      fb.writeInt(tagData.tag_name.keySet().size());

      for (String key : tagData.tag_name.keySet()) {
         fb.writeUtf(key);
         fb.writeUtf(tagData.tag_name.get(key));
      }
   }, buf -> {
      FriendlyByteBuf fb = new FriendlyByteBuf(buf);
      Map<String, String> tagdatas = new HashMap<>();
      int count = fb.readInt();
      if (count < 0 || count > 4096) throw new io.netty.handler.codec.DecoderException("Too many tags");

      for (int x = 0; x < count; x++) {
         tagdatas.put(fb.readUtf(), fb.readUtf());
      }

      return new ModTabletTags.TagData(tagdatas);
   });
   public static final StreamCodec<ByteBuf, CtoS_RequestSpecificData.TipoRichiesta> ENUM_CtoS_RequestSpecificData_TIPORICHIESTA = StreamCodec.of(
      (buf, TipoRichiesta) -> {
         FriendlyByteBuf fb = new FriendlyByteBuf(buf);
         fb.writeEnum(TipoRichiesta);
      }, buf -> {
         FriendlyByteBuf fb = new FriendlyByteBuf(buf);
         return (CtoS_RequestSpecificData.TipoRichiesta)fb.readEnum(CtoS_RequestSpecificData.TipoRichiesta.class);
      }
   );
   public static final StreamCodec<ByteBuf, StoC_RequestSpecificData.TipoDato> ENUM_StoC_RequestSpecificData_TIPODATO = StreamCodec.of(
      (buf, TipoRichiesta) -> {
         FriendlyByteBuf fb = new FriendlyByteBuf(buf);
         fb.writeEnum(TipoRichiesta);
      }, buf -> {
         FriendlyByteBuf fb = new FriendlyByteBuf(buf);
         return (StoC_RequestSpecificData.TipoDato)fb.readEnum(StoC_RequestSpecificData.TipoDato.class);
      }
   );
   public static final StreamCodec<ByteBuf, CtoS_UpdateServerConfigurationValue.TipoValore> ENUM_TIPOVALOREUPDATESERVER = StreamCodec.of(
      (buf, TipoRichiesta) -> {
         FriendlyByteBuf fb = new FriendlyByteBuf(buf);
         fb.writeEnum(TipoRichiesta);
      }, buf -> {
         FriendlyByteBuf fb = new FriendlyByteBuf(buf);
         return (CtoS_UpdateServerConfigurationValue.TipoValore)fb.readEnum(CtoS_UpdateServerConfigurationValue.TipoValore.class);
      }
   );
}
