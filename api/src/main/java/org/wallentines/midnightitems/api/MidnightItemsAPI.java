package org.wallentines.midnightitems.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wallentines.midnightcore.api.FileConfig;
import org.wallentines.midnightcore.api.text.LangProvider;
import org.wallentines.midnightitems.api.action.ItemAction;
import org.wallentines.midnightitems.api.item.MidnightItem;
import org.wallentines.midnightitems.api.requirement.ItemRequirement;
import org.wallentines.midnightlib.registry.Registry;

import java.io.File;

public abstract class MidnightItemsAPI {

    protected static MidnightItemsAPI INSTANCE;
    protected static final Logger LOGGER = LogManager.getLogger("MidnightItems");

    protected MidnightItemsAPI() {
        INSTANCE = this;
    }

    public abstract long reload();

    public abstract Registry<MidnightItem> getItemRegistry();

    public abstract Registry<ItemAction<?>> getActionRegistry();

    public abstract Registry<ItemRequirement> getRequirementRegistry();

    public abstract File getDataFolder();

    public abstract FileConfig getConfig();

    public abstract File getContentFolder();

    public abstract LangProvider getLangProvider();


    public static MidnightItemsAPI getInstance() {
        return INSTANCE;
    }

    public static Logger getLogger() {
        return LOGGER;
    }


}
