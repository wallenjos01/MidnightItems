package org.wallentines.midnightitems.common;

import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.module.lang.LangModule;
import org.wallentines.midnightcore.api.module.lang.LangProvider;
import org.wallentines.midnightcore.common.util.FileUtil;
import org.wallentines.midnightitems.api.MidnightItemsAPI;
import org.wallentines.midnightitems.api.action.ItemAction;
import org.wallentines.midnightitems.api.action.ItemActionType;
import org.wallentines.midnightitems.api.action.ToggleItemActionData;
import org.wallentines.midnightitems.api.item.MidnightItem;
import org.wallentines.midnightitems.api.requirement.ItemRequirement;
import org.wallentines.midnightlib.config.ConfigRegistry;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.config.FileConfig;
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

    private final Registry<MidnightItem> iteRegistry = new Registry<>();
    private final Registry<ItemAction<?>> actionRegistry = new Registry<>();
    private final Registry<ItemRequirement> requirementRegistry = new Registry<>();

    public MidnightItemsImpl(Path dataFolder, MidnightCoreAPI api, ConfigSection configDefaults, ConfigSection langDefaults) {

        File fldr = FileUtil.tryCreateDirectory(dataFolder);
        if(fldr == null) {
            throw new IllegalStateException("Unable to create data directory " + dataFolder);
        }

        this.dataFolder = fldr;
        this.configDefaults = configDefaults;

        ConfigRegistry.INSTANCE.registerSerializer(ItemAction.class, ItemAction.SERIALIZER);
        ConfigRegistry.INSTANCE.registerSerializer(ToggleItemActionData.class, ToggleItemActionData.SERIALIZER);

        Path langFolder = dataFolder.resolve("lang");
        langProvider = api.getModuleManager().getModule(LangModule.class).createProvider(langFolder, langDefaults);

        this.config = FileConfig.findOrCreate("config", fldr);
        this.contentFolder = dataFolder.resolve("content").toFile();

        loadConfig();
    }


    private void loadConfig() {

        config.reload();
        config.getRoot().fill(configDefaults);
        config.save();

        langProvider.reload();

        if(!contentFolder.exists() && !contentFolder.mkdirs()) {
            throw new IllegalStateException("Unable to create content folder!");
        }

        iteRegistry.clear();
        actionRegistry.clear();
        requirementRegistry.clear();

        forAllFiles(contentFolder, (namespaceFldr) -> {

            if(!namespaceFldr.isDirectory()) return;
            String namespace = namespaceFldr.getName();

            File itemsFldr = new File(namespaceFldr, "items");
            File actionsFldr = new File(namespaceFldr, "actions");
            File requirementsFldr = new File(namespaceFldr, "requirements");

            forAllFiles(requirementsFldr, (rf) -> {

                String extension = rf.getName().substring(rf.getName().lastIndexOf("."));
                String path = rf.getName().substring(0, rf.getName().length() - extension.length());
                ConfigSection data = ConfigRegistry.INSTANCE.getProviderForFileType(extension).loadFromFile(rf);

                requirementRegistry.register(new Identifier(namespace, path), ItemRequirement.parse(data));

            });

            forAllFiles(actionsFldr, (af) -> {

                String extension = af.getName().substring(af.getName().lastIndexOf("."));
                String path = af.getName().substring(0, af.getName().length() - extension.length());
                ConfigSection data = ConfigRegistry.INSTANCE.getProviderForFileType(extension).loadFromFile(af);

                actionRegistry.register(new Identifier(namespace, path), ItemActionType.parseAction(data));

            });

            forAllFiles(itemsFldr, (f) -> {

                String extension = f.getName().substring(f.getName().lastIndexOf("."));
                String path = f.getName().substring(0, f.getName().length() - extension.length());
                ConfigSection data = ConfigRegistry.INSTANCE.getProviderForFileType(extension).loadFromFile(f);
                Identifier id = new Identifier(namespace, path);

                iteRegistry.register(id, MidnightItem.parse(id, data));

            });

        });

        LOGGER.info("Registered " + requirementRegistry.getSize() + " requirements!");
        LOGGER.info("Registered " + actionRegistry.getSize() + " actions!");
        LOGGER.info("Registered " + iteRegistry.getSize() + " items!");

    }

    private void forAllFiles(File folder, Consumer<File> file) {

        if(!folder.exists() || !folder.isDirectory()) return;

        File[] files = folder.listFiles();
        if(files == null || files.length == 0) return;

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
        return iteRegistry;
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
