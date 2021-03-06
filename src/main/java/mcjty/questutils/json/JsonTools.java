package mcjty.questutils.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import mcjty.lib.container.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class JsonTools {

    public static JsonObject itemStackToJson(ItemStack item) {
        JsonObject object = new JsonObject();
        object.add("item", new JsonPrimitive(item.getItem().getRegistryName().toString()));
        if (item.getCount() != 1) {
            object.add("amount", new JsonPrimitive(item.getCount()));
        }
        if (item.getItemDamage() != 0) {
            object.add("meta", new JsonPrimitive(item.getItemDamage()));
        }
        if (item.hasTagCompound()) {
            String string = item.getTagCompound().toString();
            object.add("nbt", new JsonPrimitive(string));
        }
        return object;
    }

    public static ItemStack jsonToItemStack(JsonObject obj) {
        String itemName = obj.get("item").getAsString();
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName));
        // @todo error checking
        int amount = 1;
        if (obj.has("amount")) {
            amount = obj.get("amount").getAsInt();
        }
        int meta = 0;
        if (obj.has("meta")) {
            meta = obj.get("meta").getAsInt();
        }
        ItemStack stack = new ItemStack(item, amount, meta);
        if (obj.has("nbt")) {
            try {
                NBTTagCompound nbt = JsonToNBT.getTagFromJson(obj.get("nbt").getAsString());
                stack.setTagCompound(nbt);
            } catch (NBTException e) {
                // @todo What to do?
            }
        }
        return stack;
    }

    public static JsonArray writeItemsToJson(InventoryHelper helper, int start, int end) {
        JsonArray array = new JsonArray();
        for (int i = start ; i < end ; i++) {
            ItemStack stack = helper.getStackInSlot(i);
            if (!stack.isEmpty()) {
                JsonObject itemJson = JsonTools.itemStackToJson(stack);
                array.add(itemJson);
            }
        }
        return array;
    }

    public static void readItemsFromJson(JsonArray array, InventoryHelper helper, int start, int end) {
        for (int i = start ; i < end ; i++) {
            helper.setStackInSlot(i, ItemStack.EMPTY);
        }
        int idx = start;
        for (JsonElement element : array) {
            helper.setStackInSlot(idx++, JsonTools.jsonToItemStack(element.getAsJsonObject()));
        }
    }
}
