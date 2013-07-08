package fi.muikku.plugins.settings;

import java.util.List;

import javax.ejb.Stateful;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import fi.muikku.dao.plugins.PluginEntityDAO;
import fi.muikku.model.plugins.Plugin;

@Dependent
@Stateful
public class PluginSettingsController {
  
  @Inject
  private PluginEntityDAO pluginDAO;

  public List<Plugin> getAllPlugins() {
    List<Plugin> allPlugins = pluginDAO.listAll();
    return allPlugins;
  }
  
  public void togglePlugin(Plugin plugin) {
    pluginDAO.updateEnabled(plugin, !plugin.getEnabled());
  }
}
