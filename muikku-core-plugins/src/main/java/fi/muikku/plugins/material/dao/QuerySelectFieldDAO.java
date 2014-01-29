package fi.muikku.plugins.material.dao;

import fi.muikku.dao.DAO;
import fi.muikku.plugin.PluginDAO;
import fi.muikku.plugins.material.model.Material;
import fi.muikku.plugins.material.model.QuerySelectField;

@DAO
public class QuerySelectFieldDAO extends PluginDAO<QuerySelectField> {
	
	private static final long serialVersionUID = -5327160259588566934L;
	
	public QuerySelectField create(Material material, String name){
		
		QuerySelectField querySelectField = new QuerySelectField();
		
		querySelectField.setMaterial(material);
		querySelectField.setName(name);
		
		return persist(querySelectField);
	}

}
