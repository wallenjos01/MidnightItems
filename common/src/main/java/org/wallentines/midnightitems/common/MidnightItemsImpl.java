package org.wallentines.midnightitems.common;

import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.codec.DecodeException;
import org.wallentines.mdcfg.codec.FileWrapper;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.midnightcore.api.FileConfig;
import org.wallentines.midnightcore.api.text.LangProvider;
import org.wallentines.midnightcore.common.util.FileUtil;
import org.wallentines.midnightitems.api.MidnightItemsAPI;
import org.wallentines.midnightitems.api.action.ItemAction;
import org.wallentines.midnightitems.api.item.MidnightItem;
import org.wallentines.midnightitems.api.requirement.ItemRequirement;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightlib.registry.Registry;

import java.io.File;
import java.nio.file.Path;
import java.util.function.Consumer;

public class MidnightItemsImpl extends MidnightItemsAPI {

    private final LangProvider langProvider;

    private final File dataFolder;
    private final File contentFolder;
    private final FileConfig config;

    private final ConfigSection configDefaults;

    private final Registry<MidnightItem> itemRegistry = new Registry<>("midnightitems");
    private final Registry<ItemAction<?>> actionRegistry = new Registry<>("midnightitems");
    private final Registry<ItemRequirement> requirementRegistry = new Registry<>("midnightitems");

    public MidnightItemsImpl(Path dataFolder, ConfigSection configDefaults, ConfigSection langDefaults) {

        File fldr = FileUtil.tryCreateDirectory(dataFolder);
        if(fldr == null) {
            throw new IllegalStateException("Unable to create data directory " + dataFolder);
        }

        this.dataFolder = fldr;
        this.configDefaults = configDefaults;

        Path langFolder = dataFolder.resolve("lang");
        FileUtil.tryCreateDirectory(langFolder);
        langProvider = new LangProvider(langFolder, langDefaults);

        this.config = FileConfig.findOrCreate("config", fldr);
        this.contentFolder = dataFolder.resolve("content").toFile();

        loadConfig();
    }


    private void loadConfig() {

        config.load();
        config.getRoot().fill(configDefaults);
        config.save();

        langProvider.reload();

        if(!contentFolder.exists() && !contentFolder.mkdirs()) {
            throw new IllegalStateException("Unable to create content folder!");
        }

        itemRegistry.clear();
        actionRegistry.clear();
        requirementRegistry.clear();

        forAllFiles(contentFolder, (namespaceFldr) -> {

            if(!namespaceFldr.isDirectory()) return;
            String namespace = namespaceFldr.getName();

            File itemsFldr = new File(namespaceFldr, "items");
            File actionsFldr = new File(namespaceFldr, "actions");
            File requirementsFldr = new File(namespaceFldr, "requirements");

            forAllFiles(requirementsFldr, (rf) -> {

                String path = rf.getName().substring(0, rf.getName().lastIndexOf("."));
                FileWrapper<ConfigObject> data = FileConfig.REGISTRY.fromFile(ConfigContext.INSTANCE, rf);
                data.load();

                try {
                    requirementRegistry.register(new Identifier(namespace, path), ItemRequirement.SERIALIZER.deserialize(ConfigContext.INSTANCE, data.getRoot()).getOrThrow());
                } catch (DecodeException ex) {
                    LOGGER.warn("An exception was thrown while parsing a requirement!");
                    ex.printStackTrace();
                }
            });

            forAllFiles(actionsFldr, (af) -> {

                String path = af.getName().substring(0, af.getName().lastIndexOf("."));
                FileWrapper<ConfigObject> data = FileConfig.REGISTRY.fromFile(ConfigContext.INSTANCE, af);
                data.load();

                try {
                    actionRegistry.register(new Identifier(namespace, path), ItemAction.SERIALIZER.deserialize(ConfigContext.INSTANCE, data.getRoot()).getOrThrow());
                } catch (DecodeException ex) {
                    LOGGER.warn("An exception was thrown while parsing an action!");
                    ex.printStackTrace();
                }

            });

            forAllFiles(itemsFldr, (f) -> {

                String path = f.getName().substring(0, f.getName().lastIndexOf("."));
                FileWrapper<ConfigObject> data = FileConfig.REGISTRY.fromFile(ConfigContext.INSTANCE, f);
                data.load();

                try {
                    itemRegistry.register(new Identifier(namespace, path), MidnightItem.SERIALIZER.deserialize(ConfigContext.INSTANCE, data.getRoot()).getOrThrow());
                } catch (DecodeException ex) {
                    LOGGER.warn("An exception was thrown while parsing a MidnightItem!");
                    ex.printStackTrace();
                }

            });

        });

        LOGGER.info("Registered " + requirementRegistry.getSize() + " requirements!");
        LOGGER.info("Registered " + actionRegistry.getSize() + " actions!");
        LOGGER.info("Registered " + itemRegistry.getSize() + " items!");

    }

    private void forAllFiles(File folder, Consumer<File> file) {

        if(!folder.exists() || !folder.isDirectory()) return;

        File[] files = folder.listFiles();
        if(files == null) return;

        for(File f : files) {
            file.accept(f);
        }
    }

    public long reload() {

        long time = System.currentTimeMillis();
        loadConfig();
        return System.currentTimeMillis() - time;
    }

    public Registry<MidnightItem> getItemRegistry() {
        return itemRegistry;
    }

    public Registry<ItemAction<?>> getActionRegistry() {
        return actionRegistry;
    }

    public Registry<ItemRequirement> getRequirementRegistry() {
        return requirementRegistry;
    }

    public File getDataFolder() {
        return dataFolder;
    }

    public FileConfig getConfig() {
        return config;
    }

    public File getContentFolder() {
        return contentFolder;
    }

    public LangProvider getLangProvider() {
        return langProvider;
    }

}
