package nimble.portable_blueprints.client.screen.tablet.mineprint;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.phys.Vec2;
import nimble.portable_blueprints.fabric.client.RenderLevelStageEvent.Stage;
import nimble.portable_blueprints.fabric.network.PacketDistributor;
import nimble.portable_blueprints.altro.Blueprint;
import nimble.portable_blueprints.client.BlueprintsPreviewMethods;
import nimble.portable_blueprints.client.ServerToClientDatasShared;
import nimble.portable_blueprints.client.VisualizeBlueprintDatas;
import nimble.portable_blueprints.client.screen.tablet.TabletBlueprintInfoScreen;
import nimble.portable_blueprints.client.screen.tablet.TabletScreen;
import nimble.portable_blueprints.init.ModTabletTags;
import nimble.portable_blueprints.init.TabletClientDatas;
import nimble.portable_blueprints.network.blueprint_tags.CtoS_GetBlueprintTags;
import nimble.portable_blueprints.network.request_ids_blueprints.CtoS_RequestIDsBlueprints;
import nimble.portable_blueprints.robautile.Operazioni;
import nimble.portable_blueprints.robautile.RenderingMethods;
import org.joml.Matrix4f;

public class TabletMinePrintMethods {
   private static final Gson GSON = nimble.portable_blueprints.fabric.BlueprintJson.create();
   private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("portable_blueprints", "textures/gui/tablet/tablet_mineprint_gui.png");

   public static void renderLoading(GuiGraphics gg, int pX, int pY) {
      ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("portable_blueprints", "textures/gui/tablet/tablet_gui.png");
      long now = System.currentTimeMillis();
      double cycleMs = 3000.0;
      double value = now % (long)cycleMs / cycleMs;
      double v1 = 0.0;
      double v2 = 0.0;
      double v3 = 0.0;
      double v4 = 0.0;
      if (value < 0.25) {
         v1 = value / 0.25;
         v2 = 0.0;
         v3 = 1.0 - value / 0.25;
         v4 = 1.0;
      } else if (value >= 0.25 && value < 0.5) {
         v1 = 1.0;
         v2 = (value - 0.25) / 0.25;
         v3 = 0.0;
         v4 = 1.0 - (value - 0.25) / 0.25;
      } else if (value >= 0.5 && value < 0.75) {
         v1 = 1.0 - (value - 0.5) / 0.25;
         v2 = 1.0;
         v3 = (value - 0.5) / 0.25;
         v4 = 0.0;
      } else if (value >= 0.75) {
         v1 = 0.0;
         v2 = 1.0 - (value - 0.75) / 0.25;
         v3 = 1.0;
         v4 = (value - 0.75) / 0.25;
      }

      gg.blit(TEXTURE, pX + 3 + (int)(36.0 * v1), pY + 3 + (int)(36.0 * v2), 122, 227, 12, 12);
      gg.blit(TEXTURE, pX + 3 + (int)(36.0 * v3), pY + 3 + (int)(36.0 * v4), 122, 227, 12, 12);
   }

   public static class BlueprintPreviewItem {
      public int posX;
      public int start_posY;
      public int posY;
      public final String BName;
      public final TabletMinePrintMethods.TYPE type;
      private int idRequest = -1;
      private int BlueprintIDRequest = -1;
      public boolean isMHItem = false;
      public boolean isMHFollow = false;
      public boolean hasFollow = false;
      private BlueprintsPreviewMethods.BlueprintPreviewData BPD = null;
      private int CentroX = 0;
      private int CentroY = 0;
      private String idStruttura = "";
      private String idStrutturaOriginal;
      private UUID owner;
      public static Map<String, List<ModTabletTags.TagData>> TagStruttura = new HashMap<>();
      public boolean createIconStructure = false;
      private BufferedImage ImageFaccia;

      public BlueprintPreviewItem(String BName, TabletMinePrintMethods.TYPE type, UUID Owner) {
         this.BName = BName;
         this.type = type;
         this.owner = Owner;
         this.BlueprintIDRequest = ServerToClientDatasShared.getID();
         if (type.equals(TabletMinePrintMethods.TYPE.SERVER)) {
            CtoS_RequestIDsBlueprints.send(true, BName, this.BlueprintIDRequest, "server");
         } else if (type.equals(TabletMinePrintMethods.TYPE.SHARED_GLOBAL)) {
            CtoS_RequestIDsBlueprints.send(Owner, BName, this.BlueprintIDRequest, "global");
            Thread tickerThread = new Thread(() -> {
               try {
                  this.ImageFaccia = Operazioni.getPlayerFace(Owner);
               } catch (Exception e) {
                  e.printStackTrace();
               }
            });
            tickerThread.setDaemon(true);
            tickerThread.start();
         } else if (type.equals(TabletMinePrintMethods.TYPE.PERSONAL)) {
            CtoS_RequestIDsBlueprints.send(Owner, BName, this.BlueprintIDRequest, "personal");
         }
      }

