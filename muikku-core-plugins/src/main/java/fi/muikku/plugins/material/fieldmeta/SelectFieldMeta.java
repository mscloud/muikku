package fi.muikku.plugins.material.fieldmeta;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;

public class SelectFieldMeta extends FieldMeta {
  
  public SelectFieldMeta() {
    
  }

  public SelectFieldMeta(String name, String listType, Integer size, List<SelectFieldOptionMeta> selectFieldOptionMetas) {
    super(name);
    this.setOptions(selectFieldOptionMetas);
    this.setListType(listType);
    this.setSize(size);
  }
  
  public List<SelectFieldOptionMeta> getOptions() {
    return selectFieldOptionMetas;
  }

  public void setOptions(List<SelectFieldOptionMeta> selectFieldOptionMetas) {
    this.selectFieldOptionMetas = selectFieldOptionMetas;
  }
  
  @Override
  @JsonIgnore
  public String getType() {
    return "application/vnd.muikku.field.select";
  }

  public String getListType() {
    return listType;
  }

  public void setListType(String listType) {
    this.listType = listType;
  }

  public Integer getSize() {
    return size;
  }
  
  public void setSize(Integer size) {
    this.size = size;
  }
  
  private List<SelectFieldOptionMeta> selectFieldOptionMetas;
  private String listType;
  private Integer size;
}
