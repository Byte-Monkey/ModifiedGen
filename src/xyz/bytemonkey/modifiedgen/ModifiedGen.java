package xyz.bytemonkey.modifiedgen;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;

public class ModifiedGen extends JavaPlugin {

    private FileConfiguration c = getConfig();

    @Override
    public void onEnable() {
        try {
            Metrics metrics = new Metrics(this);
            metrics.start();
        } catch (IOException e) {
            getLogger().log(Level.WARNING, "Failed to send metrics to mcstats.org");
            e.printStackTrace();
        }
        setBiomes();
    }

    private void setBiomes() {
        try {
            String mojangPath = "net.minecraft.server." + Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
            Class clazz = Class.forName(mojangPath + ".BiomeBase");

            ArrayList<String> disabled1 = new ArrayList<>();
            ArrayList<Object> disabled2 = new ArrayList<>();

            ArrayList<String> replace1 = new ArrayList<>();
            ArrayList<Object> replace2 = new ArrayList<>();

            disabled1.addAll(c.getStringList("world.blockedbiomes"));
            replace1.addAll(c.getStringList("world.replacebiomes"));

            for (String s : disabled1) {
                Field field = clazz.getDeclaredField(s);
                field.setAccessible(true);
                Object object = field.get(null);
                disabled2.add(object);
            }

            for (String s : replace1) {
                Field field = clazz.getDeclaredField(s);
                field.setAccessible(true);
                Object object = field.get(null);
                replace2.add(object);
            }

            Field field = clazz.getDeclaredField("biomes");
            field.setAccessible(true);
            Object[] biomes = (Object[]) field.get(null);
            Random random = new Random();
            for (int i = 0; i < biomes.length; i++)
                if (biomes[i] != null)
                    for (Object oo : disabled2)
                        if (biomes[i].equals(oo))
                            biomes[i] = replace2.get(random.nextInt(replace2.size()));
            try {
                field.set(null, biomes);
            } catch (Exception e) {}
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