      public void setPosition(int posX, int posY) {
         this.posX = posX;
         this.start_posY = posY;
         this.posY = posY;
      }

      public Component render(GuiGraphics gg, int CentroX, int CentroY, int mouseX, int mouseY, Vec2 chissPosX, Vec2 chissPosY) {
         Component tooltipDaVisualizzare = null;
         this.CentroX = CentroX;
         this.CentroY = CentroY;
         this.isMouseHover(mouseX, mouseY, chissPosX, chissPosY);
         if (this.createIconStructure) {
            if (this.BPD == null && ServerToClientDatasShared.StringMap.containsKey(this.BlueprintIDRequest)) {
               String id = ServerToClientDatasShared.StringMap.get(this.BlueprintIDRequest);
               if (!BlueprintsPreviewMethods.MapIconsRL.containsKey(Blueprint.sanitizePathUnique(id))) {
                  if (this.type.equals(TabletMinePrintMethods.TYPE.SERVER)) {
                     this.idRequest = VisualizeBlueprintDatas.newVisualizeBlueprint(true, this.BName, false, false);
                  } else if (this.type.equals(TabletMinePrintMethods.TYPE.SHARED_GLOBAL)) {
                     this.idRequest = VisualizeBlueprintDatas.newVisualizeBlueprint(this.owner, this.BName, false, false);
                  } else if (this.type.equals(TabletMinePrintMethods.TYPE.PERSONAL)) {
                     this.idRequest = VisualizeBlueprintDatas.newVisualizeBlueprint(this.owner, this.BName, false, false);
                  }
               } else {
                  this.BPD = new BlueprintsPreviewMethods.BlueprintPreviewData(null);
                  this.BPD.iconTexture = BlueprintsPreviewMethods.MapIconsRL.get(Blueprint.sanitizePathUnique(id));
                  this.idStruttura = id;
                  this.idStrutturaOriginal = this.owner == null ? id : id.replace("personal", this.owner.toString()).replace("global", this.owner.toString());

                  for (Tuple<String, UUID> data : TabletClientDatas.getDatas().followedData) {
                     if (((String)data.getA()).equals(this.idStruttura)
                        && (data.getB() == null ? data.getB() == this.owner : ((UUID)data.getB()).equals(this.owner))) {
                        this.hasFollow = true;
                        break;
                     }
                  }

                  ServerToClientDatasShared.BlueprintTags.remove(this.idStrutturaOriginal);
                  PacketDistributor.sendToServer(new CtoS_GetBlueprintTags(this.owner, this.BName), new CustomPacketPayload[0]);
               }

               ServerToClientDatasShared.StringMap.remove(this.BlueprintIDRequest);
            } else if (this.BPD == null && VisualizeBlueprintDatas.BlueprintsMap.containsKey(this.idRequest)) {
               this.BPD = new BlueprintsPreviewMethods.BlueprintPreviewData(VisualizeBlueprintDatas.BlueprintsMap.get(this.idRequest));
               this.BPD.prepareData(null);
               VisualizeBlueprintDatas.BlueprintsMap.remove(this.idRequest);
               TabletScreen.IDSBlueprints.add(this.idRequest);
            } else if (this.BPD == null && this.type.equals(TabletMinePrintMethods.TYPE.PERSONAL_GLOBAL) && Blueprint.globalBlueprintExist(this.BName)) {
               try (Reader reader = new FileReader(new File(Blueprint.getGlobalBlueprintDir(), this.BName + ".bp"))) {
                  Blueprint.BlueprintDatas bd = (Blueprint.BlueprintDatas)TabletMinePrintMethods.GSON.fromJson(reader, Blueprint.BlueprintDatas.class);
                  this.BPD = new BlueprintsPreviewMethods.BlueprintPreviewData(bd);
                  String id = Blueprint.getIDBlueprint("personal", bd);
                  this.idStruttura = id;
                  if (!BlueprintsPreviewMethods.MapIconsRL.containsKey(Blueprint.sanitizePathUnique(id))) {
                     this.BPD.prepareData(null);
                  } else {
                     this.BPD.iconTexture = BlueprintsPreviewMethods.MapIconsRL.get(Blueprint.sanitizePathUnique(id));
                  }
               } catch (Exception e) {
                  e.printStackTrace();
               }
            }
         }

         if (this.idStrutturaOriginal != null
            && !this.idStrutturaOriginal.isEmpty()
            && !TagStruttura.containsKey(this.idStrutturaOriginal)
            && ServerToClientDatasShared.BlueprintTags.containsKey(this.idStrutturaOriginal)) {
            TagStruttura.put(this.idStrutturaOriginal, new ArrayList<>());
            System.out.println("imposta tag struttura");

            for (String v : ServerToClientDatasShared.BlueprintTags.get(this.idStrutturaOriginal)) {
               ModTabletTags.TagData t = ModTabletTags.getTagByID(v);
               if (t != null) {
                  TagStruttura.get(this.idStrutturaOriginal).add(t);
               }

               System.out.println("imposta tag struttura add: " + t.getOriginalTag());
            }
         }

         gg.blit(TabletMinePrintMethods.TEXTURE, CentroX + this.posX, CentroY + this.posY, 0, 0, 64, 80);
         if (this.isMHItem) {
            gg.blit(TabletMinePrintMethods.TEXTURE, CentroX + this.posX, CentroY + this.posY, 64, 0, 64, 80);
         }

         if (this.BPD != null && this.createIconStructure) {
            this.BPD
               .renderStructureImage(
                  gg,
                  CentroX + this.posX + 5,
                  CentroY + this.posY + 1,
                  54,
                  54,
                  this.type.equals(TabletMinePrintMethods.TYPE.SERVER)
                     ? "server"
                     : (this.type.equals(TabletMinePrintMethods.TYPE.SHARED_GLOBAL) ? "global" : "personal")
               );
            if (this.BPD.iconTexture != null && this.BPD.isDatiVertexPronti) {
               this.idStruttura = this.BPD.getUnicID(this.type.name());

               for (Tuple<String, UUID> data : TabletClientDatas.getDatas().followedData) {
                  if (((String)data.getA()).equals(this.idStruttura)
                     && (data.getB() == null ? data.getB() == this.owner : ((UUID)data.getB()).equals(this.owner))) {
                     this.hasFollow = true;
                     break;
                  }
               }

               this.BPD.cleanupStructureData();
            }
         }

         if (!this.idStruttura.isEmpty()) {
            if (!this.type.equals(TabletMinePrintMethods.TYPE.PERSONAL) && !this.type.equals(TabletMinePrintMethods.TYPE.PERSONAL_GLOBAL)) {
               gg.blit(TabletMinePrintMethods.TEXTURE, CentroX + this.posX + 54, CentroY + this.posY + 68, this.hasFollow ? 249 : 242, 0, 7, 12);
               if (this.isMHFollow) {
                  gg.blit(TabletMinePrintMethods.TEXTURE, CentroX + this.posX + 54, CentroY + this.posY + 68, 235, 0, 7, 12);
               }
            }
         } else {
            TabletMinePrintMethods.renderLoading(gg, CentroX + this.posX + 5, CentroY + this.posY + 1);
         }

         if (this.type.equals(TabletMinePrintMethods.TYPE.SERVER)) {
            gg.blit(TabletMinePrintMethods.TEXTURE, CentroX + this.posX + 1, CentroY + this.posY + 65, 240, 12, 16, 13);
            if (mouseX >= CentroX + this.posX + 1
               && mouseX < CentroX + this.posX + 1 + 16
               && mouseY >= CentroY + this.posY + 65
               && mouseY < CentroY + this.posY + 65 + 13) {
               tooltipDaVisualizzare = Component.translatable("gui.portable_blueprints.tablet.shared_by_server");
            }
         } else if (this.type.equals(TabletMinePrintMethods.TYPE.PERSONAL_GLOBAL)) {
            gg.blit(TabletMinePrintMethods.TEXTURE, CentroX + this.posX + 1, CentroY + this.posY + 63, 225, 17, 15, 15);
         } else if (this.type.equals(TabletMinePrintMethods.TYPE.SHARED_GLOBAL)) {
            if (this.ImageFaccia != null) {
               for (int x = 0; x < this.ImageFaccia.getWidth(); x++) {
                  for (int y = 0; y < this.ImageFaccia.getHeight(); y++) {
                     int color = this.ImageFaccia.getRGB(x, y);
                     int addX = 2 * x + CentroX + this.posX + 1;
                     int addY = 2 * y + CentroY + this.posY + 62;
                     gg.fill(addX, addY, addX + 2, addY + 2, color);
                  }
               }

               if (mouseX >= CentroX + this.posX + 1
                  && mouseX < CentroX + this.posX + 1 + this.ImageFaccia.getWidth() * 2
                  && mouseY >= CentroY + this.posY + 62
                  && mouseY < CentroY + this.posY + 62 + this.ImageFaccia.getHeight() * 2) {
                  tooltipDaVisualizzare = Component.literal("Nimbl305");
               }
            } else {
               gg.blit(TabletMinePrintMethods.TEXTURE, CentroX + this.posX + 1, CentroY + this.posY + 62, 208, 16, 16, 16);
            }
         }

         RenderingMethods.GuiGraphicsTestoScalato(
            gg,
            this.BName,
            Operazioni.lerp(0.4F, 1.0F, 1.0F - this.BName.length() / 25.0F),
            Minecraft.getInstance().font,
            new Vec2(CentroX + this.posX + 32, CentroY + this.posY + 56),
            0,
            false,
            RenderingMethods.PosizioneTesto.CENTRO
         );
         if (this.isMHFollow) {
            tooltipDaVisualizzare = Component.translatable("portable_blueprints.gui.tablet.mineprint.general.hover_follow." + (this.hasFollow ? "off" : "on"));
         }

         return tooltipDaVisualizzare;
      }

