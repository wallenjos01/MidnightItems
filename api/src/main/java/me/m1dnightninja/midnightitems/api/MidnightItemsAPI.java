package me.m1dnightninja.midnightitems.api;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.config.ConfigProvider;
import me.m1dnightninja.midnightcore.api.config.ConfigRegistry;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.config.FileConfig;
import me.m1dnightninja.midnightcore.api.module.lang.ILangModule;
import me.m1dnightninja.midnightcore.api.module.lang.ILangProvider;
import me.m1dnightninja.midnightcore.api.registry.MIdentifier;
import me.m1dnightninja.midnightcore.api.registry.MRegistry;
import me.m1dnightninja.midnightitems.api.action.ItemAction;
import me.m1dnightninja.midnightitems.api.action.ItemActionType;
import me.m1dnightninja.midnightitems.api.action.ToggleItemActionData;
import me.m1dnightninja.midnightitems.api.item.MidnightItem;
import me.m1dnightninja.midnightitems.api.requirement.ItemRequirement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.function.Consumer;

public class MidnightItemsAPI {

    private static MidnightItemsAPI instance;
    private static final Logger logger = LogManager.getLogger("MidnightItems");

    private final ILangProvider langProvider;

    private final File dataFolder;
    private final File contentFolder;
    private final FileConfig config;

    private final ConfigSection configDefaults;

    private final MRegistry<MidnightItem> itemRegistry = new MRegistry<>();
    private final MRegistry<ItemAction<?>> actionRegistry = new MRegistry<>();
    private final MRegistry<ItemRequirement> requirementRegistry = new MRegistry<>();

    public MidnightItemsAPI(File dataFolder, MidnightCoreAPI api, ConfigSection configDefaults, ConfigSection langDefaults) {

        instance = this;

        this.dataFolder = dataFolder;
        this.configDefaults = configDefaults;

        if(!dataFolder.exists() && !dataFolder.mkdirs()) {
            throw new IllegalStateException("Unable to create data folder!");
        }

        ConfigProvider prov = ConfigRegistry.INSTANCE.getDefaultProvider();
        api.getConfigRegistry().registerSerializer(ItemAction.class, ItemAction.SERIALIZER);
        api.getConfigRegistry().registerSerializer(ToggleItemActionData.class, ToggleItemActionData.SERIALIZER);

        File langFolder = new File(dataFolder, "lang");
        langProvider = api.getModule(ILangModule.class).createLangProvider(langFolder, langDefaults);

        File configFile = new File(dataFolder, "config" + prov.getFileExtension());

        this.config = new FileConfig(configFile);
        this.contentFolder = new File(dataFolder, "content");

        loadConfig();
    }


    private void loadConfig() {

        config.reload();
        config.getRoot().fill(configDefaults);
        config.save();

        langProvider.reloadAllEntries();

        if(!contentFolder.exists() && !(contentFolder.mkdirs() && contentFolder.setReadable(true) && contentFolder.setWritable(true))) {
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

                String extension = rf.getName().substring(rf.getName().lastIndexOf("."));
                String path = rf.getName().substring(0, rf.getName().length() - extension.length());
                ConfigSection data = MidnightCoreAPI.getInstance().getConfigRegistry().getProviderForFileType(extension).loadFromFile(rf);

                requirementRegistry.register(MIdentifier.create(namespace, path), ItemRequirement.parse(data));

            });

            forAllFiles(actionsFldr, (af) -> {

                String extension = af.getName().substring(af.getName().lastIndexOf("."));
                String path = af.getName().substring(0, af.getName().length() - extension.length());
                ConfigSection data = MidnightCoreAPI.getInstance().getConfigRegistry().getProviderForFileType(extension).loadFromFile(af);

                actionRegistry.register(MIdentifier.create(namespace, path), ItemActionType.parseAction(data));

            });

            forAllFiles(itemsFldr, (f) -> {

                String extension = f.getName().substring(f.getName().lastIndexOf("."));
                String path = f.getName().substring(0, f.getName().length() - extension.length());
                ConfigSection data = MidnightCoreAPI.getInstance().getConfigRegistry().getProviderForFileType(extension).loadFromFile(f);
                MIdentifier id = MIdentifier.create(namespace, path);

                itemRegistry.register(id, MidnightItem.parse(id, data));

            });

        });

        logger.info("Registered " + requirementRegistry.getSize() + " requirements!");
        logger.info("Registered " + actionRegistry.getSize() + " actions!");
        logger.info("Registered " + itemRegistry.getSize() + " items!");

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

    public static MidnightItemsAPI getInstance() {
        return instance;
    }

    public static Logger getLogger() {
        return logger;
    }

    public MRegistry<MidnightItem> getItemRegistry() {
        return itemRegistry;
    }

    public MRegistry<ItemAction<?>> getActionRegistry() {
        return actionRegistry;
    }

    public MRegistry<ItemRequirement> getRequirementRegistry() {
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

    public ILangProvider getLangProvider() {
        return langProvider;
    }
}
