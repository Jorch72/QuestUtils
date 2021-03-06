package mcjty.questutils.json;

import com.google.gson.*;
import mcjty.lib.varia.Logging;
import mcjty.questutils.blocks.QUTileEntity;
import mcjty.questutils.data.QUData;
import mcjty.questutils.data.QUEntry;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import java.io.*;
import java.util.function.Predicate;

public class JsonPersistance {

    private static JsonObject getJsonObject(String id) {
        QUTileEntity te = getQUTile(id);
        if (te != null && te.hasIdentifier()) {
            JsonObject object = new JsonObject();
            te.writeToJson(object);
            return object;
        } else {
            System.out.println("Id '" + id + "' does not seem to correspond to a QU block or doesn't have an identifier!");
            return null;
        }
    }

    public static QUTileEntity getQUTile(String id) {
        QUEntry entry = QUData.getData().getEntry(id);
        if (entry == null) {
            return null;
        }
        World world = DimensionManager.getWorld(entry.getDimension());
        if (world == null) {
            world = DimensionManager.getWorld(0).getMinecraftServer().getWorld(entry.getDimension());
        }

        TileEntity te = world.getTileEntity(entry.getPos());
        if (te instanceof QUTileEntity) {
            return (QUTileEntity) te;
        } else {
            return null;
        }
    }

    public static void write(File file, Predicate<String> matcher) {
        PrintWriter writer;
        try {
            writer = new PrintWriter(file);
        } catch (FileNotFoundException e) {
            Logging.logError("Error writing " + file.getName());
            return;
        }

        JsonArray array = new JsonArray();

        for (String id : QUData.getData().getEntries().keySet()) {
            if (matcher.test(id)) {
                JsonObject object = getJsonObject(id);
                if (object != null) {
                    array.add(object);
                }
            }
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        writer.print(gson.toJson(array));

        writer.close();
    }

    public static void read(File file, Predicate<String> matcher) {
        FileInputStream inputstream;
        try {
            inputstream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            Logging.log("Note: No file " + file.getName());
            return;
        }
        BufferedReader br;
        try {
            br = new BufferedReader(new InputStreamReader(inputstream, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            Logging.logError("Error reading file: " + file.getName());
            return;
        }

        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(br);
        for (JsonElement entry : element.getAsJsonArray()) {
            JsonObject object = entry.getAsJsonObject();
            String id = object.get("id").getAsString();
            if (matcher.test(id)) {
                QUEntry qu = QUData.getData().getEntry(id);
                if (qu == null) {
                    System.out.println("Cannot find id '" + id + "'!");
                } else {
                    QUTileEntity te = getQUTile(id);
                    if (te != null) {
                        te.readFromJson(object);
                    }
                }
            }
        }
    }

}