      public void renderStruttura(PoseStack poseStack, Matrix4f matrix4f, Stage stage) {
      }

      private void isMouseHover(int mouseX, int mouseY, Vec2 chissPosX, Vec2 chissPosY) {
         if (!this.type.equals(TabletMinePrintMethods.TYPE.PERSONAL)
            && !this.type.equals(TabletMinePrintMethods.TYPE.PERSONAL_GLOBAL)
            && !this.idStruttura.isEmpty()
            && mouseX >= this.CentroX + this.posX + 54
            && mouseX < this.CentroX + this.posX + 61
            && mouseY >= this.CentroY + this.posY + 68
            && mouseY < this.CentroY + this.posY + 80
            && this.insideChiss(mouseX, mouseY, chissPosX, chissPosY)) {
            this.isMHFollow = true;
            this.isMHItem = false;
         } else {
            this.isMHItem = mouseX >= this.CentroX + this.posX
               && mouseX < this.CentroX + this.posX + 64
               && mouseY >= this.CentroY + this.posY
               && mouseY < this.CentroY + this.posY + 80
               && this.insideChiss(mouseX, mouseY, chissPosX, chissPosY);
            this.isMHFollow = false;
         }
      }

      public boolean insideChiss(int mouseX, int mouseY, Vec2 chissPosX, Vec2 chissPosY) {
         return chissPosX != null && chissPosY != null
            ? mouseX >= this.CentroX + chissPosX.x
               && mouseX < this.CentroX + chissPosX.y
               && mouseY >= this.CentroY + chissPosY.x
               && mouseY < this.CentroY + chissPosY.y
            : true;
      }

