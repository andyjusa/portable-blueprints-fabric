package nimble.portable_blueprints.test;

import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import nimble.portable_blueprints.altro.Blueprint;
import nimble.portable_blueprints.fabric.BlueprintJson;

public final class BlueprintJsonGameTests implements FabricGameTest {
   @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
   public void upstreamOffsetCoordinatesRemainStable(GameTestHelper helper) {
      var gson = BlueprintJson.create();
      // Existing NeoForge .bp files store the offset using literal x/y/z keys.
      String fixture = """
         {"nome":"offset-fixture","altezza":1,"lunghezzaX":1,"lunghezzaZ":1,
          "offset":{"x":10,"y":-3,"z":7}}
         """;
      var blueprint = gson.fromJson(fixture, Blueprint.BlueprintDatas.class);
      helper.assertValueEqual(blueprint.offset, new BlockPos(10, -3, 7),
         "Existing .bp offsets must decode independently of runtime field mappings");
      var encoded = JsonParser.parseString(gson.toJson(blueprint)).getAsJsonObject().getAsJsonObject("offset");
      var expected = JsonParser.parseString("{\"x\":10,\"y\":-3,\"z\":7}").getAsJsonObject();
      helper.assertValueEqual(encoded, expected, "Saved offsets must keep literal x/y/z keys");
      helper.assertTrue(gson.fromJson("null", BlockPos.class) == null, "Null offsets must remain null");
      helper.assertValueEqual(gson.toJson(null, BlockPos.class), "null", "Null offsets must serialize as null");
      helper.succeed();
   }
}
