package fi.otavanopisto.muikku.ui.local.course.picker;

import org.junit.After;
import org.junit.Before;

import fi.otavanopisto.muikku.ui.base.course.picker.CoursePickerTestsBase;

public class CoursePickerTestsIT extends CoursePickerTestsBase {
  
  @Before
  public void setUp() {
    setWebDriver(createLocalDriver());
  }

}