      public void addToPosY(int v) {
         this.posY = this.start_posY + v;
      }

      public void openBlueprintInfo(boolean isFollowArea) {
         TabletBlueprintInfoScreen.OpenGUIByAppMinePrint(this.BName, this.type, this.owner, isFollowArea);
      }

      public void followAction() {
         if (!this.idStruttura.isEmpty() && !this.type.equals(TabletMinePrintMethods.TYPE.PERSONAL)) {
            boolean contiene = false;
            int count = 0;

            for (Tuple<String, UUID> data : TabletClientDatas.getDatas().followedData) {
               if (((String)data.getA()).equals(this.idStruttura) && (data.getB() == null ? data.getB() == this.owner : ((UUID)data.getB()).equals(this.owner))
                  )
                {
                  contiene = true;
                  break;
               }

               count++;
            }

            if (contiene) {
               TabletClientDatas.Datas datas = TabletClientDatas.getDatas();
               datas.followedData.remove(count);
               TabletClientDatas.setDatas(datas);
               this.hasFollow = false;
            } else {
               TabletClientDatas.Datas datas = TabletClientDatas.getDatas();
               datas.followedData.add(new Tuple(this.idStruttura, this.owner));
               TabletClientDatas.setDatas(datas);
               this.hasFollow = true;
            }

            TabletClientDatas.save();
         }
      }

      public static void resetAllSavedTag() {
         TagStruttura.clear();
      }

      public boolean hasTag(ModTabletTags.TagData tagData) {
         if (TagStruttura.containsKey(this.idStrutturaOriginal) && !TagStruttura.get(this.idStrutturaOriginal).isEmpty()) {
            for (ModTabletTags.TagData td : TagStruttura.get(this.idStrutturaOriginal)) {
               if (td.getOriginalTag().equals(tagData.getOriginalTag())) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   public enum TYPE {
      PERSONAL,
      SERVER,
      SHARED_GLOBAL,
      FOLLOW,
      PERSONAL_GLOBAL;
   }
}
